package com.lowdragmc.shimmer.fabric.core.mixins.sodium;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.lowdragmc.shimmer.client.postprocessing.PostProcessing;
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
@Mixin(value = BlockRenderer.class, remap = false)
public abstract class BlockRendererMixin {
    @ModifyReceiver(method = "renderQuadList",
            at = @At(value = "INVOKE",
                    target = "Lme/jellysquid/mods/sodium/client/render/pipeline/BlockRenderer;renderQuad(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Lme/jellysquid/mods/sodium/client/render/chunk/format/ModelVertexSink;Lme/jellysquid/mods/sodium/client/model/IndexBufferBuilder;Lnet/minecraft/world/phys/Vec3;Lme/jellysquid/mods/sodium/client/model/quad/blender/ColorSampler;Lnet/minecraft/client/renderer/block/model/BakedQuad;Lme/jellysquid/mods/sodium/client/model/light/data/QuadLightData;Lme/jellysquid/mods/sodium/client/render/chunk/compile/buffers/ChunkModelBuilder;)V"))
    private BlockRenderer injectRender(BlockRenderer blockRenderer,BlockAndTintGetter world, BlockState state, BlockPos pos, BlockPos origin, ModelVertexSink vertices, IndexBufferBuilder indices, Vec3 blockOffset, ColorSampler<BlockState> colorSampler, BakedQuad bakedQuad, QuadLightData light, ChunkModelBuilder model){
        if (((IBakedQuad)bakedQuad).isBloom() || PostProcessing.isBlockBloom()) {
//          0xf000f0 -> 0x1f001f0
            Arrays.fill(light.lm, 0xf200f2);
        }
        return blockRenderer;
    }
}