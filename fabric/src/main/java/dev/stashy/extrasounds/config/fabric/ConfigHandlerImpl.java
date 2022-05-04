package dev.stashy.extrasounds.config.fabric;

import dev.stashy.extrasounds.config.IModConfig;
import dev.stashy.extrasounds.fabric.ExtraSoundsFabric;

public class ConfigHandlerImpl {
    public static IModConfig getConfig() {
        return ExtraSoundsFabric.CONFIG;
    }
}
