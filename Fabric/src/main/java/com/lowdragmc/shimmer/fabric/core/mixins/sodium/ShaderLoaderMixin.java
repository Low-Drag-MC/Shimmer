package com.lowdragmc.shimmer.fabric.core.mixins.sodium;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.lowdragmc.shimmer.client.light.LightManager;
import com.lowdragmc.shimmer.client.postprocessing.PostProcessing;
import me.jellysquid.mods.sodium.client.gl.shader.*;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * @author KilaBash
 * @date 2022/05/28
 * @implNote ShaderLoaderMixin
 */
@Mixin(ShaderLoader.class)
public abstract class ShaderLoaderMixin {

    @SuppressWarnings("mapping")
    @ModifyExpressionValue(method = "loadShader",
        at = @At(value = "INVOKE",target = "Lme/jellysquid/mods/sodium/client/gl/shader/ShaderParser;parseShader(Ljava/lang/String;Lme/jellysquid/mods/sodium/client/gl/shader/ShaderConstants;)Ljava/lang/String;"))
    private static String transformShader(String shader,ShaderType type, ResourceLocation name){
        if (name.getPath().contains("block_layer_opaque")){
            if (type == ShaderType.FRAGMENT) {
                shader = PostProcessing.RbBloomMRTFSHInjection(shader);
            }
            if (type == ShaderType.VERTEX) {
                shader = LightManager.RbVVSHInjection(shader);
            }
        }
        return shader;
    }
}
