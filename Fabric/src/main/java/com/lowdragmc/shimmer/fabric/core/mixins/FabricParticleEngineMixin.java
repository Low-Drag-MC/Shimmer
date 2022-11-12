package com.lowdragmc.shimmer.fabric.core.mixins;

import com.lowdragmc.shimmer.client.postprocessing.IPostParticleType;
import com.lowdragmc.shimmer.client.postprocessing.PostProcessing;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Queue;

@Mixin(ParticleEngine.class)
public abstract class FabricParticleEngineMixin {

	@Shadow
	@Final
	private Map<ParticleRenderType, Queue<Particle>> particles;

	@Shadow
	@Final
	private TextureManager textureManager;

	@Inject(method = "render",
			at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V", shift = At.Shift.BEFORE))
	private void renderPostParticles(PoseStack matrixStack, MultiBufferSource.BufferSource buffer, LightTexture lightTexture, Camera activeRenderInfo, float partialTicks, CallbackInfo ci) {
		for (IPostParticleType particleRenderType : PostProcessing.getBlockBloomPostParticleTypes()) {
			Iterable<Particle> iterable = this.particles.get(particleRenderType);
			if (iterable == null) continue;
			RenderSystem.setShader(GameRenderer::getParticleShader);
			RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
			Tesselator tesselator = Tesselator.getInstance();
			BufferBuilder bufferBuilder = tesselator.getBuilder();
			particleRenderType.begin(bufferBuilder, this.textureManager);

			PostProcessing postProcessing = particleRenderType.getPost();
			postProcessing.getPostTarget(false).bindWrite(false);
			postProcessing.hasParticle();

			for (Particle particle : iterable) {
				try {
					particle.render(bufferBuilder, activeRenderInfo, partialTicks);
				} catch (Throwable throwable) {
					CrashReport crashReport = CrashReport.forThrowable(throwable, "Rendering Particle");
					CrashReportCategory crashReportCategory = crashReport.addCategory("Particle being rendered");
					crashReportCategory.setDetail("Particle", particle::toString);
					crashReportCategory.setDetail("Particle Type", particleRenderType::toString);
					throw new ReportedException(crashReport);
				}
			}
			particleRenderType.end(tesselator);

			Minecraft.getInstance().getMainRenderTarget().bindWrite(false);

		}
	}

}
