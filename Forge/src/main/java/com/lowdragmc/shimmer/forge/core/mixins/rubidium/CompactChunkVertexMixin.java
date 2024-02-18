package com.lowdragmc.shimmer.forge.core.mixins.rubidium;

import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.Material;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.impl.CompactChunkVertex;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = CompactChunkVertex.class, remap = false)
public abstract class CompactChunkVertexMixin {

    @Redirect(method = "lambda$getEncoder$0", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/terrain/material/Material;bits()I"))
    private static int injectMaterialForBloom(Material material, long ptr, Material m, ChunkVertexEncoder.Vertex vertex) {
        var origin = material.bits();
        if ((vertex.light & 0x100) != 0) {
            origin |= (0x01 << 4);
        }
        return origin;
    }

    @Redirect(method = "lambda$getEncoder$0", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/MemoryUtil;memPutInt(JI)V", ordinal = 1))
    private static void injectLightForBloom(long ptr, int light) {
        if ((light & 0x100) != 0) {
            light = 15 | 15 << 4;
        }
        MemoryUtil.memPutInt(ptr, light);
    }
}
