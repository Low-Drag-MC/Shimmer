package com.lowdragmc.shimmer.forge.core.mixins.rubidium;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.lowdragmc.shimmer.client.postprocessing.PostProcessing;
import com.lowdragmc.shimmer.core.IBakedQuad;
import me.jellysquid.mods.sodium.client.model.IndexBufferBuilder;
import me.jellysquid.mods.sodium.client.model.light.LightPipeline;
import me.jellysquid.mods.sodium.client.model.light.data.QuadLightData;
import me.jellysquid.mods.sodium.client.model.quad.ModelQuadView;
import me.jellysquid.mods.sodium.client.model.quad.blender.ColorSampler;
import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import me.jellysquid.mods.sodium.client.render.vertex.type.ChunkVertexBufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

/**
 * @author KilaBash
 * @date 2022/05/31
 * @implNote ModelBlockRendererMixin, reglowstone.pngcode uv2 for bloom info
 */
@Mixin(value = BlockRenderer.class, remap = false)
public abstract class BlockRendererMixin {

    @Inject(method = "renderQuadList ", locals = LocalCapture.CAPTURE_FAILHARD,
            at = @At(value = "INVOKE",shift = At.Shift.AFTER , by = 1,
                    target = "Lme/jellysquid/mods/sodium/client/model/light/LightPipeline;calculate(Lme/jellysquid/mods/sodium/client/model/quad/ModelQuadView;Lnet/minecraft/core/BlockPos;Lme/jellysquid/mods/sodium/client/model/light/data/QuadLightData;Lnet/minecraft/core/Direction;Lnet/minecraft/core/Direction;Z)V"))
    private void reCalculateBloomLight(BlockRenderContext ctx, LightPipeline lighter, Vec3 offset, ChunkModelBuilder builder, List quads, Direction cullFace, CallbackInfo ci, ModelQuadFacing facing, ColorSampler colorizer, ChunkVertexBufferBuilder vertexBuffer, IndexBufferBuilder indexBuffer, QuadLightData lightData, int i, int quadsSize, BakedQuad quad, ModelQuadView quadView) {
        if ((quad instanceof IBakedQuad bloomQuad && bloomQuad.isBloom()) || PostProcessing.isBlockBloom()){
            int[] lm = lightData.lm;
            for (int index = 0; index < lm.length; index++) {
                lm[index] = lm[index] | 0x10000100;
            }
        }
    }
}
