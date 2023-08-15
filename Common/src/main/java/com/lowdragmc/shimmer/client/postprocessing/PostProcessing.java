package com.lowdragmc.shimmer.client.postprocessing;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.lowdragmc.shimmer.Configuration;
import com.lowdragmc.shimmer.ShimmerConstants;
import com.lowdragmc.shimmer.Utils;
import com.lowdragmc.shimmer.client.rendertarget.CopyDepthColorTarget;
import com.lowdragmc.shimmer.client.rendertarget.MRTTarget;
import com.lowdragmc.shimmer.client.rendertarget.ProxyTarget;
import com.lowdragmc.shimmer.client.shader.RenderUtils;
import com.lowdragmc.shimmer.client.shader.ShaderInjection;
import com.lowdragmc.shimmer.core.IMainTarget;
import com.lowdragmc.shimmer.core.IParticleEngine;
import com.lowdragmc.shimmer.core.mixins.BlendModeMixin;
import com.lowdragmc.shimmer.core.mixins.ShimmerMixinPlugin;
import com.lowdragmc.shimmer.event.ShimmerReloadEvent;
import com.lowdragmc.shimmer.platform.Services;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.BlendMode;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * @author KilaBash
 * @date 2022/05/02
 * @implNote Custom PostProcessing
 */
