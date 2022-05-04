package dev.stashy.extrasounds.config;

import dev.architectury.injectables.annotations.ExpectPlatform;

public class ConfigHandler {
    @ExpectPlatform
    public static IModConfig getConfig() {
        throw new AssertionError();
    }
}
