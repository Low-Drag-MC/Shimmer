package com.lowdragmc.shimmer.config;

import com.lowdragmc.shimmer.ShimmerConstants;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

import java.util.Objects;

interface FluidChecker extends Check {

	String getFluidName();

	default Pair<ResourceLocation, Fluid> fluid() {
		var fluidName = getFluidName();
		Objects.requireNonNull(fluidName);
		if (!ResourceLocation.isValidResourceLocation(fluidName)) {
			ShimmerConstants.LOGGER.error("invalid fluid name " + fluidName + " form" + getConfigSource());
			return null;
		}
		var fluidLocation = new ResourceLocation(fluidName);
		if (!Registry.FLUID.containsKey(fluidLocation)) {
			ShimmerConstants.LOGGER.error("can't find fluid " + fluidLocation + " from" + getConfigSource());
			return Pair.of(fluidLocation, null);
		}
		Fluid fluid = Registry.FLUID.get(fluidLocation);
		return Pair.of(fluidLocation, fluid);
	}
}