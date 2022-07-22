package com.lowdragmc.shimmer.client;

import com.lowdragmc.shimmer.ShimmerConstants;
import com.lowdragmc.shimmer.client.shader.RenderUtils;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.RandomUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL43;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Aurrora {
    public static ShaderInstance auroraShader;
    private static final BufferBuilder bufferBuilder = new BufferBuilder(1024 * 100);
    private static final TextureTarget backendTexture = new TextureTarget(1, 1, false, Minecraft.ON_OSX);
    private static final List<Aurrora> aurroras = new ArrayList<>();
    private static final int MAXIMUM_AURRORA = 3;
    private static final float radiusUse = (float) (Math.PI * 2 / MAXIMUM_AURRORA);
    private static final float FADE_TIME = 1000 * 2;
    private static final Random RANDOM = new Random();
    private static long currentTime;
    private static long lastUpdateTime;
    private long beginTime;
    private long scheduledSurviveTime;
    double x1;
    double y1;
    double z1;
    double x2;
    double y2;
    double z2;

    private static void updateAurrora() {
        currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime < 1000) {
            return;
        }
        lastUpdateTime = currentTime;
        Iterator<Aurrora> iterator = aurroras.iterator();
        while (iterator.hasNext()) {
            Aurrora aurrora = iterator.next();
            if (currentTime - aurrora.beginTime >= aurrora.scheduledSurviveTime) {
                iterator.remove();
            }
        }
        while (aurroras.size() < MAXIMUM_AURRORA) {
            Aurrora aurrora = new Aurrora();
            aurrora.beginTime = currentTime;
            aurrora.scheduledSurviveTime = RANDOM.nextInt(1000 * 60, 1000 * 60 * 30);
            int x = RANDOM.nextInt(1,5);
            double beginAngle = RANDOM.nextDouble(radiusUse * x, radiusUse * (x+1));
            double beginDistance = RANDOM.nextFloat(50, 600);
            x+=RANDOM.nextInt(1,5);
            double endAngle = RANDOM.nextDouble(radiusUse * x, radiusUse * (x+1));
            double endDistance = RANDOM.nextFloat(50,600);
            aurrora.x1 = Math.cos(beginAngle) * beginDistance;
            aurrora.z1 = Math.sin(beginAngle) * beginDistance;
            aurrora.x2 = Math.cos(endAngle) * endDistance;
            aurrora.z2 = Math.sin(endAngle) * endDistance;
            aurrora.y1 = RANDOM.nextDouble(150, 200);
            aurrora.y2 = RANDOM.nextDouble(250, 300);
            aurroras.add(aurrora);
        }
    }

    private float getIntensity() {
        long surviveTime = currentTime - beginTime;
        if (surviveTime < FADE_TIME) {
            return surviveTime / FADE_TIME;
        }
        return Math.min((scheduledSurviveTime - surviveTime) / FADE_TIME, 1);
    }

    public static void render(PoseStack poseStack, ClientLevel level, Minecraft minecraft) {
        updateAurrora();
        GL43.glPushDebugGroup(GL43.GL_DEBUG_SOURCE_APPLICATION, 1, "render_sky");

        RenderTarget mainRenderTarget = minecraft.getMainRenderTarget();
        backendTexture.resize(mainRenderTarget.width, mainRenderTarget.height, Minecraft.ON_OSX);
        mainRenderTarget.bindRead();
        backendTexture.bindWrite(true);
        GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ZERO);
        RenderUtils.fastBlit(mainRenderTarget, backendTexture);
        mainRenderTarget.bindWrite(false);
        backendTexture.bindRead();

        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();

        Vec3 camPos = minecraft.gameRenderer.getMainCamera().getPosition();
        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
        Matrix4f pose = poseStack.last().pose();
        RenderSystem.setShader(() -> auroraShader);
        try {
            auroraShader.getUniform("modelViewMat").set(pose);
            auroraShader.setSampler("background", backendTexture);
            auroraShader.getUniform("iTime").set(level.getDayTime() / 10f);
            auroraShader.getUniform("screen").set((float) mainRenderTarget.width,(float) mainRenderTarget.height);
        } catch (NullPointerException e) {
            ShimmerConstants.LOGGER.error("failed to set uniform for aurora render:{}", e.getMessage());
        }
        for (var aurora : aurroras) {
            bufferBuilder.begin(VertexFormat.Mode.QUADS, ShimmerRenderTypes.AuroraRenderType.AuroraVertexFormat);
            aurora.renderAuroraToBuffer(camPos);
            bufferBuilder.end();
            BufferUploader.end(bufferBuilder);
        }
        poseStack.popPose();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        GL43.glPopDebugGroup();
    }

    private void renderAuroraToBuffer(Vec3 offset) {
        bufferBuilder.vertex(x1 + offset.x, y1, z1 + offset.z).uv(0f, 0f).endVertex();
        bufferBuilder.vertex(x2 + offset.x, y1, z2 + offset.z).uv(1f, 0f).endVertex();
        bufferBuilder.vertex(x2 + offset.x, y2, z2 + offset.z).uv(1f, 1f).endVertex();
        bufferBuilder.vertex(x1 + offset.x, y2, z1 + offset.z).uv(0f, 1f).endVertex();
        double ration = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(z1 - z2, 2)) / (y2 - y1);
        try {
            auroraShader.getUniform("Ratio").set((float) ration);
            auroraShader.getUniform("intensity").set(getIntensity());
        } catch (Exception e) {
            ShimmerConstants.LOGGER.error("failed to set uniform for aurora render:{}", e.getMessage());
        }
    }
}