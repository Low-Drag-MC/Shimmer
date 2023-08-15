package com.lowdragmc.shimmer.client.shader;

import com.lowdragmc.shimmer.ShimmerConstants;
import com.lowdragmc.shimmer.comp.iris.IrisHandle;
import com.lowdragmc.shimmer.core.mixins.MixinPluginShared;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GL46;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * @author KilaBash
 * @date 2022/5/6
 * @implNote RenderUtils
 */
public class RenderUtils {
    public static ShaderInstance blitShader;

    public static void fastBlit(RenderTarget from, RenderTarget to) {
        RenderSystem.assertOnRenderThread();
        GlStateManager._colorMask(true, true, true, true);
        GlStateManager._disableDepthTest();
        GlStateManager._depthMask(false);

        to.bindWrite(true);

        if (MixinPluginShared.IS_IRIS_LOAD && to == Minecraft.getInstance().getMainRenderTarget()) {
            IrisHandle.INSTANCE.bindWriteMain();
        } else {
            Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
        }

        blitShader.setSampler("DiffuseSampler", from.getColorTextureId());

        blitShader.apply();//iris will disable color mask here, open here
        GL46.glColorMaski(0,true, true,true,true);
        GlStateManager._enableBlend();
        RenderSystem.defaultBlendFunc();

        Tesselator tesselator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferbuilder.vertex(-1, 1, 0).endVertex();
        bufferbuilder.vertex(-1, -1, 0).endVertex();
        bufferbuilder.vertex(1, -1, 0).endVertex();
        bufferbuilder.vertex(1, 1, 0).endVertex();
        BufferUploader.draw(bufferbuilder.end());
        blitShader.clear();

        GlStateManager._depthMask(true);
        GlStateManager._colorMask(true, true, true, true);
        GlStateManager._enableDepthTest();
    }

    public static PoseStack copyPoseStack(PoseStack poseStack) {
        PoseStack finalStack = new PoseStack();
        finalStack.setIdentity();
        finalStack.poseStack.addLast(poseStack.last());
        return finalStack;
    }

    public static Pair<ShaderInstance, Consumer<ShaderInstance>> registerShaders(ResourceManager resourceManager) {
        try {
            return Pair.of(ReloadShaderManager.backupNewShaderInstance(resourceManager, new ResourceLocation(ShimmerConstants.MOD_ID, "fast_blit").toString(), DefaultVertexFormat.POSITION), shaderInstance -> {
                blitShader = shaderInstance;
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final boolean DEBUG_LABEL_AVAILABLE = GL.getCapabilities().GL_KHR_debug;

    public static void warpGLDebugLabel(String message, Runnable block) {
        if (DEBUG_LABEL_AVAILABLE && ShimmerConstants.useOpenGlDebugLabel) {
            GL43.glPushDebugGroup(GL43.GL_DEBUG_SOURCE_APPLICATION, 0, message);
            block.run();
            GL43.glPopDebugGroup();
        } else {
            block.run();
        }
    }
}
