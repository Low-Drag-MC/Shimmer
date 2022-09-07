package com.lowdragmc.shimmer.fabric.core.mixins.sodium;

import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderList;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author KilaBash
 * @date 2022/05/31
 * @implNote RenderSectionManagerAccessor
 */
@Mixin(RenderSectionManager.class)
public interface RenderSectionManagerAccessor {
    @Accessor(remap = false)
    ChunkRenderList getChunkRenderList();
}
