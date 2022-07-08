package com.lowdragmc.shimmer.core.mixins;

import com.lowdragmc.shimmer.client.postprocessing.PostProcessing;
import com.lowdragmc.shimmer.core.IBakedQuad;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.LightUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Arrays;

/**
 * @author KilaBash
 * @date 2022/7/8
 * @implNote LightUtilMixin
 */
@Mixin(LightUtil.class)
public class LightUtilMixin {

    @Inject(method = "putBakedQuad", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/client/model/pipeline/IVertexConsumer;put(I[F)V",
            ordinal = 0),
            remap = false,
            locals = LocalCapture.CAPTURE_FAILHARD)
    private static void injectPutQuadData(IVertexConsumer consumer, BakedQuad quad, CallbackInfo ci,
                                          float[] data,
                                          VertexFormat formatFrom,
                                          VertexFormat formatTo,
                                          int countFrom,
                                          int countTo,
                                          int[] eMap,
                                          int v,
                                          int e) {
        if (e == 3 && (((IBakedQuad)quad).isBloom() || PostProcessing.isBlockBloom())) {
            // 0xf000f0 -> 0x1f001f0
            Arrays.fill(data, 2);
        }
    }
}
