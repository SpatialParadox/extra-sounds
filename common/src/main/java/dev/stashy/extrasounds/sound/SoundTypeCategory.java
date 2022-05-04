package dev.stashy.extrasounds.sound;

import dev.stashy.extrasounds.config.ConfigHandler;
import dev.stashy.extrasounds.config.category.ICategory;

public enum SoundTypeCategory {
    INVENTORY("inventory", ConfigHandler.getConfig().getInventory()),
    SCROLL("scroll", ConfigHandler.getConfig().getScroll()),
    EFFECTS("effects", ConfigHandler.getConfig().getEffects()),
    CHAT("chat", ConfigHandler.getConfig().getChat());

    public final String configId;
    private final ICategory config;

    SoundTypeCategory(String id, ICategory config) {
        this.configId = id + "Volume";
        this.config = config;
    }

    public int getVolume() {
        return config.getMasterVolume();
    }
}
