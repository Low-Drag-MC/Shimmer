package com.lowdragmc.shimmer.forge.event;

import com.lowdragmc.shimmer.event.ShimmerReloadEvent;
import net.minecraftforge.eventbus.api.Event;

public class ForgeShimmerReloadEvent extends Event {
	public final ShimmerReloadEvent event;

	public ForgeShimmerReloadEvent(ShimmerReloadEvent event) {
		this.event = event;
	}
}
