package com.lowdragmc.shimmer.core.mixins;

import com.google.common.collect.Maps;
import com.lowdragmc.shimmer.client.postprocessing.PostProcessing;
import com.lowdragmc.shimmer.core.IParticleDescription;
import com.lowdragmc.shimmer.core.IParticleEngine;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleDescription;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * @author KilaBash
 * @date 2022/7/25
 * @implNote ParticleDescriptionMixin
 */
@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {
    private final Map<ResourceLocation, String> PARTICLE_EFFECT = Maps.newHashMap();

    @Inject(method = "loadParticleDescription",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleDescription;getTextures()Ljava/util/List;"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void injectLoad(ResourceManager $$0, ResourceLocation $$1, Map<ResourceLocation, List<ResourceLocation>> $$2,
                                   CallbackInfo ci,
                                   ResourceLocation $$3,
                                   Resource $$4, Reader $$5, ParticleDescription $$6) {
        if ($$6 instanceof IParticleDescription particleDescription && particleDescription.getEffect() != null) {
            PARTICLE_EFFECT.put($$1, particleDescription.getEffect());
        }
    }

    @Inject(method = "reload", at = @At(value = "HEAD"))
    private void injectReload(PreparableReloadListener.PreparationBarrier $$0, ResourceManager $$1, ProfilerFiller $$2, ProfilerFiller $$3, Executor $$4, Executor $$5, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        PARTICLE_EFFECT.clear();
    }

    @Inject(method = "createParticle", at = @At(value = "HEAD"), cancellable = true)
    private void injectCreateParticle(ParticleOptions particleOptions, double x, double y, double z, double sx, double sy, double sz, CallbackInfoReturnable<Particle> cir) {
        ResourceLocation name = Registry.PARTICLE_TYPE.getKey(particleOptions.getType());
        if (!ShimmerMixinPlugin.IS_OPT_LOAD && PARTICLE_EFFECT.containsKey(name) && this instanceof IParticleEngine particleEngine) {
            PostProcessing postProcessing = PostProcessing.getPost(PARTICLE_EFFECT.get(name));
            if (postProcessing != null) {
                cir.setReturnValue(particleEngine.createPostParticle(postProcessing, particleOptions, x, y, z, sx, sy, sz));
            }
        }
    }
}
