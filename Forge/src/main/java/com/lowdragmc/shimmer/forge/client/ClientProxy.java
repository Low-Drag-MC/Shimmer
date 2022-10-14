package com.lowdragmc.shimmer.forge.client;

import com.lowdragmc.shimmer.client.auxiliaryScreen.HsbColorWidget;
import com.lowdragmc.shimmer.forge.CommonProxy;
import com.lowdragmc.shimmer.client.ShimmerRenderTypes;
import com.lowdragmc.shimmer.Configuration;
import com.lowdragmc.shimmer.ShimmerConstants;
import com.lowdragmc.shimmer.client.light.LightManager;
import com.lowdragmc.shimmer.client.model.ShimmerMetadataSection;
import com.lowdragmc.shimmer.client.postprocessing.PostProcessing;
import com.lowdragmc.shimmer.client.shader.ReloadShaderManager;
import com.lowdragmc.shimmer.client.shader.RenderUtils;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.client.event.RegisterShadersEvent;
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
        LightManager.injectShaders();
        PostProcessing.injectShaders();
    }

    @SubscribeEvent
    public void shaderRegistry(RegisterShadersEvent event) {
        ResourceManager resourceManager = event.getResourceManager();
        try {
            event.registerShader(ReloadShaderManager.backupNewShaderInstance(resourceManager, new ResourceLocation(ShimmerConstants.MOD_ID, "fast_blit"), DefaultVertexFormat.POSITION), shaderInstance -> RenderUtils.blitShader = shaderInstance);
            event.registerShader(ReloadShaderManager.backupNewShaderInstance(resourceManager, new ResourceLocation(ShimmerConstants.MOD_ID, "rendertype_armor_cutout_no_cull"), DefaultVertexFormat.NEW_ENTITY),
                    shaderInstance -> ShimmerRenderTypes.EmissiveArmorRenderType.emissiveArmorGlintShader = shaderInstance);
            event.registerShader(ReloadShaderManager.backupNewShaderInstance(resourceManager,new ResourceLocation(ShimmerConstants.MOD_ID,"hsb_block"), HsbColorWidget.HSB_VERTEX_FORMAT), shaderInstance -> HsbColorWidget.hsbShader = shaderInstance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SubscribeEvent
    public void clientSetup(FMLClientSetupEvent e) {
        e.enqueueWork(() -> {
            ((ReloadableResourceManager)Minecraft.getInstance().getResourceManager()).registerReloadListener(this);
            onResourceManagerReload(Minecraft.getInstance().getResourceManager());
        });
    }

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
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
