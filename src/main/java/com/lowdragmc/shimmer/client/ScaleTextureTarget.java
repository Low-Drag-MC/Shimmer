package com.lowdragmc.shimmer.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * @author KilaBash
 * @date 2022/05/03
 * @implNote Bloom Magic
 */
@OnlyIn(Dist.CLIENT)
public class ScaleTextureTarget extends RenderTarget {
    float scaleWidth;
    float scaleHeight;

    public ScaleTextureTarget(float scaleWidth, float scaleHeight, int pWidth, int pHeight, boolean pUseDepth, boolean pClearError) {
        super(pUseDepth);
        this.scaleWidth = scaleWidth;
        this.scaleHeight = scaleHeight;
        RenderSystem.assertOnRenderThreadOrInit();
        this.resize(pWidth, pHeight, pClearError);
    }

    @Override
    public void resize(int pWidth, int pHeight, boolean pClearError) {
        super.resize((int)(pWidth * scaleWidth), (int)(pHeight * scaleHeight), pClearError);
    }

}
