package dev.stashy.extrasounds.config.forge;

import dev.stashy.extrasounds.config.IModConfig;
import dev.stashy.extrasounds.forge.ExtraSoundsForge;

public class ConfigHandlerImpl {
    public static IModConfig getConfig() {
        return ExtraSoundsForge.CONFIG;
    }
}
