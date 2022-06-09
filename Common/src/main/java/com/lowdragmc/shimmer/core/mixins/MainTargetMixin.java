package com.lowdragmc.shimmer.core.mixins;

import com.lowdragmc.shimmer.core.IMainTarget;
import com.lowdragmc.shimmer.platform.Services;
import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.Mixin;

/**
 * @author KilaBash
 * @date 2022/05/31
 * @implNote MainTarget, bind MRT (Multiple Render Targets)
 */
@Mixin(MainTarget.class)
public abstract class MainTargetMixin extends RenderTarget implements IMainTarget {
    private int colorBloomTextureId = -1;

    public MainTargetMixin(boolean pUseDepth) {
        super(pUseDepth);
    }

    @Override
    public int getColorBloomTextureId() {
        return colorBloomTextureId;
    }

    @Override
    public void clearBloomTexture(boolean pClearError) {
        if (colorBloomTextureId > -1) {
            this.bindWrite(true);
            GL30.glDrawBuffers(GL30.GL_COLOR_ATTACHMENT1);
            GlStateManager._clearColor(0, 0, 0, 0);
            GlStateManager._clear(16384, pClearError);
            GL30.glDrawBuffers(GL30.GL_COLOR_ATTACHMENT0);
            this.unbindWrite();
        }
    }

    @Override
    public void destroyBuffers() {
        super.destroyBuffers();
        if (this.colorBloomTextureId > -1) {
            TextureUtil.releaseTextureId(this.colorBloomTextureId);
            this.colorBloomTextureId = -1;
        }
    }

    @Override
    public void createBuffers(int pWidth, int pHeight, boolean pClearError) {
        RenderSystem.assertOnRenderThreadOrInit();
        int i = RenderSystem.maxSupportedTextureSize();
        if (pWidth > 0 && pWidth <= i && pHeight > 0 && pHeight <= i) {
            this.viewWidth = pWidth;
            this.viewHeight = pHeight;
            this.width = pWidth;
            this.height = pHeight;
            this.frameBufferId = GlStateManager.glGenFramebuffers();
            this.colorTextureId = TextureUtil.generateTextureId();
            this.colorBloomTextureId = TextureUtil.generateTextureId();
            if (this.useDepth) {
                this.depthBufferId = TextureUtil.generateTextureId();
                GlStateManager._bindTexture(this.depthBufferId);
                GlStateManager._texParameter(3553, 10241, 9728);
                GlStateManager._texParameter(3553, 10240, 9728);
                GlStateManager._texParameter(3553, 34892, 0);
                GlStateManager._texParameter(3553, 10242, 33071);
                GlStateManager._texParameter(3553, 10243, 33071);
                if (!Services.PLATFORM.isStencilEnabled(this))
                    GlStateManager._texImage2D(3553, 0, 6402, this.width, this.height, 0, 6402, 5126, null);
                else
                    GlStateManager._texImage2D(3553, 0, GL30.GL_DEPTH32F_STENCIL8, this.width, this.height, 0, GL30.GL_DEPTH_STENCIL, GL30.GL_FLOAT_32_UNSIGNED_INT_24_8_REV, null);
            }

            this.setFilterMode(9728);
            GlStateManager._bindTexture(this.colorTextureId);
            GlStateManager._texParameter(3553, 10242, 33071);
            GlStateManager._texParameter(3553, 10243, 33071);
            GlStateManager._texImage2D(3553, 0, 32856, this.width, this.height, 0, 6408, 5121, null);
            GlStateManager._glBindFramebuffer(36160, this.frameBufferId);
            GlStateManager._glFramebufferTexture2D(36160, 36064, 3553, this.colorTextureId, 0);

            GlStateManager._bindTexture(this.colorBloomTextureId);
            GlStateManager._texParameter(3553, 10242, 33071);
            GlStateManager._texParameter(3553, 10243, 33071);
            GlStateManager._texImage2D(3553, 0, 32856, this.width, this.height, 0, 6408, 5121, null);
            GlStateManager._glBindFramebuffer(36160, this.frameBufferId);
            GlStateManager._glFramebufferTexture2D(36160, 36065, 3553, this.colorBloomTextureId, 0);

            if (this.useDepth) {
                if(!Services.PLATFORM.isStencilEnabled(this))
                    GlStateManager._glFramebufferTexture2D(36160, 36096, 3553, this.depthBufferId, 0);
                else if(Services.PLATFORM.useCombinedDepthStencilAttachment()) {
                    GlStateManager._glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT, 3553, this.depthBufferId, 0);
                } else {
                    GlStateManager._glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, 3553, this.depthBufferId, 0);
                    GlStateManager._glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_STENCIL_ATTACHMENT, 3553, this.depthBufferId, 0);
                }
            }

            this.checkStatus();
            this.clear(pClearError);
            this.unbindRead();
        } else {
            throw new IllegalArgumentException("Window " + pWidth + "x" + pHeight + " size out of bounds (max. size: " + i + ")");
        }
    }

    @Override
    public void setFilterMode(int pFilterMode) {
        RenderSystem.assertOnRenderThreadOrInit();
        this.filterMode = pFilterMode;
        GlStateManager._bindTexture(this.colorTextureId);
        GlStateManager._texParameter(3553, 10241, pFilterMode);
        GlStateManager._texParameter(3553, 10240, pFilterMode);
        GlStateManager._bindTexture(0);

        GlStateManager._bindTexture(this.colorBloomTextureId);
        GlStateManager._texParameter(3553, 10241, pFilterMode);
        GlStateManager._texParameter(3553, 10240, pFilterMode);
        GlStateManager._bindTexture(0);
    }
}
