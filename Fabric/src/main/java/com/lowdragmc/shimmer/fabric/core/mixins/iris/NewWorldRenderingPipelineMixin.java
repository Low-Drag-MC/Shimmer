package com.lowdragmc.shimmer.fabric.core.mixins.iris;

import com.lowdragmc.shimmer.fabric.compact.iris.IrisFrameBufferWrapper;
import net.coderbot.iris.pipeline.newshader.NewWorldRenderingPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NewWorldRenderingPipeline.class)
public class NewWorldRenderingPipelineMixin {
    @Inject(method = "destroy",at = @At("RETURN"))
    private void injectDestroy(CallbackInfo ci){
        IrisFrameBufferWrapper.clear();
    }
}
