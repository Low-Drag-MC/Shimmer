package com.lowdragmc.shimmer.client.postprocessing;

import com.google.common.collect.Maps;
import com.lowdragmc.shimmer.EnumHelper;
import com.lowdragmc.shimmer.ShimmerMod;
import com.lowdragmc.shimmer.client.rendertarget.CopyDepthTarget;
import com.lowdragmc.shimmer.client.rendertarget.ProxyTarget;
import com.lowdragmc.shimmer.client.shader.RenderUtils;
import com.lowdragmc.shimmer.core.IParticleEngine;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author KilaBash
 * @date 2022/05/02
 * @implNote Custom PostProcessing
 */
@OnlyIn(Dist.CLIENT)
public enum PostProcessing implements ResourceManagerReloadListener {
    BLOOM_UNREAL(new ResourceLocation(ShimmerMod.MODID, "shaders/post/bloom_unreal.json")),
    BLOOM_UNITY(new ResourceLocation(ShimmerMod.MODID, "shaders/post/bloom_unity.json")),
    BLOOM_VANILLA(new ResourceLocation(ShimmerMod.MODID, "shaders/post/bloom_vanilla.json"));

    private static final Minecraft mc = Minecraft.getInstance();
    private CopyDepthTarget postTarget;
    private PostChain postChain = null;
    private boolean loadFailed = false;
    private final ResourceLocation shader;
    private final Map<RenderType, List<Consumer<VertexConsumer>>> postEntityDraw = Maps.newHashMap();
    private final Map<ParticleRenderType, IPostParticleType> particleTypeMap = Maps.newHashMap();
    private boolean hasParticle;

    PostProcessing(ResourceLocation shader) {
        this.shader = shader;
    }

    /**
     * register your custom postprocessing
     * @param name post name
     * @param shader post shader
     * @return PostProcessing
     */
    public static PostProcessing registerPost(String name, ResourceLocation shader) {
        return EnumHelper.addEnum(PostProcessing.class, name, new Class[]{ResourceLocation.class, Boolean.class}, shader);
    }

    public CopyDepthTarget getPostTarget() {
        if (postTarget == null) {
            postTarget = new CopyDepthTarget(mc.getMainRenderTarget(), Minecraft.ON_OSX);
            postTarget.setClearColor(0,0,0,0);
        }
        return postTarget;
    }

    private PostChain getPostChain() {
        if (loadFailed) return null;
        try {
            if (postChain == null) {
                postChain =new PostChain(mc.getTextureManager(), mc.getResourceManager(), mc.getMainRenderTarget(), shader);
                postChain.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
            }
        } catch (IOException e) {
            ShimmerMod.LOGGER.error("load post: [{}] post chain: [{}] failed!", this.name(), shader, e);
            loadFailed = true;
        }
        return postChain;
    }

    public void renderBlockPost() {
        PostChain postChain = getPostChain();
        if (postChain != null) {
            RenderTarget mainTarget = mc.getMainRenderTarget();
            CopyDepthTarget postTarget = getPostTarget();
            renderPost(postChain, postTarget, mainTarget);
            postTarget.clear(Minecraft.ON_OSX);
            mainTarget.bindWrite(false);
        }
    }

    public void renderPost(@Nonnull PostChain postChain, RenderTarget post, RenderTarget output) {
        RenderTarget target = postChain.getTempTarget("shimmer:input");
        if (target instanceof ProxyTarget) {
            ((ProxyTarget) target).setParent(post);
        }
        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();
        postChain.process(mc.getFrameTime());
        RenderUtils.fastBlit(postChain.getTempTarget("shimmer:output"), output);
    }

    public void renderEntityPost() {
        if (!postEntityDraw.isEmpty()) {
            RenderTarget mainTarget = mc.getMainRenderTarget();
            CopyDepthTarget postTarget = getPostTarget();
            postTarget.bindWrite(false);

            postEntityDraw.forEach((renderType, consumers) -> {
                BufferBuilder buffer = Tesselator.getInstance().getBuilder();
                buffer.begin(renderType.mode(), renderType.format());
                consumers.forEach(consumer -> consumer.accept(buffer));
                renderType.end(buffer, 0, 0, 0);
            });

            postEntityDraw.clear();

            PostChain postChain = getPostChain();
            if (postChain == null) return;

            renderPost(postChain, postTarget, mainTarget);

            postTarget.clear(Minecraft.ON_OSX);
            mainTarget.bindWrite(false);
        }
    }

    public void renderParticlePost() {
        if (hasParticle) {
            hasParticle = false;
            RenderTarget mainTarget = mc.getMainRenderTarget();
            CopyDepthTarget postTarget = getPostTarget();
            postTarget.bindWrite(false);

            PostChain postChain = getPostChain();

            if (postChain == null) return;

            renderPost(postChain, postTarget, mainTarget);
            postTarget.clear(Minecraft.ON_OSX);
            mainTarget.bindWrite(false);
        }
    }

    public void postEntity(RenderType renderType, Consumer<VertexConsumer> consumer) {
        postEntityDraw.computeIfAbsent(renderType, x->new LinkedList<>()).add(consumer);

    }

    public void postParticle(ParticleOptions particleOptions, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
        if (mc.particleEngine instanceof IParticleEngine) {
            ((IParticleEngine) mc.particleEngine).createPostParticle(this, particleOptions, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
        }
    }

    public void postParticle(ParticleOptions particleOptions, int viewDistanceSqr, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
        if (mc.particleEngine instanceof IParticleEngine) {
            Camera camera = mc.gameRenderer.getMainCamera();
            if (camera.getPosition().distanceToSqr(pX, pY, pZ) > viewDistanceSqr) return;
            ((IParticleEngine) mc.particleEngine).createPostParticle(this, particleOptions, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
        }
    }

    public ParticleRenderType getParticleType(ParticleRenderType renderType) {
        return particleTypeMap.computeIfAbsent(renderType, type -> new IPostParticleType() {
            @Override
            public ParticleRenderType getParent() {
                return type;
            }

            @Override
            public PostProcessing getPost() {
                return PostProcessing.this;
            }
        });
    }

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager pResourceManager) {
        if (postChain != null) {
            postChain.close();
        }
        postChain = null;
        loadFailed = false;
    }

    public static void resize(int width, int height) {
        for (PostProcessing postProcessing : PostProcessing.values()) {
            if (postProcessing.postChain != null) {
                postProcessing.postChain.resize(width, height);
            }
        }

        for (PostProcessing postProcessing : PostProcessing.values()) {
            if (postProcessing.postTarget != null) {
                postProcessing.postTarget.resize(mc.getMainRenderTarget(), Minecraft.ON_OSX);
            }
        }
    }

    public void hasParticle() {
        hasParticle = true;
    }
}
