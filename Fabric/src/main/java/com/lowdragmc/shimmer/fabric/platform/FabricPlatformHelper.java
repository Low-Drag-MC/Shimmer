package com.lowdragmc.shimmer.fabric.platform;

import com.lowdragmc.shimmer.ShimmerLoadConfigEvent;
import com.lowdragmc.shimmer.fabric.FabricShimmerConfig;
import com.lowdragmc.shimmer.fabric.FabricShimmerLoadConfigCallback;
import com.lowdragmc.shimmer.platform.services.IPlatformHelper;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.opengl.GL30;

/**
 * @author HypherionSA
 * @date 2022/06/09
 */
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

    @Override
    public int getUniformBufferObjectOffset() {
        return FabricShimmerConfig.CONFIG.UBO_OFFSET.get();
    }

    @Override
    public boolean useBlockBloom() {
        return FabricShimmerConfig.CONFIG.BLOCK_BLOOM.get();
    }

    @Override
    public boolean useLightMap() {
        return FabricShimmerConfig.CONFIG.USE_LIGHT_MAP.get();
    }

    @Override
    public boolean isColoredLightEnable() {
        return FabricShimmerConfig.CONFIG.ENABLED_COLORED_LIGHT.get();
    }

    @Override
    public boolean isBloomEnable() {
        return FabricShimmerConfig.CONFIG.ENABLE_BLOOM_EFFECT.get();
    }

    @Override
    public boolean isAdditiveBlend() {
        return FabricShimmerConfig.CONFIG.ADDITIVE_EFFECT.get();
    }

    @Override
    public ShimmerLoadConfigEvent postLoadConfigurationEvent(ShimmerLoadConfigEvent event) {
        return FabricShimmerLoadConfigCallback.EVENT.invoker().addConfigurationList(event);
    }

    @Override
    public int getBloomColorAttachmentNumber() {
        return FabricShimmerConfig.CONFIG.BLOOM_COLOR_ATTACHMENT_NUMBER.get() + GL30.GL_COLOR_ATTACHMENT0;
    }

    @Override
    public boolean isEnableInsetShaderInfo() {
        return FabricShimmerConfig.CONFIG.INSERT_SHADER_INFO.get() || isDevelopmentEnvironment();
    }
}
