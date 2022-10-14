package com.lowdragmc.shimmer.forge.event;

import com.lowdragmc.shimmer.event.ShimmerLoadConfigEvent;
import net.minecraftforge.eventbus.api.Event;


public class ForgeShimmerLoadConfigEvent extends Event {
    public final ShimmerLoadConfigEvent event;

    public ForgeShimmerLoadConfigEvent(ShimmerLoadConfigEvent event) {
        this.event = event;
    }

}
