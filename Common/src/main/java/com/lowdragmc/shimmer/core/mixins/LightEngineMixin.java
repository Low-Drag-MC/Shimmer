package com.lowdragmc.shimmer.core.mixins;

import com.lowdragmc.shimmer.LightQueen;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import net.minecraft.world.level.lighting.LightEngine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(LightEngine.class)
public class LightEngineMixin {


	@Shadow @Final private LongArrayFIFOQueue increaseQueue;

	@Inject(method = "runLightUpdates", at = @At(value = "INVOKE",target = "Lit/unimi/dsi/fastutil/longs/LongOpenHashSet;trim(I)Z"))
	public void a(CallbackInfoReturnable<Integer> cir){
		var queue = LightQueen.queue;
		while(!queue.isEmpty()){
			increaseQueue.enqueue(queue.dequeueLong());
		}
	}
}