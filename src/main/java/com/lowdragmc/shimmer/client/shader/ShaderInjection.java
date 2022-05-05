package com.lowdragmc.shimmer.client.shader;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.lowdragmc.shimmer.ShimmerMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author KilaBash
 * @date 2022/05/05
 * @implNote ShaderInjection
 */
@OnlyIn(Dist.CLIENT)
public class ShaderInjection {
    private static final Map<String, List<Function<String, String>>> VSH_INJECTIONS = Maps.newHashMap();
    private static final Map<String, List<Function<String, String>>> FSH_INJECTIONS = Maps.newHashMap();
    private static final Map<String, List<Function<JsonObject, JsonObject>>> CONFIG_INJECTIONS = Maps.newHashMap();

    public static void registerVSHInjection(String shaderName, Function<String, String> injection) {
        VSH_INJECTIONS.computeIfAbsent(shaderName, s->new ArrayList<>()).add(injection);
    }

    public static void registerFSHInjection(String shaderName, Function<String, String> injection) {
        FSH_INJECTIONS.computeIfAbsent(shaderName, s->new ArrayList<>()).add(injection);
    }

    public static void registerConfigInjection(String shaderName, Function<JsonObject, JsonObject> injection) {
        CONFIG_INJECTIONS.computeIfAbsent(shaderName, s->new ArrayList<>()).add(injection);
    }

    public static boolean hasInjectConfig(String shaderName) {
        return CONFIG_INJECTIONS.containsKey(shaderName);
    }
    public static JsonObject injectConfig(String shaderName, JsonObject config) {
        ShimmerMod.LOGGER.info("inject shader config {}.", shaderName);
        for (Function<JsonObject, JsonObject> function : CONFIG_INJECTIONS.getOrDefault(shaderName, Collections.emptyList())) {
            config = function.apply(config);
        }
        return config;
    }

    public static boolean hasInjectFSH(String shaderName) {
        return FSH_INJECTIONS.containsKey(shaderName);
    }
    public static String injectFSH(String shaderName, String content) {
        ShimmerMod.LOGGER.info("inject shader fsh {}.", shaderName);
        for (Function<String, String> function : FSH_INJECTIONS.getOrDefault(shaderName, Collections.emptyList())) {
            content = function.apply(content);
        }
        return content;
    }

    public static boolean hasInjectVSH(String shaderName) {
        return VSH_INJECTIONS.containsKey(shaderName);
    }
    public static String injectVSH(String shaderName, String content) {
        ShimmerMod.LOGGER.info("inject shader fsh {}.", shaderName);
        for (Function<String, String> function : VSH_INJECTIONS.getOrDefault(shaderName, Collections.emptyList())) {
            content = function.apply(content);
        }
        return content;
    }
}
