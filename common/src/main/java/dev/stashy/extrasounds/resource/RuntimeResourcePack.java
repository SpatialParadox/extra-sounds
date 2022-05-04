package dev.stashy.extrasounds.resource;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import dev.stashy.extrasounds.ExtraSounds;
import dev.stashy.extrasounds.resource.json.animation.JAnimation;
import dev.stashy.extrasounds.resource.json.blockstate.JMultipart;
import dev.stashy.extrasounds.resource.json.blockstate.JState;
import dev.stashy.extrasounds.resource.json.blockstate.JVariant;
import dev.stashy.extrasounds.resource.json.blockstate.JWhen;
import dev.stashy.extrasounds.resource.json.lang.JLang;
import dev.stashy.extrasounds.resource.json.loot.JCondition;
import dev.stashy.extrasounds.resource.json.loot.JFunction;
import dev.stashy.extrasounds.resource.json.loot.JLootTable;
import dev.stashy.extrasounds.resource.json.loot.JPool;
import dev.stashy.extrasounds.resource.json.models.JModel;
import dev.stashy.extrasounds.resource.json.models.JTextures;
import dev.stashy.extrasounds.resource.json.recipe.*;
import dev.stashy.extrasounds.resource.json.tags.JTag;
import dev.stashy.extrasounds.resource.util.CallableFunction;
import dev.stashy.extrasounds.resource.util.UnsafeByteArrayOutputStream;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;
import org.apache.commons.compress.utils.CountingInputStream;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.*;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class RuntimeResourcePack implements ResourcePack {
    public static final ExecutorService EXECUTOR_SERVICE;
    public static final boolean DUMP;
    public static final boolean DEBUG_PERFORMANCE;

    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .registerTypeAdapter(JMultipart.class, new JMultipart.Serializer())
            .registerTypeAdapter(JWhen.class, new JWhen.Serializer())
            .registerTypeAdapter(JState.class, new JState.Serializer())
            .registerTypeAdapter(JVariant.class, new JVariant.Serializer())
            .registerTypeAdapter(JTextures.class, new JTextures.Serializer())
            .registerTypeAdapter(JAnimation.class, new JAnimation.Serializer())
            .registerTypeAdapter(JFunction.class, new JFunction.Serializer())
            .registerTypeAdapter(JPool.class, new JPool.Serializer())
            .registerTypeAdapter(JPattern.class, new JPattern.Serializer())
            .registerTypeAdapter(JKeys.class, new JKeys.Serializer())
            .registerTypeAdapter(JIngredient.class, new JIngredient.Serializer())
            .registerTypeAdapter(JIngredients.class, new JIngredients.Serializer())
            .registerTypeAdapter(Identifier.class, new Identifier.Serializer())
            .registerTypeAdapter(JCondition.class, new JCondition.Serializer())
            .create();

    private static final File DEFAULT_OUTPUT = new File("rrp.debug");

    static {
        Properties properties = new Properties();
        int processors = Math.max(Runtime.getRuntime().availableProcessors() / 2 - 1, 1);
        boolean dump = false;
        boolean performance = false;
        properties.setProperty("threads", String.valueOf(processors));
        properties.setProperty("dump assets", "false");
        properties.setProperty("debug performance", "false");

        File file = new File("config/rrp.properties");
        try(FileReader reader = new FileReader(file)) {
            properties.load(reader);
            processors = Integer.parseInt(properties.getProperty("threads"));
            dump = Boolean.parseBoolean(properties.getProperty("dump assets"));
            performance = Boolean.parseBoolean(properties.getProperty("debug performance"));
        } catch(Throwable t) {
            ExtraSounds.LOGGER.warn("Invalid config, creating new one!");
            file.getParentFile().mkdirs();
            try(FileWriter writer = new FileWriter(file)) {
                properties.store(writer, "number of threads RRP should use for generating resources");
            } catch(IOException ex) {
                ExtraSounds.LOGGER.error("Unable to write to RRP config!");
                ex.printStackTrace();
            }
        }

        EXECUTOR_SERVICE = Executors.newFixedThreadPool(processors, new ThreadFactoryBuilder().setDaemon(true).setNameFormat("ARRP-Workers-%s").build());
        DUMP = dump;
        DEBUG_PERFORMANCE = performance;
    }

    public final int packVersion;
    private final Identifier id;
    private final Lock waiting = new ReentrantLock();
    private final Map<Identifier, Supplier<byte[]>> data = new ConcurrentHashMap<>();
    private final Map<Identifier, Supplier<byte[]>> assets = new ConcurrentHashMap<>();
    private final Map<String, Supplier<byte[]>> root = new ConcurrentHashMap<>();
    private final Map<Identifier, JLang> langMergable = new ConcurrentHashMap<>();

    public static RuntimeResourcePack create(String id) {
        return new RuntimeResourcePack(new Identifier(id));
    }

    public static RuntimeResourcePack create(String id, int version) {
        return new RuntimeResourcePack(new Identifier(id), version);
    }

    public static RuntimeResourcePack create(Identifier id) {
        return new RuntimeResourcePack(id);
    }

    public static RuntimeResourcePack create(Identifier id, int version) {
        return new RuntimeResourcePack(id, version);
    }

    public RuntimeResourcePack(Identifier id) {
        this(id, 5);
    }

    public RuntimeResourcePack(Identifier id, int version) {
        this.packVersion = version;
        this.id = id;
    }

    public void addRecoloredImage(Identifier identifier, InputStream target, IntUnaryOperator operator) {
        this.addLazyResource(ResourceType.CLIENT_RESOURCES, fix(identifier, "textures", "png"), (i, r) -> {
            try {

                // optimize buffer allocation, input and output image after recoloring should be roughly the same size
                CountingInputStream is = new CountingInputStream(target);

                // repaint image
                BufferedImage base = ImageIO.read(is);
                BufferedImage recolored = new BufferedImage(base.getWidth(), base.getHeight(), BufferedImage.TYPE_INT_ARGB);
                for(int y = 0; y < base.getHeight(); y++) {
                    for(int x = 0; x < base.getWidth(); x++) {
                        recolored.setRGB(x, y, operator.applyAsInt(base.getRGB(x, y)));
                    }
                }

                // write image
                UnsafeByteArrayOutputStream baos = new UnsafeByteArrayOutputStream((int) is.getBytesRead());
                ImageIO.write(recolored, "png", baos);
                return baos.getBytes();
            } catch(Throwable e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });
    }

    public byte[] addLang(Identifier identifier, JLang lang) {
        return this.addAsset(fix(identifier, "lang", "json"), serialize(lang.getLang()));
    }

    public void mergeLang(Identifier identifier, JLang lang) {
        this.langMergable.compute(identifier, (identifier1, lang1) -> {
            if(lang1 == null) {
                lang1 = new JLang();
                this.addLazyResource(ResourceType.CLIENT_RESOURCES, identifier, (pack, identifier2) -> {
                    return pack.addLang(identifier, lang);
                });
            }
            lang1.getLang().putAll(lang.getLang());
            return lang1;
        });
    }

    public byte[] addLootTable(Identifier identifier, JLootTable table) {
        return this.addData(fix(identifier, "loot_tables", "json"), serialize(table));
    }

    public Future<byte[]> addAsyncResource(ResourceType type, Identifier path, CallableFunction<Identifier, byte[]> data) {
        Future<byte[]> future = EXECUTOR_SERVICE.submit(() -> data.get(path));
        this.getSys(type).put(path, () -> {
            try {
                return future.get();
            } catch(InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
        return future;
    }

    public void addLazyResource(ResourceType type, Identifier path, BiFunction<RuntimeResourcePack, Identifier, byte[]> func) {
        this.getSys(type).put(path, new Memoized<>(func, path));
    }


    public byte[] addResource(ResourceType type, Identifier path, byte[] data) {
        this.getSys(type).put(path, () -> data);
        return data;
    }

    public Future<byte[]> addAsyncRootResource(String path, CallableFunction<String, byte[]> data) {
        Future<byte[]> future = EXECUTOR_SERVICE.submit(() -> data.get(path));
        this.root.put(path, () -> {
            try {
                return future.get();
            } catch(InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
        return future;
    }

    public void addLazyRootResource(String path, BiFunction<RuntimeResourcePack, String, byte[]> data) {
        this.root.put(path, new Memoized<>(data, path));
    }

    public byte[] addRootResource(String path, byte[] data) {
        this.root.put(path, () -> data);
        return data;
    }

    public byte[] addAsset(Identifier path, byte[] data) {
        return this.addResource(ResourceType.CLIENT_RESOURCES, path, data);
    }

    public byte[] addData(Identifier path, byte[] data) {
        return this.addResource(ResourceType.SERVER_DATA, path, data);
    }

    public byte[] addModel(JModel model, Identifier path) {
        return this.addAsset(fix(path, "models", "json"), serialize(model));
    }

    public byte[] addBlockState(JState state, Identifier path) {
        return this.addAsset(fix(path, "blockstates", "json"), serialize(state));
    }

    public byte[] addTexture(Identifier id, BufferedImage image) {
        UnsafeByteArrayOutputStream ubaos = new UnsafeByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", ubaos);
        } catch(IOException e) {
            throw new RuntimeException("impossible.", e);
        }
        return this.addAsset(fix(id, "textures", "png"), ubaos.getBytes());
    }

    public byte[] addAnimation(Identifier id, JAnimation animation) {
        return this.addAsset(fix(id, "textures", "png.mcmeta"), serialize(animation));
    }

    public byte[] addTag(Identifier id, JTag tag) {
        return this.addData(fix(id, "tags", "json"), serialize(tag));
    }

    public byte[] addRecipe(Identifier id, JRecipe recipe) {
        return this.addData(fix(id, "recipes", "json"), serialize(recipe));
    }

    public Future<?> async(Consumer<RuntimeResourcePack> action) {
        this.lock();
        return EXECUTOR_SERVICE.submit(() -> {
            action.accept(this);
            this.waiting.unlock();
        });
    }

    public void dumpDirect(Path output) {
        ExtraSounds.LOGGER.info("dumping " + this.id + "'s assets and data");
        // data dump time
        try {
            for(Map.Entry<String, Supplier<byte[]>> e : this.root.entrySet()) {
                Path root = output.resolve(e.getKey());
                Files.createDirectories(root.getParent());
                Files.write(root, e.getValue().get());
            }

            Path assets = output.resolve("assets");
            Files.createDirectories(assets);
            for(Map.Entry<Identifier, Supplier<byte[]>> entry : this.assets.entrySet()) {
                this.write(assets, entry.getKey(), entry.getValue().get());
            }

            Path data = output.resolve("data");
            Files.createDirectories(data);
            for(Map.Entry<Identifier, Supplier<byte[]>> entry : this.data.entrySet()) {
                this.write(data, entry.getKey(), entry.getValue().get());
            }
        } catch(IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void load(Path dir) throws IOException {
        Stream<Path> stream = Files.walk(dir);
        for(Path file : (Iterable<Path>) () -> stream.filter(Files::isRegularFile).map(dir::relativize).iterator()) {
            String s = file.toString();
            if(s.startsWith("assets")) {
                String path = s.substring("assets".length() + 1);
                this.load(path, this.assets, Files.readAllBytes(file));
            } else if(s.startsWith("data")) {
                String path = s.substring("data".length() + 1);
                this.load(path, this.data, Files.readAllBytes(file));
            } else {
                byte[] data = Files.readAllBytes(file);
                this.root.put(s, () -> data);
            }
        }
        stream.close();
    }

    public void dump() {
        this.dump(DEFAULT_OUTPUT);
    }

    public void dump(Path path) {
        Identifier id = this.getId();
        Path folder = path.resolve(id.getNamespace() + ';' + id.getPath());
        this.dumpDirect(folder);
    }

    public void dump(File output) {
        this.dump(Paths.get(output.toURI()));
    }

    public void dump(ZipOutputStream zos) throws IOException {
        this.lock();
        for(Map.Entry<String, Supplier<byte[]>> entry : this.root.entrySet()) {
            zos.putNextEntry(new ZipEntry(entry.getKey()));
            zos.write(entry.getValue().get());
            zos.closeEntry();
        }

        for(Map.Entry<Identifier, Supplier<byte[]>> entry : this.assets.entrySet()) {
            Identifier id = entry.getKey();
            zos.putNextEntry(new ZipEntry("assets/" + id.getNamespace() + "/" + id.getPath()));
            zos.write(entry.getValue().get());
            zos.closeEntry();
        }

        for(Map.Entry<Identifier, Supplier<byte[]>> entry : this.data.entrySet()) {
            Identifier id = entry.getKey();
            zos.putNextEntry(new ZipEntry("data/" + id.getNamespace() + "/" + id.getPath()));
            zos.write(entry.getValue().get());
            zos.closeEntry();
        }
        this.waiting.unlock();
    }

    public void load(ZipInputStream stream) throws IOException {
        ZipEntry entry;
        while((entry = stream.getNextEntry()) != null) {
            String s = entry.toString();
            if(s.startsWith("assets")) {
                String path = s.substring("assets".length() + 1);
                this.load(path, this.assets, this.read(entry, stream));
            } else if(s.startsWith("data")) {
                String path = s.substring("data".length() + 1);
                this.load(path, this.data, this.read(entry, stream));
            } else {
                byte[] data = this.read(entry, stream);
                this.root.put(s, () -> data);
            }
        }
    }

    public Identifier getId() {
        return this.id;
    }

    /**
     * pack.png and that's about it I think/hope
     *
     * @param fileName the name of the file, can't be a path tho
     * @return the pack.png image as a stream
     */
    @Override
    public InputStream openRoot(String fileName) {
        if(!fileName.contains("/") && !fileName.contains("\\")) {
            this.lock();
            Supplier<byte[]> supplier = this.root.get(fileName);
            if(supplier == null) {
                this.waiting.unlock();
                return null;
            }

            this.waiting.unlock();
            return new ByteArrayInputStream(supplier.get());
        } else {
            throw new IllegalArgumentException("File name can't be a path");
        }
    }

    @Override
    public InputStream open(ResourceType type, Identifier id) {
        this.lock();
        Supplier<byte[]> supplier = this.getSys(type).get(id);
        if(supplier == null) {
            ExtraSounds.LOGGER.warn("No resource found for " + id);
            this.waiting.unlock();
            return null;
        }
        this.waiting.unlock();
        return new ByteArrayInputStream(supplier.get());
    }

    @Override
    public Collection<Identifier> findResources(ResourceType type, String namespace, String prefix, int maxDepth, Predicate<String> pathFilter) {
        this.lock();
        Set<Identifier> identifiers = new HashSet<>();
        for(Identifier identifier : this.getSys(type).keySet()) {
            if(identifier.getNamespace().equals(namespace) && identifier.getPath().startsWith(prefix) && pathFilter.test(identifier.getPath())) {
                identifiers.add(identifier);
            }
        }

        this.waiting.unlock();
        return identifiers;
    }

    @Override
    public boolean contains(ResourceType type, Identifier id) {
        this.lock();
        boolean contains = this.getSys(type).containsKey(id);
        this.waiting.unlock();
        return contains;
    }

    @Override
    public Set<String> getNamespaces(ResourceType type) {
        this.lock();
        Set<String> namespaces = new HashSet<>();
        for(Identifier identifier : this.getSys(type).keySet()) {
            namespaces.add(identifier.getNamespace());
        }
        this.waiting.unlock();
        return namespaces;
    }

    // if it works, don't touch it
    @Override
    public <T> T parseMetadata(ResourceMetadataReader<T> metaReader) {
        if(metaReader.getKey().equals("pack")) {
            JsonObject object = new JsonObject();
            object.addProperty("pack_format", this.packVersion);
            object.addProperty("description", "runtime resource pack");
            return metaReader.fromJson(object);
        }

        ExtraSounds.LOGGER.info("'" + metaReader.getKey() + "' is an unsupported metadata key!");
        return metaReader.fromJson(new JsonObject());
    }

    @Override
    public String getName() {
        return "Runtime Resource Pack (" + this.id + ")";
    }

    @Override
    public void close() {
        ExtraSounds.LOGGER.info("closing rrp " + this.id);

        // lock
        this.lock();
        if(DUMP) {
            this.dump();
        }

        // unlock
        this.waiting.unlock();
    }

    private static byte[] serialize(Object object) {
        UnsafeByteArrayOutputStream ubaos = new UnsafeByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(ubaos);
        GSON.toJson(object, writer);
        try {
            writer.close();
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
        return ubaos.getBytes();
    }

    private static Identifier fix(Identifier identifier, String prefix, String append) {
        return new Identifier(identifier.getNamespace(), prefix + '/' + identifier.getPath() + '.' + append);
    }

    protected byte[] read(ZipEntry entry, InputStream stream) throws IOException {
        byte[] data = new byte[Math.toIntExact(entry.getSize())];
        if(stream.read(data) != data.length) {
            throw new IOException("Zip stream was cut off! (maybe incorrect zip entry length? maybe u didn't flush your stream?)");
        }
        return data;
    }

    protected void load(String fullPath, Map<Identifier, Supplier<byte[]>> map, byte[] data) {
        int sep = fullPath.indexOf('/');
        String namespace = fullPath.substring(0, sep);
        String path = fullPath.substring(sep + 1);
        map.put(new Identifier(namespace, path), () -> data);
    }

    private void lock() {
        if(!this.waiting.tryLock()) {
            if(DEBUG_PERFORMANCE) {
                long start = System.currentTimeMillis();
                this.waiting.lock();
                long end = System.currentTimeMillis();
                ExtraSounds.LOGGER.warn("waited " + (end - start) + "ms for lock in RRP: " + this.id);
            } else {
                this.waiting.lock();
            }
        }
    }

    private void write(Path dir, Identifier identifier, byte[] data) {
        try {
            Path file = dir.resolve(identifier.getNamespace()).resolve(identifier.getPath());
            Files.createDirectories(file.getParent());
            try(OutputStream output = Files.newOutputStream(file)) {
                output.write(data);
            }
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<Identifier, Supplier<byte[]>> getSys(ResourceType side) {
        return side == ResourceType.CLIENT_RESOURCES ? this.assets : this.data;
    }

    private class Memoized<T> implements Supplier<byte[]> {
        private final BiFunction<RuntimeResourcePack, T, byte[]> func;
        private final T path;
        private byte[] data;

        public Memoized(BiFunction<RuntimeResourcePack, T, byte[]> func, T path) {
            this.func = func;
            this.path = path;
        }

        @Override
        public byte[] get() {
            if(this.data == null) {
                this.data = func.apply(RuntimeResourcePack.this, path);
            }

            return this.data;
        }
    }
}
