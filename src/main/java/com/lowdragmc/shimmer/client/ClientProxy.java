package com.lowdragmc.shimmer.client;

import com.lowdragmc.shimmer.CommonProxy;
import com.lowdragmc.shimmer.ShimmerMod;
import com.lowdragmc.shimmer.client.bloom.Bloom;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.io.IOException;

/**
 * @author KilaBash
 * @date: 2022/05/02
 * @implNote com.lowdragmc.shimmer.client.ClientProxy
 */
public class ClientProxy extends CommonProxy {

    @SubscribeEvent
    public void shaderRegistry(RegisterShadersEvent event) {
        ShimmerRenderTypes.registerShaders(event);
        ResourceManager resourceManager = event.getResourceManager();
        try {
            event.registerShader(new ShaderInstance(resourceManager, new ResourceLocation(ShimmerMod.MODID, "rendertype_solid"), DefaultVertexFormat.BLOCK),
                    shaderInstance -> GameRenderer.rendertypeSolidShader = shaderInstance);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            event.registerShader(new ShaderInstance(resourceManager, new ResourceLocation(ShimmerMod.MODID, "rendertype_cutout"), DefaultVertexFormat.BLOCK),
                    shaderInstance -> GameRenderer.rendertypeCutoutShader = shaderInstance);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            event.registerShader(new ShaderInstance(resourceManager, new ResourceLocation(ShimmerMod.MODID, "rendertype_cutout_mipped"), DefaultVertexFormat.BLOCK),
                    shaderInstance -> GameRenderer.rendertypeCutoutMippedShader = shaderInstance);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            event.registerShader(new ShaderInstance(resourceManager, new ResourceLocation(ShimmerMod.MODID, "rendertype_tripwire"), DefaultVertexFormat.BLOCK),
                    shaderInstance -> GameRenderer.rendertypeTripwireShader = shaderInstance);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SubscribeEvent
    public void clientSetup(FMLClientSetupEvent e) {
        e.enqueueWork(()->{
            ((ReloadableResourceManager)Minecraft.getInstance().getResourceManager()).registerReloadListener(Bloom.INSTANCE);
            ItemBlockRenderTypes.setRenderLayer(CommonProxy.PISTON_BLOCK, renderType -> ShimmerRenderTypes.bloom() == renderType);
            ItemBlockRenderTypes.setRenderLayer(Blocks.FIRE, ShimmerRenderTypes.bloom());
        });
    }
}
