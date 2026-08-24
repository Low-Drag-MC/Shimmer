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
            // Embeddium's chunk shaders carry `v_Color`/`_material_params`, not Sodium's
            // `v_ColorModulator`/`_vert_material`, so the Embeddium variants are the ones that fit here.
            if (type == ShaderType.FRAGMENT) {
                shader = PostProcessing.embeddiumBloomMRTFSHInjection(shader);
            }
            if (type == ShaderType.VERTEX) {
                shader = LightManager.embeddiumVVSHInjection(shader);
            }
        }
        return shader;
    }
}
