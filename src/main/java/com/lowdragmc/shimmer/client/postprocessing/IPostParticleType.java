package com.lowdragmc.shimmer.client.postprocessing;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureManager;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2022/05/09
 * @implNote TODO
 */
public interface IPostParticleType extends ParticleRenderType {

    ParticleRenderType getParent();
    PostProcessing getPost();

    @Override
    @ParametersAreNonnullByDefault
    default void begin(BufferBuilder pBuilder, TextureManager pTextureManager) {
        getParent().begin(pBuilder, pTextureManager);
    }

    @Override
    @ParametersAreNonnullByDefault
    default void end(Tesselator pTesselator) {
        getParent().end(pTesselator);
    }
}
