package com.lowdragmc.shimmer.client;

import com.lowdragmc.shimmer.ShimmerMod;
import com.lowdragmc.shimmer.client.light.LightManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * @author KilaBash
 * @date 2022/5/12
 * @implNote EventListener
 */
@Mod.EventBusSubscriber(modid = ShimmerMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class EventListener {
    public static void onWorldUnload(WorldEvent.Unload event) {
        LightManager.clear();
    }
}
