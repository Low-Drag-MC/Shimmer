package com.lowdragmc.shimmer.core.mixins;

import com.google.common.collect.ImmutableList;
import com.lowdragmc.shimmer.client.ShimmerRenderTypes;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * @author KilaBash
 * @date 2022/05/02
 * @implNote RenderTypeMixin, inject a new rendertype (bloom)
 */
@Mixin(RenderType.class)
public class RenderTypeMixin {

    @Inject(method = "chunkBufferLayers",  at = @At(value = "HEAD"), cancellable = true)
    private static void injectChunkBufferLayers(CallbackInfoReturnable<List<RenderType>> cir) {
        cir.setReturnValue(ImmutableList.of(
                RenderType.solid(),
                RenderType.cutoutMipped(),
                RenderType.cutout(),
                ShimmerRenderTypes.bloom(),
                RenderType.translucent(),
                RenderType.tripwire()));
    }
}
