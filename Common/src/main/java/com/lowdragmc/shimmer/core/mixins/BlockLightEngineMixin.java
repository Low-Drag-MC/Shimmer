package com.lowdragmc.shimmer.core.mixins;

import com.lowdragmc.shimmer.client.light.LightManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.BlockLightEngine;
import net.minecraft.world.level.lighting.LayerLightSectionStorage;
import net.minecraft.world.level.lighting.LightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author KilaBash
 * @date 2022/05/29
 * @implNote LevelChunkMixin
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@Mixin(BlockLightEngine.class)
public abstract class BlockLightEngineMixin extends LightEngine {
	protected BlockLightEngineMixin(LightChunkGetter lightChunkGetter, LayerLightSectionStorage layerLightSectionStorage) {
		super(lightChunkGetter, layerLightSectionStorage);
		throw new RuntimeException("mixin class's constructor will ne be invoked");
	}

	@Inject(method = "getEmission" , at = @At("HEAD"), cancellable = true)
	private void shimmer$injectBlockLight(long pos, BlockState state, CallbackInfoReturnable<Integer> cir){
		var blockPos = BlockPos.of(pos);
		int light = LightManager.INSTANCE.getLight(state, blockPos);
		if (light > 0) cir.setReturnValue(light);
	}
}