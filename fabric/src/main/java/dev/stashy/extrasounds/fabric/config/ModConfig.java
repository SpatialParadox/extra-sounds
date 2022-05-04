package dev.stashy.extrasounds.fabric.config;

import dev.stashy.extrasounds.ExtraSounds;
import dev.stashy.extrasounds.config.IModConfig;
import dev.stashy.extrasounds.config.category.ICategory;
import dev.stashy.extrasounds.config.category.IChatCategory;
import dev.stashy.extrasounds.config.category.IInventoryCategory;
import dev.stashy.extrasounds.config.category.IScrollCategory;
import dev.stashy.extrasounds.fabric.config.category.ChatCategory;
import dev.stashy.extrasounds.fabric.config.category.EffectsCategory;
import dev.stashy.extrasounds.fabric.config.category.InventoryCategory;
import dev.stashy.extrasounds.fabric.config.category.ScrollCategory;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = ExtraSounds.MOD_ID)
public class ModConfig implements IModConfig, ConfigData {
    @ConfigEntry.Gui.TransitiveObject
    InventoryCategory inventory = new InventoryCategory();

    @ConfigEntry.Gui.TransitiveObject
    ScrollCategory scroll = new ScrollCategory();

    @ConfigEntry.Gui.TransitiveObject
    EffectsCategory effects = new EffectsCategory();

    @ConfigEntry.Gui.TransitiveObject
    ChatCategory chat = new ChatCategory();

    @Override
    public IInventoryCategory getInventory() {
        return inventory;
    }

    @Override
    public IScrollCategory getScroll() {
        return scroll;
    }

    @Override
    public ICategory getEffects() {
        return effects;
    }

    @Override
    public IChatCategory getChat() {
        return chat;
    }
}
