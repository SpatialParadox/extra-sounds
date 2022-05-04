package dev.stashy.extrasounds.mixin.inventory;

import dev.stashy.extrasounds.SoundManager;
import dev.stashy.extrasounds.config.ConfigHandler;
import dev.stashy.extrasounds.sound.SoundTypeCategory;
import dev.stashy.extrasounds.sound.Sounds;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public class InventoryScreenSounds {
    @Inject(at = @At("HEAD"), method = "init")
    void open(CallbackInfo ci) {
        if (ConfigHandler.getConfig().getInventory().isOpenEnabled()) {
            SoundManager.playSound(Sounds.INVENTORY_OPEN, SoundTypeCategory.INVENTORY, 1f);
        }
    }

    @Inject(at = @At("HEAD"), method = "close")
    void close(CallbackInfo ci) {
        if (ConfigHandler.getConfig().getInventory().isOpenEnabled()) {
            SoundManager.playSound(Sounds.INVENTORY_CLOSE, SoundTypeCategory.INVENTORY, 1f);
        }
    }
}
