package com.lowdragmc.shimmer.fabric.core.mixins.iris;

import com.lowdragmc.shimmer.ShimmerConstants;
import net.coderbot.iris.gl.shader.StandardMacros;
import net.coderbot.iris.shaderpack.StringPair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = StandardMacros.class, remap = false)
public abstract class StandardMacrosMixin {

    @Shadow
    private static void define(List<StringPair> list, String s) {
    }

    @Inject(method = "createStandardEnvironmentDefines",
            at = @At(value = "INVOKE", ordinal = 1,
                    target = "Lnet/coderbot/iris/gl/shader/StandardMacros;define(Ljava/util/List;Ljava/lang/String;)V"),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private static void injectMacro(CallbackInfoReturnable<Iterable<StringPair>> cir, ArrayList<StringPair> list) {
        define(list, ShimmerConstants.SHIMMER_IDENTIFIER_MACRO);
    }
}
