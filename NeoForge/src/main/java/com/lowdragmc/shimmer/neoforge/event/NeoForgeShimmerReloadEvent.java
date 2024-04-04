package com.lowdragmc.shimmer.neoforge.event;

import com.lowdragmc.shimmer.event.ShimmerReloadEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

public class NeoForgeShimmerReloadEvent extends Event implements IModBusEvent {
	public final ShimmerReloadEvent event;

	public NeoForgeShimmerReloadEvent(ShimmerReloadEvent event) {
		this.event = event;
	}
}
