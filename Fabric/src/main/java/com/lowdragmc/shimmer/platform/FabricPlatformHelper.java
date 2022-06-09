package com.lowdragmc.shimmer.platform;

import com.lowdragmc.shimmer.platform.services.IPlatformHelper;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.fabricmc.loader.api.FabricLoader;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public boolean isStencilEnabled(RenderTarget target) {
        return false;
    }

    @Override
    public boolean useCombinedDepthStencilAttachment() {
        return false;
    }

    @Override
    public void enableStencil(RenderTarget renderTarget) {
        /* Unused on Fabric */
    }
}
