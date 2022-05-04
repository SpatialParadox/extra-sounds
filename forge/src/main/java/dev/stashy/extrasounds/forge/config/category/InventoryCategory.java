package dev.stashy.extrasounds.forge.config.category;

import dev.stashy.extrasounds.config.category.IInventoryCategory;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class InventoryCategory implements IInventoryCategory {
    @ConfigEntry.BoundedDiscrete(max = 100)
    int volume = 100;

    boolean click = true;

    boolean drag = true;

    boolean drop = true;

    boolean open = true;

    @Override
    public int getMasterVolume() {
        return volume;
    }

    @Override
    public boolean isClickEnabled() {
        return click;
    }

    @Override
    public boolean isDragEnabled() {
        return drag;
    }

    @Override
    public boolean isDropEnabled() {
        return drop;
    }

    @Override
    public boolean isOpenEnabled() {
        return open;
    }
}
