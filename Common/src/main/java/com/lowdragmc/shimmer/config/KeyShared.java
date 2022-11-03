package com.lowdragmc.shimmer.config;

public abstract class KeyShared implements Check {
	ShimmerConfig shimmerConfig;

	@Override
	public String getConfigSource() {
		return shimmerConfig.configSource;
	}
}