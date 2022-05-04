package dev.stashy.extrasounds.fabric.config.category;

import dev.stashy.extrasounds.config.category.IChatCategory;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class ChatCategory implements IChatCategory {
    @ConfigEntry.BoundedDiscrete(max = 100)
    int volume = 100;

    boolean mention = true;

    boolean typing = true;

    @Override
    public int getMasterVolume() {
        return volume;
    }

    @Override
    public boolean isMentionEnabled() {
        return mention;
    }

    @Override
    public boolean isTypingEnabled() {
        return typing;
    }
}
