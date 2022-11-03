package com.lowdragmc.shimmer.config;

import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.Asserts;

import java.util.Map;

public class Bloom extends KeyShared implements Check, BlockChecker, FluidChecker {
	@SerializedName("fluid")
	public String fluidName;
	@SerializedName("particle")
	public String particleName;
	@SerializedName("block")
	public String blockName;
	@SerializedName("state")
	public Map<String, String> state;


	@Override
	public void check() {
		int i = 0;
		if (StringUtils.isNotEmpty(fluidName)) i++;
		if (StringUtils.isNotEmpty(particleName)) i++;
		if (StringUtils.isNotEmpty(blockName)) i++;
		Asserts.check(i == 1, "only of Field fluid, particle or block can be specified for bloom");
	}

	@Override
	public String getBlockName() {
		return blockName;
	}

	@Override
	public String getFluidName() {
		return fluidName;
	}

	public boolean hasState() {
		return state != null && !state.isEmpty();
	}

}