package com.lowdragmc.shimmer.client.light;

import com.mojang.math.Vector3f;
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

    protected ColorPointLight(LightManager lightManager, Vector3f pos, int color, float radius, int offset) {
        this.lightManager = lightManager;
        x = pos.x();
        y = pos.y();
        z = pos.z();
        a = (((color >> 24) & 0xff) / 255f);
        r = (((color >> 16) & 0xff) / 255f);
        g = (((color >> 8) & 0xff) / 255f);
        b = (((color) & 0xff) / 255f);
        this.radius = radius;
        this.offset = offset;
    }

    protected float[] getData() {
        return new float[]{r,g,b,a,x,y,z,radius};
    }

    protected void uploadBuffer(FloatBuffer buffer) {
        buffer.put(getData());
    }

}
