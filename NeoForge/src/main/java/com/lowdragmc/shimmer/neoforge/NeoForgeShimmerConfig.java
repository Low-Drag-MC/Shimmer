package com.lowdragmc.shimmer.neoforge;

import com.lowdragmc.shimmer.ShimmerConstants;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig;

public class NeoForgeShimmerConfig {
    private static ModConfigSpec.IntValue UBO_OFFSET;
    private static ModConfigSpec.BooleanValue BLOCK_BLOOM;
    private static ModConfigSpec.BooleanValue LIGHT_MAP;
    private static ModConfigSpec.BooleanValue COLORED_LIGHT_ENABLE;
    private static ModConfigSpec.BooleanValue BLOOM_ENABLE;
    private static ModConfigSpec.BooleanValue ADDITIVE_BLEND;
    private static ModConfigSpec.IntValue BLOOM_COLOR_ATTACHMENT_NUMBER;
    private static ModConfigSpec.BooleanValue INSERT_SHADER_INFO;
    private static ModConfigSpec.BooleanValue ENABLE_BUILDIN_SETTING;

    public static ModConfigSpec.IntValue getUboOffset() {
        if (UBO_OFFSET == null) {
            logAccessUnInit("UBO_OFFSET");
            registerConfig();
        }
        return UBO_OFFSET;
    }

    public static ModConfigSpec.BooleanValue getBlockBloom() {
        if (BLOCK_BLOOM == null) {
            logAccessUnInit("BLOCK_BLOOM");
            registerConfig();
        }
        return BLOCK_BLOOM;
    }

    public static ModConfigSpec.BooleanValue getLightMap() {
        if (LIGHT_MAP == null) {
            logAccessUnInit("LIGHT_MAP");
            registerConfig();
        }
        return LIGHT_MAP;
    }

    public static ModConfigSpec.BooleanValue getColoredLightEnable(){
        if (COLORED_LIGHT_ENABLE == null){
            logAccessUnInit("COLORED_LIGHT_ENABLE");
            registerConfig();
        }
        return COLORED_LIGHT_ENABLE;
    }

    public static ModConfigSpec.BooleanValue getBloomEnable(){
        if (BLOOM_ENABLE == null){
            logAccessUnInit("BLOOM_ENABLE");
            registerConfig();
        }
        return BLOOM_ENABLE;
    }

    public static ModConfigSpec.BooleanValue getAdditiveBlend(){
        if (ADDITIVE_BLEND == null){
            logAccessUnInit("ADDITIVE_BLEND");
            registerConfig();
        }
        return ADDITIVE_BLEND;
    }

    //TODO config hasn't load when we need it, need change
    public static ModConfigSpec.IntValue getBloomColorAttachmentNumber() {
        if (BLOOM_COLOR_ATTACHMENT_NUMBER == null){
            logAccessUnInit("BLOOM_COLOR_ATTACHMENT_NUMBER");
            registerConfig();
        }
        return BLOOM_COLOR_ATTACHMENT_NUMBER;
    }

    //TODO config hasn't load when we need it, need change
    public static ModConfigSpec.BooleanValue getInsertShaderInfo(){
        if (INSERT_SHADER_INFO == null){
            logAccessUnInit("INSERT_SHADER_INFO");
            registerConfig();
        }
        return INSERT_SHADER_INFO;
    }

    public static ModConfigSpec.BooleanValue getEnableBuildinSetting(){
        if (ENABLE_BUILDIN_SETTING == null) {
            logAccessUnInit("ENABLE_BUILDIN_SETTING");
            registerConfig();
        }
        return ENABLE_BUILDIN_SETTING;
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
        ModConfigSpec.Builder clientBuilder = new ModConfigSpec.Builder();
        registerClientConfig(clientBuilder);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT,clientBuilder.build());
    }

    private static void registerClientConfig(ModConfigSpec.Builder builder){
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
                        "using light analyzeShaderProperties for more realistic lighting",
                        "true for light analyzeShaderProperties ON",
                        "If using the light analyzeShaderProperties, the light is more realistic and avoid lights through the wall. The light is smoother when closed. It is a tradeoff.")
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
        ENABLE_BUILDIN_SETTING = builder.comment(
        """
                whether enable buildin shimmer.json
                """
        ).define("enable buildin setting", true);
    }

}
