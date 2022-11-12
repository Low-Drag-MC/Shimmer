package com.lowdragmc.shimmer.client.rendertarget;

import com.lowdragmc.shimmer.platform.Services;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL43;

/**
 * @author KilaBash
 * @date 2022/05/03
 * @implNote CopyDepthColorTarget, hook depth from other buffer
 */
public class CopyDepthColorTarget extends RenderTarget {
    private final float[] clearChannels = Util.make(() -> new float[]{1.0F, 1.0F, 1.0F, 0.0F});
    private int redirectedColorAttachment = -1;
    private final boolean hookColorAttachment;

    public CopyDepthColorTarget(RenderTarget hookTarget, boolean hookColorAttachment) {
        super(true);//always need depth
        this.hookColorAttachment = hookColorAttachment;
        RenderSystem.assertOnRenderThreadOrInit();
        this.resize(hookTarget.width, hookTarget.height, Minecraft.ON_OSX);
        hookDepthBuffer(this, hookTarget.getDepthTextureId());
        if (this.hookColorAttachment) {
            reAttachColorAttachment(this, this.getColorTextureId(), GL30.GL_COLOR_ATTACHMENT1);
            hookColorAttachment(this, hookTarget.getColorTextureId());
        }
    }

    public void resize(RenderTarget hookTarget, boolean onOsx) {
        this.resize(hookTarget.width, hookTarget.height, onOsx);
        hookDepthBuffer(this, hookTarget.getDepthTextureId());
        if (hookColorAttachment) {
            reAttachColorAttachment(this, this.getColorTextureId(), GL30.GL_COLOR_ATTACHMENT1);
            hookColorAttachment(this, hookTarget.getColorTextureId());
        }
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
        if (hookColorAttachment){
            this.enableSelfColorAttachment();
            GlStateManager._clear(GL30.GL_COLOR_BUFFER_BIT, pClearError);
            this.disableAttachment();
        }else {
            GlStateManager._clear(GL30.GL_COLOR_BUFFER_BIT, pClearError);
        }
        this.unbindWrite();
    }

    public void clearDepth(boolean pClearError) {
        super.clear(pClearError);
    }

    public static void hookDepthBuffer(RenderTarget fbo, int depthBuffer) {
        //Hook DepthBuffer
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo.frameBufferId);
        if (!Services.PLATFORM.isStencilEnabled(fbo))
            GlStateManager._glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_TEXTURE_2D, depthBuffer, 0);
        else if (Services.PLATFORM.useCombinedDepthStencilAttachment()) {
            GlStateManager._glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT, GL30.GL_TEXTURE_2D, depthBuffer, 0);
        } else {
            GlStateManager._glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_TEXTURE_2D, depthBuffer, 0);
            GlStateManager._glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_STENCIL_ATTACHMENT, GL30.GL_TEXTURE_2D, depthBuffer, 0);
        }
    }

    public static void reAttachColorAttachment(CopyDepthColorTarget fbo, int colorBuffer, int toColorAttachment) {
        fbo.redirectedColorAttachment = toColorAttachment;
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo.frameBufferId);
        GlStateManager._bindTexture(colorBuffer);
        GlStateManager._glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, toColorAttachment, GL30.GL_TEXTURE_2D, colorBuffer, 0);
    }

    public static void hookColorAttachment(RenderTarget fbo, int colorBuffer) {
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo.frameBufferId);
        GlStateManager._bindTexture(colorBuffer);
        GlStateManager._glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL30.GL_TEXTURE_2D, colorBuffer, 0);
    }

    public void enabledAttachment() {
        if (hookColorAttachment && this.redirectedColorAttachment > -1) {
            GL30.glDrawBuffers(new int[]{GL30.GL_COLOR_ATTACHMENT0, redirectedColorAttachment});
        }
    }

    public void enableSelfColorAttachment() {
        if (this.redirectedColorAttachment > -1) {
            GL30.glDrawBuffers(redirectedColorAttachment);
        }else {
            GL30.glDrawBuffers(GL30.GL_COLOR_ATTACHMENT0);
        }
    }

    public void disableAttachment() {
        GL43.glDrawBuffers(GL30.GL_COLOR_ATTACHMENT0);
    }

    @Override
    public void destroyBuffers() {
        super.destroyBuffers();
        this.redirectedColorAttachment = -1;
    }
}
