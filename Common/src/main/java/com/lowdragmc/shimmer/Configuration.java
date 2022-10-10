package com.lowdragmc.shimmer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lowdragmc.shimmer.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author KilaBash
 * @date 2022/05/05
 * @implNote Configs
 */
public class Configuration {
    private static final ResourceLocation configLocation = new ResourceLocation(ShimmerConstants.MOD_ID, "shimmer.json");
    public static List<JsonObject> config = new ArrayList<>();

    public static void load() {
        config.clear();
        String causedSource = null;
        try {
            List<Resource> resources = Minecraft.getInstance().getResourceManager().getResourceStack(configLocation);
            for (var resource : resources){
                causedSource = resource.sourcePackId();
                try (InputStreamReader reader = new InputStreamReader(resource.open())) {
                    JsonElement jsonElement = JsonParser.parseReader(reader);
                    if (jsonElement instanceof JsonObject jsonObject){
                        config.add(jsonObject);
                    }else {
                        ShimmerConstants.LOGGER.info("failed to parse resource:{}",resource.sourcePackId());
                    }
                }
            }
            for (var entry : Services.PLATFORM.postLoadConfigurationEvent(new ShimmerLoadConfigEvent()).additionConfigurations.entrySet()) {
                causedSource = "configuration added by mod " + entry.getKey();
                JsonElement jsonElement = JsonParser.parseString(entry.getValue());
                if (jsonElement instanceof JsonObject jsonObject) {
                    config.add(jsonObject);
                } else {
                    ShimmerConstants.LOGGER.info("failed to parse configuration added by {}", entry.getKey());
                }
            }
        } catch (IOException ignored) {
            ShimmerConstants.LOGGER.info("failed to get config resources, caused by " + causedSource);
        }
    }

}
