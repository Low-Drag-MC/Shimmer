package com.lowdragmc.shimmer.client.rendertarget;

import com.lowdragmc.shimmer.comp.iris.IrisHandle;
import com.mojang.blaze3d.pipeline.RenderTarget;

/**
 * only used for "intarget": "shimmer:composite_source",
 */
public class SelectRenderTarget extends RenderTarget {

    public SelectRenderTarget() {
        super(false);
    }

    @Override
    public int getColorTextureId() {
        return IrisHandle.INSTANCE.getCompositeId();
    }
}
