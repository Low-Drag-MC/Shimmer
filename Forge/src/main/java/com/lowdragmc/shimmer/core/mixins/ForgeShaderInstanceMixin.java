package com.lowdragmc.shimmer.core.mixins;

import com.google.gson.JsonObject;
import com.lowdragmc.shimmer.client.shader.ShaderInjection;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.GsonHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.Reader;

@Mixin(ShaderInstance.class)
public class ForgeShaderInstanceMixin {

    @Shadow
    @Final
    private String name;

    @Redirect(method = "<init>(Lnet/minecraft/server/packs/resources/ResourceProvider;Lnet/minecraft/resources/ResourceLocation;Lcom/mojang/blaze3d/vertex/VertexFormat;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/GsonHelper;parse(Ljava/io/Reader;)Lcom/google/gson/JsonObject;"))
    private JsonObject injectResize(Reader pReader)  {
        JsonObject jsonObject = GsonHelper.parse(pReader);
        if (ShaderInjection.hasInjectConfig(name)) {
            return ShaderInjection.injectConfig(name, jsonObject);
        }
        return jsonObject;
    }

}
