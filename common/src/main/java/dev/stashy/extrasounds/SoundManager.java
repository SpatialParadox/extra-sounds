package dev.stashy.extrasounds;

import dev.stashy.extrasounds.debug.DebugUtils;
import dev.stashy.extrasounds.sound.SoundRegistry;
import dev.stashy.extrasounds.sound.SoundType;
import dev.stashy.extrasounds.sound.SoundTypeCategory;
import dev.stashy.extrasounds.sound.Sounds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class SoundManager {
    private static long lastPlayed = System.currentTimeMillis();

    public static void playSound(ItemStack stack, SoundType type) {
        Identifier itemId = Registry.ITEM.getId(stack.getItem());
        String idString = ExtraSounds.getClickId(itemId);
        if (!Identifier.isValid(idString)) {
            ExtraSounds.LOGGER.error("Unable to parse sound from ID: " + idString);
            return;
        }

        Identifier id = Identifier.tryParse(idString);
        SoundEvent event = SoundRegistry.SOUND_EVENTS.get(id);

        if (event != null) {
            playSound(event, type);
        } else {
            ExtraSounds.LOGGER.error("Sound cannot be found in registry: " + id);
        }
    }

    public static void playSound(StatusEffect effect, boolean add) {
        DebugUtils.effectLog(effect, add);

        SoundEvent e;
        if (add) {
            e = switch (effect.getCategory()) {
                case HARMFUL -> Sounds.EFFECT_ADD_NEGATIVE;
                case NEUTRAL, BENEFICIAL -> Sounds.EFFECT_ADD_POSITIVE;
            };
        } else {
            e = switch (effect.getCategory()) {
                case HARMFUL -> Sounds.EFFECT_REMOVE_NEGATIVE;
                case NEUTRAL, BENEFICIAL -> Sounds.EFFECT_REMOVE_POSITIVE;
            };
        }

        playSound(e, SoundType.EFFECT);
    }

    public static void playSound(SoundEvent snd, SoundType type) {
        playSound(snd, type.category, type.pitch);
    }

    public static void playSound(SoundEvent snd, SoundType type, float volume) {
        playSound(snd, volume, type.pitch);
    }

    public static void playSound(SoundEvent snd, SoundTypeCategory soundTypeCategory, float pitch) {
        playSound(snd, soundTypeCategory.getVolume() / 100.0f, pitch);
    }

    public static void playSound(SoundEvent snd, float volume, float pitch) {
        if (volume == 0.0f) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastPlayed > 5) {
            MinecraftClient.getInstance().getSoundManager()
                    .play(new PositionedSoundInstance(snd.getId(), SoundCategory.MASTER, volume,
                            pitch, false, 0, SoundInstance.AttenuationType.NONE, 0.0D, 0.0D,
                            0.0D, true));

            lastPlayed = now;
            DebugUtils.soundLog(snd);
        }
    }
}
