package dev.stashy.extrasounds.sound;

import dev.stashy.extrasounds.ExtraSounds;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class SoundRegistry {
    // Using a map here, as (for some reason) the Registry is overwritten on Forge upon joining a Forge server.
    // This might be related to it syncing with the server (?). This still works in effectively the same way as the
    // but may break some things if we allow mod developers to add custom sounds.
    public static final Map<Identifier, SoundEvent> SOUND_EVENTS = new HashMap<>();


    public static SoundEvent register(Identifier id) {
        var e = new SoundEvent(id);
        SOUND_EVENTS.put(id, e);
        return e;
    }

    static SoundEvent register(String id) {
        return register(new Identifier(ExtraSounds.MOD_ID + ":" + id));
    }
}
