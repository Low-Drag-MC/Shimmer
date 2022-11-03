package com.lowdragmc.shimmer.config;

import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.Asserts;

import javax.annotation.Nullable;
import java.util.Map;


public class BlockLight extends Light implements BlockChecker, FluidChecker {
	@SerializedName("block")
	public String blockName;
	@SerializedName("fluid")
	public String fluidName;
	@SerializedName("state")
	@Nullable
	public Map<String, String> state;

	@Override
	public void check() {
		super.check();
		Asserts.check(StringUtils.isNotEmpty(blockName) || StringUtils.isNotEmpty(fluidName),
				"field block or fluid must be specified for BlockLight");
		Asserts.check(!(StringUtils.isNotEmpty(blockName) && StringUtils.isNotEmpty(fluidName)),
				"can't set block and fluid at same time");
	}

	public boolean hasState() {
		return state != null && !state.isEmpty();
	}

	@Override
	public String getBlockName() {
		return blockName;
	}

	@Override
	public String getFluidName() {
		return fluidName;
	}
}