package com.lowdragmc.shimmer.neoforge;

import net.neoforged.bus.api.IEventBus;

/**
 * @author KilaBash
 * @date 2022/05/02
 * @implNote com.lowdragmc.shimmer.CommonProxy
 */
public class CommonProxy {

    public CommonProxy(IEventBus modBus) {
        modBus.register(this);
    }

}
