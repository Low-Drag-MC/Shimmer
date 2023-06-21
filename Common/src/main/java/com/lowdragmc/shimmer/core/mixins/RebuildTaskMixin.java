package com.lowdragmc.shimmer.core.mixins;

import com.google.common.collect.ImmutableList;
import com.lowdragmc.shimmer.LightQueen;
import com.lowdragmc.shimmer.client.light.ColorPointLight;
import com.lowdragmc.shimmer.client.light.LightManager;
import com.lowdragmc.shimmer.client.postprocessing.PostProcessing;
import com.lowdragmc.shimmer.core.IRenderChunk;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LightEngine;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author KilaBash
 * @date 2022/05/02
 * @implNote RebuildTaskMixin, used to compile and save light info to the chunk.
 */
@Mixin(ChunkRenderDispatcher.RenderChunk.RebuildTask.class)
public abstract class RebuildTaskMixin {
    @SuppressWarnings("target") @Shadow(aliases = {"this$1", "f_112859_", "field_20839"}) @Final ChunkRenderDispatcher.RenderChunk this$1;
    ImmutableList.Builder<ColorPointLight> lights;

    @Redirect(method = "compile",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/chunk/RenderChunkRegion;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;",
                    ordinal = 0))
    private BlockState injectCompile(RenderChunkRegion instance, BlockPos pPos) {
        BlockState blockstate = instance.getBlockState(pPos);
        FluidState fluidstate = blockstate.getFluidState();
        if (LightManager.INSTANCE.isBlockHasLight(blockstate.getBlock(), fluidstate)) {
            ColorPointLight light = LightManager.INSTANCE.getBlockStateLight(instance, pPos, blockstate, fluidstate);
            if (light != null) {
                lights.add(light);
                var longPos = BlockPos.asLong(pPos.getX(),pPos.getY(),pPos.getZ());
                LightQueen.queue.enqueue(longPos);
                LightQueen.queue.enqueue(LightEngine.QueueEntry.increaseLightFromEmission(
                        (int)light.radius,true
                ));
            }
        }

        PostProcessing.setupBloom(blockstate, fluidstate);
        return blockstate;
    }

    @Inject(method = "compile", at = @At(value = "HEAD"))
    private void injectCompilePre(float x, float y, float z, ChunkBufferBuilderPack arg, CallbackInfoReturnable<ChunkRenderDispatcher.RenderChunk.RebuildTask.CompileResults> cir) {
        lights = ImmutableList.builder();
    }

    @Inject(method = "compile", at = @At(value = "RETURN"))
    private void injectCompilePost(float x, float y, float z, ChunkBufferBuilderPack arg, CallbackInfoReturnable<ChunkRenderDispatcher.RenderChunk.RebuildTask.CompileResults> cir) {
        if (this$1 instanceof IRenderChunk) {
            ((IRenderChunk) this$1).setShimmerLights(lights.build());
        }
        PostProcessing.cleanBloom();
    }
}
