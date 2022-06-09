package com.lowdragmc.shimmer.client.rendertarget;

import com.lowdragmc.shimmer.platform.Services;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import org.lwjgl.opengl.GL30;

/**
 * @author KilaBash
 * @date 2022/05/03
 * @implNote CopyDepthTarget, hook depth from other buffer
 */
public class CopyDepthTarget extends RenderTarget {
    private final float[] clearChannels = Util.make(() -> new float[]{1.0F, 1.0F, 1.0F, 0.0F});
    public CopyDepthTarget(RenderTarget depthBuffer, boolean onOsx) {
        super(true);
        RenderSystem.assertOnRenderThreadOrInit();
        this.resize(depthBuffer.width, depthBuffer.height, onOsx);
        hookDepthBuffer(this, depthBuffer.getDepthTextureId());
    }

    public void resize(RenderTarget depthBuffer, boolean onOsx) {
        this.resize(depthBuffer.width, depthBuffer.height, onOsx);
        hookDepthBuffer(this, depthBuffer.getDepthTextureId());
    }

    @Override
    public void setClearColor(float pRed, float pGreen, float pBlue, float pAlpha) {
        super.setClearColor(pRed, pGreen, pBlue, pAlpha);
        this.clearChannels[0] = pRed;
        this.clearChannels[1] = pGreen;
        this.clearChannels[2] = pBlue;
        this.clearChannels[3] = pAlpha;
    }

    @Override
    public void clear(boolean pClearError) {
        RenderSystem.assertOnRenderThreadOrInit();
        this.bindWrite(true);
        GlStateManager._clearColor(this.clearChannels[0], this.clearChannels[1], this.clearChannels[2], this.clearChannels[3]);
        int i = 16384;
        GlStateManager._clear(i, pClearError);
        this.unbindWrite();
    }

    public void clearDepth(boolean pClearError) {
        super.clear(pClearError);
    }

    public static void hookDepthBuffer(RenderTarget fbo, int depthBuffer) {
        //Hook DepthBuffer
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo.frameBufferId);
        if(!Services.PLATFORM.isStencilEnabled(fbo))
            GlStateManager._glFramebufferTexture2D(36160, 36096, 3553, depthBuffer, 0);
        else if(Services.PLATFORM.useCombinedDepthStencilAttachment()) {
            GlStateManager._glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT, 3553, depthBuffer, 0);
        } else {
            GlStateManager._glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, 3553, depthBuffer, 0);
            GlStateManager._glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_STENCIL_ATTACHMENT, 3553, depthBuffer, 0);
        }
    }

}
