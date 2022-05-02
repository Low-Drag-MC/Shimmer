package com.lowdragmc.shimmer.client.rendertype;

import com.lowdragmc.shimmer.ShimmerMod;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterShadersEvent;

import java.io.IOException;

/**
 * @author KilaBash
 * @date 2022/05/02
 * @implNote com.lowdragmc.shimmer.client.rendertype
 */
@OnlyIn(Dist.CLIENT)
public class ShimmerRenderTypes {
    private static RenderType BLOOM;

    public static RenderType bloom() {
        return BLOOM == null ? BLOOM = BloomRenderType.create() : BLOOM;
    }

    public static void registerShaders(RegisterShadersEvent event) {
        try {
            event.registerShader(new ShaderInstance(event.getResourceManager(),
                    new ResourceLocation(ShimmerMod.MODID, "rendertype_bloom"), DefaultVertexFormat.BLOCK),
                    shaderInstance -> BloomRenderType.brightSolidShader = shaderInstance);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class BloomRenderType extends RenderType {
        public static ShaderInstance brightSolidShader;
        private static final ShaderStateShard RENDERTYPE_BLOOM_SHADER = new ShaderStateShard(() -> brightSolidShader);

        // Dummy constructor needed to make java happy
        private BloomRenderType(String s, VertexFormat v, VertexFormat.Mode m, int i, boolean b, boolean b2, Runnable r, Runnable r2) {
            super(s, v, m, i, b, b2, r, r2);
            throw new IllegalStateException("This class is not meant to be constructed!");
        }

        private static RenderType create() {
            RenderType.CompositeState compositeState = RenderType.CompositeState.builder()
                    .setLightmapState(LIGHTMAP)
                    .setShaderState(RENDERTYPE_BLOOM_SHADER)
                    .setTextureState(BLOCK_SHEET)
                    .createCompositeState(true);
            return create("bloom", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 131072, true, false, compositeState);
        }
    }
}
