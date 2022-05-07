package com.lowdragmc.shimmer.client.light;

import com.google.common.collect.Maps;
import com.lowdragmc.shimmer.client.shader.ShaderInjection;
import com.lowdragmc.shimmer.client.shader.ShaderUBO;
import com.lowdragmc.shimmer.core.IRenderChunk;
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
import java.util.Map;

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
    
    private static String ChunkInjection(String s) {
        s = s.replace("void main()", "#moj_import <shimmer.glsl>\n\nvoid main()");
        return new StringBuffer(s).insert(s.lastIndexOf('}'), "vertexColor = color_light(pos, vertexColor);\n").toString();
    }

    private static String ArmorInjection(String s) {
        //TODO fix armor lighting. what the hell!!!!!
        s = s.replace("void main()", "#moj_import <shimmer.glsl>\n\nvoid main()");
        return new StringBuffer(s).insert(s.lastIndexOf('}'), "vertexColor = color_light(Position, vertexColor);\n").toString();
    }

    private static String EntityInjectionLightMapColor(String s) {
        s = s.replace("void main()", "#moj_import <shimmer.glsl>\n\nvoid main()");
        return new StringBuffer(s).insert(s.lastIndexOf('}'), "lightMapColor = color_light(IViewRotMat * Position, lightMapColor);\n").toString();
    }

    private static String EntityInjectionVertexColor(String s) {
        s = s.replace("void main()", "#moj_import <shimmer.glsl>\n\nvoid main()");
        return new StringBuffer(s).insert(s.lastIndexOf('}'), "vertexColor = color_light(IViewRotMat * Position, vertexColor);\n").toString();
    }

    public static void injectShaders() {

        ShaderInjection.registerVSHInjection("rendertype_solid", LightManager::ChunkInjection);
        ShaderInjection.registerVSHInjection("rendertype_cutout", LightManager::ChunkInjection);
        ShaderInjection.registerVSHInjection("rendertype_cutout_mipped", LightManager::ChunkInjection);
        ShaderInjection.registerVSHInjection("rendertype_translucent", LightManager::ChunkInjection);
        ShaderInjection.registerVSHInjection("rendertype_armor_cutout_no_cull", LightManager::ArmorInjection);
        ShaderInjection.registerVSHInjection("rendertype_entity_cutout", LightManager::EntityInjectionLightMapColor);
        ShaderInjection.registerVSHInjection("rendertype_entity_cutout_no_cull", LightManager::EntityInjectionLightMapColor);
        ShaderInjection.registerVSHInjection("rendertype_entity_cutout_no_cull_z_offset", LightManager::EntityInjectionLightMapColor);
        ShaderInjection.registerVSHInjection("rendertype_entity_decal", LightManager::EntityInjectionVertexColor);
        ShaderInjection.registerVSHInjection("rendertype_entity_no_outline", LightManager::EntityInjectionVertexColor);
        ShaderInjection.registerVSHInjection("rendertype_entity_shadow", LightManager::EntityInjectionVertexColor);
        ShaderInjection.registerVSHInjection("rendertype_entity_smooth_cutout", LightManager::EntityInjectionLightMapColor);
        ShaderInjection.registerVSHInjection("rendertype_entity_solid", LightManager::EntityInjectionLightMapColor);
        ShaderInjection.registerVSHInjection("rendertype_entity_translucent", LightManager::EntityInjectionLightMapColor);
        ShaderInjection.registerVSHInjection("rendertype_entity_translucent_cull", LightManager::EntityInjectionVertexColor);
    }

    public void renderLevelPre(ObjectArrayList<LevelRenderer.RenderChunkInfo> renderChunksInFrustum, float camX, float camY, float camZ) {
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

    public void renderLevelPost() {
        envUBO.bufferSubData(0, new int[]{0});
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



// *********************** block light *********************** //


    private final Map<Block, ColorPointLight> BLOCK_MAP = Maps.newHashMap();
    private final Map<BlockState, ColorPointLight> STATE_MAP = Maps.newHashMap();

    public boolean isBlockHasLight(BlockState blockState) {
        return STATE_MAP.containsKey(blockState) || BLOCK_MAP.containsKey(blockState.getBlock());
    }

    public ColorPointLight getBlockLight(BlockPos blockpos, BlockState blockstate) {
        ColorPointLight template = STATE_MAP.get(blockstate);
        if (template == null) {
            template = BLOCK_MAP.get(blockstate.getBlock());
        }
        return new ColorPointLight(blockpos, template);
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

    public void registerBlockLight(Block block, int color, int radius) {
        BLOCK_MAP.put(block, new ColorPointLight(color, radius));
    }

    public void registerBlockStateLight(BlockState blockState, int color, int radius) {
        STATE_MAP.put(blockState, new ColorPointLight(color, radius));
    }
}
