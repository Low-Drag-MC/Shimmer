package com.lowdragmc.shimmer;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class ForgeShimmerConfig {
    public static ForgeConfigSpec.IntValue UBO_OFFSET;

    public static void registerConfig(){
        ForgeConfigSpec.Builder commonBuilder = new ForgeConfigSpec.Builder();
        registerCommonConfig(commonBuilder);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON,commonBuilder.build());
        ForgeConfigSpec.Builder clientBuilder = new ForgeConfigSpec.Builder();
        registerClientConfig(clientBuilder);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT,clientBuilder.build());
        ForgeConfigSpec.Builder serverBuilder = new ForgeConfigSpec.Builder();
        registerServerConfig(serverBuilder);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER,serverBuilder.build());
    }

    private static void registerCommonConfig(ForgeConfigSpec.Builder builder){
    }

    private static void registerClientConfig(ForgeConfigSpec.Builder builder){
        UBO_OFFSET = builder.comment(
                "setting UBO offset",
                "-1 for auto setting",
                "maximum is limited by your gpu,the OpenGL constant:GL_MAX_UNIFORM_BUFFER_BINDINGS-1,at least 36-1")
                .defineInRange("UBO offset",-1,-1,128);
    }

    private static void registerServerConfig(ForgeConfigSpec.Builder builder){
    }
}
