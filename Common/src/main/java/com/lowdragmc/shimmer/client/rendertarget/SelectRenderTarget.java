package com.lowdragmc.shimmer.client.rendertarget;

import com.lowdragmc.shimmer.comp.iris.IrisHandle;
import com.lowdragmc.shimmer.core.mixins.MixinPluginShared;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;

/**
 * only used for "intarget": "shimmer:composite_source",
 */
public class SelectRenderTarget extends RenderTarget {

    public SelectRenderTarget() {
        super(false);
    }

    @Override
    public int getColorTextureId() {
        return MixinPluginShared.IS_IRIS_LOAD ? IrisHandle.INSTANCE.getCompositeId()
                : Minecraft.getInstance().getMainRenderTarget().getColorTextureId();
    }
}
