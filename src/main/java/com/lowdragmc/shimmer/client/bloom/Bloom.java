package com.lowdragmc.shimmer.client.bloom;

import com.lowdragmc.shimmer.ShimmerMod;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * @author KilaBash
 * @date 2022/05/02
 * @implNote Bloom Magic
 */
@OnlyIn(Dist.CLIENT)
public class Bloom implements ResourceManagerReloadListener {
    public static Bloom INSTANCE = new Bloom();

    private RenderTarget highlight = null;
    private PostChain postChain = null;
    private final Minecraft mc = Minecraft.getInstance();
    private boolean loadFailed = false;

    private Bloom() {
    }

    private RenderTarget getHighlightTarget() {
        if (highlight == null) {
            highlight = new TextureTarget(mc.getWindow().getWidth(), mc.getWindow().getHeight(), false, Minecraft.ON_OSX);
        }
        return highlight;
    }

    private PostChain getPostChain() {
        if (loadFailed) return null;
        try {
            if (postChain == null) {
                postChain =new PostChain(mc.getTextureManager(), mc.getResourceManager(), mc.getMainRenderTarget(),
                        new ResourceLocation(ShimmerMod.MODID, "shaders/post/bloom_unreal.json"));
                postChain.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
            }
        } catch (IOException e) {
            loadFailed = true;
        }
        return postChain;
    }

    public void renderBloom() {
        PostChain postChain = getPostChain();
        if (postChain != null) {
            RenderSystem.depthMask(false);
            RenderSystem.disableDepthTest();
            postChain.process(mc.getFrameTime());
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
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

    public void resize(int width, int height) {
        if (postChain != null) {
            postChain.resize(width, height);
        }
        if (highlight != null) {
            highlight.resize(width, height, Minecraft.ON_OSX);
        }
    }
}
