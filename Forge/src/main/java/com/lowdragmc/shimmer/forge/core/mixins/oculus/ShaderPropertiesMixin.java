package com.lowdragmc.shimmer.forge.core.mixins.oculus;

import com.lowdragmc.shimmer.comp.iris.IrisHandle;
import net.coderbot.iris.shaderpack.ShaderProperties;
import net.coderbot.iris.shaderpack.StringPair;
import net.coderbot.iris.shaderpack.option.ShaderPackOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ShaderProperties.class, remap = false)
public class ShaderPropertiesMixin {

    @Inject(method = "<init>(Ljava/lang/String;Lnet/coderbot/iris/shaderpack/option/ShaderPackOptions;Ljava/lang/Iterable;)V",at = @At("TAIL"))
    private void shaderProperties(String contents, ShaderPackOptions shaderPackOptions, Iterable<StringPair> environmentDefines, CallbackInfo ci){
        IrisHandle.analyzeShaderProperties(contents);
    }
}
