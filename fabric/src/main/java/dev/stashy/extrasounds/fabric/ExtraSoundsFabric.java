package dev.stashy.extrasounds.fabric;

import dev.stashy.extrasounds.ExtraSounds;
import dev.stashy.extrasounds.fabric.config.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;

public class ExtraSoundsFabric implements ClientModInitializer {
    public static ModConfig CONFIG;

    @Override
    public void onInitializeClient() {
        CONFIG = AutoConfig.register(ModConfig.class, GsonConfigSerializer::new).getConfig();
        ExtraSounds.init();
    }
}
