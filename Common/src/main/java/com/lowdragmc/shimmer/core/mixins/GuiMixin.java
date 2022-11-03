package com.lowdragmc.shimmer.core.mixins;

import com.lowdragmc.shimmer.client.auxiliaryScreen.Eyedropper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
	@Inject(method = "render",at = @At("HEAD"))
	private void updateAndRenderEyeDropper(PoseStack poseStack, float partialTick, CallbackInfo ci){
		Eyedropper.mode.update(poseStack);
	}
}
