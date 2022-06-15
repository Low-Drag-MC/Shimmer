package com.lowdragmc.shimmer.core.mixins;

import com.lowdragmc.shimmer.client.light.LightManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author HypherionSA
 * @date 2022/06/09
 */
@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Shadow
    public ClientLevel level;

    /***
     * Workaround for Forge's WorldEvent.Unload event
     * @param screen
     * @param ci
     */
    @Inject(method = "clearLevel(Lnet/minecraft/client/gui/screens/Screen;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/Minecraft;updateScreenAndTick(Lnet/minecraft/client/gui/screens/Screen;)V",
                    shift = At.Shift.AFTER))
    private void worldUnloadEvent(Screen screen, CallbackInfo ci) {
        if (this.level != null) {
            LightManager.clear();
        }
    }

}
