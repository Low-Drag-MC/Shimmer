package com.lowdragmc.shimmer.fabric.core.mixins.sodium;

import com.lowdragmc.shimmer.client.light.LightManager;
import com.lowdragmc.shimmer.core.IRenderChunk;
import com.lowdragmc.shimmer.core.mixins.MixinPluginShared;
import com.lowdragmc.shimmer.fabric.compat.vs.VSShipLightCollector;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import me.jellysquid.mods.sodium.client.render.chunk.lists.ChunkRenderList;
import me.jellysquid.mods.sodium.client.render.viewport.Viewport;
import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.FloatBuffer;

/**
 * @author KilaBash
 * @date 2022/05/31
 * @implNote Collects Shimmer colored lights from Sodium render sections
 *           and uploads them to the light UBO. Includes VS2 ship light support.
 */
@Mixin(SodiumWorldRenderer.class)
public abstract class SodiumWorldRendererMixin {

    @Shadow(remap = false) private RenderSectionManager renderSectionManager;

    @Inject(method = "setupTerrain", at = @At(value = "HEAD"), remap = false)
    private void injectCompilePre(Camera camera, Viewport viewport, int frame, boolean spectator, boolean updateChunksImmediately, CallbackInfo ci) {
        Vec3 position = camera.getPosition();
        int blockLightSize = 0;
        int left = LightManager.INSTANCE.leftBlockLightCount();
        FloatBuffer buffer = LightManager.INSTANCE.getBuffer();
        buffer.clear();

        // --- Collect lights from main-world terrain render lists ---
        var chunkrenderListIterator = ((RenderSectionManagerAccessor) renderSectionManager).getRenderLists().iterator();
        while (chunkrenderListIterator.hasNext()) {
            if (left <= blockLightSize) break;
            var chunkRenderList = chunkrenderListIterator.next();
            var region = chunkRenderList.getRegion();
            var sectionIterator = chunkRenderList.sectionsWithGeometryIterator(false);
            if (sectionIterator == null) continue;
            while (sectionIterator.hasNext()) {
                var section = region.getSection(sectionIterator.nextByteAsInt());
                if (section == null) continue;
                if (left <= blockLightSize) break;
                if (section instanceof IRenderChunk shimmerRenderChunk) {
                    for (var shimmerLight : shimmerRenderChunk.getShimmerLights()) {
                        if (left <= blockLightSize) break;
                        shimmerLight.uploadBuffer(buffer);
                        blockLightSize++;
                    }
                }
            }
        }

        // --- Collect lights from VS2 ship render lists (positions transformed to world-space) ---
        if (MixinPluginShared.IS_VS2_LOAD) {
            blockLightSize += VSShipLightCollector.collectShipLights(
                    renderSectionManager, buffer, left - blockLightSize);
        }

        LightManager.INSTANCE.renderLevelPre(blockLightSize, (float)position.x,(float) position.y, (float)position.z);
    }

}
