package com.lowdragmc.shimmer.forge.event;

import com.lowdragmc.shimmer.event.ShimmerLoadConfigEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;


public class ForgeShimmerLoadConfigEvent extends Event implements IModBusEvent {
    public final ShimmerLoadConfigEvent event;

    public ForgeShimmerLoadConfigEvent(ShimmerLoadConfigEvent event) {
        this.event = event;
    }

}
