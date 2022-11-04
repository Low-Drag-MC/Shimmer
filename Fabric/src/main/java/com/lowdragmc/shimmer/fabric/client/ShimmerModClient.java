package com.lowdragmc.shimmer.fabric.client;

import com.lowdragmc.shimmer.Configuration;
import com.lowdragmc.shimmer.ShimmerConstants;
import com.lowdragmc.shimmer.client.auxiliaryScreen.AuxiliaryScreen;
import com.lowdragmc.shimmer.client.auxiliaryScreen.Eyedropper;
import com.lowdragmc.shimmer.client.light.LightManager;
import com.lowdragmc.shimmer.client.model.ShimmerMetadataSection;
import com.lowdragmc.shimmer.client.postprocessing.PostProcessing;
import com.lowdragmc.shimmer.client.shader.ReloadShaderManager;
import com.lowdragmc.shimmer.core.mixins.MixinPluginShared;
import com.lowdragmc.shimmer.fabric.FabricShimmerConfig;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

import javax.annotation.Nullable;

import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.literal;

/**
 * @author HypherionSA
 * @date 2022/06/09
 */
public class ShimmerModClient implements ClientModInitializer, SimpleSynchronousResourceReloadListener {

    @Override
    public void onInitializeClient() {
        LightManager.injectShaders();
	    PostProcessing.injectShaders();

        ClientCommandManager.DISPATCHER.register(literal("shimmer")
                .then(literal("reload_postprocessing")
                    .executes(context -> {
                        for (PostProcessing post : PostProcessing.values()) {
                            post.onResourceManagerReload(null);
                        }
                        return 1;
                    }))
                .then(literal("clear_lights")
                    .executes(context -> {
                        LightManager.clear();
                        return 1;
                    }))
                .then(literal("reload_shader")
                    .executes(context -> {
                        if (MixinPluginShared.IS_DASH_LOADER){
                            context.getSource().sendError(new TextComponent("disabled when dash loader is installed"));
                            return 0;
                        }else {
                            ReloadShaderManager.reloadShader();
                            return 1;
                        }
                    }))
                .then(literal("confirm_clear_resource")
                    .executes(context -> {
                        ReloadShaderManager.cleanResource();
                        return 1;
                    }))
                .then(literal("colored_light")
                    .then(argument("switch_state", BoolArgumentType.bool()).executes(
                        context -> {
                            FabricShimmerConfig.CONFIG.BLOCK_BLOOM.set(context.getArgument("switch_state", Boolean.class));
                            return 1;
                        }
                    )))
                .then(literal("bloom")
                    .then(argument("switch_state", BoolArgumentType.bool()).executes(
                        context -> {
                            FabricShimmerConfig.CONFIG.ENABLE_BLOOM_EFFECT.set(context.getArgument("switch_state", Boolean.class));
                            return 1;
                        }
                    )))
                .then(literal("additive_blend")
                        .then(argument("switch_state", BoolArgumentType.bool()).executes(
                                context -> {
                                    FabricShimmerConfig.CONFIG.ADDITIVE_EFFECT.set(context.getArgument("switch_state", Boolean.class));
                                    ReloadShaderManager.reloadShader();
                                    return 1;
                                }
                        )))
                .then(literal("auxiliary_screen")
                        .executes(context -> {
                            Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(new AuxiliaryScreen()));
                            return 1;
                        }))
                .then(literal("eyedropper")
                        .executes(context -> {
                            if (!Eyedropper.getState()) {
                                context.getSource().sendFeedback(new TextComponent("enter eyedropper mode, backend: " + Eyedropper.mode.modeName()));
                            } else {
                                context.getSource().sendFeedback(new TextComponent("exit eyedropper mode"));
                            }
                            Eyedropper.switchState();
                            return 1;
                        }))
                .then(literal("eyedropper").then(literal("backend")
                        .then(literal("ShaderStorageBufferObject").executes(
                                context -> {
                                    Eyedropper.switchMode(Eyedropper.ShaderStorageBufferObject);
                                    return 1;
                                }
                        )).then(literal("glGetTexImage").executes(
                                context -> {
                                    Eyedropper.switchMode(Eyedropper.DOWNLOAD);
                                    return 1;
                                }
                        ))))
        );

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(this);

        KeyBindingHelper.registerKeyBinding(ShimmerConstants.recordScreenColor);

        HudRenderCallback.EVENT.register((matrixStack, tickDelta) -> Eyedropper.update(matrixStack));
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
