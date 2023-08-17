package com.lowdragmc.shimmer.client.light;

import com.lowdragmc.shimmer.client.shader.ShaderUBO;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.util.profiling.ProfilerFiller;
import org.lwjgl.opengl.GL46;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

public class ColoredLightTracer {

    private static int uvCount = 0;
    private static int noUvCount = 0;
    private static final FloatBuffer dataBuffer = MemoryUtil.memAllocFloat(2048 * ColorPointLight.STRUCT_SIZE);
    private static int count = 0;
    private static final float OFFSET = 0.6f;
    public static int updateFrequent = -1;
    private static boolean needUpdate = true;


    public static void render(ProfilerFiller profiler, Camera camera, PoseStack poseStack) {
        if (updateFrequent == -1) return;
        profiler.push("render_colored_light");

        var tesselator = Tesselator.getInstance();
        var builder = tesselator.getBuilder();
        var camPos = camera.getPosition();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        builder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

        dataBuffer.position(0);
        for (int i = 0; i < noUvCount + uvCount; i++) {
            float r = dataBuffer.get();
            float g = dataBuffer.get();
            float b = dataBuffer.get();
            float a = dataBuffer.get();
            float x = dataBuffer.get();
            float y = dataBuffer.get();
            float z = dataBuffer.get();
            float radius = dataBuffer.get();
            LevelRenderer.addChainedFilledBoxVertices(poseStack, builder,
                    x - OFFSET, y - OFFSET, z - OFFSET, x + OFFSET, y + OFFSET, z + OFFSET,
                    1.0f, 1.0f, 1.0f, 0.3f);
        }
        tesselator.end();

        poseStack.popPose();

        profiler.pop();
    }

    public static void refreshData(ShaderUBO lightUBO, int uvCount, int noUVCount) {
        if (needUpdate || updateFrequent == 0) {
            Minecraft.getInstance().getProfiler().push("refresh_colored_light_data");
            lightUBO.bindBuffer();
            dataBuffer.position(0);
            dataBuffer.limit((uvCount + noUVCount) * ColorPointLight.STRUCT_SIZE);
            GL46.glGetBufferSubData(GL46.GL_UNIFORM_BUFFER, 0L, dataBuffer);
            lightUBO.unBindBuffer();

            ColoredLightTracer.uvCount = uvCount;
            ColoredLightTracer.noUvCount = noUVCount;

            needUpdate = false;
            Minecraft.getInstance().getProfiler().pop();
        }
    }

    public static void tryRefreshNeedUpdate() {
        if (updateFrequent == -1) return;
        count++;
        if (count >= updateFrequent) {
            needUpdate = true;
            count = 0;
        }
    }


}
