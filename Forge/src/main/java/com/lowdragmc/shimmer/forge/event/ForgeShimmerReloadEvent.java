package com.lowdragmc.shimmer.forge.event;

import com.lowdragmc.shimmer.event.ShimmerReloadEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;

public class ForgeShimmerReloadEvent extends Event implements IModBusEvent {
	public final ShimmerReloadEvent event;

	public ForgeShimmerReloadEvent(ShimmerReloadEvent event) {
		this.event = event;
	}
}
