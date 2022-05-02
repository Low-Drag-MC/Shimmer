package com.lowdragmc.shimmer.core.mixins;

import com.lowdragmc.shimmer.core.IMainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.IntSupplier;

/**
 * @author KilaBash
 * @date 2022/05/03
 * @implNote PostChainMixin
 */
@Mixin(PostChain.class)
public class PostChainMixin {

    @Final @Shadow private RenderTarget screenTarget;

    @Redirect(method = "parsePassNode",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/PostPass;addAuxAsset(Ljava/lang/String;Ljava/util/function/IntSupplier;II)V",
                    ordinal = 2))
    private void injectParsePassNode(PostPass postPass, String auxName, IntSupplier auxFramebuffer, int width, int height) {
        if (auxName.equals("minecraft:main:bloom") && screenTarget instanceof IMainTarget) {
            auxFramebuffer = ((IMainTarget) screenTarget)::getColorBloomTextureId;
        }
        postPass.addAuxAsset(auxName, auxFramebuffer, width, height);
    }

    @Inject(method = "getRenderTarget", at = @At(value = "HEAD"), cancellable = true)
    private void injectGetRenderTarget(String pTarget, CallbackInfoReturnable<RenderTarget> cir) {
        if (pTarget  != null && pTarget.equals("minecraft:main:bloom")) {
            cir.setReturnValue(screenTarget);
        }
    }
}
