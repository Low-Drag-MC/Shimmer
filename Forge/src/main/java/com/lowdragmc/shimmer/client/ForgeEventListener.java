package com.lowdragmc.shimmer.client;

import com.lowdragmc.shimmer.ForgeShimmerConfig;
import com.lowdragmc.shimmer.ShimmerConstants;
import com.lowdragmc.shimmer.client.light.LightManager;
import com.lowdragmc.shimmer.client.postprocessing.PostProcessing;
import com.lowdragmc.shimmer.client.shader.ReloadShaderManager;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * @author KilaBash
 * @date 2022/5/12
 * @implNote ForgeEventListener
 */
@Mod.EventBusSubscriber(modid = ShimmerConstants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class ForgeEventListener {
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
                        }))
                .then(Commands.literal("reload_shader")
                        .executes(context -> {
                            ReloadShaderManager.reloadShader();
                            return 1;
                        }))
                .then(Commands.literal("confirm_clear_resource")
                        .executes(context -> {
                            ReloadShaderManager.cleanResource();
                            return 1;
                        })
                )
                .then(Commands.literal("colored_light")
                        .then(Commands.argument("switch_state", BoolArgumentType.bool()).executes(
                                context -> {
                                    ForgeShimmerConfig.getColoredLightEnable().set(context.getArgument("switch_state", Boolean.class));
                                    return 1;
                                }
                        )))
                .then(Commands.literal("bloom")
                        .then(Commands.argument("switch_state", BoolArgumentType.bool()).executes(
                                context -> {
                                    ForgeShimmerConfig.getBloomEnable().set(context.getArgument("switch_state", Boolean.class));
                                    return 1;
                                }
                        )))
                .then(Commands.literal("additive_blend")
                        .then(Commands.argument("switch_state", BoolArgumentType.bool()).executes(
                                context -> {
                                    ForgeShimmerConfig.getAdditiveBlend().set(context.getArgument("switch_state", Boolean.class));
                                    ReloadShaderManager.reloadShader();
                                    return 1;
                                }
                        )))
        );
    }
}
