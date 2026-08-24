package com.lowdragmc.shimmer.forge.core.mixins.rubidium;

import com.google.common.collect.ImmutableList;
import com.lowdragmc.shimmer.client.light.ColorPointLight;
import com.lowdragmc.shimmer.client.light.LightManager;
import com.lowdragmc.shimmer.client.postprocessing.PostProcessing;
import com.lowdragmc.shimmer.core.IRenderChunk;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildOutput;
import me.jellysquid.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask;
import me.jellysquid.mods.sodium.client.util.task.CancellationToken;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author KilaBash
 * @date 2022/05/28
 * @implNote ChunkRenderRebuildTaskMixin
 */
@Mixin(ChunkBuilderMeshingTask.class)

public abstract class ChunkBuilderMeshingTaskMixin {
    @Shadow(remap = false)
    @Final
    private RenderSection render;
    @Unique
    ImmutableList.Builder<ColorPointLight> shimmer$lights;

    /**
     * @implNote The slice is taken as {@link Object} rather than {@code WorldSlice} on purpose. Embeddium's
     * Forge build declares WorldSlice as implementing the Fabric Rendering API's view interfaces but doesn't
     * ship them, so naming that type here makes ModLauncher walk a hierarchy it can't resolve and the game
     * dies while transforming the class. Going through {@link BlockAndTintGetter} keeps our bytecode clear of
     * it; {@code getBlockState(BlockPos)} on WorldSlice is a straight delegate to {@code getBlockState(x,y,z)}.
     */
    @Redirect(method = "execute(Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lme/jellysquid/mods/sodium/client/util/task/CancellationToken;)Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
            at = @At(value = "INVOKE",
                    target = "Lme/jellysquid/mods/sodium/client/world/WorldSlice;getBlockState(III)Lnet/minecraft/world/level/block/state/BlockState;"),
            remap = false)
    private BlockState injectChunkCompileAddLight(@Coerce Object sliceObject, int x, int y, int z) {
        var slice = (BlockAndTintGetter) sliceObject;
        var pos = new BlockPos(x, y, z);
        var blockState = slice.getBlockState(pos);
        if (!blockState.isAir()) {
            var fluidState = blockState.getFluidState();
            if (LightManager.INSTANCE.isBlockHasLight(blockState.getBlock(), fluidState)) {
                var light = LightManager.INSTANCE.getBlockStateLight(slice, pos, blockState, fluidState);
                if (light != null) shimmer$lights.add(light);
            }
            PostProcessing.setupBloom(blockState, fluidState);
        }
        return blockState;
    }

    @Inject(method = "execute(Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lme/jellysquid/mods/sodium/client/util/task/CancellationToken;)Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;", at = @At("HEAD"), remap = false)
    private void injectChunkCompilePre(ChunkBuildContext buildContext, CancellationToken cancellationToken, CallbackInfoReturnable<ChunkBuildOutput> cir) {
        shimmer$lights = ImmutableList.builder();
    }

    @Inject(method = "execute(Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lme/jellysquid/mods/sodium/client/util/task/CancellationToken;)Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;", at = @At("RETURN"), remap = false)
    private void injectChunkCompilePost(ChunkBuildContext buildContext, CancellationToken cancellationToken, CallbackInfoReturnable<ChunkBuildOutput> cir) {
        if (this.render instanceof IRenderChunk shimmerRenderChunk) {
            shimmerRenderChunk.setShimmerLights(shimmer$lights.build());
        }
        shimmer$lights = null;
        PostProcessing.cleanBloom();
    }
}
