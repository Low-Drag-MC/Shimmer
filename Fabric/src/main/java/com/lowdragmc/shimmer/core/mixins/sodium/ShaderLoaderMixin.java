package com.lowdragmc.shimmer.core.mixins.sodium;

import com.lowdragmc.shimmer.client.light.LightManager;
import com.lowdragmc.shimmer.client.postprocessing.PostProcessing;
import me.jellysquid.mods.sodium.client.gl.shader.*;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.jellysquid.mods.sodium.client.gl.shader.ShaderLoader.getShaderSource;

/**
 * @author KilaBash
 * @date 2022/05/28
 * @implNote ShaderLoaderMixin
 */
@Mixin(ShaderLoader.class)
public abstract class ShaderLoaderMixin {

    @Inject(method = "loadShader", at = @At(value = "HEAD"), cancellable = true, remap = false)
    private static void injectLoadShader(ShaderType type, ResourceLocation name, ShaderConstants constants, CallbackInfoReturnable<GlShader> cir) {
        if (name.getPath().contains("block_layer_opaque")) {
            String shader = ShaderParser.parseShader(getShaderSource(name), constants);
            if (type == ShaderType.FRAGMENT) {
                shader = PostProcessing.RbBloomMRTFSHInjection(shader);
            }
            if (type == ShaderType.VERTEX) {
                shader = LightManager.RbVVSHInjection(shader);
            }
            cir.setReturnValue(new GlShader(type, name, shader));
        }
    }
}
