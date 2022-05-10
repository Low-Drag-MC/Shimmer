package com.lowdragmc.shimmer.core.mixins;

import com.google.common.collect.ImmutableBiMap;
import com.lowdragmc.shimmer.client.ShimmerRenderTypes;
import com.lowdragmc.shimmer.core.IMultiLayerModelLoader;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.client.model.MultiLayerModel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

/**
 * @author KilaBash
 * @date 2022/05/07
 * @implNote MultiLayerModelLoaderMixin, inject a new rendertype (bloom)
 */
@Mixin(MultiLayerModel.Loader.class)
public class MultiLayerModelLoaderMixin implements IMultiLayerModelLoader {
    @Final @Mutable @Shadow public static ImmutableBiMap<String, RenderType> BLOCK_LAYERS;

    public void update() {
        BLOCK_LAYERS = ImmutableBiMap.<String, RenderType>builder()
                .put("solid", RenderType.solid())
                .put("cutout", RenderType.cutout())
                .put("cutout_mipped", RenderType.cutoutMipped())
                .put("bloom", ShimmerRenderTypes.bloom())
                .put("translucent", RenderType.translucent())
                .put("tripwire", RenderType.tripwire())
                .build();
    }
}
