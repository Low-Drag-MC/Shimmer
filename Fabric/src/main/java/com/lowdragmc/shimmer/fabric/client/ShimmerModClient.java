package com.lowdragmc.shimmer.fabric.client;

import com.lowdragmc.shimmer.Configuration;

import com.lowdragmc.shimmer.ShimmerFields;
import com.lowdragmc.shimmer.Utils;
import com.lowdragmc.shimmer.client.auxiliaryScreen.AuxiliaryScreen;
import com.lowdragmc.shimmer.client.auxiliaryScreen.Eyedropper;
import com.lowdragmc.shimmer.client.light.LightManager;
import com.lowdragmc.shimmer.client.model.ShimmerMetadataSection;
import com.lowdragmc.shimmer.client.postprocessing.PostProcessing;
import com.lowdragmc.shimmer.client.shader.ReloadShaderManager;
import com.lowdragmc.shimmer.core.mixins.MixinPluginShared;
import com.lowdragmc.shimmer.fabric.FabricShimmerConfig;
import com.lowdragmc.shimmer.platform.Services;
import com.lowdragmc.shimmer.renderdoc.RenderDoc;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;


/**
 * @author HypherionSA
 * @date 2022/06/09
 */
public class ShimmerModClient implements ClientModInitializer, SimpleSynchronousResourceReloadListener {

    @Override
    public void onInitializeClient() {
        LightManager.injectShaders();
        PostProcessing.injectShaders();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(literal("shimmer")
                        .then(literal("reload_postprocessing")
                                .executes(context -> {
                                    for (PostProcessing post : PostProcessing.values()) {
                                        post.onResourceManagerReload(null);
                                    }
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(literal("clear_lights")
                                .executes(context -> {
                                    LightManager.clear();
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(literal("reload_shader")
                                .executes(context -> {
                                    if (MixinPluginShared.IS_DASH_LOADER){
                                        context.getSource().sendFeedback(Component.literal("disabled when dash loader is installed"));
                                        return 0;
                                    }else {
                                        ReloadShaderManager.reloadShader();
                                        return Command.SINGLE_SUCCESS;
                                    }
                                }))
                        .then(literal("confirm_clear_resource")
                                .executes(context -> {
                                    ReloadShaderManager.cleanResource();
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(literal("colored_light")
                                .then(argument("switch_state", BoolArgumentType.bool()).executes(
                                        context -> {
                                            FabricShimmerConfig.CONFIG.BLOCK_BLOOM.set(context.getArgument("switch_state", Boolean.class));
                                            return Command.SINGLE_SUCCESS;
                                        }
                                )))
                        .then(literal("bloom")
                                .then(argument("switch_state", BoolArgumentType.bool()).executes(
                                        context -> {
                                            FabricShimmerConfig.CONFIG.ENABLE_BLOOM_EFFECT.set(context.getArgument("switch_state", Boolean.class));
                                            return Command.SINGLE_SUCCESS;
                                        }
                                )))
                        .then(literal("additive_blend")
                                .then(argument("switch_state", BoolArgumentType.bool()).executes(
                                        context -> {
                                            FabricShimmerConfig.CONFIG.ADDITIVE_EFFECT.set(context.getArgument("switch_state", Boolean.class));
                                            ReloadShaderManager.reloadShader();
                                            return Command.SINGLE_SUCCESS;
                                        }
                                )))
                        .then(literal("auxiliary_screen")
                                .executes(context -> {
                                    Minecraft.getInstance().tell(()-> Minecraft.getInstance().setScreen(new AuxiliaryScreen()));
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(literal("eyedropper")
                                .executes(context -> {
                                    if (!Eyedropper.getState()) {
                                        context.getSource().sendFeedback(Component.literal("enter eyedropper mode, backend: " + Eyedropper.mode.modeName()));
                                    } else {
                                        context.getSource().sendFeedback(Component.literal("exit eyedropper mode"));
                                    }
                                    Eyedropper.switchState();
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(literal("eyedropper").then(literal("backend")
                                .then(literal("ShaderStorageBufferObject").executes(
                                        context -> {
                                            Eyedropper.switchMode(Eyedropper.ShaderStorageBufferObject);
                                            return Command.SINGLE_SUCCESS;
                                        }
                                )).then(literal("glGetTexImage").executes(
                                        context -> {
                                            Eyedropper.switchMode(Eyedropper.DOWNLOAD);
                                            return Command.SINGLE_SUCCESS;
                                        }
                                ))))
                        .then(literal("dumpLightBlockStates")
                                .executes(context -> {
                                    if (Utils.dumpAllLightingBlocks()){
                                        context.getSource().sendFeedback(Component.literal("dump successfully to cfg/shimmer/LightBlocks.txt"));
                                    }else {
                                        context.getSource().sendError(Component.literal("dump failed, see log for detailed information"));
                                    }
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(literal("renderDoc")
                                .executes(context -> {
                                    if (Services.PLATFORM.isRenderDocEnable()) {
                                        var pid = RenderDoc.launchReplayUI(true);
                                        if (pid == 0) {
                                            context.getSource().sendError(Component.literal("unable to init renderDoc"));
                                        } else {
                                            context.getSource().sendFeedback(Component.literal("openSuccess, pid=" + pid));
                                        }
                                    } else {
                                        context.getSource().sendFeedback(Component.literal("renderDoc not enable"));
                                    }
                                    return Command.SINGLE_SUCCESS;
                                }))
                ));

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(this);

        KeyBindingHelper.registerKeyBinding(ShimmerFields.recordScreenColor);

        //error inject place, need render before crosshair
        //HudRenderCallback.EVENT.register((guiGraphics, tickDelta) -> Eyedropper.update(guiGraphics));
    }

    @Override
    public ResourceLocation getFabricId() {
        return null;
    }

    @Override
    public void onResourceManagerReload(@Nullable ResourceManager resourceManager) {
        Configuration.load();
        LightManager.INSTANCE.loadConfig();
        PostProcessing.loadConfig();
        ShimmerMetadataSection.onResourceManagerReload();
        LightManager.onResourceManagerReload();
        for (PostProcessing postProcessing : PostProcessing.values()) {
            postProcessing.onResourceManagerReload(resourceManager);
        }
    }
}
