package com.lowdragmc.shimmer.client;

import com.lowdragmc.shimmer.ShimmerConstants;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author KilaBash
 * @date 2022/05/02
 * @implNote com.lowdragmc.shimmer.client.rendertype
 */
public class ShimmerRenderTypes {

    public static RenderType emissiveArmor(ResourceLocation resourceLocation) {
        return EmissiveArmorRenderType.EMISSIVE_ARMOR_CUTOUT_NO_CULL.apply(resourceLocation);
    }

    public static Pair<ShaderInstance, Consumer<ShaderInstance>> registerShaders(ResourceManager resourceManager) {
        try {
            return Pair.of(new ShaderInstance(resourceManager, new ResourceLocation(ShimmerConstants.MOD_ID, "rendertype_armor_cutout_no_cull").toString(), DefaultVertexFormat.NEW_ENTITY),
                    shaderInstance -> EmissiveArmorRenderType.emissiveArmorGlintShader = shaderInstance);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static class EmissiveArmorRenderType extends RenderType {
        public static ShaderInstance emissiveArmorGlintShader;
        private static final ShaderStateShard RENDERTYPE_BLOOM_SHADER = new ShaderStateShard(() -> emissiveArmorGlintShader);

        // Fxxk MOJ, have to use dummy constructor to make java happy
        private EmissiveArmorRenderType(String s, VertexFormat v, VertexFormat.Mode m, int i, boolean b, boolean b2, Runnable r, Runnable r2) {
            super(s, v, m, i, b, b2, r, r2);
            throw new IllegalStateException("This class is not meant to be constructed!");
        }

        private static final Function<ResourceLocation, RenderType> EMISSIVE_ARMOR_CUTOUT_NO_CULL = Util.memoize((p_173206_) -> {
            CompositeState rendertype$compositestate = CompositeState.builder()
                    .setShaderState(RENDERTYPE_BLOOM_SHADER)
                    .setTextureState(new TextureStateShard(p_173206_, false, false))
                    .setTransparencyState(NO_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                    .createCompositeState(true);
            return create("emissive_armor_cutout_no_cull", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, false, rendertype$compositestate);
        });

    }

}
