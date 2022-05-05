package com.lowdragmc.shimmer;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.util.Arrays;
import java.util.List;

/**
 * @author KilaBash
 * @date 2022/05/05
 * @implNote Configs
 */
public class Configuration {

    public static void register() {
        registerClientConfigs();
    }

    private static void registerClientConfigs() {
        ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();
        PowergenConfig.registerClientConfig(CLIENT_BUILDER);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT_BUILDER.build());
    }

    public static class PowergenConfig {
        public static ForgeConfigSpec.ConfigValue<List<? extends String>> RENDER_SCALE;

        public static void registerClientConfig(ForgeConfigSpec.Builder CLIENT_BUILDER) {
            CLIENT_BUILDER.comment("Client settings for the power generator").push("powergen");
            RENDER_SCALE = CLIENT_BUILDER
                    .comment("Scale of the renderer")
                    .defineList("light", Arrays.asList("a", "b"), list-> true);
            CLIENT_BUILDER.pop();
        }
    }
}
