package com.lowdragmc.shimmer.core.mixins;

import com.lowdragmc.shimmer.client.ShimmerRenderTypes;
import com.lowdragmc.shimmer.client.light.LightManager;
import com.lowdragmc.shimmer.client.postprocessing.PostProcessing;
import com.lowdragmc.shimmer.client.shader.ReloadShaderManager;
import com.lowdragmc.shimmer.client.shader.RenderUtils;
import com.lowdragmc.shimmer.platform.Services;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author KilaBash
 * @date 2022/05/02
 * @implNote GameRendererMixin, used to refresh shader and fbo size.
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow @Final private Map<String, ShaderInstance> shaders;

    @Inject(method = "resize", at = @At(value = "RETURN"))
    private void injectResize(int width, int height, CallbackInfo ci) {
        PostProcessing.resize(width, height);
    }

    @Inject(method = "reloadShaders", at = @At(value = "RETURN"))
    private void injectReloadShaders(ResourceManager pResourceManager, CallbackInfo ci) {
        if (Services.PLATFORM.isLoadingStateValid()) {
            LightManager.INSTANCE.reloadShaders();
        }
    }

    /* Replacement for RegisterShadersEvent, as fabric has no equivalent event  */
    @Inject(method = "reloadShaders", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;shutdownShaders()V", shift = At.Shift.AFTER))
    private void reloadShaders(ResourceManager resourceManager, CallbackInfo ci) {
        if (Services.PLATFORM.getPlatformName().equalsIgnoreCase("fabric")) {
            Pair<ShaderInstance, Consumer<ShaderInstance>> blitShader = RenderUtils.registerShaders(resourceManager);
            this.shaders.put(blitShader.getFirst().getName(), blitShader.getFirst());
            blitShader.getSecond().accept(blitShader.getFirst());

            Pair<ShaderInstance, Consumer<ShaderInstance>> armorShader = ShimmerRenderTypes.registerShaders(resourceManager);
            this.shaders.put(armorShader.getFirst().getName(), armorShader.getFirst());
            armorShader.getSecond().accept(armorShader.getFirst());
        }
    }

    @SuppressWarnings({"UnresolvedMixinReference", "InvalidMemberReference", "InvalidInjectorMethodSignature", "MixinAnnotationTarget"})
    @Redirect(method = "reloadShaders",
            at = @At(value = "NEW", target = "(Lnet/minecraft/server/packs/resources/ResourceProvider;Ljava/lang/String;Lcom/mojang/blaze3d/vertex/VertexFormat;)Lnet/minecraft/client/renderer/ShaderInstance;"))
    private ShaderInstance redirectReloadShaders(ResourceProvider resourceProvider, String shaderName, VertexFormat vertexFormat) throws IOException {
        return ReloadShaderManager.backupNewShaderInstance(resourceProvider, shaderName, vertexFormat);
    }

    @SuppressWarnings({"UnresolvedMixinReference", "InvalidMemberReference", "InvalidInjectorMethodSignature", "MixinAnnotationTarget"})
    @Redirect(method = "loadEffect",
        at = @At(value = "NEW",target = "(Lnet/minecraft/client/renderer/texture/TextureManager;Lnet/minecraft/server/packs/resources/ResourceManager;Lcom/mojang/blaze3d/pipeline/RenderTarget;Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/PostChain;"))
    private PostChain redirectLoadEffect(TextureManager textureManager, ResourceManager resourceManager, RenderTarget renderTarget, ResourceLocation resourceLocation) throws IOException {
        return ReloadShaderManager.backupNewPostChain(textureManager,resourceManager,renderTarget,resourceLocation);
    }
}
