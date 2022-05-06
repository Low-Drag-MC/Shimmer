package com.lowdragmc.shimmer.client;

import com.lowdragmc.shimmer.ShimmerMod;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterShadersEvent;
import org.lwjgl.opengl.GL30;

import java.io.IOException;
import java.util.function.Function;

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

    public static RenderType emissiveArmor(ResourceLocation resourceLocation) {
        return EmissiveArmorRenderType.EMISSIVE_ARMOR_CUTOUT_NO_CULL.apply(resourceLocation);
    }

    public static void registerShaders(RegisterShadersEvent event) {
        ResourceManager resourceManager = event.getResourceManager();
        try {
            event.registerShader(new ShaderInstance(resourceManager, new ResourceLocation(ShimmerMod.MODID, "rendertype_bloom"), DefaultVertexFormat.BLOCK),
                    shaderInstance -> BloomRenderType.bloomShader = shaderInstance);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            event.registerShader(new ShaderInstance(resourceManager, new ResourceLocation(ShimmerMod.MODID, "rendertype_armor_cutout_no_cull"), DefaultVertexFormat.NEW_ENTITY),
                    shaderInstance -> EmissiveArmorRenderType.emissiveArmorGlintShader = shaderInstance);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class BloomRenderType extends RenderType {
        public static ShaderInstance bloomShader;
        private static final ShaderStateShard RENDERTYPE_BLOOM_SHADER = new ShaderStateShard(() -> bloomShader) {
            @Override
            public void setupRenderState() {
                GL30.glDrawBuffers(new int[] {GL30.GL_COLOR_ATTACHMENT0, GL30.GL_COLOR_ATTACHMENT1});
                super.setupRenderState();
            }

            @Override
            public void clearRenderState() {
                super.clearRenderState();
                GL30.glDrawBuffers(GL30.GL_COLOR_ATTACHMENT0);
            }
        };

        // Fxxk MOJ, have to use dummy constructor to make java happy
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

    private static class EmissiveArmorRenderType extends RenderType {
        public static ShaderInstance emissiveArmorGlintShader;
        private static final ShaderStateShard RENDERTYPE_BLOOM_SHADER = new ShaderStateShard(() -> emissiveArmorGlintShader);

        // Fxxk MOJ, have to use dummy constructor to make java happy
        private EmissiveArmorRenderType(String s, VertexFormat v, VertexFormat.Mode m, int i, boolean b, boolean b2, Runnable r, Runnable r2) {
            super(s, v, m, i, b, b2, r, r2);
            throw new IllegalStateException("This class is not meant to be constructed!");
        }

        private static final Function<ResourceLocation, RenderType> EMISSIVE_ARMOR_CUTOUT_NO_CULL = Util.memoize((p_173206_) -> {
            RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_BLOOM_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(p_173206_, false, false))
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
