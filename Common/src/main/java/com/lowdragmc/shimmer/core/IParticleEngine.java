package com.lowdragmc.shimmer.core;

import com.lowdragmc.shimmer.client.postprocessing.PostProcessing;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.ParticleOptions;

public interface IParticleEngine {
    Particle createPostParticle(PostProcessing postProcessing, ParticleOptions pParticleData, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed);

}
