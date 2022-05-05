package com.lowdragmc.shimmer.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lowdragmc.shimmer.CommonProxy;
import com.lowdragmc.shimmer.client.bloom.Bloom;
import com.lowdragmc.shimmer.client.light.LightManager;
import com.lowdragmc.shimmer.client.shader.ShaderInjection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * @author KilaBash
 * @date: 2022/05/02
 * @implNote com.lowdragmc.shimmer.client.ClientProxy
 */
public class ClientProxy extends CommonProxy implements ResourceManagerReloadListener {

    public ClientProxy() {
        LightManager.injectShaders();
    }

    @SubscribeEvent
    public void shaderRegistry(RegisterShadersEvent event) {
        ShimmerRenderTypes.registerShaders(event);
    }

    @SubscribeEvent
    public void clientSetup(FMLClientSetupEvent e) {
        e.enqueueWork(()->{
            ((ReloadableResourceManager)Minecraft.getInstance().getResourceManager()).registerReloadListener(this);
            ItemBlockRenderTypes.setRenderLayer(CommonProxy.PISTON_BLOCK, renderType -> ShimmerRenderTypes.bloom() == renderType);
            ItemBlockRenderTypes.setRenderLayer(Blocks.FIRE, ShimmerRenderTypes.bloom());
        });
    }

    @SubscribeEvent
    public void modelBaked(ModelBakeEvent event) {

    }

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
        for (Bloom bloom : Bloom.values()) {
            bloom.onResourceManagerReload(resourceManager);
        }
    }
}
