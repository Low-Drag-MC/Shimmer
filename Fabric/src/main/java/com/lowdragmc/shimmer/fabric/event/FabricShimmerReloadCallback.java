package com.lowdragmc.shimmer.fabric.event;

import com.lowdragmc.shimmer.event.ShimmerReloadEvent;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

@FunctionalInterface
public interface FabricShimmerReloadCallback {
	Event<FabricShimmerReloadCallback> EVENT = EventFactory.createArrayBacked(FabricShimmerReloadCallback.class,
			listeners -> event -> {
				for (var listener : listeners) {
					listener.onReload(event);
				}
				return event;
			});

	ShimmerReloadEvent onReload(ShimmerReloadEvent event);

}
