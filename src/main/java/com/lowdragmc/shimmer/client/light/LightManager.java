package com.lowdragmc.shimmer.client.light;

import com.lowdragmc.shimmer.client.shader.ShaderUBO;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.compress.utils.Lists;
import org.lwjgl.BufferUtils;

import javax.annotation.Nonnull;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.Optional;

/**
 * @author KilaBash
 * @date 2022/05/04
 * @implNote LightManager
 */
@OnlyIn(Dist.CLIENT)
public enum LightManager {
    INSTANCE;
    private final List<ColorPointLight> lights = Lists.newArrayList();
    private final FloatBuffer buffer = BufferUtils.createFloatBuffer(2048 * ColorPointLight.STRUCT_SIZE);
    private ShaderUBO shaderUBO;

    public void setupUniform(double camX, double camY, double camZ) {
        ShaderInstance shader = RenderSystem.getShader();
        if (shader != null) {
            Optional.ofNullable(shader.getUniform("LightCount")).ifPresent(uniform -> uniform.set(lights.size()));
            Optional.ofNullable(shader.getUniform("CamPos")).ifPresent(uniform -> uniform.set((float)camX, (float)camY, (float)camZ));
        }
    }

    public void init() {
        if (shaderUBO == null) {
            int size = getOffset(2048);
            // create ubo
            shaderUBO = new ShaderUBO();
            shaderUBO.createBufferData(size);
            shaderUBO.blockBinding(0);
        }
        GameRenderer gameRenderer = Minecraft.getInstance().gameRenderer;
        ShaderInstance bloom = gameRenderer.getShader("shimmer:rendertype_bloom");
        if (bloom != null) {
            shaderUBO.bindToShader(bloom.getId(), "Lights");
        }
    }

    public ColorPointLight addLight(Vector3f pos, int color, float radius) {
        lights.clear();
        ColorPointLight light = new ColorPointLight(this, pos, color, radius, getOffset(lights.size()));
        lights.add(light);
        shaderUBO.bufferSubData(light.offset, light.getData());
        return light;
    }

    public int getOffset(int index) {
        return (index * ColorPointLight.STRUCT_SIZE) << 2;
    }

}
