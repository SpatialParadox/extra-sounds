package dev.stashy.extrasounds.mixin;

import com.mojang.datafixers.util.Unit;
import dev.stashy.extrasounds.ExtraSounds;
import dev.stashy.extrasounds.resource.ResourcePackCallback;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.minecraft.resource.ResourcePack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

@Mixin(ReloadableResourceManagerImpl.class)
public class ReloadableResourceManagerImplMixin {
    @ModifyVariable(method = "reload",
            at = @At(value = "HEAD"), argsOnly = true)
    private List<ResourcePack> registerARRPs(List<ResourcePack> packs, Executor prepareExecutor,
                                             Executor applyExecutor,
                                             CompletableFuture<Unit> initialStage,
                                             List<ResourcePack> packs0) throws ExecutionException, InterruptedException {
        ExtraSounds.LOGGER.info("Register resource packs - before vanilla");
        List<ResourcePack> before = new ArrayList<>();
        ResourcePackCallback.BEFORE_VANILLA.invoker().insert(before);

        before.addAll(packs);

        return before;
    }
}
