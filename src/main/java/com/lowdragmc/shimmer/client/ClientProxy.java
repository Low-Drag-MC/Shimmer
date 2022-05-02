package com.lowdragmc.shimmer.client;

import com.lowdragmc.shimmer.CommonProxy;
import com.lowdragmc.shimmer.client.rendertype.ShimmerRenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * @author KilaBash
 * @date: 2022/05/02
 * @implNote com.lowdragmc.shimmer.client.ClientProxy
 */
public class ClientProxy extends CommonProxy {

    @SubscribeEvent
    public void shaderRegistry(RegisterShadersEvent event) {
        ShimmerRenderTypes.registerShaders(event);
    }

    @SubscribeEvent
    public void clientSetup(FMLClientSetupEvent e) {
        e.enqueueWork(()->{
            ((ReloadableResourceManager)Minecraft.getInstance().getResourceManager()).registerReloadListener(Bloom.INSTANCE);
            ItemBlockRenderTypes.setRenderLayer(CommonProxy.TEST_BLOCK, renderType -> {
                if (ShimmerRenderTypes.bloom() == renderType) {
                    return true;
                }
                return false;
            });
        });
    }
}
