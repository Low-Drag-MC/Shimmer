package com.lowdragmc.shimmer.client.light;

import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.nio.FloatBuffer;

/**
 * @author KilaBash
 * @date 2022/05/04
 * @implNote ColorPointLight
 */
@OnlyIn(Dist.CLIENT)
public class ColorPointLight {
    public static final int STRUCT_SIZE = (4 + 3 + 1);
    float r, g, b, a;
    float x, y, z;
    float radius;
    LightManager lightManager;
    int offset;

    protected ColorPointLight(BlockPos pos, int color, float radius) {
        this(null, new Vector3f(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f), color, radius, 0);
    }

    protected ColorPointLight(LightManager lightManager, Vector3f pos, int color, float radius, int offset) {
        this.lightManager = lightManager;
        x = pos.x();
        y = pos.y();
        z = pos.z();
        setColor(color);
        this.radius = radius;
        this.offset = offset;
    }

    public void setColor(int color) {
        a = (((color >> 24) & 0xff) / 255f);
        r = (((color >> 16) & 0xff) / 255f);
        g = (((color >> 8) & 0xff) / 255f);
        b = (((color) & 0xff) / 255f);
    }

    public void setColor(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public void setPos() {

    }

    public boolean isRemoved() {
        return lightManager == null;
    }

    public void remove() {
        if (lightManager != null) {
            lightManager.removeLight(this);
            lightManager = null;
        }
    }

    public void update() {
        Minecraft.getInstance().execute(() -> lightManager.lightUBO.bufferSubData(offset, getData()));
    }

    protected float[] getData() {
        return new float[]{r,g,b,a,x,y,z,radius};
    }

    protected void uploadBuffer(FloatBuffer buffer) {
        buffer.put(getData());
    }

}
