package com.lowdragmc.shimmer.client.shader;

import com.lowdragmc.shimmer.ShimmerMod;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterShadersEvent;

import java.io.IOException;

/**
 * @author KilaBash
 * @date 2022/5/6
 * @implNote ShaderUtils
 */
@OnlyIn(Dist.CLIENT)
public class ShaderUtils {
    public static ShaderInstance blitShader;

    public static void fastBlit(RenderTarget from, RenderTarget to) {
        RenderSystem.assertOnRenderThread();
        GlStateManager._colorMask(true, true, true, true);
        GlStateManager._disableDepthTest();
        GlStateManager._depthMask(false);

        to.bindWrite(true);

        blitShader.setSampler("DiffuseSampler", from.getColorTextureId());

        blitShader.apply();
        GlStateManager._enableBlend();
        RenderSystem.defaultBlendFunc();

        Tesselator tesselator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferbuilder.vertex(-1, 1, 0).endVertex();
        bufferbuilder.vertex(-1, -1, 0).endVertex();
        bufferbuilder.vertex(1, -1, 0).endVertex();
        bufferbuilder.vertex(1, 1, 0).endVertex();
        bufferbuilder.end();
        BufferUploader._endInternal(bufferbuilder);
        blitShader.clear();

        GlStateManager._depthMask(true);
        GlStateManager._colorMask(true, true, true, true);
        GlStateManager._enableDepthTest();
    }

    public static void registerShaders(RegisterShadersEvent event) {
        ResourceManager resourceManager = event.getResourceManager();
        try {
            event.registerShader(new ShaderInstance(resourceManager, new ResourceLocation(ShimmerMod.MODID, "fast_blit"), DefaultVertexFormat.POSITION), shaderInstance -> blitShader = shaderInstance);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
