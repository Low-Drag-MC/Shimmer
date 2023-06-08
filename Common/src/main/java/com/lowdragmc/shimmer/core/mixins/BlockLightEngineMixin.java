package com.lowdragmc.shimmer.core.mixins;

import com.google.common.primitives.Ints;
import com.lowdragmc.shimmer.client.light.LightManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.BlockLightEngine;
import net.minecraft.world.level.lighting.LayerLightSectionStorage;
import net.minecraft.world.level.lighting.LightEngine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
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

	@Inject(method = "getEmission" , at = @At("HEAD") , cancellable = true)
	private void a(long blockPos, BlockState blockState, CallbackInfoReturnable<Integer> cir){
		int light = LightManager.INSTANCE.getLight(this.chunkSource.getLevel(),BlockPos.of(blockPos));
		if (light >0) cir.setReturnValue(light);
	}

}
