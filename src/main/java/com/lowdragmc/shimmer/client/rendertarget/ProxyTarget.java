package com.lowdragmc.shimmer.client.rendertarget;

import com.mojang.blaze3d.pipeline.RenderTarget;

import javax.annotation.Nonnull;

/**
 * @author KilaBash
 * @date 2022/05/09
 * @implNote ProxyTarget
 */
public class ProxyTarget extends RenderTarget {
    RenderTarget parent;

    public ProxyTarget(boolean useDepth) {
        super(useDepth);
    }

    public ProxyTarget(RenderTarget parent) {
        super(parent.useDepth);
        setParent(parent);
    }

    public void setParent(RenderTarget parent) {
        this.parent = parent;
        this.width = parent.width;
        this.height = parent.height;
        this.filterMode = parent.filterMode;
        this.viewWidth = parent.viewWidth;
        this.viewHeight = parent.viewHeight;
        this.frameBufferId = parent.frameBufferId;
    }

    @Override
    public void resize(int pWidth, int pHeight, boolean pClearError) {
        this.viewWidth = pWidth;
        this.viewHeight = pHeight;
        this.width = pWidth;
        this.height = pHeight;
    }

    @Override
    public void destroyBuffers() {
        parent.destroyBuffers();
    }

    @Override
    public void copyDepthFrom(@Nonnull RenderTarget pOtherTarget) {
        parent.copyDepthFrom(pOtherTarget);
    }

    @Override
    public void createBuffers(int pWidth, int pHeight, boolean pClearError) {
        parent.createBuffers(pWidth, pHeight, pClearError);
    }

    @Override
    public void setFilterMode(int pFilterMode) {
        parent.setFilterMode(pFilterMode);
    }

    @Override
    public void checkStatus() {
        parent.checkStatus();
    }

    @Override
    public void bindRead() {
        parent.bindRead();
    }

    @Override
    public void unbindRead() {
        parent.unbindRead();
    }

    @Override
    public void bindWrite(boolean pSetViewport) {
        parent.bindWrite(pSetViewport);
    }

    @Override
    public void unbindWrite() {
        parent.unbindWrite();
    }

    @Override
    public void setClearColor(float pRed, float pGreen, float pBlue, float pAlpha) {
        parent.setClearColor(pRed, pGreen, pBlue, pAlpha);
    }

    @Override
    public void blitToScreen(int pWidth, int pHeight) {
        parent.blitToScreen(pWidth, pHeight);
    }

    @Override
    public void blitToScreen(int pWidth, int pHeight, boolean pDisableBlend) {
        parent.blitToScreen(pWidth, pHeight, pDisableBlend);
    }

    @Override
    public void clear(boolean pClearError) {
        parent.clear(pClearError);
    }

    @Override
    public int getColorTextureId() {
        return parent.getColorTextureId();
    }

    @Override
    public int getDepthTextureId() {
        return parent.getDepthTextureId();
    }

    @Override
    public void enableStencil() {
        parent.enableStencil();
    }

    @Override
    public boolean isStencilEnabled() {
        return parent.isStencilEnabled();
    }
}
