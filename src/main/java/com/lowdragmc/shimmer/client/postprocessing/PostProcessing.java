package com.lowdragmc.shimmer.client.postprocessing;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lowdragmc.shimmer.ShimmerMod;
import com.lowdragmc.shimmer.client.rendertarget.CopyDepthTarget;
import com.lowdragmc.shimmer.client.rendertarget.MRTTarget;
import com.lowdragmc.shimmer.client.rendertarget.ProxyTarget;
import com.lowdragmc.shimmer.client.shader.RenderUtils;
import com.lowdragmc.shimmer.client.shader.ShaderInjection;
import com.lowdragmc.shimmer.core.IMainTarget;
import com.lowdragmc.shimmer.core.IParticleEngine;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author KilaBash
 * @date 2022/05/02
 * @implNote Custom PostProcessing
 */
@OnlyIn(Dist.CLIENT)
public class PostProcessing implements ResourceManagerReloadListener {
    private static final Map<String, PostProcessing> POST_PROCESSING_MAP = new HashMap<>();
    public static final PostProcessing BLOOM_UNREAL = new PostProcessing("bloom_unreal", new ResourceLocation(ShimmerMod.MODID, "shaders/post/bloom_unreal.json"));
    public static final PostProcessing BLOOM_UNITY = new PostProcessing("bloom_unity", new ResourceLocation(ShimmerMod.MODID, "shaders/post/bloom_unity.json"));
    public static final PostProcessing BLOOM_VANILLA = new PostProcessing("bloom_vanilla", new ResourceLocation(ShimmerMod.MODID, "shaders/post/bloom_vanilla.json"));
    public static final PostProcessing WARP = new PostProcessing("warp", new ResourceLocation(ShimmerMod.MODID, "shaders/post/warp.json"));

    private static final Minecraft mc = Minecraft.getInstance();
    public final String name;
    private CopyDepthTarget postTarget;
    private PostChain postChain = null;
    private boolean loadFailed = false;
    private final ResourceLocation shader;
    private final List<Consumer<MultiBufferSource>> postEntityDraw = Lists.newArrayList();
    private final Map<ParticleRenderType, IPostParticleType> particleTypeMap = Maps.newHashMap();
    private boolean hasParticle;

    private PostProcessing(String name, ResourceLocation shader) {
        this.shader = shader;
        this.name = name;
        POST_PROCESSING_MAP.put(name, this);
    }

    /**
     * register your custom postprocessing or replace the original one
     * @param name post name
     * @param shader post shader
     * @return PostProcessing
     */
    public static PostProcessing registerPost(String name, ResourceLocation shader) {
        return new PostProcessing(name, shader);
    }

    @Nullable
    public static PostProcessing getPost(String name) {
        return POST_PROCESSING_MAP.get(name);
    }

    public static Collection<PostProcessing> values() {
        return POST_PROCESSING_MAP.values();
    }
    
    public static PostProcessing getBlockBloom() {
        return BLOOM_UNREAL;
    }

    public static float getITime(float pPartialTicks) {
        return mc.level == null ? pPartialTicks : (mc.level.getGameTime() + pPartialTicks) / 20f;
    }

    public static void injectShaders() {
        ShaderInjection.registerVSHInjection("rendertype_solid", PostProcessing::BloomMRTVSHInjection);
        ShaderInjection.registerVSHInjection("rendertype_cutout", PostProcessing::BloomMRTVSHInjection);
        ShaderInjection.registerVSHInjection("rendertype_cutout_mipped", PostProcessing::BloomMRTVSHInjection);

        ShaderInjection.registerFSHInjection("rendertype_solid", PostProcessing::BloomMRTFSHInjection);
        ShaderInjection.registerFSHInjection("rendertype_cutout", PostProcessing::BloomMRTFSHInjection);
        ShaderInjection.registerFSHInjection("rendertype_cutout_mipped", PostProcessing::BloomMRTFSHInjection);
    }

    private static String BloomMRTVSHInjection(String s) {
        s = new StringBuffer(s).insert(s.lastIndexOf("void main()"), """                        
                        out float isBloom;
                        """).toString();
        s = new StringBuffer(s).insert(s.lastIndexOf('}'), """
                        isBloom = UV2.x;
                        """).toString();
        return s;
    }

    private static String BloomMRTFSHInjection(String s) {
        s = new StringBuffer(s).insert(s.lastIndexOf("out vec4 fragColor"), """
                        in float isBloom;
                        """).toString();
        s = new StringBuffer(s).insert(s.lastIndexOf("void main()"), """
                        out vec4 bloomColor;
                        """).toString();
        s = new StringBuffer(s).insert(s.lastIndexOf('}'), """
                    if (isBloom > 255.) {
                        bloomColor = fragColor;
                    } else {
                        bloomColor = vec4(0.);
                    }
                """).toString();
        return s;
    }

    public static String RbBloomMRTFSHInjection(String s) {
        s = new StringBuffer(s).insert(s.lastIndexOf("void main()"), """
                        out vec4 bloomColor;
                        """).toString();
        s = new StringBuffer(s).insert(s.lastIndexOf('}'), """
                    if (v_LightCoord.x > .97) {
                        bloomColor = fragColor;
                    } else {
                        bloomColor = vec4(0.);
                    }
                """).toString();
        return s;
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
            ShimmerMod.LOGGER.error("load post: [{}] post chain: [{}] failed!", this.name, shader, e);
            loadFailed = true;
        }
        return postChain;
    }

    public void renderBlockPost() {
        PostChain postChain = getPostChain();
        if (postChain != null) {
            RenderTarget mainTarget = mc.getMainRenderTarget();
            renderPost(postChain, new MRTTarget((IMainTarget) mainTarget), mainTarget);
            ((IMainTarget) mainTarget).clearBloomTexture(Minecraft.ON_OSX);
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

    public void renderEntityPost(ProfilerFiller profilerFiller) {
        if (!postEntityDraw.isEmpty() ) {
            profilerFiller.popPush("ENTITY_" + name.toUpperCase());

            RenderTarget mainTarget = mc.getMainRenderTarget();
            CopyDepthTarget postTarget = getPostTarget();
            postTarget.bindWrite(false);

            MultiBufferSource.BufferSource bufferSource = PostMultiBufferSource.BUFFER_SOURCE;
            for (Consumer<MultiBufferSource> sourceConsumer : postEntityDraw) {
                sourceConsumer.accept(bufferSource);
            }

            bufferSource.endBatch();

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

    /**
     * post rendering. rendered in next frame, before entity.
     * @param sourceConsumer BufferSource
     */
    public void postEntity(Consumer<MultiBufferSource> sourceConsumer) {
        postEntityDraw.add(sourceConsumer);
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
    public void onResourceManagerReload(@Nullable ResourceManager pResourceManager) {
        if (postChain != null) {
            postChain.close();
        }
        if (postTarget != null) {
            postTarget.destroyBuffers();
        }
        postTarget = null;
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
