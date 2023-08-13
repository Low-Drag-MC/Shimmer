package com.lowdragmc.shimmer.fabric.core.mixins.sodium;

import com.lowdragmc.shimmer.client.light.ColorPointLight;
import com.lowdragmc.shimmer.core.IRenderChunk;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Collections;
import java.util.List;

/**
 * @author KilaBash
 * @date 2022/05/28
 * @implNote TODO
 */
@Mixin(RenderSection.class)
public abstract class RenderSectionMixin implements IRenderChunk {
    @Unique
    List<ColorPointLight> shimmerLights = Collections.emptyList();

    @Override
    public List<ColorPointLight> getShimmerLights() {
        return shimmerLights;
    }

    @Override
    public void setShimmerLights(List<ColorPointLight> lights) {
        shimmerLights = lights;
    }

}
