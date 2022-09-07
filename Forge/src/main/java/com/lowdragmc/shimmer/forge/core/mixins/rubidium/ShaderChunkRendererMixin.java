package com.lowdragmc.shimmer.forge.core.mixins.rubidium;

import com.lowdragmc.shimmer.client.light.LightManager;
import me.jellysquid.mods.sodium.client.gl.shader.GlProgram;
import me.jellysquid.mods.sodium.client.render.chunk.ShaderChunkRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author KilaBash
 * @date 2022/05/31
 * @implNote ShaderChunkRenderer
 */
@Mixin(ShaderChunkRenderer.class)
public abstract class ShaderChunkRendererMixin {

    @Inject(method = "createShader", at = @At(value = "RETURN"), remap = false)
    private void injectLoadShader(String path, ChunkShaderOptions options, CallbackInfoReturnable<GlProgram<ChunkShaderInterface>> cir) {
        LightManager.INSTANCE.bindRbProgram(cir.getReturnValue().handle());
    }
}
