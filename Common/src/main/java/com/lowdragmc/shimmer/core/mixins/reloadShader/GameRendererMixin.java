package com.lowdragmc.shimmer.core.mixins.reloadShader;


import com.lowdragmc.shimmer.client.shader.ReloadShaderManager;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.IOException;

@Mixin(GameRenderer.class)
abstract public class GameRendererMixin {
	@SuppressWarnings({"UnresolvedMixinReference", "InvalidMemberReference", "InvalidInjectorMethodSignature", "MixinAnnotationTarget"})
	@Redirect(method = "reloadShaders",
			at = @At(value = "NEW", target = "(Lnet/minecraft/server/packs/resources/ResourceProvider;Ljava/lang/String;Lcom/mojang/blaze3d/vertex/VertexFormat;)Lnet/minecraft/client/renderer/ShaderInstance;"))
	private ShaderInstance redirectReloadShaders(ResourceProvider resourceProvider, String shaderName, VertexFormat vertexFormat) throws IOException {
		return ReloadShaderManager.backupNewShaderInstance(resourceProvider, shaderName, vertexFormat);
	}

	@SuppressWarnings({"UnresolvedMixinReference", "InvalidMemberReference", "InvalidInjectorMethodSignature", "MixinAnnotationTarget"})
	@Redirect(method = "loadEffect",
			at = @At(value = "NEW", target = "(Lnet/minecraft/client/renderer/texture/TextureManager;Lnet/minecraft/server/packs/resources/ResourceManager;Lcom/mojang/blaze3d/pipeline/RenderTarget;Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/PostChain;"))
	private PostChain redirectLoadEffect(TextureManager textureManager, ResourceManager resourceManager, RenderTarget renderTarget, ResourceLocation resourceLocation) throws IOException {
		return ReloadShaderManager.backupNewPostChain(textureManager, resourceManager, renderTarget, resourceLocation);
	}
}
