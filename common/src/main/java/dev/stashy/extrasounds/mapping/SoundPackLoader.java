package dev.stashy.extrasounds.mapping;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.stashy.extrasounds.ExtraSounds;
import dev.stashy.extrasounds.debug.DebugUtils;
import dev.stashy.extrasounds.json.SoundEntrySerializer;
import dev.stashy.extrasounds.json.SoundSerializer;
import dev.stashy.extrasounds.resource.ResourcePackCallback;
import dev.stashy.extrasounds.resource.RuntimeResourcePack;
import dev.stashy.extrasounds.sound.SoundRegistry;
import dev.stashy.extrasounds.sound.Sounds;
import net.minecraft.client.sound.Sound;
import net.minecraft.client.sound.SoundEntry;
import net.minecraft.item.BlockItem;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SoundPackLoader {
    private static final RuntimeResourcePack genericPack = RuntimeResourcePack.create(ExtraSounds.MOD_ID);
    private static final Identifier soundsJsonId = new Identifier("extrasounds:sounds.json");

    public static Map<String, SoundGenerator> mappers = new HashMap<>();

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(SoundEntry.class, new SoundEntrySerializer())
            .registerTypeAdapter(Sound.class, new SoundSerializer())
            .create();

    public static void init() {
        // Original mod used Fabric entrypoints to achieve this; Forge doesn't have these, so we'll define this manually.
        // This could be solved by providing an API for other mods to include their own sound generators, but this is
        // out of scope for now.
        SoundGenerator vanillaGenerator = VanillaGenerator.generator;
        mappers.put(vanillaGenerator.namespace(), vanillaGenerator);

        var itemMap = Registry.ITEM.getIds().stream().map(id -> {
            var sndId = new Identifier(ExtraSounds.MOD_ID, ExtraSounds.getClickId(id, false));
            if (!SoundRegistry.SOUND_EVENTS.containsKey(sndId)) {
                SoundRegistry.register(sndId);
            }

            SoundEntry snd = Sounds.aliased(Sounds.ITEM_PICK);
            if (mappers.containsKey(id.getNamespace())) {
                snd = mappers.get(id.getNamespace()).itemSoundGenerator().apply(id);
            } else if (Registry.ITEM.get(id) instanceof BlockItem b) {
                try {
                    snd = Sounds.event(
                            b.getBlock().getSoundGroup(b.getBlock().getDefaultState()).getPlaceSound().getId());
                } catch (Exception e) {
                    ExtraSounds.LOGGER.error("Failed to create SoundEvent for " + id);
                    ExtraSounds.LOGGER.error(e);
                }
            }

            return new Pair<>(sndId.getPath(), snd);
        }).collect(Collectors.toMap(Pair::getLeft, Pair::getRight, (a, b) -> b, HashMap::new));

        var json = gson.toJson(itemMap).getBytes();
        DebugUtils.exportSoundsJson(json);
        DebugUtils.exportGenerators();
        genericPack.addResource(ResourceType.CLIENT_RESOURCES, soundsJsonId, json);

        ResourcePackCallback.BEFORE_VANILLA.register((packs) -> {
            packs.add(genericPack);
        });
    }
}
