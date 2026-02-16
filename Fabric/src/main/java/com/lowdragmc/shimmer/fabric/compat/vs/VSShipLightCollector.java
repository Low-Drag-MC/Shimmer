package com.lowdragmc.shimmer.fabric.compat.vs;

import com.lowdragmc.shimmer.ShimmerConstants;
import com.lowdragmc.shimmer.client.light.ColorPointLight;
import com.lowdragmc.shimmer.core.IRenderChunk;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import me.jellysquid.mods.sodium.client.render.chunk.lists.SortedRenderLists;
import org.joml.Matrix4dc;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.mod.mixinducks.mod_compat.sodium.RenderSectionManagerDuck;

import java.nio.FloatBuffer;
import java.util.Map;

/**
 * Collects Shimmer colored lights from Valkyrien Skies 2 ship render sections
 * and transforms their positions from shipyard-space into world-space before
 * uploading to the light UBO buffer.
 *
 * <p>This class references VS2 types directly and must <b>only</b> be loaded
 * when VS2 is confirmed present at runtime (guarded by
 * {@code MixinPluginShared.IS_VS2_LOAD}).</p>
 */
public final class VSShipLightCollector {

    private VSShipLightCollector() {}

    /**
     * Iterates every loaded ship's Sodium render lists, collects Shimmer lights
     * from each {@link IRenderChunk} section, transforms the light positions
     * through the ship's shipToWorld matrix, and writes them into {@code buffer}.
     *
     * @param renderSectionManager the Sodium render section manager (must also
     *                             implement VS2's {@link RenderSectionManagerDuck})
     * @param buffer               the shared FloatBuffer to append light data into
     * @param budget               maximum number of lights we're allowed to add
     * @return the number of ship lights actually written
     */
    public static int collectShipLights(RenderSectionManager renderSectionManager,
                                        FloatBuffer buffer, int budget) {
        if (!(renderSectionManager instanceof RenderSectionManagerDuck duck)) {
            return 0;
        }

        int added = 0;
        Map<ClientShip, SortedRenderLists> shipRenderLists = duck.vs_getShipRenderLists();

        for (var entry : shipRenderLists.entrySet()) {
            if (budget <= added) break;

            ClientShip ship = entry.getKey();
            SortedRenderLists renderLists = entry.getValue();

            // Ship's current render transform: shipyard coords → world coords
            Matrix4dc shipToWorld = ship.getRenderTransform().getShipToWorld();

            var chunkListIterator = renderLists.iterator();
            while (chunkListIterator.hasNext()) {
                if (budget <= added) break;

                var chunkRenderList = chunkListIterator.next();
                var region = chunkRenderList.getRegion();
                var sectionIterator = chunkRenderList.sectionsWithGeometryIterator(false);
                if (sectionIterator == null) continue;

                while (sectionIterator.hasNext()) {
                    if (budget <= added) break;

                    var section = region.getSection(sectionIterator.nextByteAsInt());
                    if (section == null) continue;

                    if (section instanceof IRenderChunk shimmerRenderChunk) {
                        for (ColorPointLight light : shimmerRenderChunk.getShimmerLights()) {
                            if (budget <= added) break;

                            // Transform light position from shipyard-space → world-space
                            Vector3d worldPos = shipToWorld.transformPosition(
                                    new Vector3d(light.x, light.y, light.z));

                            // Write the light struct: vec4 color, vec3 position, float radius
                            buffer.put(light.r);
                            buffer.put(light.g);
                            buffer.put(light.b);
                            buffer.put(light.a);
                            buffer.put((float) worldPos.x);
                            buffer.put((float) worldPos.y);
                            buffer.put((float) worldPos.z);
                            buffer.put(light.radius);

                            added++;
                        }
                    }
                }
            }
        }

        if (added > 0) {
            ShimmerConstants.LOGGER.debug("Collected {} ship lights from {} VS2 ships",
                    added, shipRenderLists.size());
        }

        return added;
    }
}
