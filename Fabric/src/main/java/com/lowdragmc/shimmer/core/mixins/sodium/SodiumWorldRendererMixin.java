package com.lowdragmc.shimmer.core.mixins.sodium;

import com.lowdragmc.shimmer.client.light.ColorPointLight;
import com.lowdragmc.shimmer.client.light.LightManager;
import com.lowdragmc.shimmer.core.IRenderChunk;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderList;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion;
import me.jellysquid.mods.sodium.client.util.frustum.Frustum;
import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.FloatBuffer;
import java.util.List;
import java.util.Map;

/**
 * @author KilaBash
 * @date 2022/05/31
 * @implNote TODO
 */
@Mixin(SodiumWorldRenderer.class)
public abstract class SodiumWorldRendererMixin {

    @Shadow(remap = false) private RenderSectionManager renderSectionManager;

    @Inject(method = "updateChunks", at = @At(value = "HEAD"), remap = false)
    private void injectCompilePre(Camera camera, Frustum frustum, int frame, boolean spectator, CallbackInfo ci) {
        Vec3 position = camera.getPosition();
        int blockLightSize = 0;
        int left = LightManager.INSTANCE.leftLightCount();
        FloatBuffer buffer = LightManager.INSTANCE.getBuffer();
        buffer.clear();
        ChunkRenderList chunkRenderList = ((RenderSectionManagerAccessor)renderSectionManager).getChunkRenderList();
        for (Map.Entry<RenderRegion, List<RenderSection>> entry : chunkRenderList.sorted(false)) {
            if (left <= blockLightSize) {
                break;
            }
            List<RenderSection> regionSections = entry.getValue();
            for (RenderSection regionSection : regionSections) {
                if (left <= blockLightSize) {
                    break;
                }
                if (regionSection instanceof IRenderChunk renderChunk) {
                    for (ColorPointLight shimmerLight : renderChunk.getShimmerLights()) {
                        if (left <= blockLightSize) {
                            break;
                        }
                        shimmerLight.uploadBuffer(buffer);
                        blockLightSize++;
                    }
                }
            }
        }
        buffer.flip();
        LightManager.INSTANCE.renderLevelPre(blockLightSize, (float)position.x,(float) position.y, (float)position.z);
    }

}
