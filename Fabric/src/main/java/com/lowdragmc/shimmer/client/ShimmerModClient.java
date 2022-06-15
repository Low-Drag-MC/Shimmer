package com.lowdragmc.shimmer.client;

import com.lowdragmc.shimmer.Configuration;
import com.lowdragmc.shimmer.client.light.LightManager;
import com.lowdragmc.shimmer.client.model.ShimmerMetadataSection;
import com.lowdragmc.shimmer.client.postprocessing.PostProcessing;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.literal;

/**
 * @author HypherionSA
 * @date 2022/06/09
 */
public class ShimmerModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Configuration.load();
        LightManager.injectShaders();
        PostProcessing.injectShaders();

        /*if (((Object)(MultiLayerModel.Loader.INSTANCE)) instanceof IMultiLayerModelLoader) {
            ((IMultiLayerModelLoader)(Object)(MultiLayerModel.Loader.INSTANCE)).update();
        }*/

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
                        })));

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {

            @Override
            public void onResourceManagerReload(ResourceManager resourceManager) {
                ShimmerMetadataSection.onResourceManagerReload();
                LightManager.onResourceManagerReload();
                for (PostProcessing postProcessing : PostProcessing.values()) {
                    postProcessing.onResourceManagerReload(resourceManager);
                }
            }

            @Override
            public ResourceLocation getFabricId() {
                return null;
            }
        });

        LightManager.INSTANCE.loadConfig();
    }

}
