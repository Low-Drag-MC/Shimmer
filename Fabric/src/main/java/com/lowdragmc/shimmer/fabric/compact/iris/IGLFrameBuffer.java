package com.lowdragmc.shimmer.fabric.compact.iris;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import org.jetbrains.annotations.Nullable;

public interface IGLFrameBuffer {
    int shimmer$getDepthTexture();

    Int2IntMap shimmer$getColorAttachmentMap();

    int shimmer$getReadBuffer();

    int @Nullable [] shimmer$getDrawBuffers();
}
