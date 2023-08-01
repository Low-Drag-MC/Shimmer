package com.lowdragmc.shimmer.core.mixins;

import com.lowdragmc.shimmer.platform.Services;
import com.lowdragmc.shimmer.renderdoc.RenderDoc;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.TimeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderSystem.class)
public class RenderSystemMixin {

	/**
	 * init renderDoc before glfw is init
	 */
	@Inject(method = "initBackendSystem", at = @At(value = "HEAD"))
	private static void initRenderDoc(CallbackInfoReturnable<TimeSource.NanoTimeSource> cir) {
		if (Services.PLATFORM.isRenderDocEnable()) {
			RenderDoc.init();
		}
	}
}
