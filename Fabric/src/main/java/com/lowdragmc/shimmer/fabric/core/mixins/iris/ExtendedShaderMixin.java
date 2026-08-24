package com.lowdragmc.shimmer.fabric.core.mixins.iris;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.lowdragmc.shimmer.fabric.ShimmerMod;
import com.lowdragmc.shimmer.fabric.compact.iris.IrisFrameBufferWrapper;
import com.lowdragmc.shimmer.fabric.core.mixins.ShaderInstanceAccessor;
import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import net.irisshaders.iris.pipeline.programs.ExtendedShader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Objects;

@Mixin(value = ExtendedShader.class)
public class ExtendedShaderMixin {

    @SuppressWarnings("unused")
    @ModifyReceiver(method = "apply", at = @At(value = "INVOKE", target = "Lnet/irisshaders/iris/gl/framebuffer/GlFramebuffer;bind()V"))
    private GlFramebuffer injectFrameBufferToUseWrapper(GlFramebuffer origin) {
        if (ShimmerMod.postTextureId > 0 && Objects.equals(((ShaderInstanceAccessor) this).getName(), "particles")) {
            return IrisFrameBufferWrapper.from(origin, ShimmerMod.postTextureId);
        }
        return origin;
    }
}
