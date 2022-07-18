package com.lowdragmc.shimmer.client.postprocessing;

import com.google.common.collect.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lowdragmc.shimmer.Configuration;
import com.lowdragmc.shimmer.ShimmerConstants;
import com.lowdragmc.shimmer.Utils;
import com.lowdragmc.shimmer.client.rendertarget.CopyDepthTarget;
import com.lowdragmc.shimmer.client.rendertarget.MRTTarget;
import com.lowdragmc.shimmer.client.rendertarget.ProxyTarget;
import com.lowdragmc.shimmer.client.shader.RenderUtils;
import com.lowdragmc.shimmer.client.shader.ShaderInjection;
import com.lowdragmc.shimmer.core.IMainTarget;
import com.lowdragmc.shimmer.core.IParticleEngine;
import com.lowdragmc.shimmer.core.mixins.BlendModeMixin;
import com.lowdragmc.shimmer.core.mixins.ShimmerMixinPlugin;
import com.lowdragmc.shimmer.platform.Services;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.BlendMode;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author KilaBash
 * @date 2022/05/02
 * @implNote Custom PostProcessing
 */
public class PostProcessing implements ResourceManagerReloadListener {
    public static final Set<RenderType> CHUNK_TYPES = Sets.newHashSet(RenderType.solid(), RenderType.cutoutMipped(), RenderType.cutout());

