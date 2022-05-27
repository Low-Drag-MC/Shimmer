package com.lowdragmc.shimmer.core.mixins;

import com.google.common.collect.ImmutableList;
import com.lowdragmc.shimmer.client.light.ColorPointLight;
import com.lowdragmc.shimmer.client.light.LightManager;
import com.lowdragmc.shimmer.core.IRenderChunk;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.Random;
import java.util.Set;

/**
 * @author KilaBash
 * @date 2022/05/02
 * @implNote RebuildTaskMixin, used to compile and save light info to the chunk.
 */
@Mixin(targets = { "net/minecraft/client/renderer/chunk/ChunkRenderDispatcher$RenderChunk$RebuildTask" })
public abstract class RebuildTaskMixin {
    @Shadow @Final ChunkRenderDispatcher.RenderChunk this$1;
    ImmutableList.Builder<ColorPointLight> lights;

    @Inject(method = "compile",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$RenderChunk$RebuildTask;getModelData(Lnet/minecraft/core/BlockPos;)Lnet/minecraftforge/client/model/data/IModelData;"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void injectCompile(float pX, float pY, float pZ,
                               ChunkRenderDispatcher.CompiledChunk pCompiledChunk,
                               ChunkBufferBuilderPack pBuffers,
                               CallbackInfoReturnable<Set<BlockEntity>> cir,
                               int i, BlockPos blockpos, BlockPos blockpos1,
                               VisGraph visgraph, Set set,
                               RenderChunkRegion renderchunkregion,
                               PoseStack posestack, Random random,
                               BlockRenderDispatcher blockrenderdispatcher,
                               Iterator var15,
                               BlockPos blockpos2,
                               BlockState blockstate,
                               BlockState blockstate1,
                               FluidState fluidstate) {
        if (LightManager.INSTANCE.isBlockHasLight(blockstate.getBlock(), fluidstate)) {
            ColorPointLight light = LightManager.INSTANCE.getBlockStateLight(blockpos2, blockstate, fluidstate);
            if (light != null) {
                lights.add(light);
            }
        }
    }

    @Inject(method = "compile", at = @At(value = "HEAD"))
    private void injectCompilePre(float pX, float pY, float pZ, ChunkRenderDispatcher.CompiledChunk pCompiledChunk, ChunkBufferBuilderPack pBuffers, CallbackInfoReturnable<Set<BlockEntity>> cir) {
        lights = ImmutableList.builder();
    }

    @Inject(method = "compile", at = @At(value = "RETURN"))
    private void injectCompilePost(float pX, float pY, float pZ, ChunkRenderDispatcher.CompiledChunk pCompiledChunk, ChunkBufferBuilderPack pBuffers, CallbackInfoReturnable<Set<BlockEntity>> cir) {
        if (this$1 instanceof IRenderChunk) {
            ((IRenderChunk) this$1).setShimmerLights(lights.build());
        }
    }
}
