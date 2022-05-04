package com.lowdragmc.shimmer.core.mixins;

import com.lowdragmc.shimmer.client.bloom.Bloom;
import com.lowdragmc.shimmer.client.light.LightManager;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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

    @Inject(method = "reloadShaders", at = @At(value = "RETURN"))
    private void injectReloadShaders(ResourceManager pResourceManager, CallbackInfo ci) {
        LightManager.INSTANCE.init();
    }

}
