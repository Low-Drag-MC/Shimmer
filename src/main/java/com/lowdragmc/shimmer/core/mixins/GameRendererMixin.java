package com.lowdragmc.shimmer.core.mixins;

import com.lowdragmc.shimmer.client.Bloom;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author KilaBash
 * @date 2022/05/02
 * @implNote LevelRendererMixin
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Inject(method = "resize", at = @At(value = "HEAD"))
    private void injectResize(int widget, int height, CallbackInfo ci) {
        Bloom.INSTANCE.resize(widget, height);
    }

}
