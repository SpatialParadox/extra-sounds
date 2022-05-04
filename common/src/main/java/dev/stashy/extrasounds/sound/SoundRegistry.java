package dev.stashy.extrasounds.sound;

import dev.architectury.registry.registries.Registrar;
import dev.stashy.extrasounds.ExtraSounds;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class SoundRegistry {
    public static final Registrar<SoundEvent> SOUND_EVENTS = ExtraSounds.REGISTRIES.get()
            .get(Registry.SOUND_EVENT_KEY);


    public static SoundEvent register(Identifier id) {
        var e = new SoundEvent(id);
        SOUND_EVENTS.register(id, () -> e);
        return e;
    }

    static SoundEvent register(String id) {
        return register(new Identifier(ExtraSounds.MOD_ID + ":" + id));
    }
}
