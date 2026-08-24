package com.lowdragmc.shimmer.fabric.core.mixins.iris;

import com.lowdragmc.shimmer.comp.iris.IrisHandle;
import net.irisshaders.iris.helpers.StringPair;
import net.irisshaders.iris.shaderpack.option.ShaderPackOptions;
import net.irisshaders.iris.shaderpack.properties.ShaderProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ShaderProperties.class, remap = false)
public class ShaderPropertiesMixin {

    @Inject(method = "<init>(Ljava/lang/String;Lnet/irisshaders/iris/shaderpack/option/ShaderPackOptions;Ljava/lang/Iterable;)V", at = @At("TAIL"))
    private void shaderProperties(String contents, ShaderPackOptions shaderPackOptions, Iterable<StringPair> environmentDefines, CallbackInfo ci) {
        IrisHandle.analyzeShaderProperties(contents);
    }
}
