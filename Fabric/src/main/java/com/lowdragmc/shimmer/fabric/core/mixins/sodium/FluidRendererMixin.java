package com.lowdragmc.shimmer.fabric.core.mixins.sodium;

import com.lowdragmc.shimmer.client.postprocessing.PostProcessing;
import me.jellysquid.mods.sodium.client.model.light.data.QuadLightData;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.FluidRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author KilaBash
 * @date 2022/06/19
 * @implNote FluidRendererMixin, hook fluid bloom. The target's parameters aren't captured - see the Forge
 * copy for why Embeddium's {@code WorldSlice} stays out of our bytecode. Nothing here needed them.
 */
@Mixin(FluidRenderer.class)
public abstract class FluidRendererMixin {
    @Shadow(remap = false) @Final private QuadLightData quadLightData;

    @Inject(method = "updateQuad", at = @At(value = "RETURN"), remap = false)
    private void injectRender(CallbackInfo ci) {
        if (PostProcessing.isFluidBloom()) {
//             0xf000f0 -> 0x1f001f0
//            Arrays.fill(this.quadLightData.lm, 0x1000100);
            var lm = this.quadLightData.lm;
            for (int index = 0; index < lm.length; index++) {
                lm[index] = lm[index] | 0x10000100;
            }
        }
    }
}
