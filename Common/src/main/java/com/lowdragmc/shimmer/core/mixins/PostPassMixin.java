package com.lowdragmc.shimmer.core.mixins;

import com.lowdragmc.shimmer.client.postprocessing.PostProcessing;
import com.lowdragmc.shimmer.client.shader.ReloadShaderManager;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

/**
 * @author KilaBash
 * @date 2022/05/03
 * @implNote PostChainMixin, add more features for vanilla PostChain stuff
 */
@Mixin(PostPass.class)
public abstract class PostPassMixin {
    @Shadow @Final private EffectInstance effect;

    /**
     * @author KilaBash
     */
    @Inject(method = "process",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/EffectInstance;safeGetUniform(Ljava/lang/String;)Lcom/mojang/blaze3d/shaders/AbstractUniform;", ordinal = 1))
    private void injectParseTargetNode(float pPartialTicks, CallbackInfo ci) {
        this.effect.safeGetUniform("iTime").set(PostProcessing.getITime(pPartialTicks));
    }

    @SuppressWarnings({"UnresolvedMixinReference", "InvalidMemberReference", "InvalidInjectorMethodSignature", "MixinAnnotationTarget"})
    @Redirect(method = "<init>", at = @At(value = "NEW", target = "(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/lang/String;)Lnet/minecraft/client/renderer/EffectInstance;"))
    private EffectInstance redirectEffectInstance(ResourceManager resourceManager, String string) throws IOException {
        return ReloadShaderManager.backupNewEffectInstance(resourceManager,string);
    }

}
