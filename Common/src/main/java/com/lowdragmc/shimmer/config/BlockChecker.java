package com.lowdragmc.shimmer.config;

import com.lowdragmc.shimmer.ShimmerConstants;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.Objects;

interface BlockChecker extends Check {
	String getBlockName();

	default Pair<ResourceLocation, Block> block() {
		var blockName = getBlockName();
		Objects.requireNonNull(blockName);
		if (!ResourceLocation.isValidResourceLocation(blockName)) {
			ShimmerConstants.LOGGER.error("invalid block name " + blockName + " form" + getConfigSource());
			return null;
		}
		var blockLocation = new ResourceLocation(blockName);
		if (!BuiltInRegistries.BLOCK.containsKey(blockLocation)) {
			ShimmerConstants.LOGGER.error("can't find block " + blockLocation + " from" + getConfigSource());
			return Pair.of(blockLocation, null);
		}
		return Pair.of(blockLocation, BuiltInRegistries.BLOCK.get(blockLocation));
	}
}