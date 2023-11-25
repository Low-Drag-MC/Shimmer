package com.lowdragmc.shimmer.comp.iris;

import java.util.function.Predicate;

public class ShaderpackInjections {
    public static void injectShaders() {
        injectTerrain();
    }

    public static void injectTerrain() {
        // BSL
        ShaderpackInjection.TERRAIN.registerTerrainVshInjection(contains("BSL Shaders"), s -> injectBefore(s, "if (mc_Entity.x >= 10200 && mc_Entity.x < 10300)", bloomUV("mat = 3.0;")));
        // AstraLex
        ShaderpackInjection.TERRAIN.registerTerrainVshInjection(contains("_   ___ _____ ___    _   _    _____  __"), s -> injectBefore(s, "if (mc_Entity.x == 10269 || mc_Entity.x == 10273 || mc_Entity.x == 10274)", bloomUV("mat = 4.0;")));
        // Complementary
        ShaderpackInjection.TERRAIN.registerTerrainVshInjection(contains("Complementary"), s -> injectAfter(s, "mat = int(mc_Entity.x + 0.5);", bloomUV("mat = 11000;")));
        ShaderpackInjection.TERRAIN.registerTerrainFshInjection(contains("Complementary"), s -> injectBefore(s, "vec2 lmCoordM = lmCoord;", "if (mat == 11000) emission = 5.0;"));
        // Super Duper Vanilla
        ShaderpackInjection.TERRAIN.registerTerrainVshInjection(contains("Super Duper Vanilla"), s -> injectAfter(s, "blockId = int(mc_Entity.x);", bloomUV("blockId = 11000;")));
        ShaderpackInjection.TERRAIN.registerTerrainFshInjection(contains("Super Duper Vanilla"), s -> injectBefore(s, "material.albedo.rgb = toLinear(material.albedo.rgb);", "if (blockId == 11000) {material.emissive = .5;material.smoothness = 0.9;}"));
    }

    private static Predicate<String> contains(String str) {
        return s -> s.contains(str);
    }

    private static String injectBefore(String content, String before, String injection) {
        int index = content.indexOf(before);
        if (index == -1) return content;
        return content.substring(0, index) + injection + content.substring(index);
    }

    private static String injectAfter(String content, String after, String injection) {
        int index = content.indexOf(after);
        if (index == -1) return content;
        return content.substring(0, index + after.length()) + injection + content.substring(index + after.length());
    }

    private static String bloomUV(String injection) {
        return "if((int(gl_MultiTexCoord1.x) & 0x100) != 0) {%s}".formatted(injection);
    }
}
