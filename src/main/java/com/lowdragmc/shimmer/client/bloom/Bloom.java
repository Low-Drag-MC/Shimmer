package com.lowdragmc.shimmer.client.bloom;

import com.google.common.collect.Maps;
import com.lowdragmc.shimmer.ShimmerMod;
import com.lowdragmc.shimmer.core.IMainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
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
    BLOCK_BLOOM(new ResourceLocation(ShimmerMod.MODID, "shaders/post/bloom_unreal.json"), true),
    Entity_LAST_BLOOM(new ResourceLocation(ShimmerMod.MODID, "shaders/post/bloom_vanilla.json"), false);

    private static final Minecraft mc = Minecraft.getInstance();
    private PostChain postChain = null;
    private boolean loadFailed = false;
    private final ResourceLocation shader;
    private final Map<RenderType, List<Consumer<VertexConsumer>>> postDraw = Maps.newHashMap();
    private final boolean alwaysRender;

    Bloom(ResourceLocation shader, boolean alwaysRender) {
        this.shader = shader;
        this.alwaysRender = alwaysRender;
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
        if (postDraw.isEmpty() && !alwaysRender) return;
        if (!postDraw.isEmpty()) {
            postDraw.forEach((renderType, consumers) -> {
                BufferBuilder buffer = Tesselator.getInstance().getBuilder();
                buffer.begin(renderType.mode(), renderType.format());
                consumers.forEach(consumer -> consumer.accept(buffer));
                renderType.end(buffer, 0, 0, 0);
            });
            postDraw.clear();
        }
        PostChain postChain = getPostChain();
        if (postChain != null) {
            RenderSystem.depthMask(false);
            RenderSystem.disableDepthTest();
            postChain.process(mc.getFrameTime());
            RenderTarget mainTarget = mc.getMainRenderTarget();
            mainTarget.bindWrite(false);
            Matrix4f lastProj = RenderSystem.getProjectionMatrix();
            postChain.getTempTarget("output").blitToScreen(mainTarget.width, mainTarget.height, false);

            if (mainTarget instanceof IMainTarget) {
                ((IMainTarget) mainTarget).clearBloomTexture(Minecraft.ON_OSX);
                mainTarget.bindWrite(false);
            }

            RenderSystem.setProjectionMatrix(lastProj);
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
        }
    }

    public void postBloom(RenderType renderType, Consumer<VertexConsumer> consumer) {
        postDraw.computeIfAbsent(renderType, x->new LinkedList<>()).add(consumer);
    }

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager pResourceManager) {
        if (postChain != null) {
            postChain.close();
        }
        postChain = null;
        loadFailed = false;
    }

    public void resize(int width, int height) {
        if (postChain != null) {
            postChain.resize(width, height);
        }
    }
}
