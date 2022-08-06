package com.lowdragmc.shimmer.core.mixins;

import com.lowdragmc.shimmer.core.IMainTarget;
import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import org.lwjgl.opengl.*;
import org.spongepowered.asm.mixin.Mixin;

/**
 * @author KilaBash
 * @date 2022/05/31
 * @implNote MainTarget, bind MRT (Multiple Render Targets)
 */
@Mixin(MainTarget.class)
public abstract class MainTargetMixin extends RenderTarget implements IMainTarget {
    private int colorBloomTextureId = -1;

    private MainTargetMixin(boolean pUseDepth) {
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
            GL20.glDrawBuffers(GL30.GL_COLOR_ATTACHMENT1);
            GlStateManager._clearColor(0, 0, 0, 0);
            GlStateManager._clear(GL11.GL_COLOR_BUFFER_BIT, pClearError);
            GL20.glDrawBuffers(GL30.GL_COLOR_ATTACHMENT0);
            this.unbindWrite();
        }
    }

    @Override
    public void destroyBloomTextureBuffers() {
        if (this.colorBloomTextureId > -1) {
            TextureUtil.releaseTextureId(this.colorBloomTextureId);
            this.colorBloomTextureId = -1;
        }
    }

    @Override
    public void createBuffersHeads(int pWidth, int pHeight, boolean pClearError) {
        this.colorBloomTextureId = TextureUtil.generateTextureId();
        GlStateManager._bindTexture(this.colorBloomTextureId);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GlStateManager._texImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, pWidth, pHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, null);

    }

    @Override
    public void createBuffersTail(int pWidth, int pHeight, boolean pClearError) {
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.frameBufferId);
        GlStateManager._bindTexture(this.colorBloomTextureId);
        GlStateManager._glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT1, GL11.GL_TEXTURE_2D, this.colorBloomTextureId, 0);
    }

    @Override
    public void setBloomFilterMode(int pFilterMode) {
        GlStateManager._bindTexture(this.colorBloomTextureId);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, pFilterMode);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, pFilterMode);
        GlStateManager._bindTexture(0);
    }
}
