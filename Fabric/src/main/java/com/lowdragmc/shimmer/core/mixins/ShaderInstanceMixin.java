package com.lowdragmc.shimmer.core.mixins;

import com.google.gson.JsonObject;
import com.lowdragmc.shimmer.client.shader.ShaderInjection;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.util.GsonHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.Reader;

/**
 * @author KilaBash
 * @date 2022/05/02
 * @implNote ShaderInstanceMixin,  inject custom shader config to vanilla shader configs.
 */
@Mixin(ShaderInstance.class)
public abstract class ShaderInstanceMixin {

    @Mutable
    @Shadow @Final private String name;

    @Redirect(method = "<init>",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/GsonHelper;parse(Ljava/io/Reader;)Lcom/google/gson/JsonObject;"))
    private JsonObject injectResize(Reader pReader)  {
        JsonObject jsonObject = GsonHelper.parse(pReader);
        if (ShaderInjection.hasInjectConfig(name)) {
            return ShaderInjection.injectConfig(name, jsonObject);
        }
        return jsonObject;
    }

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
