package com.lowdragmc.shimmer.client.bloom;

import com.google.common.collect.Maps;
import com.lowdragmc.shimmer.ShimmerMod;
import com.lowdragmc.shimmer.client.rendertarget.CopyDepthTarget;
import com.lowdragmc.shimmer.client.shader.ShaderUtils;
import com.lowdragmc.shimmer.core.IMainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author KilaBash
 * @date 2022/05/02
 * @implNote Bloom Magic
 */
@OnlyIn(Dist.CLIENT)
public enum Bloom implements ResourceManagerReloadListener {
    BLOCK_BLOOM(new ResourceLocation(ShimmerMod.MODID, "shaders/post/bloom_unreal.json")),
    ENTITY_LAST_BLOOM(new ResourceLocation(ShimmerMod.MODID, "shaders/post/bloom_vanilla.json"));

    private static final Minecraft mc = Minecraft.getInstance();
    private static CopyDepthTarget bloomTarget;
    private PostChain postChain = null;
    private boolean loadFailed = false;
    private final ResourceLocation shader;
    private final Map<RenderType, List<Consumer<VertexConsumer>>> postDraw = Maps.newHashMap();

    Bloom(ResourceLocation shader) {
        this.shader = shader;
    }

    public static CopyDepthTarget getBloomTarget() {
        if (bloomTarget == null) {
            bloomTarget = new CopyDepthTarget(mc.getMainRenderTarget(), Minecraft.ON_OSX);
            bloomTarget.setClearColor(0,0,0,0);
        }
        return bloomTarget;
    }

    private PostChain getPostChain() {
        if (loadFailed) return null;
        try {
            if (postChain == null) {
                postChain =new PostChain(mc.getTextureManager(), mc.getResourceManager(), mc.getMainRenderTarget(), shader);
                postChain.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
            }
        } catch (IOException e) {
            ShimmerMod.LOGGER.error("load bloom: [{}] post chain: [{}] failed!", this.name(), shader, e);
            loadFailed = true;
        }
        return postChain;
    }

    public void renderBloom() {
        if (postDraw.isEmpty() && this != BLOCK_BLOOM) return;
        PostChain postChain = getPostChain();
        if (postChain != null) {
            RenderTarget mainTarget = mc.getMainRenderTarget();

            if (!postDraw.isEmpty()) {
                CopyDepthTarget bloom = getBloomTarget();
                bloom.clear(Minecraft.ON_OSX);
                bloom.bindWrite(true);

                postDraw.forEach((renderType, consumers) -> {
                    BufferBuilder buffer = Tesselator.getInstance().getBuilder();
                    buffer.begin(renderType.mode(), renderType.format());
                    consumers.forEach(consumer -> consumer.accept(buffer));
                    renderType.end(buffer, 0, 0, 0);
                });

                postDraw.clear();

                ShaderUtils.fastBlit(bloom, mainTarget);
            }

            RenderSystem.depthMask(false);
            RenderSystem.disableDepthTest();
            postChain.process(mc.getFrameTime());

            ShaderUtils.fastBlit(postChain.getTempTarget("output"), mainTarget);

            if (mainTarget instanceof IMainTarget) {
                ((IMainTarget) mainTarget).clearBloomTexture(Minecraft.ON_OSX);
                mainTarget.bindWrite(false);
            }

        }
    }

    public void postBloom(RenderType renderType, Consumer<VertexConsumer> consumer) {
        if (this != BLOCK_BLOOM) {
            postDraw.computeIfAbsent(renderType, x->new LinkedList<>()).add(consumer);
        }
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
        for (Bloom bloom : Bloom.values()) {
            if (bloom.postChain != null) {
                bloom.postChain.resize(width, height);
            }
        }

        if (bloomTarget != null) {
            bloomTarget.resize(mc.getMainRenderTarget(), Minecraft.ON_OSX);
        }
    }
}
