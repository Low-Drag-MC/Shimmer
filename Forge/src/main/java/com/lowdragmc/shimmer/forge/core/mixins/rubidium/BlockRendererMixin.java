package com.lowdragmc.shimmer.forge.core.mixins.rubidium;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.lowdragmc.shimmer.client.postprocessing.PostProcessing;
import com.lowdragmc.shimmer.core.IBakedQuad;
import me.jellysquid.mods.sodium.client.model.light.LightPipeline;
import me.jellysquid.mods.sodium.client.model.light.data.QuadLightData;
import me.jellysquid.mods.sodium.client.model.quad.BakedQuadView;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * @author KilaBash
 * @date 2022/05/31
 * @implNote ModelBlockRendererMixin, reglowstone.pngcode uv2 for bloom info
 */
@Mixin(value = BlockRenderer.class, remap = false)
public abstract class BlockRendererMixin {

    @ModifyReturnValue(method = "getVertexLight", at = @At("RETURN"))
    private QuadLightData reCalculateBloomLight(QuadLightData lightData, BlockRenderContext ctx, LightPipeline lighter, Direction cullFace, BakedQuadView quad) {
        if ((quad instanceof IBakedQuad bloomQuad && bloomQuad.isBloom()) || PostProcessing.isBlockBloom()){
            int[] lm = lightData.lm;
            for (int index = 0; index < lm.length; index++) {
                lm[index] = lm[index] | 0x10000100;
            }
        }
        return lightData;
    }
}
