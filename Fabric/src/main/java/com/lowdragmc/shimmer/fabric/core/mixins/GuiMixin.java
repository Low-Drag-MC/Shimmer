package com.lowdragmc.shimmer.fabric.core.mixins;

import com.lowdragmc.shimmer.client.auxiliaryScreen.Eyedropper;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

	/**
	 * {@link net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback}<br>
	 * is later than crosshair render<br>
	 * inject it by ourselves
	 */
	@Inject(method = "renderCrosshair" , at = @At("HEAD"))
	private void injectRenderCrosshairForEyeDropper(GuiGraphics guiGraphics, CallbackInfo ci){
		Eyedropper.update(guiGraphics);
	}
}
