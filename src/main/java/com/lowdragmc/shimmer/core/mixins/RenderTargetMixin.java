package com.lowdragmc.shimmer.core.mixins;

import com.mojang.blaze3d.pipeline.RenderTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author KilaBash
 * @date 2022/05/03
 * @implNote RenderTargetMixin
 */
@Mixin(RenderTarget.class)
public abstract class RenderTargetMixin {

    @Redirect(method = "createBuffers",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;setFilterMode(I)V"))
    private void injectCreateBuffers(RenderTarget instance, int pFilterMode) {
        instance.setFilterMode(instance.filterMode == 0 ? pFilterMode : instance.filterMode);
    }
}
