package com.lowdragmc.shimmer.core.mixins;

import com.lowdragmc.shimmer.TracedGLState;
import com.mojang.blaze3d.platform.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlStateManager.class)
public abstract class GLStateManagerMixin {
	@Inject(method = "_glBindFramebuffer", at = @At("HEAD"), remap = false)
	private static void traceBindBuffer(int target, int frameBuffer, CallbackInfo ci) {
		TracedGLState.bindFrameBuffer = frameBuffer;
	}
}
