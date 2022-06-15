package com.lowdragmc.shimmer.core.mixins.sodium;

import com.lowdragmc.shimmer.core.IBakedQuad;
import me.jellysquid.mods.sodium.client.model.IndexBufferBuilder;
import me.jellysquid.mods.sodium.client.model.light.LightPipeline;
import me.jellysquid.mods.sodium.client.model.light.data.QuadLightData;
import me.jellysquid.mods.sodium.client.model.quad.blender.ColorSampler;
import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.format.ModelVertexSink;
import me.jellysquid.mods.sodium.client.render.pipeline.BlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Arrays;
import java.util.List;

/**
 * @author KilaBash
 * @date 2022/05/31
 * @implNote ModelBlockRendererMixin, reglowstone.pngcode uv2 for bloom info
 */
@Mixin(BlockRenderer.class)
public abstract class BlockRendererMixin {
    @Inject(method = "renderQuadList", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/pipeline/BlockRenderer;renderQuad(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Lme/jellysquid/mods/sodium/client/render/chunk/format/ModelVertexSink;Lme/jellysquid/mods/sodium/client/model/IndexBufferBuilder;Lnet/minecraft/world/phys/Vec3;Lme/jellysquid/mods/sodium/client/model/quad/blender/ColorSampler;Lnet/minecraft/client/renderer/block/model/BakedQuad;Lme/jellysquid/mods/sodium/client/model/light/data/QuadLightData;Lme/jellysquid/mods/sodium/client/render/chunk/compile/buffers/ChunkModelBuilder;)V"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void injectResize(BlockAndTintGetter world, BlockState state,
                              BlockPos pos, BlockPos origin,
                              LightPipeline lighter, Vec3 offset,
                              ChunkModelBuilder buffers, List<BakedQuad> quads,
                              ModelQuadFacing facing, CallbackInfo ci,
                              ColorSampler<BlockState> colorizer,
                              ModelVertexSink vertices, IndexBufferBuilder indices, int i, int quadsSize,
                              BakedQuad quad,
                              QuadLightData light) {
        if (quad instanceof IBakedQuad bakedQuad && bakedQuad.isBloom()) {
            // 0xf000f0 -> 0x1f001f0
            Arrays.fill(light.lm, 0xf200f2);
        }
    }
}
