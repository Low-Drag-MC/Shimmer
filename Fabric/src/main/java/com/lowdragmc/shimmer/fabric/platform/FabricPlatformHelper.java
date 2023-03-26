package com.lowdragmc.shimmer.fabric.platform;

import com.lowdragmc.shimmer.event.ShimmerLoadConfigEvent;
import com.lowdragmc.shimmer.event.ShimmerReloadEvent;
import com.lowdragmc.shimmer.fabric.FabricShimmerConfig;
import com.lowdragmc.shimmer.fabric.event.FabricShimmerLoadConfigCallback;
import com.lowdragmc.shimmer.fabric.event.FabricShimmerReloadCallback;
import com.lowdragmc.shimmer.platform.services.IPlatformHelper;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import org.lwjgl.opengl.GL30;

import java.nio.file.Path;
import java.util.List;

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
    public List<String> getLoadedMods() {
        return FabricLoader.getInstance().getAllMods().stream().map(modContainer -> modContainer.getMetadata().getId()).toList();
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

	public ShimmerReloadEvent postReloadEvent(ShimmerReloadEvent event){
		return FabricShimmerReloadCallback.EVENT.invoker().onReload(event);
	}

	@Override
	public int getBloomColorAttachmentNumber() {
		return FabricShimmerConfig.CONFIG.BLOOM_COLOR_ATTACHMENT_NUMBER.get() + GL30.GL_COLOR_ATTACHMENT0;
	}

	@Override
	public boolean isEnableInsetShaderInfo() {
		return FabricShimmerConfig.CONFIG.INSERT_SHADER_INFO.get() || isDevelopmentEnvironment();
	}

	@Override
	public ResourceLocation getFluidTextureLocation(Fluid fluid, boolean isStill) {
		FluidRenderHandler handler = FluidRenderHandlerRegistry.INSTANCE.get(fluid);
		TextureAtlasSprite[] sprites = handler.getFluidSprites(null, null, fluid.defaultFluidState());
		return (isStill ? sprites[0] : sprites[1]).getName();
	}

	@Override
	public int getFluidColor(Fluid fluid){
		FluidRenderHandler handler = FluidRenderHandlerRegistry.INSTANCE.get(fluid);
		return handler.getFluidColor(null,null,fluid.defaultFluidState());
	}

	@Override
	public Path getConfigDir(){
		return FabricLoader.getInstance().getConfigDir();
	}

}
