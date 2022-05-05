package dev.stashy.extrasounds.forge;

import dev.architectury.platform.forge.EventBuses;
import dev.stashy.extrasounds.ExtraSounds;
import dev.stashy.extrasounds.forge.config.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkConstants;

@Mod(ExtraSounds.MOD_ID)
public class ExtraSoundsForge {
    public static ModConfig CONFIG;

    public ExtraSoundsForge() {
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class,
                    () -> new ConfigGuiHandler.ConfigGuiFactory((client, parent) ->
                            AutoConfig.getConfigScreen(ModConfig.class, parent).get()));

            IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
            EventBuses.registerModEventBus(ExtraSounds.MOD_ID, eventBus);

            CONFIG = AutoConfig.register(ModConfig.class, GsonConfigSerializer::new).getConfig();
            ExtraSounds.init();
        });
    }
}
