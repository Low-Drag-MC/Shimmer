package com.lowdragmc.shimmer.fabric.core.mixins.sodium;

import com.llamalad7.mixinextras.sugar.Local;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.impl.CompactChunkVertex;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = CompactChunkVertex.class, remap = false)
public abstract class CompactChunkVertexMixin {

    @Redirect(method = "lambda$getEncoder$0", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/MemoryUtil;memPutByte(JB)V", ordinal = 0))
    private static void injectMaterialForBloom(long ptr, byte value, @Local(argsOnly = true) ChunkVertexEncoder.Vertex vertex) {
        if ((vertex.light & 0x100) != 0) {
            value |= (0x01 << 4);
        }
        MemoryUtil.memPutByte(ptr, value);
    }

    @Redirect(method = "lambda$getEncoder$0", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/MemoryUtil;memPutInt(JI)V", ordinal = 1))
    private static void injectLightForBloom(long ptr, int value) {
        if ((value & 0x100) != 0) {
            value = 15 | 15 << 4;
        }
        MemoryUtil.memPutInt(ptr, value);
    }
}
