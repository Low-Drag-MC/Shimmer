package com.lowdragmc.shimmer.forge;

import com.lowdragmc.shimmer.ShimmerLoadConfigEvent;
import net.minecraftforge.eventbus.api.Event;


public class ForgeShimmerLoadConfigEvent extends Event {
    public final ShimmerLoadConfigEvent event;

    public ForgeShimmerLoadConfigEvent(ShimmerLoadConfigEvent event) {
        this.event = event;
    }

}
