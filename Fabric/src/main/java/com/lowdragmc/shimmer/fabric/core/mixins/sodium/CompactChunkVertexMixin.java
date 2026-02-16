package com.lowdragmc.shimmer.fabric.core.mixins.sodium;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.impl.CompactChunkVertex;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Sodium 0.5.2+ refactored the vertex encoder and removed the old encodeDrawParameters hook.
 * We instead propagate Shimmer's bloom flag through the light value and apply it inside packLightAndData().
 *
 * Shimmer uses bit 0x100 in the packed light int as a side-band "bloom" marker:
 * - it does not affect Sodium's light encoding (which reads only the low byte of each 16-bit half)
 * - we forward it as 0x10000 in the encoded light so packLightAndData can see it.
 */
@Mixin(value = CompactChunkVertex.class, remap = false)
public abstract class CompactChunkVertexMixin {

    private static final int SHIMMER_BLOOM_FLAG_IN_PACKED_LIGHT = 0x100;   // set upstream by Shimmer
    private static final int SHIMMER_BLOOM_FLAG_IN_ENCODED_LIGHT = 0x10000; // internal to this mixin

    // 248 (0xF8) is the maximum lightmap coordinate used by vanilla MC (full brightness).
    private static final int FULL_BRIGHT_ENCODED = (0xF8) | (0xF8 << 8);

    @ModifyReturnValue(method = "encodeLight", at = @At("RETURN"))
    private static int shimmer$encodeLight_forwardBloomFlag(int encoded, int light) {
        if ((light & SHIMMER_BLOOM_FLAG_IN_PACKED_LIGHT) != 0) {
            return encoded | SHIMMER_BLOOM_FLAG_IN_ENCODED_LIGHT;
        }
        return encoded;
    }

    @ModifyVariable(method = "packLightAndData", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private static int shimmer$packLightAndData_forceFullBrightForBloom(int light) {
        if ((light & SHIMMER_BLOOM_FLAG_IN_ENCODED_LIGHT) != 0) {
            // Keep the flag until packing time; it will be masked out by (light & 0xFFFF) in the original method.
            return (FULL_BRIGHT_ENCODED | SHIMMER_BLOOM_FLAG_IN_ENCODED_LIGHT);
        }
        return light;
    }

    @ModifyVariable(method = "packLightAndData", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private static int shimmer$packLightAndData_addBloomBitToMaterial(int material, int light) {
        if ((light & SHIMMER_BLOOM_FLAG_IN_ENCODED_LIGHT) != 0) {
            // Store bloom bit in material params (bit 4) so shaders can branch.
            material |= (1 << 4);
        }
        return material;
    }
}