@SuppressWarnings("unused")
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

    public static AtomicBoolean enableBloomFilter = new AtomicBoolean(false);
    private static final Minecraft mc = Minecraft.getInstance();
    public final String name;
    private CopyDepthColorTarget postTargetWithoutColor;
    private CopyDepthColorTarget postTargetWithColor;
    private PostChain postChain = null;
    private boolean loadFailed = false;
    private final ResourceLocation shader;
    private final List<Consumer<MultiBufferSource>> postEntityDrawFilter = Lists.newArrayList();
    private final List<Consumer<MultiBufferSource>> postEntityDrawForce = Lists.newArrayList();
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
        s = new StringBuffer(s).insert(s.lastIndexOf("in vec3 v_ColorModulator;"), """
                        in float isBloom;
                        """).toString();
        s = new StringBuffer(s).insert(s.lastIndexOf("void main()"), """
                        out vec4 bloomColor;
                        """).toString();
        s = new StringBuffer(s).insert(s.lastIndexOf('}'), """
                    if (isBloom > 255.) {
                        bloomColor = out_FragColor * smoothstep(u_FogEnd,u_FogStart,v_FragDistance);
                    } else {
                        bloomColor = vec4(0.);
                    }
                """).toString();
        return s;
    }

    public CopyDepthColorTarget getPostTarget(boolean hookColorAttachment) {
        if (hookColorAttachment) {
            if (postTargetWithColor == null) {
                postTargetWithColor = new CopyDepthColorTarget(mc.getMainRenderTarget(), true);
                postTargetWithColor.setClearColor(0, 0, 0, 0);
                postTargetWithColor.clear(Minecraft.ON_OSX);
            }
            return postTargetWithColor;
        }else {
            if (postTargetWithoutColor == null) {
                postTargetWithoutColor = new CopyDepthColorTarget(mc.getMainRenderTarget(), false);
                postTargetWithoutColor.setClearColor(0, 0, 0, 0);
                postTargetWithoutColor.clear(Minecraft.ON_OSX);
            }
            return postTargetWithoutColor;
        }
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
        if (postChain != null && Services.PLATFORM.useBlockBloom() && allowPost()) {
            enableBloomFilter.set(true);
            RenderTarget mainTarget = mc.getMainRenderTarget();
            renderPost(postChain, new MRTTarget((IMainTarget) mainTarget), mainTarget);
            ((IMainTarget) mainTarget).clearBloomTexture(Minecraft.ON_OSX);
            mainTarget.bindWrite(false);
            enableBloomFilter.set(false);
        }
    }

    public void renderPost(@NotNull PostChain postChain, RenderTarget post, RenderTarget output) {
        RenderTarget target = postChain.getTempTarget("shimmer:input");
        if (target instanceof ProxyTarget) {
            ((ProxyTarget) target).setParent(post);
        }
        BlendMode lastBlendMode = BlendModeMixin.getLastApplied();
        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();
        postChain.process(mc.getFrameTime());
        RenderUtils.fastBlit(postChain.getTempTarget("shimmer:output"), output);
        BlendModeMixin.setLastApplied(lastBlendMode);
    }

    public void renderEntityPost(ProfilerFiller profilerFiller) {
        if (!postEntityDrawFilter.isEmpty()) {
            RenderUtils.warpGLDebugLabel("post_filtered_" + this.name, () -> {
                BlendMode lastBlendMode = BlendModeMixin.getLastApplied();
                profilerFiller.popPush("ENTITY_" + name.toUpperCase());

                RenderTarget mainTarget = mc.getMainRenderTarget();
                CopyDepthColorTarget postTarget = getPostTarget(true);
                postTarget.bindWrite(false);

                MultiBufferSource.BufferSource bufferSource = PostMultiBufferSource.BUFFER_SOURCE;
                RenderUtils.warpGLDebugLabel("draw_post_entities", () -> {
                    postTarget.enabledAttachment();
                    for (Consumer<MultiBufferSource> sourceConsumer : postEntityDrawFilter) {
                        sourceConsumer.accept(bufferSource);
                    }
                    bufferSource.endBatch();
                    postTarget.disableAttachment();
                });

                postEntityDrawFilter.clear();

                PostChain postChain = getPostChain();
                if (postChain == null) return;

                if (allowPost()) {
                    RenderUtils.warpGLDebugLabel("actual_post_process", () -> {
                        renderPost(postChain, postTarget, mainTarget);
                    });
                } else {
                    RenderUtils.warpGLDebugLabel("reject_post", () -> {
                        RenderUtils.fastBlit(postTarget, mainTarget);
                    });
                }

                postTarget.clear(Minecraft.ON_OSX);
                mainTarget.bindWrite(false);

                BlendModeMixin.setLastApplied(lastBlendMode);
                GlStateManager._disableBlend();
            });
        }
        if (!postEntityDrawForce.isEmpty()) {
            RenderUtils.warpGLDebugLabel("post_force_" + this.name, () -> {
                BlendMode lastBlendMode = BlendModeMixin.getLastApplied();
                profilerFiller.popPush("ENTITY_" + name.toUpperCase());

                RenderTarget mainTarget = mc.getMainRenderTarget();
                CopyDepthColorTarget postTarget = getPostTarget(false);
                postTarget.bindWrite(false);

                MultiBufferSource.BufferSource bufferSource = PostMultiBufferSource.BUFFER_SOURCE;
                RenderUtils.warpGLDebugLabel("draw_post_entities", () -> {
                    for (Consumer<MultiBufferSource> sourceConsumer : postEntityDrawForce) {
                        sourceConsumer.accept(bufferSource);
                    }
                    bufferSource.endBatch();
                });

                postEntityDrawForce.clear();

                PostChain postChain = getPostChain();
                if (postChain == null) return;

                if (allowPost()) {
                    RenderUtils.warpGLDebugLabel("actual_post_process", () -> {
                        renderPost(postChain, postTarget, mainTarget);
                    });
                } else {
                    RenderUtils.warpGLDebugLabel("reject_post", () -> {
                        RenderUtils.fastBlit(postTarget, mainTarget);
                    });
                }

                postTarget.clear(Minecraft.ON_OSX);
                mainTarget.bindWrite(false);

                BlendModeMixin.setLastApplied(lastBlendMode);
                GlStateManager._disableBlend();
            });
        }
    }

    public boolean allowPost() {
        return !(this == PostProcessing.BLOOM_UNREAL || this == PostProcessing.BLOOM_UNITY) || Services.PLATFORM.isBloomEnable();
    }

    public void renderParticlePost() {
        if (hasParticle) {
            hasParticle = false;
            RenderTarget mainTarget = mc.getMainRenderTarget();
            CopyDepthColorTarget postTarget = getPostTarget(false);
            postTarget.bindWrite(false);

            PostChain postChain = getPostChain();

            if (postChain == null) return;

            if (allowPost()) {
                renderPost(postChain, postTarget, mainTarget);
            } else {
                RenderUtils.fastBlit(postTarget, mainTarget);
            }

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
        postEntityDrawFilter.add(sourceConsumer);
    }

    public void postEntityForce(Consumer<MultiBufferSource> sourceConsumer) {
        if (ShimmerMixinPlugin.IS_OPT_LOAD) {
            sourceConsumer.accept(PostMultiBufferSource.BUFFER_SOURCE);
            PostMultiBufferSource.BUFFER_SOURCE.endBatch();
            return;
        }
        postEntityDrawForce.add(sourceConsumer);
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

            @Override
            public String toString() {
                return "POST_WRAPPED_" + type.toString();
            }
        });
    }

	public static List<IPostParticleType> getBlockBloomPostParticleTypes(){
		return List.copyOf(getBlockBloom().particleTypeMap.values());
	}

    @Override
    public void onResourceManagerReload(@Nullable ResourceManager pResourceManager) {
        if (postChain != null) {
            postChain.close();
        }
        if (postTargetWithoutColor != null) {
            postTargetWithoutColor.destroyBuffers();
        }
        if (postTargetWithColor != null) {
            postTargetWithColor.destroyBuffers();
        }
        postTargetWithoutColor = null;
        postTargetWithColor = null;
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
            if (postProcessing.postTargetWithColor != null) {
                postProcessing.postTargetWithColor.resize(mc.getMainRenderTarget(), Minecraft.ON_OSX);
            }
            if (postProcessing.postTargetWithoutColor != null) {
                postProcessing.postTargetWithoutColor.resize(mc.getMainRenderTarget(), Minecraft.ON_OSX);
            }
        }
    }

    public void hasParticle() {
        hasParticle = true;
    }

    public static Set<BlockState> BLOOM_BLOCK = new HashSet<>();
    public static Set<Fluid> BLOOM_FLUID = new HashSet<>();
    public static Set<ResourceLocation> BLOOM_PARTICLE = new HashSet<>();
    private static final ThreadLocal<Boolean> BLOCK_BLOOM = ThreadLocal.withInitial(()->false);
    private static final ThreadLocal<Boolean> FLUID_BLOOM = ThreadLocal.withInitial(()->false);

    public static void loadConfig() {
        BLOOM_BLOCK.clear();
        BLOOM_FLUID.clear();
        BLOOM_PARTICLE.clear();

		for (var config : Configuration.configs){
			for (var bloom : config.blooms){
				if (bloom.particleName != null) {
					if (!ResourceLocation.isValidResourceLocation(bloom.particleName)){
						ShimmerConstants.LOGGER.error("invalid particle name " + bloom.particleName + " form" + config.configSource);
						continue;
					}
					var particleLocation = new ResourceLocation(bloom.particleName);
					BLOOM_PARTICLE.add(particleLocation);
				}else if (bloom.fluidName != null){
					Pair<ResourceLocation, Fluid> fluid = bloom.fluid();
					if (fluid == null || fluid.right() == null) continue;
					BLOOM_FLUID.add(fluid.right());
				}else {
					var blockPair = bloom.block();
					if (blockPair == null || blockPair.left() == null || blockPair.right() == null) continue;
					var block =  blockPair.right();

					if (bloom.hasState()){

						if (Utils.checkBlockProperties(config.configSource,bloom.state,blockPair.left())) continue;

						List<BlockState> availableStates = Utils.getAvailableStates(bloom.state, block);
						BLOOM_BLOCK.addAll(availableStates);

					}else {
						BLOOM_BLOCK.addAll(block.getStateDefinition().getPossibleStates());
					}
				}
			}
		}

	    Services.PLATFORM.postReloadEvent(new ShimmerReloadEvent(ShimmerReloadEvent.ReloadType.BLOOM));


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

    public static void forceBloomBlock(Runnable runnable) {
        BLOCK_BLOOM.set(true);
        runnable.run();
        BLOCK_BLOOM.set(false);
    }

    public static void forceBloomFluid(Runnable runnable) {
        FLUID_BLOOM.set(true);
        runnable.run();
        FLUID_BLOOM.set(false);
    }

    public static void cleanBloom() {
        BLOCK_BLOOM.set(false);
        FLUID_BLOOM.set(false);
    }
}
