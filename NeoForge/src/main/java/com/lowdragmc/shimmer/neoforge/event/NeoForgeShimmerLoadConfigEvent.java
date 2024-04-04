package com.lowdragmc.shimmer.neoforge.event;

import com.lowdragmc.shimmer.event.ShimmerLoadConfigEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;


public class NeoForgeShimmerLoadConfigEvent extends Event implements IModBusEvent {
    public final ShimmerLoadConfigEvent event;

    public NeoForgeShimmerLoadConfigEvent(ShimmerLoadConfigEvent event) {
        this.event = event;
    }

}
