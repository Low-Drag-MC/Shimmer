package com.lowdragmc.shimmer.fabric.compact.iris;

import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import org.apache.http.util.Asserts;
import org.lwjgl.opengl.GL30C;

import java.util.HashMap;
import java.util.Objects;

public class IrisFrameBufferWrapper extends GlFramebuffer {

    private static final HashMap<GlFramebuffer, IrisFrameBufferWrapper> map = new HashMap<>();

    public static IrisFrameBufferWrapper warp(GlFramebuffer origin, int proxyTextureId) {
        var warp = new IrisFrameBufferWrapper();
        if (origin.hasDepthAttachment()) {
            var depthColorTextureId = ((IGLFrameBuffer) origin).shimmer$getDepthTexture();
            Asserts.check(depthColorTextureId > 0, "depthColorTextureId should > 0");
            warp.addDepthAttachment(depthColorTextureId);
        }
        ((IGLFrameBuffer) origin).shimmer$getColorAttachmentMap().forEach((index, textureID) -> {
            if (index != 0) {
                warp.addColorAttachment(index, textureID);
            } else {
                warp.addColorAttachment(0, proxyTextureId);
            }
        });

        var readBuffer = ((IGLFrameBuffer) origin).shimmer$getReadBuffer();
        Asserts.check(readBuffer >= 0, "read buffer should > 0");
        warp.readBuffer(readBuffer);

        var drawBuffers = ((IGLFrameBuffer) origin).shimmer$getDrawBuffers();
        Objects.requireNonNull(drawBuffers, "drawBuffers shouldn't be null");
        warp.drawBuffers(drawBuffers);

        int status = warp.getStatus();
        if (status != GL30C.GL_FRAMEBUFFER_COMPLETE) {
            throw new IllegalStateException("Unexpected error while creating framebuffer: Draw buffers Status: " + status);
        }

        return warp;
    }

    public static IrisFrameBufferWrapper from(GlFramebuffer origin, int proxyTextureId) {
        return map.computeIfAbsent(origin, f -> warp(f, proxyTextureId));
    }

    public static void clear() {
        map.forEach(($,buffer) -> buffer.destroy());
        map.clear();
    }
}
