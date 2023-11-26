package com.lowdragmc.shimmer.forge.core.mixins.rubidium;

import me.jellysquid.mods.sodium.client.util.ModelQuadUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author KilaBash
 * @date 2023/11/26
 * @implNote ModelQuadUtilMixin
 */
@Mixin(value = ModelQuadUtil.class, remap = false)
public abstract class ModelQuadUtilMixin {

    @Inject(method = "mergeBakedLight", at = @At("HEAD"), cancellable = true, remap = false)
    private static void injectBakedLight(int packedLight, int calcLight, CallbackInfoReturnable<Integer> cir) {
        if ((calcLight & 0x10000100) != 0) {
            cir.setReturnValue(calcLight);
        }
    }
}
