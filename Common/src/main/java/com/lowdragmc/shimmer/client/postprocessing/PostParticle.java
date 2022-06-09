package com.lowdragmc.shimmer.client.postprocessing;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.core.particles.ParticleGroup;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

/**
 * @author KilaBash
 * @date 2022/05/09
 * @implNote BloomParticle
 */
public class PostParticle extends Particle {

    public final Particle parent;
    public final PostProcessing postProcessing;

    public PostParticle(Particle parent, PostProcessing postProcessing) {
        super(null, 0, 0, 0);
        this.parent = parent;
        this.postProcessing = postProcessing;
    }

    @Override
    @Nonnull
    public Particle setPower(float pMultiplier) {
        return parent.setPower(pMultiplier);
    }

    @Override
    public void setParticleSpeed(double pXd, double pYd, double pZd) {
        parent.setParticleSpeed(pXd, pYd, pZd);
    }

    @Override
    @Nonnull
    public Particle scale(float pScale) {
        return parent.scale(pScale);
    }

    @Override
    public void setColor(float pParticleRed, float pParticleGreen, float pParticleBlue) {
        parent.setColor(pParticleRed, pParticleGreen, pParticleBlue);
    }

    @Override
    public void setLifetime(int pParticleLifeTime) {
        parent.setLifetime(pParticleLifeTime);
    }

    @Override
    public int getLifetime() {
        return parent.getLifetime();
    }

    @Override
    public void tick() {
        parent.tick();
    }

    @Override
    @Nonnull
    public String toString() {
        return "bloom_" + parent.toString();
    }

    @Override
    public void remove() {
        parent.remove();
    }

    @Override
    public void setPos(double pX, double pY, double pZ) {
        if (this.parent != null) {
            parent.setPos(pX, pY, pZ);
        }
    }

    @Override
    public void move(double pX, double pY, double pZ) {
        parent.move(pX, pY, pZ);
    }

    @Override
    public boolean isAlive() {
        return parent.isAlive();
    }

    @Override
    @Nonnull
    public AABB getBoundingBox() {
        return parent == null ? super.getBoundingBox() : parent.getBoundingBox();
    }

    @Override
    public void setBoundingBox(@Nonnull AABB pBb) {
        if (parent != null) {
            parent.setBoundingBox(pBb);
        }
    }

    @Override
    @Nonnull
    public Optional<ParticleGroup> getParticleGroup() {
        return parent.getParticleGroup();
    }


    @Override
    @ParametersAreNonnullByDefault
    public void render(VertexConsumer pBuffer, Camera pRenderInfo, float pPartialTicks) {
        parent.render(pBuffer, pRenderInfo, pPartialTicks);
    }

    @Override
    @Nonnull
    public ParticleRenderType getRenderType() {
        return postProcessing.getParticleType(parent.getRenderType());
    }
}
