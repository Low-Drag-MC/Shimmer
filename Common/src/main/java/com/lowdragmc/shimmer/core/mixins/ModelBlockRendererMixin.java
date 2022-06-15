package com.lowdragmc.shimmer.core.mixins;

import com.lowdragmc.shimmer.core.IBakedQuad;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author KilaBash
 * @date 2022/05/31
 * @implNote ModelBlockRendererMixin, reglowstone.pngcode uv2 for bloom info
 */
@Mixin(ModelBlockRenderer.class)
public abstract class ModelBlockRendererMixin {
    @Redirect(method = "putQuadData", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;putBulkData(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/model/BakedQuad;[FFFF[IIZ)V"))
    private void injectResize(VertexConsumer pConsumer, PoseStack.Pose pPose, BakedQuad pQuad, float[] brightnesses, float r, float g, float b, int[] lightmaps, int packedOverlay, boolean mulColor) {
        if (pQuad instanceof IBakedQuad bakedQuad && bakedQuad.isBloom()) {
            for (int i = 0; i < lightmaps.length; i++) {
                lightmaps[i] |= 0x1000100; // 0xf000f0 -> 0x1f001f0
            }
        }
        pConsumer.putBulkData(pPose, pQuad, brightnesses, r, g, b, lightmaps, packedOverlay, mulColor);
    }
}
