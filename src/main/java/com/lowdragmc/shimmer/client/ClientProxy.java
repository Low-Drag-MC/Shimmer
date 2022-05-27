package com.lowdragmc.shimmer.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lowdragmc.shimmer.CommonProxy;
import com.lowdragmc.shimmer.Configuration;
import com.lowdragmc.shimmer.client.postprocessing.PostProcessing;
import com.lowdragmc.shimmer.client.light.LightManager;
import com.lowdragmc.shimmer.client.shader.RenderUtils;
import com.lowdragmc.shimmer.core.IMultiLayerModelLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.model.MultiLayerModel;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author KilaBash
 * @date: 2022/05/02
 * @implNote com.lowdragmc.shimmer.client.ClientProxy
 */
public class ClientProxy extends CommonProxy implements ResourceManagerReloadListener {

    public ClientProxy() {
        Configuration.load();
        LightManager.injectShaders();
        // compatible with runData
        if (((Object)(MultiLayerModel.Loader.INSTANCE)) instanceof IMultiLayerModelLoader) {
            ((IMultiLayerModelLoader)(Object)(MultiLayerModel.Loader.INSTANCE)).update();
        }
    }

    @SubscribeEvent
    public void shaderRegistry(RegisterShadersEvent event) {
        RenderUtils.registerShaders(event);
        ShimmerRenderTypes.registerShaders(event);
    }

    @SubscribeEvent
    public void clientSetup(FMLClientSetupEvent e) {
        e.enqueueWork(() -> {
            ((ReloadableResourceManager)Minecraft.getInstance().getResourceManager()).registerReloadListener(this);
            LightManager.INSTANCE.loadConfig();
            JsonElement jsonElement = Configuration.config.get("CustomLayer");
            if (jsonElement.isJsonArray()) {
                JsonArray customLayers = jsonElement.getAsJsonArray();
                for (JsonElement object : customLayers) {
                    JsonObject jsonObj = object.getAsJsonObject();
                    if (jsonObj.has("block")) {
                        Block bb = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(jsonObj.get("block").getAsString()));
                        if (bb != null) {
                            RenderType[] types = detectTypes(jsonObj);
                            ItemBlockRenderTypes.setRenderLayer(bb, type -> ArrayUtils.contains(types, type));
                        }
                    } else if (jsonObj.has("fluid")) {
                        Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(jsonObj.get("fluid").getAsString()));
                        if (fluid != null) {
                            RenderType[] types = detectTypes(jsonObj);
                            ItemBlockRenderTypes.setRenderLayer(fluid, type -> ArrayUtils.contains(types, type));
                        }
                    }
                }
            }
        });
    }

    private RenderType[] detectTypes(JsonObject jsonObj) {
        JsonArray layers = jsonObj.get("layers").getAsJsonArray();
        RenderType[] types = new RenderType[layers.size()];
        for (int i = 0; i < layers.size(); i++) {
            types[i] = MultiLayerModel.Loader.BLOCK_LAYERS.get(layers.get(i).getAsString());
        }
        return types;
    }

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
        for (PostProcessing postProcessing : PostProcessing.values()) {
            postProcessing.onResourceManagerReload(resourceManager);
        }
    }
}
