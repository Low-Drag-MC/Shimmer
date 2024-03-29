package com.lowdragmc.shimmer.client.shader;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.lowdragmc.shimmer.ShimmerConstants;

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
public class ShaderInjection {
    private static final Map<String, List<Function<String, String>>> VSH_INJECTIONS = Maps.newHashMap();
    private static final Map<String, List<Function<String, String>>> FSH_INJECTIONS = Maps.newHashMap();

    public static void registerVSHInjection(String shaderName, Function<String, String> injection) {
        VSH_INJECTIONS.computeIfAbsent(shaderName, s->new ArrayList<>()).add(injection);
    }

    public static void registerFSHInjection(String shaderName, Function<String, String> injection) {
        FSH_INJECTIONS.computeIfAbsent(shaderName, s->new ArrayList<>()).add(injection);
    }

    public static boolean hasInjectFSH(String shaderName) {
        return FSH_INJECTIONS.containsKey(shaderName);
    }
    public static String injectFSH(String shaderName, String content) {
        ShimmerConstants.LOGGER.info("inject shader fsh {}.", shaderName);
        for (Function<String, String> function : FSH_INJECTIONS.getOrDefault(shaderName, Collections.emptyList())) {
            content = function.apply(content);
        }
        return content;
    }

    public static boolean hasInjectVSH(String shaderName) {
        return VSH_INJECTIONS.containsKey(shaderName);
    }

    public static String injectVSH(String shaderName, String content) {
        ShimmerConstants.LOGGER.info("inject shader vsh {}.", shaderName);
        for (Function<String, String> function : VSH_INJECTIONS.getOrDefault(shaderName, Collections.emptyList())) {
            content = function.apply(content);
        }
        return content;
    }
}
