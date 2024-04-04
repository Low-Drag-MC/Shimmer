package com.lowdragmc.shimmer.neoforge.platform;

import com.lowdragmc.shimmer.event.ShimmerLoadConfigEvent;
import com.lowdragmc.shimmer.event.ShimmerReloadEvent;
import com.lowdragmc.shimmer.neoforge.NeoForgeShimmerConfig;
import com.lowdragmc.shimmer.client.postprocessing.PostParticle;
import com.lowdragmc.shimmer.client.postprocessing.PostProcessing;
import com.lowdragmc.shimmer.neoforge.event.NeoForgeShimmerLoadConfigEvent;
import com.lowdragmc.shimmer.neoforge.event.NeoForgeShimmerReloadEvent;
import com.lowdragmc.shimmer.platform.services.IPlatformHelper;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.particle.Particle;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.common.NeoForgeConfig;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.loading.FMLConfig;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforgespi.language.IModInfo;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;

import java.nio.file.Path;
import java.util.List;

/**
 * @author HypherionSA
 * @date 2022/06/09
 */
public class NeoForgePlatformHelper implements IPlatformHelper {

	@Override
	public String getPlatformName() {
		return "NeoForge";
	}

	@Override
	public boolean isModLoaded(String modId) {
		return ModList.get().isLoaded(modId);
	}

	@Override
	public List<String> getLoadedMods() {
		return ModList.get().getMods().stream().map(IModInfo::getModId).toList();
	}

	@Override
	public boolean isDevelopmentEnvironment() {
		return !FMLLoader.isProduction();
	}

	@Override
	public boolean isStencilEnabled(RenderTarget target) {
		return target.isStencilEnabled();
	}

	@Override
	public boolean useCombinedDepthStencilAttachment() {
		return NeoForgeConfig.CLIENT.useCombinedDepthStencilAttachment.get();
	}

	@Override
	public void enableStencil(RenderTarget renderTarget) {
		renderTarget.enableStencil();
	}

	@Override
	public int getUniformBufferObjectOffset() {
		int configValue = NeoForgeShimmerConfig.getUboOffset().get();
		if (configValue == -1) {
			if (ModList.get().isLoaded("modernui")) {
				return 6;
			} else {
				return 1;
			}
		} else {
			return Mth.clamp(configValue, 0, GL11.glGetInteger(GL31.GL_MAX_UNIFORM_BUFFER_BINDINGS) - 1);
		}
	}

	@Override
	public boolean useBlockBloom() {
		return NeoForgeShimmerConfig.getBlockBloom().get();
	}

	@Override
	public boolean useLightMap() {
		return NeoForgeShimmerConfig.getLightMap().get();
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public PostParticle createPostParticle(Particle parent, PostProcessing postProcessing) {
		return new PostParticle(parent, postProcessing) {
			@Override
			public boolean shouldCull() {
				return parent.shouldCull();
			}
		};

	}

	@Override
	public boolean isLoadingStateValid() {
		return ModLoader.isLoadingStateValid();
	}

	@Override
	public boolean isColoredLightEnable() {
		return NeoForgeShimmerConfig.getColoredLightEnable().get();
	}

	@Override
	public boolean isBloomEnable() {
		return NeoForgeShimmerConfig.getBloomEnable().get();
	}

	@Override
	public boolean isAdditiveBlend() {
		return NeoForgeShimmerConfig.getAdditiveBlend().get();
	}

	@Override
	public ShimmerLoadConfigEvent postLoadConfigurationEvent(ShimmerLoadConfigEvent event) {
		ModLoader.get().postEvent(new NeoForgeShimmerLoadConfigEvent(event));
		return event;
	}

	@Override
	public ShimmerReloadEvent postReloadEvent(ShimmerReloadEvent event){
		ModLoader.get().postEvent(new NeoForgeShimmerReloadEvent(event));
		return event;
	}

    @Override
    public int getBloomColorAttachmentNumber() {
        //TODO config hasn't load when we need it, need change
        return 1 + GL30.GL_COLOR_ATTACHMENT0;
    }

    @Override
    public boolean isEnableInsetShaderInfo() {
        //TODO config hasn't load when we need it, need change
        return isDevelopmentEnvironment();
    }

	@Override
	public ResourceLocation getFluidTextureLocation(Fluid fluid, boolean isStill) {
		IClientFluidTypeExtensions fluidTypeExtensions = IClientFluidTypeExtensions.of(fluid);
		return isStill ? fluidTypeExtensions.getStillTexture() : fluidTypeExtensions.getFlowingTexture();
	}

	@Override
	public int getFluidColor(Fluid fluid) {
		return IClientFluidTypeExtensions.of(fluid).getTintColor();
	}

	@Override
	public Path getConfigDir() {
		return FMLPaths.CONFIGDIR.get();
	}

	@Override
	public boolean isRenderDocEnable() {
		//TODO config hasn't load when we need it, need change
		if (FMLConfig.getBoolConfigValue(FMLConfig.ConfigValue.EARLY_WINDOW_CONTROL)) return false;
		return isDevelopmentEnvironment();
	}

	@Override
	public boolean enableBuildinSetting() {
		return NeoForgeShimmerConfig.getEnableBuildinSetting().get();
	}

}
