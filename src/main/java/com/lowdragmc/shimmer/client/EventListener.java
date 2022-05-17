package com.lowdragmc.shimmer.client;

import com.lowdragmc.shimmer.ShimmerMod;
import com.lowdragmc.shimmer.client.light.LightManager;
import com.lowdragmc.shimmer.client.postprocessing.PostProcessing;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * @author KilaBash
 * @date 2022/5/12
 * @implNote EventListener
 */
@Mod.EventBusSubscriber(modid = ShimmerMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class EventListener {
    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        if (event.getWorld() == Minecraft.getInstance().level) {
            LightManager.clear();
        }
    }

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("shimmer")
                .then(Commands.literal("reload_postprocessing")
                        .executes(context -> {
                            for (PostProcessing post : PostProcessing.values()) {
                                post.onResourceManagerReload(null);
                            }
                            return 1;
                        }))
                .then(Commands.literal("clear_lights")
                        .executes(context -> {
                            LightManager.clear();
                            return 1;
                        })));
    }
}
