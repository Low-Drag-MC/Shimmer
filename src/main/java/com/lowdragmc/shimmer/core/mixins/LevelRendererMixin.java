package com.lowdragmc.shimmer.core.mixins;

import com.lowdragmc.shimmer.client.ShimmerRenderTypes;
import com.lowdragmc.shimmer.client.postprocessing.PostProcessing;
import com.lowdragmc.shimmer.client.light.LightManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

/**
 * @author KilaBash
 * @date 2022/05/02
 * @implNote LevelRendererMixin, used to inject level renderer, for block, entity, particle postprocessing.
 */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Shadow protected abstract void renderChunkLayer(RenderType pRenderType, PoseStack pPoseStack, double pCamX, double pCamY, double pCamZ, Matrix4f pProjectionMatrix);

    @Shadow @Nullable private ClientLevel level;

    @Shadow @Final private ObjectArrayList<LevelRenderer.RenderChunkInfo> renderChunksInFrustum;

    @Inject(method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/DimensionSpecialEffects;constantAmbientLight()Z"))
    private void injectRenderLevel(PoseStack poseStack, float pPartialTick, long pFinishNanoTime, boolean pRenderBlockOutline, Camera camera, GameRenderer pGameRenderer, LightTexture pLightTexture, Matrix4f projectionMatrix, CallbackInfo ci) {
        Vec3 camPos = camera.getPosition();
        PostProcessing.getBlockBloom().getPostTarget().bindWrite(false);
        this.renderChunkLayer(ShimmerRenderTypes.bloom(), poseStack, camPos.x, camPos.y, camPos.z, projectionMatrix);
        this.level.getProfiler().popPush("block_bloom");
        PostProcessing.getBlockBloom().renderBlockPost();
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
        LightManager.INSTANCE.renderLevelPre(renderChunksInFrustum, (float)position.x,(float) position.y, (float)position.z);
    }

    @Inject(method = "renderLevel", at = @At(value = "RETURN"))
    private void injectRenderLevelPost(PoseStack pPoseStack, float pPartialTick, long pFinishNanoTime, boolean pRenderBlockOutline, Camera pCamera, GameRenderer pGameRenderer, LightTexture pLightTexture, Matrix4f pProjectionMatrix, CallbackInfo ci) {
        LightManager.INSTANCE.renderLevelPost();
    }

}
