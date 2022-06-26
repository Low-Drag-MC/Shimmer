package com.lowdragmc.shimmer;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class ForgeShimmerConfig {
    private static ForgeConfigSpec.IntValue UBO_OFFSET;
    private static ForgeConfigSpec.BooleanValue BLOCK_BLOOM;
    private static ForgeConfigSpec.BooleanValue LIGHT_MAP;
    private static ForgeConfigSpec.BooleanValue MRT_REVERSED;

    public static ForgeConfigSpec.IntValue getUboOffset() {
        if (UBO_OFFSET == null) {
            registerConfig();
        }
        return UBO_OFFSET;
    }

    public static ForgeConfigSpec.BooleanValue getBlockBloom() {
        if (BLOCK_BLOOM == null) {
            registerConfig();
        }
        return BLOCK_BLOOM;
    }

    public static ForgeConfigSpec.BooleanValue getLightMap() {
        if (LIGHT_MAP == null) {
            registerConfig();
        }
        return LIGHT_MAP;
    }

    public static ForgeConfigSpec.BooleanValue getMrtReversed() {
        if (MRT_REVERSED == null) {
            registerConfig();
        }
        return MRT_REVERSED;
    }

    public static void registerConfig(){
        ForgeConfigSpec.Builder clientBuilder = new ForgeConfigSpec.Builder();
        registerClientConfig(clientBuilder);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT,clientBuilder.build());
    }

    private static void registerClientConfig(ForgeConfigSpec.Builder builder){
        UBO_OFFSET = builder.comment(
                "setting UBO offset",
                "-1 for auto setting",
                "maximum is limited by your gpu,the OpenGL constant:GL_MAX_UNIFORM_BUFFER_BINDINGS-1,at least 36-1")
                .defineInRange("UBO offset",-1,-1,128);
        BLOCK_BLOOM = builder.comment(
                        "setting block bloom",
                        "true for effect on",
                        "Block bloom only, does not apply to post-processing")
                .define("Block Bloom Effect",true);
        LIGHT_MAP = builder.comment(
                        "using light map for more realistic lighting",
                        "true for light map ON",
                        "If using the light map, the light is more realistic and avoid lights through the wall. The light is smoother when closed. It is a tradeoff.")
                .define("Using Light Map",true);
        MRT_REVERSED = builder.comment(
                        "If you are using an AMD Graphics, you may need to set true for Shimmer to work properly")
                .define("MRT Reversed",false);
    }

}
