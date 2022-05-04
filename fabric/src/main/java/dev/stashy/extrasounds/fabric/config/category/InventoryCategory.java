package dev.stashy.extrasounds.fabric.config.category;

import dev.stashy.extrasounds.config.category.IInventoryCategory;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class InventoryCategory implements IInventoryCategory {
    @ConfigEntry.BoundedDiscrete(max = 100)
    @ConfigEntry.Category("inventory")
    int volume = 100;

    @ConfigEntry.Category("inventory")
    boolean click = true;

    @ConfigEntry.Category("inventory")
    boolean drag = true;

    @ConfigEntry.Category("inventory")
    boolean drop = true;

    @ConfigEntry.Category("inventory")
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
