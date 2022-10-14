package com.lowdragmc.shimmer.forge.platform;

import com.lowdragmc.shimmer.event.ShimmerLoadConfigEvent;
import com.lowdragmc.shimmer.event.ShimmerReloadEvent;
import com.lowdragmc.shimmer.forge.ForgeShimmerConfig;
import com.lowdragmc.shimmer.client.postprocessing.PostParticle;
import com.lowdragmc.shimmer.client.postprocessing.PostProcessing;
import com.lowdragmc.shimmer.forge.event.ForgeShimmerLoadConfigEvent;
import com.lowdragmc.shimmer.forge.event.ForgeShimmerReloadEvent;
import com.lowdragmc.shimmer.platform.services.IPlatformHelper;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.particle.Particle;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;

import java.nio.file.Path;

/**
 * @author HypherionSA
 * @date 2022/06/09
 */
public class ForgePlatformHelper implements IPlatformHelper {

	@Override
	public String getPlatformName() {
		return "Forge";
	}

	@Override
	public boolean isModLoaded(String modId) {
		return ModList.get().isLoaded(modId);
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
		return ForgeConfig.CLIENT.useCombinedDepthStencilAttachment.get();
	}

	@Override
	public void enableStencil(RenderTarget renderTarget) {
		renderTarget.enableStencil();
	}

	@Override
	public int getUniformBufferObjectOffset() {
		int configValue = ForgeShimmerConfig.getUboOffset().get();
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
		return ForgeShimmerConfig.getBlockBloom().get();
	}

	@Override
	public boolean useLightMap() {
		return ForgeShimmerConfig.getLightMap().get();
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
		return ForgeShimmerConfig.getColoredLightEnable().get();
	}

	@Override
	public boolean isBloomEnable() {
		return ForgeShimmerConfig.getBloomEnable().get();
	}

	@Override
	public boolean isAdditiveBlend() {
		return ForgeShimmerConfig.getAdditiveBlend().get();
	}

	@Override
	public ShimmerLoadConfigEvent postLoadConfigurationEvent(ShimmerLoadConfigEvent event) {
		MinecraftForge.EVENT_BUS.post(new ForgeShimmerLoadConfigEvent(event));
		return event;
	}

	@Override
	public ShimmerReloadEvent postReloadEvent(ShimmerReloadEvent event){
		MinecraftForge.EVENT_BUS.post(new ForgeShimmerReloadEvent(event));
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

}
