package com.lowdragmc.shimmer.core.mixins;

import com.lowdragmc.shimmer.client.light.ColorPointLight;
import com.lowdragmc.shimmer.client.light.LightManager;
import com.lowdragmc.shimmer.client.postprocessing.PostProcessing;
import com.lowdragmc.shimmer.client.shader.ReloadShaderManager;
import com.lowdragmc.shimmer.core.IRenderChunk;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.FloatBuffer;

/**
 * @author KilaBash
 * @date 2022/05/02
 * @implNote LevelRendererMixin, used to inject level renderer, for block, entity, particle postprocessing.
 */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Shadow @Nullable private ClientLevel level;

    @Shadow @Final private ObjectArrayList<LevelRenderer.RenderChunkInfo> renderChunksInFrustum;

    @Inject(method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/DimensionSpecialEffects;constantAmbientLight()Z"))
    private void injectRenderLevel(PoseStack poseStack, float pPartialTick, long pFinishNanoTime, boolean pRenderBlockOutline, Camera camera, GameRenderer pGameRenderer, LightTexture pLightTexture, Matrix4f projectionMatrix, CallbackInfo ci) {
        this.level.getProfiler().popPush("block_bloom");
        PostProcessing.getBlockBloom().renderBlockPost();
    }

    @Inject(method = "renderChunkLayer",
            at = @At(value = "HEAD"))
    private void preRenderChunkLayer(RenderType pRenderType,
                                        PoseStack pPoseStack, double pCamX,
                                        double pCamY, double pCamZ,
                                        Matrix4f pProjectionMatrix,
                                        CallbackInfo ci) {
        if (PostProcessing.CHUNK_TYPES.contains(pRenderType)) {
            GL30.glDrawBuffers(new int[] {GL30.GL_COLOR_ATTACHMENT0, GL30.GL_COLOR_ATTACHMENT1});
        }
    }

    @Inject(method = "renderChunkLayer",
            at = @At(value = "RETURN"))
    private void postRenderChunkLayer(RenderType pRenderType,
                                        PoseStack pPoseStack, double pCamX,
                                        double pCamY, double pCamZ,
                                        Matrix4f pProjectionMatrix,
                                        CallbackInfo ci) {
        if (PostProcessing.CHUNK_TYPES.contains(pRenderType)) {
            GL30.glDrawBuffers(GL30.GL_COLOR_ATTACHMENT0);
        }
    }

    @Inject(method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;checkPoseStack(Lcom/mojang/blaze3d/vertex/PoseStack;)V",
                    ordinal = 1))
    private void injectRenderLevelBloom(PoseStack poseStack, float pPartialTick, long pFinishNanoTime, boolean pRenderBlockOutline, Camera camera, GameRenderer pGameRenderer, LightTexture pLightTexture, Matrix4f projectionMatrix, CallbackInfo ci) {
        ProfilerFiller profilerFiller = this.level.getProfiler();
        for (PostProcessing postProcessing : PostProcessing.values()) {
            postProcessing.renderEntityPost(profilerFiller);
        }
    }

    @Inject(method = "renderLevel", at = @At(value = "HEAD"))
    private void injectRenderLevelPre(PoseStack pPoseStack, float pPartialTick, long pFinishNanoTime, boolean pRenderBlockOutline, Camera pCamera, GameRenderer pGameRenderer, LightTexture pLightTexture, Matrix4f pProjectionMatrix, CallbackInfo ci) {
        Vec3 position = pCamera.getPosition();
        int blockLightSize = 0;
        int left = LightManager.INSTANCE.leftBlockLightCount();
        FloatBuffer buffer = LightManager.INSTANCE.getBuffer();
        buffer.clear();
        for (LevelRenderer.RenderChunkInfo chunkInfo : renderChunksInFrustum) {
            if (left <= blockLightSize) {
                break;
            }
            if (chunkInfo.chunk instanceof IRenderChunk) {
                for (ColorPointLight shimmerLight : ((IRenderChunk) chunkInfo.chunk).getShimmerLights()) {
                    if (left <= blockLightSize) {
                        break;
                    }
                    shimmerLight.uploadBuffer(buffer);
                    blockLightSize++;
                }
            }
        }
        LightManager.INSTANCE.renderLevelPre(blockLightSize, (float)position.x,(float) position.y, (float)position.z);
    }

    @Inject(method = "renderLevel", at = @At(value = "RETURN"))
    private void injectRenderLevelPost(PoseStack pPoseStack, float pPartialTick, long pFinishNanoTime, boolean pRenderBlockOutline, Camera pCamera, GameRenderer pGameRenderer, LightTexture pLightTexture, Matrix4f pProjectionMatrix, CallbackInfo ci) {
        LightManager.INSTANCE.renderLevelPost();
    }

    @SuppressWarnings({"UnresolvedMixinReference", "InvalidMemberReference", "InvalidInjectorMethodSignature", "MixinAnnotationTarget"})
    @Redirect(method = "initOutline",at = @At(value = "NEW",
            target = "(Lnet/minecraft/client/renderer/texture/TextureManager;Lnet/minecraft/server/packs/resources/ResourceManager;Lcom/mojang/blaze3d/pipeline/RenderTarget;Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/PostChain;"
    ))
    private PostChain redirectInitOutline(TextureManager textureManager, ResourceManager resourceManager, RenderTarget renderTarget, ResourceLocation resourceLocation) throws IOException {
        return ReloadShaderManager.backupNewPostChain(textureManager,resourceManager,renderTarget,resourceLocation);
    }

    @SuppressWarnings({"UnresolvedMixinReference", "InvalidMemberReference", "InvalidInjectorMethodSignature", "MixinAnnotationTarget"})
    @Redirect(method = "initTransparency",at = @At(value = "NEW",
            target = "(Lnet/minecraft/client/renderer/texture/TextureManager;Lnet/minecraft/server/packs/resources/ResourceManager;Lcom/mojang/blaze3d/pipeline/RenderTarget;Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/PostChain;"
    ))
    private PostChain redirectInitTransparency(TextureManager textureManager, ResourceManager resourceManager, RenderTarget renderTarget, ResourceLocation resourceLocation) throws IOException {
        return ReloadShaderManager.backupNewPostChain(textureManager,resourceManager,renderTarget,resourceLocation);
    }
}
