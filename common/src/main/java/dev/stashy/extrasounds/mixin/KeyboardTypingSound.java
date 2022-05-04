package dev.stashy.extrasounds.mixin;

import dev.stashy.extrasounds.SoundManager;
import dev.stashy.extrasounds.config.ConfigHandler;
import dev.stashy.extrasounds.sound.SoundType;
import dev.stashy.extrasounds.sound.Sounds;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextFieldWidget.class)
public class KeyboardTypingSound
{
    @Inject(at = @At("RETURN"), method = "charTyped")
    public void type(char chr, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (!ConfigHandler.getConfig().getChat().isTypingEnabled()) {
            return;
        }

        if (cir.getReturnValue()) {
            SoundManager.playSound(Sounds.KEYBOARD_TYPE, SoundType.CHAT);
        }
    }
}
