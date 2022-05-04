package dev.stashy.extrasounds.config;

import dev.stashy.extrasounds.config.category.ICategory;
import dev.stashy.extrasounds.config.category.IChatCategory;
import dev.stashy.extrasounds.config.category.IInventoryCategory;
import dev.stashy.extrasounds.config.category.IScrollCategory;

public interface IModConfig {
    IInventoryCategory getInventory();
    IScrollCategory getScroll();
    ICategory getEffects();
    IChatCategory getChat();
}
