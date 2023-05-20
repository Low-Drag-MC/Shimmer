package com.lowdragmc.shimmer.core.mixins;

import com.lowdragmc.shimmer.client.auxiliaryScreen.Eyedropper;
import com.lowdragmc.shimmer.client.auxiliaryScreen.HsbColorWidget;
import com.lowdragmc.shimmer.client.light.LightManager;
import com.lowdragmc.shimmer.client.postprocessing.PostProcessing;
import com.lowdragmc.shimmer.client.shader.RenderUtils;
import com.lowdragmc.shimmer.client.ShimmerRenderTypes;
import com.lowdragmc.shimmer.client.shader.ShaderSSBO;
import com.lowdragmc.shimmer.platform.Services;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author KilaBash
 * @date 2022/05/02
 * @implNote GameRendererMixin, used to refresh shader and fbo size.
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow @Final private Map<String, ShaderInstance> shaders;

	@Shadow @Final private ResourceManager resourceManager;

	@Shadow public abstract void setRenderHand(boolean renderHand);

	@Inject(method = "resize", at = @At(value = "RETURN"))
    private void injectResize(int width, int height, CallbackInfo ci) {
        PostProcessing.resize(width, height);
    }

    @Inject(method = "reloadShaders", at = @At(value = "RETURN"))
    private void injectReloadShaders(ResourceProvider pResourceManager, CallbackInfo ci) {
        if (Services.PLATFORM.isLoadingStateValid()) {
            LightManager.INSTANCE.reloadShaders();
        }
    }

    /* Replacement for RegisterShadersEvent, as fabric has no equivalent event  */
    @Inject(method = "reloadShaders", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;shutdownShaders()V", shift = At.Shift.AFTER))
    private void reloadShaders(ResourceProvider resourceProvider, CallbackInfo ci) {
        if (Services.PLATFORM.getPlatformName().equalsIgnoreCase("fabric")) {
			this.setupShader(RenderUtils::registerShaders,resourceManager);
			this.setupShader(ShimmerRenderTypes::registerShaders,resourceManager);
			this.setupShader(HsbColorWidget::registerShaders,resourceManager);
	        if (ShaderSSBO.support()) {
		        this.setupShader(Eyedropper::registerShaders,resourceManager);
	        }
        }
    }

	private void setupShader(Function<ResourceManager,Pair<ShaderInstance, Consumer<ShaderInstance>>> function,ResourceManager manager){
		var shader = function.apply(manager);
		this.shaders.put(shader.getFirst().getName(),shader.getFirst());
		shader.getSecond().accept(shader.getFirst());
	}


}
