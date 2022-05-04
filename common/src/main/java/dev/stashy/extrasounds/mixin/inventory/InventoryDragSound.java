package dev.stashy.extrasounds.mixin.inventory;

import dev.stashy.extrasounds.SoundManager;
import dev.stashy.extrasounds.config.ConfigHandler;
import dev.stashy.extrasounds.sound.SoundType;
import dev.stashy.extrasounds.sound.Sounds;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Set;

@Mixin(HandledScreen.class)
public class InventoryDragSound
{
    @Shadow
    @Final
    protected Set<Slot> cursorDragSlots;

    @Inject(method = "mouseDragged", at = @At(value = "INVOKE", target = "Ljava/util/Set;add(Ljava/lang/Object;)Z"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void dragSound(double mouseX, double mouseY, int button, double deltaX, double deltaY,
                           CallbackInfoReturnable<Boolean> cir, Slot slot) {

        if (!ConfigHandler.getConfig().getInventory().isDragEnabled()) {
            return;
        }

        if (!cursorDragSlots.contains(slot) && cursorDragSlots.size() > 0) {
            SoundManager.playSound(Sounds.ITEM_DRAG, SoundType.PLACE);
        }
    }
}
