package com.lowdragmc.shimmer.core.mixins;

import com.lowdragmc.shimmer.client.Bloom;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author KilaBash
 * @date 2022/05/02
 * @implNote LevelRendererMixin
 */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Redirect(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderChunkLayer(Lnet/minecraft/client/renderer/RenderType;Lcom/mojang/blaze3d/vertex/PoseStack;DDDLcom/mojang/math/Matrix4f;)V",
    ordinal = 2))
    private void injectRenderLevel(LevelRenderer instance, RenderType cutoutMipped, PoseStack poseStack, double camX, double camY, double camZ, Matrix4f projectionMatrix) {
        Bloom.INSTANCE.renderLayer(instance, cutoutMipped, poseStack, camX, camY, camZ, projectionMatrix);
    }

//    Vec3 vec3 = pCamera.getPosition();
//    double camX = vec3.x();
//    double camY = vec3.y();
//    double camZ = vec3.z();
//    RenderSystem.depthMask(false);
//    this.callRenderChunkLayer(ShimmerRenderTypes.bloom(), poseStack, camX, camY, camZ, projectionMatrix);
//    RenderSystem.depthMask(true);
}
