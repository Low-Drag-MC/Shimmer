package com.lowdragmc.shimmer.client;

import com.lowdragmc.shimmer.CommonProxy;
import com.lowdragmc.shimmer.Configuration;
import com.lowdragmc.shimmer.ShimmerConstants;
import com.lowdragmc.shimmer.client.model.ShimmerMetadataSection;
import com.lowdragmc.shimmer.client.postprocessing.PostProcessing;
import com.lowdragmc.shimmer.client.light.LightManager;
import com.lowdragmc.shimmer.client.shader.RenderUtils;
import com.lowdragmc.shimmer.core.IMultiLayerModelLoader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.model.MultiLayerModel;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
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
        PostProcessing.injectShaders();
        // compatible with runData
        if (((Object)(MultiLayerModel.Loader.INSTANCE)) instanceof IMultiLayerModelLoader) {
            ((IMultiLayerModelLoader)(Object)(MultiLayerModel.Loader.INSTANCE)).update();
        }
    }

    @SubscribeEvent
    public void shaderRegistry(RegisterShadersEvent event) {
        ResourceManager resourceManager = event.getResourceManager();
        try {
            event.registerShader(new ShaderInstance(resourceManager, new ResourceLocation(ShimmerConstants.MOD_ID, "fast_blit"), DefaultVertexFormat.POSITION), shaderInstance -> RenderUtils.blitShader = shaderInstance);
            event.registerShader(new ShaderInstance(resourceManager, new ResourceLocation(ShimmerConstants.MOD_ID, "rendertype_armor_cutout_no_cull"), DefaultVertexFormat.NEW_ENTITY),
                    shaderInstance -> ShimmerRenderTypes.EmissiveArmorRenderType.emissiveArmorGlintShader = shaderInstance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SubscribeEvent
    public void clientSetup(FMLClientSetupEvent e) {
        e.enqueueWork(() -> {
            ((ReloadableResourceManager)Minecraft.getInstance().getResourceManager()).registerReloadListener(this);
            LightManager.INSTANCE.loadConfig();
        });
    }

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
        ShimmerMetadataSection.onResourceManagerReload();
        LightManager.onResourceManagerReload();
        for (PostProcessing postProcessing : PostProcessing.values()) {
            postProcessing.onResourceManagerReload(resourceManager);
        }
    }
}
