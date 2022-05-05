package com.lowdragmc.shimmer.client.light;

import com.lowdragmc.shimmer.client.shader.ShaderInjection;
import com.lowdragmc.shimmer.client.shader.ShaderUBO;
import com.lowdragmc.shimmer.core.IRenderChunk;
import com.lowdragmc.shimmer.test.ColoredFireBlock;
import com.mojang.math.Vector3f;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.compress.utils.Lists;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;
import java.util.List;
import java.util.function.Function;

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
    ShaderUBO lightUBO;
    ShaderUBO envUBO;

    public static void injectShaders() {
        Function<String, String> chunkInjection = s -> s
                .replace("void main()", "#moj_import <shimmer.glsl>\n\nvoid main()")
                .replace("texCoord0 = UV0;", "texCoord0 = UV0;\nvertexColor = color_light(pos, vertexColor);\n");

        Function<String, String> entityInjection1 = s -> s
                .replace("void main()", "#moj_import <shimmer.glsl>\n\nvoid main()")
                .replace("texCoord0 = UV0;", "texCoord0 = UV0;\nvertexColor = color_light(Position, vertexColor);\n");

        Function<String, String> entityInjection2 = s -> s
                .replace("void main()", "#moj_import <shimmer.glsl>\n\nvoid main()")
                .replace("texCoord0 = UV0;", "texCoord0 = UV0;\nvertexColor = color_light(IViewRotMat * Position, vertexColor);\n");

        ShaderInjection.registerVSHInjection("rendertype_solid", chunkInjection);
        ShaderInjection.registerVSHInjection("rendertype_cutout", chunkInjection);
        ShaderInjection.registerVSHInjection("rendertype_cutout_mipped", chunkInjection);
        ShaderInjection.registerVSHInjection("rendertype_translucent", chunkInjection);
        ShaderInjection.registerVSHInjection("rendertype_armor_cutout_no_cull", entityInjection1);
        ShaderInjection.registerVSHInjection("rendertype_entity_alpha", entityInjection1);
        ShaderInjection.registerVSHInjection("rendertype_entity_cutout", entityInjection2);
        ShaderInjection.registerVSHInjection("rendertype_entity_cutout_no_cull", entityInjection2);
        ShaderInjection.registerVSHInjection("rendertype_entity_cutout_no_cull_z_offset", entityInjection2);
        ShaderInjection.registerVSHInjection("rendertype_entity_decal", entityInjection2);
        ShaderInjection.registerVSHInjection("rendertype_entity_no_outline", entityInjection2);
        ShaderInjection.registerVSHInjection("rendertype_entity_shadow", entityInjection2);
        ShaderInjection.registerVSHInjection("rendertype_entity_smooth_cutout", entityInjection2);
        ShaderInjection.registerVSHInjection("rendertype_entity_solid", entityInjection2);
        ShaderInjection.registerVSHInjection("rendertype_entity_translucent", entityInjection2);
        ShaderInjection.registerVSHInjection("rendertype_entity_translucent_cull", entityInjection2);
    }

    public void setupChunkLights(ObjectArrayList<LevelRenderer.RenderChunkInfo> renderChunksInFrustum, float camX, float camY, float camZ) {
        int blockLightSize = 0;
        buffer.clear();
        for (LevelRenderer.RenderChunkInfo chunkInfo : renderChunksInFrustum) {
            if (chunkInfo.chunk instanceof IRenderChunk) {
                for (ColorPointLight shimmerLight : ((IRenderChunk) chunkInfo.chunk).getShimmerLights()) {
                    shimmerLight.uploadBuffer(buffer);
                    blockLightSize++;
                }
            }
        }
        buffer.flip();
        if (blockLightSize > 0) {
            lightUBO.bufferSubData(getOffset(lights.size()), buffer);
        }

        envUBO.bufferSubData(0, new int[]{lights.size() + blockLightSize});
        envUBO.bufferSubData(16, new float[]{camX, camY, camZ});
    }

    public void reloadShaders() {
        if (lightUBO == null) {
            int size = getOffset(2048);
            // create ubo
            lightUBO = new ShaderUBO();
            lightUBO.createBufferData(size, GL30.GL_STREAM_DRAW); // stream -- modified each frame
            lightUBO.blockBinding(0);

            envUBO = new ShaderUBO();
            envUBO.createBufferData(32, GL30.GL_STREAM_DRAW); // stream -- modified each frame
            envUBO.blockBinding(1);
        }
        bindProgram("shimmer:rendertype_bloom");
        bindProgram("rendertype_solid");
        bindProgram("rendertype_cutout");
        bindProgram("rendertype_cutout_mipped");
        bindProgram("rendertype_translucent");
        bindProgram("rendertype_armor_cutout_no_cull");
        bindProgram("rendertype_entity_alpha");
        bindProgram("rendertype_entity_cutout");
        bindProgram("rendertype_entity_cutout_no_cull");
        bindProgram("rendertype_entity_cutout_no_cull_z_offset");
        bindProgram("rendertype_entity_decal");
        bindProgram("rendertype_entity_no_outline");
        bindProgram("rendertype_entity_shadow");
        bindProgram("rendertype_entity_smooth_cutout");
        bindProgram("rendertype_entity_solid");
        bindProgram("rendertype_entity_translucent");
        bindProgram("rendertype_entity_translucent_cull");
    }

    private void bindProgram(String shaderName) {
        GameRenderer gameRenderer = Minecraft.getInstance().gameRenderer;
        ShaderInstance instance = gameRenderer.getShader(shaderName);
        if (instance != null) {
            lightUBO.bindToShader(instance.getId(), "Lights");
            envUBO.bindToShader(instance.getId(), "Env");
        }
    }

    public ColorPointLight addLight(Vector3f pos, int color, float radius) {
        ColorPointLight light = new ColorPointLight(this, pos, color, radius, getOffset(lights.size()));
        lights.add(light);
        lightUBO.bufferSubData(light.offset, light.getData());
        return light;
    }

    public int getOffset(int index) {
        return (index * ColorPointLight.STRUCT_SIZE) << 2;
    }

    public boolean isBlockHasLight(BlockState blockState) {
        Block block = blockState.getBlock();
        return block == Blocks.REDSTONE_BLOCK || block == Blocks.SLIME_BLOCK || block == Blocks.SPONGE || block == Blocks.SEA_LANTERN || block instanceof ColoredFireBlock;
    }

    public ColorPointLight getBlockLight(BlockPos blockpos, BlockState blockstate) {
        Block block = blockstate.getBlock();
        int color = 0xffffffff;
        if (block instanceof ColoredFireBlock) {
            color = ((ColoredFireBlock) block).color;
        } else if (block == Blocks.REDSTONE_BLOCK) {
            color = 0xffff0000;
        } else if (block == Blocks.SLIME_BLOCK) {
            color = 0xff00ff00;
        } else if (block == Blocks.SPONGE) {
            color = 0xffffff00;
        } else if (block == Blocks.SEA_LANTERN) {
            color = 0xff00ffff;
        }
        return new ColorPointLight(blockpos, color, 5);
    }

    void removeLight(ColorPointLight removed) {
        int index = lights.indexOf(removed);
        if (index >= 0) {
            for (int i = index + 1; i < lights.size(); i++) {
                lights.get(i).offset = getOffset(i - 1);
            }
            lights.remove(index);
            if (index < lights.size()) {
                Minecraft.getInstance().execute(() -> {
                    buffer.clear();
                    for (int i = index; i < lights.size(); i++) {
                        lights.get(i).uploadBuffer(buffer);
                    }
                    buffer.flip();
                    lightUBO.bufferSubData(getOffset(index), buffer);
                });
            }
        }
    }
}
