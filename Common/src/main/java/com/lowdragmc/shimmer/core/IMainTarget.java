package com.lowdragmc.shimmer.core;

/**
 * @author KilaBash
 * @date 2022/05/31
 * @implNote IMainTarget
 */
public interface IMainTarget {
    int getColorBloomTextureId();
    void clearBloomTexture(boolean error);
    void destroyBloomTextureBuffers();
    void setBloomFilterMode(int pFilterMode);
    void createBuffersHeads(int pWidth, int pHeight, boolean pClearError);
    void createBuffersTail(int pWidth, int pHeight, boolean pClearError);
}
