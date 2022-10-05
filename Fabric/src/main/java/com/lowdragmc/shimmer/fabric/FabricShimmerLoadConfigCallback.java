package com.lowdragmc.shimmer.fabric;

import com.lowdragmc.shimmer.ShimmerLoadConfigEvent;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

@FunctionalInterface
public interface FabricShimmerLoadConfigCallback {
    Event<FabricShimmerLoadConfigCallback> EVENT = EventFactory.createArrayBacked(FabricShimmerLoadConfigCallback.class,
            (listeners) -> (event) -> {
                for (var listener : listeners) {
                    listener.addConfigurationList(event);
                }
                return event;
            });

    ShimmerLoadConfigEvent addConfigurationList(ShimmerLoadConfigEvent event);

}
