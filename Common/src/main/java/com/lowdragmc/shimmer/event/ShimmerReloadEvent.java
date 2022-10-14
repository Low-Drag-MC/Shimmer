package com.lowdragmc.shimmer.event;

/**
 * called when shimmer reload colored light and blooms<br>
 * subscribe to do java register<br>
 * for forge: subscriber ForgeShimmerReloadEvent<br>
 * for fabric: use FabricShimmerReloadCallback.EVENT#register
 */
public class ShimmerReloadEvent implements ShimmerEvent{
	public enum ReloadType{
		COLORED_LIGHT,BLOOM
	}

	private ReloadType reloadType;

	public ShimmerReloadEvent(ReloadType reloadType){
		this.reloadType = reloadType;
	}

	public ReloadType getReloadType() {
		return reloadType;
	}
}
