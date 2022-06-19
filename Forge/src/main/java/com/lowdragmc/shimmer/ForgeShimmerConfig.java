package com.lowdragmc.shimmer;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class ForgeShimmerConfig {
    public static ForgeConfigSpec.IntValue UBO_OFFSET;
    public static ForgeConfigSpec.BooleanValue BLOCK_BLOOM;
    public static ForgeConfigSpec.BooleanValue MRT_REVERSED;

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
        MRT_REVERSED = builder.comment(
                        "If you are using an AMD Graphics, you may need to set true for Shimmer to work properly")
                .define("MRT Reversed",false);
    }

}
