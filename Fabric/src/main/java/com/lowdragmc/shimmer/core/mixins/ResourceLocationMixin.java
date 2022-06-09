package com.lowdragmc.shimmer.core.mixins;

import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * @author HypherionSA
 * @date 2022/06/09
 */
@Mixin(ResourceLocation.class)
public class ResourceLocationMixin {

    /***
     * Work around, or hack if you will, for fabric not supporting modded shaders
     * @param string
     * @return
     */
    @ModifyArg(method = "<init>(Ljava/lang/String;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/ResourceLocation;decompose(Ljava/lang/String;C)[Ljava/lang/String;", ordinal = 0))
    private static String decomposeInject(String string) {
        if (string.startsWith("shaders") && string.contains("shimmer:")) {
            return "shimmer:" + string.replace("shimmer:", "");
        }
        return string;
    }

}
