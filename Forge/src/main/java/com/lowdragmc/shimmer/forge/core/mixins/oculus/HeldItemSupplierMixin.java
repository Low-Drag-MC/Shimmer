package com.lowdragmc.shimmer.forge.core.mixins.oculus;

import com.lowdragmc.shimmer.client.light.LightManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.coderbot.iris.uniforms.IdMapUniforms.HeldItemSupplier", remap = false)
public class HeldItemSupplierMixin {

    @Shadow
    private int lightValue;

    @Shadow
    private Vector3f lightColor;

    @Shadow @Final private InteractionHand hand;

    @Inject(method = "update", at = @At("TAIL"))
    private void injectColoredLight(CallbackInfo ci) {
        if (hand != InteractionHand.MAIN_HAND) return;
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        var light = LightManager.INSTANCE.getPlayerHeldItemLight(player);
        if (light != null) {
            lightValue = (int) light.radius;
            lightColor = new Vector3f(light.r, light.g, light.b);
            LightManager.INSTANCE.removePlayerLight(player.getUUID());
        }
    }
}
