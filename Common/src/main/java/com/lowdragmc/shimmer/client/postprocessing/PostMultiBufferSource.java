package com.lowdragmc.shimmer.client.postprocessing;

import com.mojang.blaze3d.vertex.BufferBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.Util;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.ModelBakery;

/**
 * @author KilaBash
 * @date 2022/5/11
 * @implNote PostMultiBufferSource, for proper rendering pipeline
 */
public class PostMultiBufferSource extends MultiBufferSource.BufferSource {
    private final static ChunkBufferBuilderPack fixedBufferPack = new ChunkBufferBuilderPack();
    public final static PostMultiBufferSource BUFFER_SOURCE = new PostMultiBufferSource();

    private static void put(Object2ObjectLinkedOpenHashMap<RenderType, BufferBuilder> pMapBuilders, RenderType pRenderType) {
        pMapBuilders.put(pRenderType, new BufferBuilder(pRenderType.bufferSize()));
    }

    protected PostMultiBufferSource() {
        super(new BufferBuilder(256), Util.make(new Object2ObjectLinkedOpenHashMap<>(), (map) -> {
            map.put(Sheets.solidBlockSheet(), fixedBufferPack.builder(RenderType.solid()));
            map.put(Sheets.cutoutBlockSheet(), fixedBufferPack.builder(RenderType.cutout()));
            map.put(Sheets.bannerSheet(), fixedBufferPack.builder(RenderType.cutoutMipped()));
//            map.put(ShimmerRenderTypes.bloom(), fixedBufferPack.builder(ShimmerRenderTypes.bloom()));
            map.put(Sheets.translucentCullBlockSheet(), fixedBufferPack.builder(RenderType.translucent()));
            put(map, Sheets.shieldSheet());
            put(map, Sheets.bedSheet());
            put(map, Sheets.shulkerBoxSheet());
            put(map, Sheets.signSheet());
            put(map, Sheets.chestSheet());
            put(map, RenderType.translucentNoCrumbling());
            put(map, RenderType.armorGlint());
            put(map, RenderType.armorEntityGlint());
            put(map, RenderType.glint());
            put(map, RenderType.glintDirect());
            put(map, RenderType.glintTranslucent());
            put(map, RenderType.entityGlint());
            put(map, RenderType.entityGlintDirect());
            put(map, RenderType.waterMask());
            ModelBakery.DESTROY_TYPES.forEach((renderType) -> put(map, renderType));
        }));
    }

    @Override
    public void endBatch() {
        endBatch(RenderType.solid());
        endBatch(RenderType.endPortal());
        endBatch(RenderType.endGateway());
        endBatch(Sheets.solidBlockSheet());
        endBatch(Sheets.cutoutBlockSheet());
        endBatch(Sheets.bedSheet());
        endBatch(Sheets.shulkerBoxSheet());
        endBatch(Sheets.signSheet());
        endBatch(Sheets.chestSheet());
        endBatch(Sheets.translucentCullBlockSheet());
        endBatch(Sheets.bannerSheet());
        endBatch(Sheets.shieldSheet());
        endBatch(RenderType.armorGlint());
        endBatch(RenderType.armorEntityGlint());
        endBatch(RenderType.glint());
        endBatch(RenderType.glintDirect());
        endBatch(RenderType.glintTranslucent());
        endBatch(RenderType.entityGlint());
        endBatch(RenderType.entityGlintDirect());
        endBatch(RenderType.waterMask());
        super.endBatch();
    }
}
