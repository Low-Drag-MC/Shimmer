package com.lowdragmc.shimmer.client.light;

import com.lowdragmc.shimmer.client.Drawer;
import com.lowdragmc.shimmer.client.shader.RenderUtils;
import com.lowdragmc.shimmer.platform.Services;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class LightCounter {
    private static int playerHeldItemLightCount;
    private static int itemEntityLightCount;
    private static int freeNoUVLightCount;

    private static int freeUVLightCount;

    private static int blockLightCount;

    public static int getPlayerHeldItemLightCount() {
        return playerHeldItemLightCount;
    }

    public static void setPlayerHeldItemLightCount(int playerHeldItemLightCount) {
        LightCounter.playerHeldItemLightCount = playerHeldItemLightCount;
    }

    public static int getItemEntityLightCount() {
        return itemEntityLightCount;
    }

    public static void setItemEntityLightCount(int itemEntityLightCount) {
        LightCounter.itemEntityLightCount = itemEntityLightCount;
    }

    public static int getFreeNoUVLightCount() {
        return freeNoUVLightCount;
    }

    public static void setFreeNoUVLightCount(int freeNoUVLightCount) {
        LightCounter.freeNoUVLightCount = freeNoUVLightCount;
    }

    public static int getTotalUVLightCount() {
        return freeUVLightCount + (isChunkLightUseUv() ? blockLightCount : 0);
    }

    public static int getFreeUVLightCount() {
        return freeUVLightCount;
    }

    public static void setFreeUVLightCount(int freeUVLightCount) {
        LightCounter.freeUVLightCount = freeUVLightCount;
    }

    public static int getTotalNoUVLightCount() {
        return playerHeldItemLightCount + itemEntityLightCount + freeNoUVLightCount +
                (isChunkLightUseUv() ? 0 : blockLightCount);
    }

    public static int getBlockLightCount() {
        return blockLightCount;
    }

    public static void setBlockLightCount(int blockLightCount) {
        LightCounter.blockLightCount = blockLightCount;
    }

    public static int getTotalLightCount() {
        return getTotalNoUVLightCount() + getTotalUVLightCount();
    }

    public static void resetNoUvLightCount() {
        playerHeldItemLightCount = 0;
        itemEntityLightCount = 0;
        freeNoUVLightCount = 0;
        if (isChunkLightUseUv()) blockLightCount = 0;
    }

    public static boolean isChunkLightUseUv() {
        return Services.PLATFORM.useLightMap();
    }

    public static class Render {
        public static boolean enable = Services.PLATFORM.isDevelopmentEnvironment();

        public static void update(GuiGraphics guiGraphics) {
            if (!enable) return;
            RenderUtils.warpGLDebugLabel("shimmer_light_counter", () -> {
                var playerHeldItemLightCount = LightCounter.getPlayerHeldItemLightCount();
                var itemEntityLightCount = LightCounter.getItemEntityLightCount();
                var freeNoUVLightCount = LightCounter.getFreeNoUVLightCount();
                var freeUVLightCount = LightCounter.getFreeUVLightCount();
                var blockLightCount = LightCounter.getBlockLightCount();

                var totalNoUvLightCount = LightCounter.getTotalNoUVLightCount();
                var totalUvLightCount = LightCounter.getTotalUVLightCount();

                var totalLightCount = LightCounter.getTotalLightCount();

                var drawer = new Drawer(guiGraphics);

                drawer.anchor(50, 50)
                        .resetPos()
                        .defaultFont()
                        .enableShadow();

                var percent = totalLightCount * 100.0 / 2048;

                ChatFormatting color;
                if (percent < 10.0) {
                    color = ChatFormatting.BLUE;
                } else if (percent < 30.0) {
                    color = ChatFormatting.AQUA;
                } else if (percent < 50) {
                    color = ChatFormatting.GOLD;
                } else {
                    color = ChatFormatting.RED;
                }

                int fps = Minecraft.getInstance().getFps();
                var renderDistance = Minecraft.getInstance().options.renderDistance().get();

                drawer.appendLine("Fps:" + fps + " RenderDistance:" + renderDistance)
                        .appendLine("Total Light Count:" + totalLightCount)
                        .append(" Max:" + 2048 + " Percent:")
                        .append(Component.literal(String.format("%.2f%%", percent)).withStyle(color))
                        .newLine()
                        .newLine()
                        .appendLine("NO UV light:" + totalNoUvLightCount)
                        .appendLine("PlayerHeldItemLight:" + playerHeldItemLightCount)
                        .appendLine("ItemEntityLightCount:" + itemEntityLightCount)
                        .appendLine("FreeNoUVLightCount:" + freeNoUVLightCount)
                        .newLine()
                        .newLine()
                        .append("UV light: " + totalUvLightCount)
                        .appendLine("FreeUVLightCount" + freeUVLightCount)
                        .newLine()
                        .newLine()
                        .append("Chunk Light Count:" + blockLightCount).append(" ").append("use uv:").append(Boolean.toString(LightCounter.isChunkLightUseUv()));

                if (percent > 30.0) {
                    drawer.newLine().append(Component.literal("too many colored light will cause performance issue").withStyle(color));
                    if (fps < 30) {
                        drawer.appendLine(Component.literal("consider modify configuration not set colored light for common block or reduce view distance").withStyle(color));
                    }
                }

                drawer.flush();
            });
        }


    }
}
