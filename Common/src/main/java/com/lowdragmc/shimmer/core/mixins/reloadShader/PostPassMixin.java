package com.lowdragmc.shimmer.core.mixins.reloadShader;

import com.lowdragmc.shimmer.client.shader.ReloadShaderManager;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.IOException;


@Mixin(PostPass.class)
public abstract class PostPassMixin {

    @SuppressWarnings({"UnresolvedMixinReference", "InvalidMemberReference", "InvalidInjectorMethodSignature", "MixinAnnotationTarget"})
    @Redirect(method = "<init>", at = @At(value = "NEW", target = "(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/lang/String;)Lnet/minecraft/client/renderer/EffectInstance;"))
    private EffectInstance redirectEffectInstance(ResourceManager resourceManager, String string) throws IOException {
        return ReloadShaderManager.backupNewEffectInstance(resourceManager,string);
    }

}