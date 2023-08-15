package com.lowdragmc.shimmer.fabric.core.mixins.iris;

import com.lowdragmc.shimmer.fabric.compact.iris.IGLFrameBuffer;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(value = GlFramebuffer.class, remap = false)
public class GlFrameBufferMixin implements IGLFrameBuffer {

    @Shadow(remap = false)
    @Final
    private Int2IntMap attachments;
    @Unique
    int depthTexture = -1;
    @Unique
    int readBuffer = -1;
    @Unique
    int[] drawBuffers = null;

    @Inject(method = "addDepthAttachment", at = @At("TAIL"))
    private void recordDepthTexture(int texture, CallbackInfo ci) {
        this.depthTexture = texture;
    }

    @Inject(method = "readBuffer", at = @At("TAIL"))
    private void recordReadBuffers(int readBuffer, CallbackInfo ci) {
        this.readBuffer = readBuffer;
    }

    @Inject(method = "drawBuffers", at = @At("TAIL"))
    private void recordDrawBuffers(int[] buffers, CallbackInfo ci) {
        drawBuffers = Arrays.copyOf(buffers, buffers.length);
    }

    @Override
    public int shimmer$getDepthTexture() {
        return depthTexture;
    }

    @Override
    public Int2IntMap shimmer$getColorAttachmentMap() {
        return attachments;
    }

    @Override
    public int shimmer$getReadBuffer() {
        return readBuffer;
    }

    @Override
    public int @Nullable [] shimmer$getDrawBuffers() {
        return drawBuffers;
    }
}
