package dev.stashy.extrasounds.fabric.config.category;

import dev.stashy.extrasounds.config.category.ICategory;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class EffectsCategory implements ICategory {
    @ConfigEntry.BoundedDiscrete(max = 100)
    int volume = 100;

    @Override
    public int getMasterVolume() {
        return volume;
    }
}
