package com.lowdragmc.shimmer.client.light;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.nio.FloatBuffer;

/**
 * @author KilaBash
 * @date 2022/05/04
 * @implNote ColorPointLight
 */
public class ColorPointLight {
    public static final int STRUCT_SIZE = (4 + 3 + 1);
    public float r, g, b, a;
    public float x, y, z;
    public float radius;
    LightManager lightManager;
    int offset;
    /**
     * only used for player dynamic light
     */
    public boolean enable = true;
    /**
     * only use for block light
     */
    final boolean uv;

    protected ColorPointLight(Vec3 pos , Template template, boolean uv) {
        a = template.a;
        r = template.r;
        g = template.g;
        b = template.b;
        radius = template.radius;
        x = (float) (pos.x() + 0.5f);
        y = (float) (pos.y() + 0.5f);
        z = (float) (pos.z() + 0.5f);
        this.uv = uv;
    }

    protected ColorPointLight(BlockPos pos , Template template, boolean uv) {
        a = template.a;
        r = template.r;
        g = template.g;
        b = template.b;
        radius = template.radius;
        x = pos.getX() + 0.5f;
        y = pos.getY() + 0.5f;
        z = pos.getZ() + 0.5f;
        this.uv = uv;
    }

    protected ColorPointLight(LightManager lightManager, Vector3f pos, int color, float radius, int offset, boolean uv) {
        x = pos.x();
        y = pos.y();
        z = pos.z();
        setColor(color);
        this.lightManager = lightManager;
        this.radius = radius;
        this.offset = offset;
        this.uv = uv;
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

    public void setPos(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
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
        if (lightManager != null && offset >= 0 && uv) {
            Minecraft.getInstance().execute(() -> lightManager.lightUBO.bufferSubData(offset, getData()));
        }
    }

    protected float[] getData() {
        return new float[]{r,g,b,a,x,y,z,radius};
    }

    public void uploadBuffer(FloatBuffer buffer) {
        buffer.put(getData());
    }

    public static class Template {
        public float r, g, b, a;
        public float radius;

        public Template(float radius, int color) {
            setColor(color);
            this.radius = radius;
        }

        public Template(float radius, float r, float g, float b, float a) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
            this.radius = radius;
        }

        public void setColor(int color) {
            a = (((color >> 24) & 0xff) / 255f);
            r = (((color >> 16) & 0xff) / 255f);
            g = (((color >> 8) & 0xff) / 255f);
            b = (((color) & 0xff) / 255f);
        }
    }

}
