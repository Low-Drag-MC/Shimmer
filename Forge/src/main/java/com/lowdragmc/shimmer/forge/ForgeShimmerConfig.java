package com.lowdragmc.shimmer.forge;

import com.lowdragmc.shimmer.ShimmerConstants;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class ForgeShimmerConfig {
    private static ForgeConfigSpec.IntValue UBO_OFFSET;
    private static ForgeConfigSpec.BooleanValue BLOCK_BLOOM;
    private static ForgeConfigSpec.BooleanValue LIGHT_MAP;
    private static ForgeConfigSpec.BooleanValue COLORED_LIGHT_ENABLE;
    private static ForgeConfigSpec.BooleanValue BLOOM_ENABLE;
    private static ForgeConfigSpec.BooleanValue ADDITIVE_BLEND;
    private static ForgeConfigSpec.IntValue BLOOM_COLOR_ATTACHMENT_NUMBER;
    private static ForgeConfigSpec.BooleanValue INSERT_SHADER_INFO;

    public static ForgeConfigSpec.IntValue getUboOffset() {
        if (UBO_OFFSET == null) {
            logAccessUnInit("UBO_OFFSET");
            registerConfig();
        }
        return UBO_OFFSET;
    }

    public static ForgeConfigSpec.BooleanValue getBlockBloom() {
        if (BLOCK_BLOOM == null) {
            logAccessUnInit("BLOCK_BLOOM");
            registerConfig();
        }
        return BLOCK_BLOOM;
    }

    public static ForgeConfigSpec.BooleanValue getLightMap() {
        if (LIGHT_MAP == null) {
            logAccessUnInit("LIGHT_MAP");
            registerConfig();
        }
        return LIGHT_MAP;
    }

    public static ForgeConfigSpec.BooleanValue getColoredLightEnable(){
        if (COLORED_LIGHT_ENABLE == null){
            logAccessUnInit("COLORED_LIGHT_ENABLE");
            registerConfig();
        }
        return COLORED_LIGHT_ENABLE;
    }

    public static ForgeConfigSpec.BooleanValue getBloomEnable(){
        if (BLOOM_ENABLE == null){
            logAccessUnInit("BLOOM_ENABLE");
            registerConfig();
        }
        return BLOOM_ENABLE;
    }

    public static ForgeConfigSpec.BooleanValue getAdditiveBlend(){
        if (ADDITIVE_BLEND == null){
            logAccessUnInit("ADDITIVE_BLEND");
            registerConfig();
        }
        return ADDITIVE_BLEND;
    }

    //TODO config hasn't load when we need it, need change
    public static ForgeConfigSpec.IntValue getBloomColorAttachmentNumber() {
        if (BLOOM_COLOR_ATTACHMENT_NUMBER == null){
            logAccessUnInit("BLOOM_COLOR_ATTACHMENT_NUMBER");
            registerConfig();
        }
        return BLOOM_COLOR_ATTACHMENT_NUMBER;
    }

    //TODO config hasn't load when we need it, need change
    public static ForgeConfigSpec.BooleanValue getInsertShaderInfo(){
        if (INSERT_SHADER_INFO == null){
            logAccessUnInit("INSERT_SHADER_INFO");
            registerConfig();
        }
        return INSERT_SHADER_INFO;
    }

    private static void logAccessUnInit(String configValueName){
        ShimmerConstants.LOGGER.error("trying to access uninitialized shimmer config value:{}," +
            "see stacktrace at debug log file",configValueName);
        ShimmerConstants.LOGGER.debug("{} thread stacktrace",Thread.currentThread().getName());
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            ShimmerConstants.LOGGER.debug(element);
        }
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
        COLORED_LIGHT_ENABLE = builder.define("enable colored light", true);
        BLOOM_ENABLE = builder.define("enable bloom effect",true);
        ADDITIVE_BLEND = builder.comment(
                        "using additive blend for colored lights ",
                        "true - vivid, false - realistic")
                .define("additive effect",false);
        BLOOM_COLOR_ATTACHMENT_NUMBER = builder.comment(
        """
                the color attachment number used for store bloom information
                range from 1 (0 for vanilla use) to GL_MAX_COLOR_ATTACHMENTS-1, at least 8-1
                """
        ).defineInRange("bloom color attachment number", 1, 1, 16);
        INSERT_SHADER_INFO = builder.comment(
        """
                whether inset shader name into shader source file or not
                """
        ).define("inset shader", false);
    }

}
