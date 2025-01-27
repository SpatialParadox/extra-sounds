package dev.stashy.extrasounds.forge.config.category;

import dev.stashy.extrasounds.config.category.IScrollCategory;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class ScrollCategory implements IScrollCategory {
    @ConfigEntry.BoundedDiscrete(max = 100)
    int volume = 100;

    boolean inventoryScroll = true;

    boolean hotbarScroll = true;

    @Override
    public int getMasterVolume() {
        return volume;
    }

    @Override
    public boolean isInventoryScrollEnabled() {
        return inventoryScroll;
    }

    @Override
    public boolean isHotbarScrollEnabled() {
        return hotbarScroll;
    }
}
