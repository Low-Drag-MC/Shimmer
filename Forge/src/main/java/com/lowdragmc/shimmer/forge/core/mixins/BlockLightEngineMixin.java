package com.lowdragmc.shimmer.forge.core.mixins;

import com.lowdragmc.shimmer.client.light.LightManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.BlockLightEngine;
import net.minecraft.world.level.lighting.LayerLightSectionStorage;
import net.minecraft.world.level.lighting.LightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

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

//	@ModifyVariable(method = "getEmission"
//		, at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/level/block/state/BlockState;getLightEmission(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)I"))
//	private int injectColoredLightSource(int light,long pos, BlockState blockState){
//		if (Minecraft.getInstance().level == null) return light;
//		var shimmerLight = LightManager.INSTANCE.getLight(this.chunkSource.getLevel(), BlockPos.of(pos));
//		return Math.max(light,shimmerLight);
//		return light;
//	}

}
