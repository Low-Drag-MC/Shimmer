package com.lowdragmc.shimmer.core.mixins;

import com.google.gson.JsonObject;
import com.lowdragmc.shimmer.core.IParticleDescription;
import net.minecraft.client.particle.ParticleDescription;
import net.minecraft.util.GsonHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author KilaBash
 * @date 2022/7/25
 * @implNote ParticleDescriptionMixin
 */
@Mixin(ParticleDescription.class)
public class ParticleDescriptionMixin implements IParticleDescription {
    private String effect;

    @Inject(method = "fromJson", at = @At(value = "RETURN"))
    private static void injectLoad(JsonObject $$0, CallbackInfoReturnable<ParticleDescription> cir) {
        ParticleDescription description = cir.getReturnValue();
        JsonObject shimmer = GsonHelper.getAsJsonObject($$0, "shimmer", null);
        if (shimmer != null && description instanceof IParticleDescription iParticleDescription) {
            iParticleDescription.setPostEffect(GsonHelper.getAsString(shimmer, "effect", null));
        }
    }

    @Override
    public String getEffect() {
        return effect;
    }

    @Override
    public void setPostEffect(String effect) {
        this.effect = effect;
    }
}
