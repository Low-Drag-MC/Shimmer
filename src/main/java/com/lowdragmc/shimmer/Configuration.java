package com.lowdragmc.shimmer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;

import java.io.File;

/**
 * @author KilaBash
 * @date 2022/05/05
 * @implNote Configs
 */
public class Configuration {
    public static JsonObject config;

    public static void load() {
        // compatible with runData
        if (Minecraft.getInstance() != null && Minecraft.getInstance().gameDirectory != null) {
            File path = new File(Minecraft.getInstance().gameDirectory, "config/shimmer.json");
            FileUtility.extractJarFiles(String.format("/assets/%s/%s", ShimmerMod.MODID, "config"), new File(Minecraft.getInstance().gameDirectory, "config"), false);
            JsonElement jsonElement = FileUtility.loadJson(path);
            if (jsonElement instanceof JsonObject) {
                config = (JsonObject) jsonElement;
            }
        }
    }

}
