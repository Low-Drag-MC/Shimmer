package com.lowdragmc.shimmer.core.mixins;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lowdragmc.shimmer.client.ScaleTextureTarget;
import com.lowdragmc.shimmer.core.IMainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.util.GsonHelper;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Map;

/**
 * @author KilaBash
 * @date 2022/05/03
 * @implNote PostChainMixin
 */
@Mixin(PostChain.class)
public abstract class PostChainMixin {

    @Final @Shadow private RenderTarget screenTarget;

    @Shadow @Final private Map<String, RenderTarget> customRenderTargets;
    @Shadow private int screenWidth;
    @Shadow private int screenHeight;
    @Shadow @Final private List<RenderTarget> fullSizedTargets;

    public void addTempTarget(String pName, float sw, float sh) {
        RenderTarget rendertarget = new ScaleTextureTarget(sw, sh, screenWidth, screenHeight, true, Minecraft.ON_OSX);
        rendertarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        if (screenTarget.isStencilEnabled()) { rendertarget.enableStencil(); }
        this.customRenderTargets.put(pName, rendertarget);
        this.fullSizedTargets.add(rendertarget);
    }

    /**
     * @author KilaBash
     */
    @Inject(method = "parseTargetNode",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/GsonHelper;getAsInt(Lcom/google/gson/JsonObject;Ljava/lang/String;I)I",
                    ordinal = 0), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void injectParseTargetNode(JsonElement pJson, CallbackInfo ci, JsonObject jsonobject, String s) {
        if (jsonobject.has("scaleSize")) {
            JsonObject scaleSize = GsonHelper.getAsJsonObject(jsonobject, "scaleSize");
            float width = GsonHelper.getAsFloat(scaleSize, "width", 1);
            float height = GsonHelper.getAsFloat(scaleSize, "height", 1);
            addTempTarget(s, width, height);
            if (GsonHelper.getAsBoolean(jsonobject, "bilinear", false)) {
                customRenderTargets.get(s).setFilterMode(GL11.GL_LINEAR);
            }
            ci.cancel();
        }
    }

    @Inject(method = "parseTargetNode",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/PostChain;addTempTarget(Ljava/lang/String;II)V",
                    ordinal = 1,
                    shift = At.Shift.AFTER),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void injectParseTargetNodePost(JsonElement pJson, CallbackInfo ci, JsonObject jsonobject, String s) {
        if (GsonHelper.getAsBoolean(jsonobject, "bilinear", false)) {
            customRenderTargets.get(s).setFilterMode(GL11.GL_LINEAR);
        }
    }

    @Inject(method = "getRenderTarget", at = @At(value = "HEAD"), cancellable = true)
    private void injectGetRenderTarget(String pTarget, CallbackInfoReturnable<RenderTarget> cir) {
        if (pTarget != null && pTarget.equals("minecraft:main:bloom") && screenTarget instanceof IMainTarget) {
            RenderTarget renderTarget = new RenderTarget(false) {
                @Override
                public int getColorTextureId() {
                    return ((IMainTarget) screenTarget).getColorBloomTextureId();
                }

                @Override
                public void resize(int pWidth, int pHeight, boolean pClearError) {
                    this.viewWidth = pWidth;
                    this.viewHeight = pHeight;
                    this.width = pWidth;
                    this.height = pHeight;
                }

                @Override
                public void bindRead() {
                    RenderSystem.assertOnRenderThread();
                    GlStateManager._bindTexture(this.getColorTextureId());
                }

                @Override
                public void bindWrite(boolean pSetViewport) {
                }
            };
            renderTarget.width = screenTarget.width;
            renderTarget.height = screenTarget.height;
            cir.setReturnValue(renderTarget);
        }
    }

    @Redirect(method = "parseUniformNode", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/shaders/Uniform;set(F)V"))
    private void injectParseUniformNode(Uniform instance, float pX) {
        if (instance.getType() < 4) {
            instance.set((int)pX);
        } else {
            instance.set(pX);
        }
    }

    @Redirect(method = "parseUniformNode", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/shaders/Uniform;set(FF)V"))
    private void injectParseUniformNode2(Uniform instance, float pX, float pY) {
        if (instance.getType() < 4) {
            instance.set((int)pX, (int)pY);
        } else {
            instance.set(pX, pY);
        }
    }

    @Redirect(method = "parseUniformNode", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/shaders/Uniform;set(FFF)V"))
    private void injectParseUniformNode3(Uniform instance, float pX, float pY, float pZ) {
        if (instance.getType() < 4) {
            instance.set((int)pX, (int)pY, (int)pZ);
        } else {
            instance.set(pX, pY, pZ);
        }
    }

    @Redirect(method = "parseUniformNode", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/shaders/Uniform;set(FFFF)V"))
    private void injectParseUniformNode4(Uniform instance, float pX, float pY, float pZ, float pW) {
        if (instance.getType() < 4) {
            instance.set((int)pX, (int)pY, (int)pZ, (int)pW);
        } else {
            instance.set(pX, pY, pZ, pW);
        }
    }
}
