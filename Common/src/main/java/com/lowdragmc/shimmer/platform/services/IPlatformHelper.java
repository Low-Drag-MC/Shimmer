package com.lowdragmc.shimmer.platform.services;

import com.lowdragmc.shimmer.client.postprocessing.PostParticle;
import com.lowdragmc.shimmer.client.postprocessing.PostProcessing;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.particle.Particle;

/**
 * @author HypherionSA
 * @date 2022/06/09
 */
public interface IPlatformHelper {

    /**
     * Gets the name of the current platform
     *
     * @return The name of the current platform.
     */
    String getPlatformName();

    /**
     * Checks if a mod with the given id is loaded.
     *
     * @param modId The mod to check if it is loaded.
     * @return True if the mod is loaded, false otherwise.
     */
    boolean isModLoaded(String modId);

    default boolean isClassFound(String className) {
        try {
            Class.forName(className, false, getClass().getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Check if the game is currently in a development environment.
     *
     * @return True if in a development environment, false otherwise.
     */
    boolean isDevelopmentEnvironment();

    /**
     * This is specific to forge, apparently.
     * @return - True or False on Forge, always false on Fabric
     */
    boolean isStencilEnabled(RenderTarget target);

    /**
     * This is specific to forge, apparently.
     * @return - True or False on Forge, always false on Fabric
     */
    boolean useCombinedDepthStencilAttachment();

    void enableStencil(RenderTarget renderTarget);

    int getUniformBufferObjectOffset();

    boolean useBlockBloom();

    boolean useLightMap();

    default PostParticle createPostParticle(Particle parent, PostProcessing postProcessing) {
        return new PostParticle(parent, postProcessing);
    }

    /**
     * when stepping into broken state , stop doing some operations to prevent crashing from shimmer
     */
    default boolean isLoadingStateValid() {
        return true;
    }

    default boolean isColoredLightEnable() {
        return true;
    }

    default boolean isBloomEnable(){
        return true;
    }
}
