package com.lowdragmc.shimmer.client.light;

import com.google.common.collect.MinMaxPriorityQueue;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.item.ItemEntity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;

@SuppressWarnings("UnstableApiUsage")
public class ItemEntityLightSourceManager {
    private static final int MAX_ITEM_ENTITY_LIGHT_COUNT = 64;
    private static final int MAX_ACCOUNTED_ITEM_ENTITY_LIGHT_DISTANCE = 32;

    public static final ArrayList<@NotNull ColorPointLight> renderLights = new ArrayList<>();
    private static final MinMaxPriorityQueue<ItemEntityLight> collectLights = MinMaxPriorityQueue
            .orderedBy(Comparator.comparingDouble(ItemEntityLight::distance))
            .expectedSize(MAX_ITEM_ENTITY_LIGHT_COUNT)
            .create();

    public static void tickItemEntity(ItemEntity item) {
        if (!item.level().isClientSide()) return;
        if (!item.isAlive()) return;
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        float distance = item.distanceTo(player);
        if (distance > MAX_ACCOUNTED_ITEM_ENTITY_LIGHT_DISTANCE) return;
        var light = LightManager.INSTANCE.getItemLight(item.getItem(), item.position());
        if (light == null) return;
        collectLights.add(new ItemEntityLight(distance, light));
    }

    public static void onAllItemEntityTickEnd() {
        renderLights.clear();
        collectLights.forEach(l -> renderLights.add(l.light));
        collectLights.clear();
    }

    public record ItemEntityLight(float distance, ColorPointLight light) {
    }

    public static ArrayList<ColorPointLight> getRenderLights() {
        return renderLights;
    }

}
