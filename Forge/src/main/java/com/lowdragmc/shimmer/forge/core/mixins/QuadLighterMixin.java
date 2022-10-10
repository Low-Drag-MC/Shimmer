package com.lowdragmc.shimmer.forge.core.mixins;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.lowdragmc.shimmer.client.postprocessing.PostProcessing;
import com.lowdragmc.shimmer.core.IBakedQuad;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraftforge.client.model.lighting.QuadLighter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * @author KilaBash
 * @date 2022/7/8
 * @implNote QuadLighterMixin
 */
@Mixin(QuadLighter.class)
public class QuadLighterMixin {

    @Shadow @Final private int[] lightmap;

    @ModifyReceiver(method = "process",
            at = @At(value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;putBulkData(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/model/BakedQuad;[FFFF[IIZ)V"))
    private VertexConsumer injectPutQuadData(VertexConsumer vertexConsumer, PoseStack.Pose poseEntry, BakedQuad quad, float[] colorMuls, float red, float green, float blue, int[] combinedLights, int combinedOverlay, boolean mulColor) {
        if ((quad instanceof IBakedQuad bloomQuad && bloomQuad.isBloom()) || PostProcessing.isBlockBloom()) {
            for (int i = 0; i < lightmap.length; i++) {
                lightmap[i] = lightmap[i] | 0x10000100;
            }
        }
        return vertexConsumer;
    }
}
