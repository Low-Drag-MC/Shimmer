package com.lowdragmc.shimmer.core.mixins;

import com.lowdragmc.shimmer.client.postprocessing.PostProcessing;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author KilaBash
 * @date 2022/06/19
 * @implNote LiquidBlockRendererMixin, hook fluid bloom.
 */
@Mixin(LiquidBlockRenderer.class)
public abstract class LiquidBlockRendererMixin {

    @Shadow protected abstract int getLightColor(BlockAndTintGetter $$0, BlockPos $$1);

    @Redirect(method = "tesselate", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/block/LiquidBlockRenderer;getLightColor(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;)I"))
    private int injectResize(LiquidBlockRenderer instance, BlockAndTintGetter level, BlockPos pos) {
        if (PostProcessing.isFluidBloom()) {
            return 0x1000100;
        }
        return this.getLightColor(level, pos);
    }

}
