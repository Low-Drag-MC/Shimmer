package com.lowdragmc.shimmer.fabric.core.mixins.iris;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.lowdragmc.shimmer.fabric.compact.iris.GBufferMainRenderTarget;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.IrisChunkProgramOverrides;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = IrisChunkProgramOverrides.class, remap = false)
public class IrisChunkProgramOverridesMixin {

    @ModifyReceiver(method = "bindFramebuffer", at = @At(value = "INVOKE", target = "Lnet/coderbot/iris/gl/framebuffer/GlFramebuffer;bind()V"))
    @SuppressWarnings("unused")
    private GlFramebuffer recordBindedGbufferFrameBuffer(GlFramebuffer origin) {
        GBufferMainRenderTarget.lastGBufferUsedFrameBuffer = origin;
        return origin;
    }

}
