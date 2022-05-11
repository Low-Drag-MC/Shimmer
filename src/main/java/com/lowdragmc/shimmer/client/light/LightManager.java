package com.lowdragmc.shimmer.client.light;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lowdragmc.shimmer.Configuration;
import com.lowdragmc.shimmer.client.shader.ShaderInjection;
import com.lowdragmc.shimmer.client.shader.ShaderUBO;
import com.lowdragmc.shimmer.core.IRenderChunk;
import com.mojang.math.Vector3f;
import com.mojang.realmsclient.util.JsonUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.compress.utils.Lists;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;

import javax.annotation.Nullable;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author KilaBash
 * @date 2022/05/04
 * @implNote LightManager
 */
@OnlyIn(Dist.CLIENT)
public enum LightManager {
    INSTANCE;
    private final List<ColorPointLight> lights = new ArrayList<>(2048);
    private final FloatBuffer buffer = BufferUtils.createFloatBuffer(2048 * ColorPointLight.STRUCT_SIZE);
    ShaderUBO lightUBO;
    ShaderUBO envUBO;
    
    private static String ChunkInjection(String s) {
        s = s.replace("void main()", "#moj_import <shimmer.glsl>\n\nvoid main()");
        return new StringBuffer(s).insert(s.lastIndexOf('}'), "vertexColor = color_light(pos, vertexColor);\n").toString();
    }

    private static String PositionInjection(String s) {
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

        ShaderInjection.registerVSHInjection("particle", LightManager::PositionInjection);
        ShaderInjection.registerVSHInjection("rendertype_solid", LightManager::ChunkInjection);
        ShaderInjection.registerVSHInjection("rendertype_cutout", LightManager::ChunkInjection);
        ShaderInjection.registerVSHInjection("rendertype_cutout_mipped", LightManager::ChunkInjection);
        ShaderInjection.registerVSHInjection("rendertype_translucent", LightManager::ChunkInjection);
        ShaderInjection.registerVSHInjection("rendertype_armor_cutout_no_cull", LightManager::PositionInjection);
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

    public static void clear() {
        for (ColorPointLight light : INSTANCE.lights) {
            light.lightManager = null;
        }
        INSTANCE.lights.clear();
    }

    public void renderLevelPre(ObjectArrayList<LevelRenderer.RenderChunkInfo> renderChunksInFrustum, float camX, float camY, float camZ) {
        int blockLightSize = 0;
        int left = 2048 - lights.size();
        buffer.clear();
        for (LevelRenderer.RenderChunkInfo chunkInfo : renderChunksInFrustum) {
            if (left <= blockLightSize) {
                break;
            }
            if (chunkInfo.chunk instanceof IRenderChunk) {
                for (ColorPointLight shimmerLight : ((IRenderChunk) chunkInfo.chunk).getShimmerLights()) {
                    if (left <= blockLightSize) {
                        break;
                    }
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
        bindProgram("particle");
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

    /**
     * Create and add a new PointLight. Have to maintain instances yourself.
     * @param pos position
     * @param color colored
     * @param radius radius
     * @return instance created. null -- if no more available space.
     */
    @Nullable
    public ColorPointLight addLight(Vector3f pos, int color, float radius) {
        if (lights.size() == 2048) return null;
        ColorPointLight light = new ColorPointLight(this, pos, color, radius, getOffset(lights.size()));
        lights.add(light);
        lightUBO.bufferSubData(light.offset, light.getData());
        return light;
    }

    private int getOffset(int index) {
        return (index * ColorPointLight.STRUCT_SIZE) << 2;
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


// *********************** block light *********************** //

    private final Map<Block, Function<BlockState, ColorPointLight.Template>> BLOCK_MAP = Maps.newHashMap();

    public boolean isBlockHasLight(Block block) {
        return BLOCK_MAP.containsKey(block);
    }

    @Nullable
    public ColorPointLight getBlockStateLight(BlockPos blockpos, BlockState blockstate) {
        ColorPointLight.Template
                template = BLOCK_MAP.getOrDefault(blockstate.getBlock(), s -> null).apply(blockstate);
        return template == null ? null : new ColorPointLight(blockpos, template);
    }

    /**
     * register colored light for a block.
     * @param block block
     * @param supplier light supplier from a BlockState
     */
    public void registerBlockLight(Block block, Function<BlockState, ColorPointLight.Template> supplier) {
        BLOCK_MAP.put(block, supplier);
    }

    public void registerBlockLight(Block block, int color, float radius) {
        ColorPointLight.Template template = new ColorPointLight.Template(radius, color);
        registerBlockLight(block, state -> template);
    }

    public void loadConfig() {
        JsonElement jsonElement = Configuration.config.get("LightBlock");
        if (jsonElement.isJsonArray()) {
            JsonArray blocks = jsonElement.getAsJsonArray();
            for (JsonElement block : blocks) {
                JsonObject jsonObj = block.getAsJsonObject();
                if (jsonObj.has("block")) {
                    Block bb = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(jsonObj.get("block").getAsString()));
                    int a = JsonUtils.getIntOr("a", jsonObj, 0);
                    int r = JsonUtils.getIntOr("r", jsonObj, 0);
                    int g = JsonUtils.getIntOr("g", jsonObj, 0);
                    int b = JsonUtils.getIntOr("b", jsonObj, 0);
                    if (bb != null) {
                        registerBlockLight(bb, (a << 24) | (r << 16) | (g << 8) | b, jsonObj.get("radius").getAsFloat());
                    }
                }
            }
        }
    }
}
