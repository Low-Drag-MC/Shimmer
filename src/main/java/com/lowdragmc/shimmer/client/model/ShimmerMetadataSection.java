package com.lowdragmc.shimmer.client.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lowdragmc.shimmer.ShimmerMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public record ShimmerMetadataSection(boolean bloom) {
    public static final String SECTION_NAME = ShimmerMod.MODID;
    private static final Map<ResourceLocation, ShimmerMetadataSection> METADATA_CACHE = new HashMap<>();

    @Nullable
    public static ShimmerMetadataSection getMetadata(ResourceLocation res) {
        if (METADATA_CACHE.containsKey(res)) {
            return METADATA_CACHE.get(res);
        }
        ShimmerMetadataSection ret;
        try (Resource resource = Minecraft.getInstance().getResourceManager()
                .getResource(res)) {
            ret = resource.getMetadata(Serializer.INSTANCE);
        } catch (Exception e) {
            ret = null;
        }
        METADATA_CACHE.put(res, ret);
        return ret;
    }

    public static boolean isBloom(TextureAtlasSprite sprite) {
        ShimmerMetadataSection ret = getMetadata(spriteToAbsolute(sprite.getName()));
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
        @Nonnull
        public String getMetadataSectionName() {
            return SECTION_NAME;
        }

        @Override
        @Nonnull
        public ShimmerMetadataSection fromJson(@Nonnull JsonObject json) {
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