    private static final Map<String, PostProcessing> POST_PROCESSING_MAP = new HashMap<>();
    public static final PostProcessing BLOOM_UNREAL = new PostProcessing("bloom_unreal", new ResourceLocation(ShimmerConstants.MOD_ID, "shaders/post/bloom_unreal.json"));
    public static final PostProcessing BLOOM_UNITY = new PostProcessing("bloom_unity", new ResourceLocation(ShimmerConstants.MOD_ID, "shaders/post/bloom_unity.json"));
    public static final PostProcessing WARP = new PostProcessing("warp", new ResourceLocation(ShimmerConstants.MOD_ID, "shaders/post/warp.json"));
    public static final PostProcessing VHS = new PostProcessing("vhs", new ResourceLocation(ShimmerConstants.MOD_ID, "shaders/post/vhs.json"));
    public static final PostProcessing FLICKER = new PostProcessing("flicker", new ResourceLocation(ShimmerConstants.MOD_ID, "shaders/post/flicker.json"));
    public static final PostProcessing HALFTONE = new PostProcessing("halftone", new ResourceLocation(ShimmerConstants.MOD_ID, "shaders/post/halftone.json"));
    public static final PostProcessing DOT_SCREEN = new PostProcessing("dot_screen", new ResourceLocation(ShimmerConstants.MOD_ID, "shaders/post/dot_screen.json"));

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
        if (mc.level == null || !mc.level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).get()) {
            return System.currentTimeMillis() % 1200000 / 1000f;
        } else {
            return ((mc.level.dayTime() % 24000) + pPartialTicks) / 20f;
        }
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
                        isBloom = float(UV2.x);
                        """).toString();
        return s;
    }

    private static String BloomMRTFSHInjection(String s) {
        s = s.replace("#version 150", "#version 330 core");
        s = new StringBuilder(s).insert(s.lastIndexOf("#moj_import <fog.glsl>"),"#moj_import <shimmer_fog.glsl>\n").toString();
        s = new StringBuffer(s).insert(s.lastIndexOf("out vec4 fragColor"), """
                        in float isBloom;
                        """).toString();
        s = s.replace("out vec4 fragColor", "layout (location = 0) out vec4 fragColor");
        s = new StringBuffer(s).insert(s.lastIndexOf("void main()"), """
                        layout (location = 1) out vec4 bloomColor;
                        """).toString();
        s = new StringBuffer(s).insert(s.lastIndexOf('}'), """
                    if (isBloom > 255.) {
                        bloomColor = fragColor * (1 - fogValue);
                    } else {
                        bloomColor = vec4(0.);
                    }
                """).toString();
        s = new StringBuilder(s).insert(s.lastIndexOf("vec4 color"),"float fogValue;\n").toString();
        s = new StringBuffer(s).insert(s.lastIndexOf("FogColor")+"FogColor".length(),",fogValue").toString();
        return s;
    }

    public static String RbBloomMRTFSHInjection(String s) {
        s = new StringBuffer(s).insert(s.lastIndexOf("void main()"), """
                        out vec4 bloomColor;
                        """).toString();
        s = new StringBuffer(s).insert(s.lastIndexOf('}'), """
                    if (v_LightCoord.x > .97) {
                        bloomColor = fragColor * smoothstep(u_FogEnd,u_FogStart,v_FragDistance);
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
                postChain = new PostChain(mc.getTextureManager(), mc.getResourceManager(), mc.getMainRenderTarget(), shader);
                postChain.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
            }
        } catch (IOException e) {
            ShimmerConstants.LOGGER.error("load post: [{}] post chain: [{}] failed!", this.name, shader, e);
            loadFailed = true;
        }
        return postChain;
    }

    public void renderBlockPost() {
        PostChain postChain = getPostChain();
        if (postChain != null && Services.PLATFORM.useBlockBloom()) {
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
        if (!postEntityDraw.isEmpty()) {
            BlendMode lastBlendMode = BlendModeMixin.getLastApplied();
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

            BlendModeMixin.setLastApplied(lastBlendMode);
            GlStateManager._disableBlend();
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
        if (ShimmerMixinPlugin.IS_OPT_LOAD) {
            sourceConsumer.accept(PostMultiBufferSource.BUFFER_SOURCE);
            PostMultiBufferSource.BUFFER_SOURCE.endBatch();
            return;
        }
        postEntityDraw.add(sourceConsumer);
    }

    public void postParticle(ParticleOptions particleOptions, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
        if (ShimmerMixinPlugin.IS_OPT_LOAD) {
            mc.particleEngine.createParticle(particleOptions, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
            return;
        }
        if (mc.particleEngine instanceof IParticleEngine) {
            ((IParticleEngine) mc.particleEngine).createPostParticle(this, particleOptions, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
        }
    }

    public void postParticle(ParticleOptions particleOptions, int viewDistanceSqr, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
        if (ShimmerMixinPlugin.IS_OPT_LOAD) {
            mc.particleEngine.createParticle(particleOptions, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
            return;
        }
        if (mc.particleEngine instanceof IParticleEngine) {
            Camera camera = mc.gameRenderer.getMainCamera();
            if (camera.getPosition().distanceToSqr(pX, pY, pZ) > viewDistanceSqr) return;
            ((IParticleEngine) mc.particleEngine).createPostParticle(this, particleOptions, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
        }
    }

    public void postParticle(Particle particle) {
        if (ShimmerMixinPlugin.IS_OPT_LOAD) {
            mc.particleEngine.add(particle);
            return;
        }
        if (mc.particleEngine instanceof IParticleEngine) {
            particle = Services.PLATFORM.createPostParticle(particle, this);
            mc.particleEngine.add(particle);
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

    public static Set<BlockState> BLOOM_BLOCK = new HashSet<>();
    public static Set<Fluid> BLOOM_FLUID = new HashSet<>();
    private static final ThreadLocal<Boolean> BLOCK_BLOOM = ThreadLocal.withInitial(()->false);
    private static final ThreadLocal<Boolean> FLUID_BLOOM = ThreadLocal.withInitial(()->false);

    public static void loadConfig() {
        JsonElement jsonElement = Configuration.config.get("BloomBlock");
        if (jsonElement != null && jsonElement.isJsonArray()) {
            JsonArray bloomBlocks = jsonElement.getAsJsonArray();
            for (JsonElement block : bloomBlocks) {
                JsonObject jsonObj = block.getAsJsonObject();
                if (jsonObj.has("block")) {
                    ResourceLocation location = new ResourceLocation(jsonObj.get("block").getAsString());
                    if (!Registry.BLOCK.containsKey(location)) continue;
                    Block bb = Registry.BLOCK.get(location);
                    if (jsonObj.has("state") && jsonObj.get("state").isJsonObject()) {
                        Set<BlockState> available = Utils.getAllPossibleStates(jsonObj, bb);
                        if (!available.isEmpty()) {
                            BLOOM_BLOCK.addAll(available);
                        }
                    } else {
                        BLOOM_BLOCK.addAll(bb.getStateDefinition().getPossibleStates());
                    }
                } else if (jsonObj.has("fluid")) {
                    ResourceLocation location = new ResourceLocation(jsonObj.get("fluid").getAsString());
                    if (!Registry.FLUID.containsKey(location)) continue;
                    Fluid ff = Registry.FLUID.get(location);
                    BLOOM_FLUID.add(ff);
                }
            }
        }
    }

    public static boolean isBlockBloom() {
        return BLOCK_BLOOM.get();
    }

    public static boolean isFluidBloom() {
        return FLUID_BLOOM.get();
    }

    public static void setupBloom(BlockState blockState, FluidState fluidState) {
        if (BLOOM_BLOCK.contains(blockState)) {
            BLOCK_BLOOM.set(true);
        } else {
            BLOCK_BLOOM.set(false);
        }
        Fluid fluid = fluidState.getType();
        if (BLOOM_FLUID.contains(fluid) || (fluid instanceof FlowingFluid flowingFluid && BLOOM_FLUID.contains(flowingFluid.getSource()))) {
            FLUID_BLOOM.set(true);
        } else {
            FLUID_BLOOM.set(false);
        }
    }

    public static void cleanBloom() {
        BLOCK_BLOOM.set(false);
        FLUID_BLOOM.set(false);
    }
}
