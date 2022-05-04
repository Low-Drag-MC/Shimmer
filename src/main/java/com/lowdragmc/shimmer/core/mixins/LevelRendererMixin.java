package com.lowdragmc.shimmer.core.mixins;

import com.lowdragmc.shimmer.client.ShimmerRenderTypes;
import com.lowdragmc.shimmer.client.bloom.Bloom;
import com.lowdragmc.shimmer.client.light.LightManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author KilaBash
 * @date 2022/05/02
 * @implNote LevelRendererMixin
 */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Redirect(method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;renderChunkLayer(Lnet/minecraft/client/renderer/RenderType;Lcom/mojang/blaze3d/vertex/PoseStack;DDDLcom/mojang/math/Matrix4f;)V",
                    ordinal = 2))
    private void injectRenderLevel(LevelRenderer instance, RenderType cutout, PoseStack poseStack, double camX, double camY, double camZ, Matrix4f projectionMatrix) {
        ((LevelRendererAccessor)instance).callRenderChunkLayer(cutout, poseStack, camX, camY, camZ, projectionMatrix);
        ((LevelRendererAccessor)instance).callRenderChunkLayer(ShimmerRenderTypes.bloom(), poseStack, camX, camY, camZ, projectionMatrix);
        Bloom.INSTANCE.renderBloom();
    }

    @Inject(method = "renderChunkLayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;setupShaderLights(Lnet/minecraft/client/renderer/ShaderInstance;)V"))
    private void injectRenderChunkLayer(RenderType renderType, PoseStack poseStack, double camX, double camY, double camZ, Matrix4f projectionMatrix, CallbackInfo ci) {
        LightManager.INSTANCE.setupUniform(camX, camY, camZ);
    }

//    Vec3 vec3 = pCamera.getPosition();
//    double camX = vec3.x();
//    double camY = vec3.y();
//    double camZ = vec3.z();
//    RenderSystem.depthMask(false);
//    this.callRenderChunkLayer(ShimmerRenderTypes.bloom(), poseStack, camX, camY, camZ, projectionMatrix);
//    RenderSystem.depthMask(true);
}
