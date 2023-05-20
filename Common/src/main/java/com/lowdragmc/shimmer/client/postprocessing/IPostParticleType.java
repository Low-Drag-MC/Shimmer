package com.lowdragmc.shimmer.client.postprocessing;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureManager;
import org.jetbrains.annotations.NotNull;

/**
 * @author KilaBash
 * @date 2022/05/09
 * @implNote TODO
 */
public interface IPostParticleType extends ParticleRenderType {

    ParticleRenderType getParent();
    PostProcessing getPost();

    @Override
    default void begin(@NotNull BufferBuilder pBuilder, @NotNull TextureManager pTextureManager) {
        getParent().begin(pBuilder, pTextureManager);
    }

    @Override
    default void end(@NotNull Tesselator pTesselator) {
        getParent().end(pTesselator);
    }
}
