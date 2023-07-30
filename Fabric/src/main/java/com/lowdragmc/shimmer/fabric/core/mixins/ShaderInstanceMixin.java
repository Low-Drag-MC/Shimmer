package com.lowdragmc.shimmer.fabric.core.mixins;

import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * @author KilaBash
 * @date 2022/05/02
 * @implNote ShaderInstanceMixin,  inject custom shader config to vanilla shader configs.
 */
@Mixin(ShaderInstance.class)
public abstract class ShaderInstanceMixin {

    @Mutable
    @Shadow @Final private String name;

    /**
     * @author HypherionSA
     * @date 2022/06/09
     * Ensure the shader is loading from the correct resource location. Fabric ignores the ResourceLocation path passed to this method
     */
    @ModifyArg(method = "getOrCreate", at = @At(value = "INVOKE", target = "Lnet/minecraft/FileUtil;getFullResourcePath(Ljava/lang/String;)Ljava/lang/String;"), index = 0)
    private static String injectResourcePath(String string) {
        if (string.contains("shimmer:")) {
            return "shimmer:" + string.replace("shimmer:", "");
        }
        return string;
    }

}
