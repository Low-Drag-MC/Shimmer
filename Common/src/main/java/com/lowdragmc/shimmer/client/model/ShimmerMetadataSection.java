package com.lowdragmc.shimmer.client.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lowdragmc.shimmer.ShimmerConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public record ShimmerMetadataSection(boolean bloom) {
    public static final String SECTION_NAME = ShimmerConstants.MOD_ID;
    private static final Map<ResourceLocation, ShimmerMetadataSection> METADATA_CACHE = new HashMap<>();

    @Nullable
    public static ShimmerMetadataSection getMetadata(ResourceLocation res) {
        if (METADATA_CACHE.containsKey(res)) {
            return METADATA_CACHE.get(res);
        }
        ShimmerMetadataSection ret;
        try {
            var metadata = Minecraft.getInstance().getResourceManager().getResource(res)
                    .orElseThrow().metadata();
            ret = metadata.getSection(Serializer.INSTANCE).orElse(null);
        }catch (IOException | NoSuchElementException e){
            ret = null;
        }
        METADATA_CACHE.put(res, ret);
        return ret;
    }

    public static boolean isBloom(TextureAtlasSprite sprite) {
        @SuppressWarnings("resource")
        ShimmerMetadataSection ret = getMetadata(spriteToAbsolute(sprite.contents().name()));
        return ret != null && ret.bloom;
    }

    public static ResourceLocation spriteToAbsolute(ResourceLocation sprite) {
        if (!sprite.getPath().startsWith("textures/")) {
            sprite = new ResourceLocation(sprite.getNamespace(), "textures/" + sprite.getPath());
        }
        if (!sprite.getPath().endsWith(".png")) {
            sprite = new ResourceLocation(sprite.getNamespace(), sprite.getPath() + ".png");
        }
        return sprite;
    }

    public static void onResourceManagerReload() {
        METADATA_CACHE.clear();
    }

    public static class Serializer
            implements MetadataSectionSerializer<ShimmerMetadataSection> {
        static Serializer INSTANCE = new Serializer();

        @Override
        @NotNull
        public String getMetadataSectionName() {
            return SECTION_NAME;
        }

        @Override
        @NotNull
        public ShimmerMetadataSection fromJson(@NotNull JsonObject json) {
            boolean bloom = false;
            if (json.isJsonObject()) {
                JsonObject obj = json.getAsJsonObject();
                if (obj.has("bloom")) {
                    JsonElement element = obj.get("bloom");
                    if (element.isJsonPrimitive() &&
                            element.getAsJsonPrimitive().isBoolean()) {
                        bloom = element.getAsBoolean();
                    }
                }
            }
            return new ShimmerMetadataSection(bloom);
        }
    }
}
