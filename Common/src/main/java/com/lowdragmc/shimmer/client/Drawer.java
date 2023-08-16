package com.lowdragmc.shimmer.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

@SuppressWarnings("unused")
public class Drawer {
    private final GuiGraphics guiGraphics;

    private int anchorX, anchorY;
    private int currentX, currentY;
    private int color = Integer.MAX_VALUE;
    private Font font = Minecraft.getInstance().font;
    private int lineHeight = font.lineHeight + 2;
    private boolean useShadow = true;
    private int lastLineWidth = -1;
    private int blankLineCount = 0;
    private int contentLineCount = 0;

    private static final int ORTH_LINE_HEIGHT = 1;

    public Drawer(GuiGraphics guiGraphics) {
        this.guiGraphics = guiGraphics;
    }

    public Drawer anchor(int x, int y) {
        this.anchorX = x;
        this.anchorY = y;
        return this;
    }

    public Drawer lineHeight(int lineHeight) {
        this.lineHeight = lineHeight;
        return this;
    }

    public Drawer lineSpan(int lineSpan) {
        this.lineHeight = font.lineHeight + lineHeight;
        return this;
    }

    public Drawer newLine() {
        if (currentX != anchorX) {
            blankLineCount++;
            lastLineWidth = currentY - anchorX;
        } else {
            contentLineCount++;
        }
        this.currentX = anchorX;
        this.currentY += lineHeight;
        return this;
    }

    public Drawer color(int color) {
        this.color = color;
        return this;
    }

    public Drawer resetPos() {
        this.currentX = anchorX;
        this.currentY = anchorY;
        return this;
    }

    public Drawer setFont(Font font) {
        this.font = font;
        return this;
    }

    public Drawer defaultFont() {
        this.font = Minecraft.getInstance().font;
        return this;
    }

    public Drawer enableShadow() {
        this.useShadow = true;
        return this;
    }

    public Drawer disableShadow() {
        this.useShadow = false;
        return this;
    }

    public Drawer append(String str) {
        guiGraphics.drawString(font, str, currentX, currentY, color, useShadow);
        currentX += font.width(str);
        return this;
    }

    public Drawer append(Component str) {
        guiGraphics.drawString(font, str, currentX, currentY, color, useShadow);
        currentX += font.width(str);
        return this;
    }

    public Drawer append(FormattedCharSequence str) {
        guiGraphics.drawString(font, str, currentX, currentY, color, useShadow);
        currentX += font.width(str);
        return this;
    }

    public Drawer appendLine(String str) {
        nextLineIfNecessary();
        guiGraphics.drawString(font, str, currentX, currentY, color, useShadow);
        currentX += font.width(str);
        return this;
    }

    public Drawer appendLine(Component str) {
        nextLineIfNecessary();
        guiGraphics.drawString(font, str, currentX, currentY, color, useShadow);
        currentX += font.width(str);
        return this;
    }

    public Drawer appendLine(FormattedCharSequence str) {
        nextLineIfNecessary();
        guiGraphics.drawString(font, str, currentX, currentY, color, useShadow);
        currentX += font.width(str);
        return this;
    }

    private void nextLineIfNecessary() {
        if (currentX != anchorX) newLine();
    }

    public Drawer horizontalLine(int fromOffset, int length) {
        guiGraphics.hLine(currentX + fromOffset, currentX + fromOffset + length, currentY, color);
        currentY += ORTH_LINE_HEIGHT;
        return this;
    }

    public Drawer horizontalLineForLast() {
        if (lastLineWidth > 0) {
            horizontalLine(0, lastLineWidth);
        }
        return this;
    }

    public Drawer moveY(int offset) {
        this.currentX += offset;
        return this;
    }

    public Drawer flush() {
        guiGraphics.flush();
        return this;
    }

}