package com.lowdragmc.shimmer.client.light;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lowdragmc.shimmer.Configuration;
import com.lowdragmc.shimmer.FileUtility;
import com.lowdragmc.shimmer.ShimmerConstants;
import com.lowdragmc.shimmer.client.shader.ShaderInjection;
import com.lowdragmc.shimmer.client.shader.ShaderUBO;
import com.lowdragmc.shimmer.platform.Services;
import com.mojang.math.Vector3f;
import com.mojang.realmsclient.util.JsonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * @author KilaBash
 * @date 2022/05/04
 * @implNote LightManager
 */
public enum LightManager {
    INSTANCE;
    private final List<ColorPointLight> lights = new ArrayList<>(2048);
    private final FloatBuffer buffer = BufferUtils.createFloatBuffer(2048 * ColorPointLight.STRUCT_SIZE);
    ShaderUBO lightUBO;
    ShaderUBO envUBO;

    private static String ChunkInjection(String s) {
        s = s.replace("void main()", "#moj_import <shimmer.glsl>\n\nvoid main()");
        return new StringBuffer(s).insert(s.lastIndexOf('}'), "vertexColor = color_light_uv(pos, vertexColor,UV2);\n").toString();
    }

    private static String PositionInjection(String s) {
        //TODO fix armor lighting. what the hell!!!!!
        s = s.replace("void main()", "#moj_import <shimmer.glsl>\n\nvoid main()");
        return new StringBuffer(s).insert(s.lastIndexOf('}'), "vertexColor = color_light_uv(Position, vertexColor,UV2);\n").toString();
    }

    private static String EntityInjectionLightMapColor(String s) {
        s = s.replace("void main()", "#moj_import <shimmer.glsl>\n\nvoid main()");
        return new StringBuffer(s).insert(s.lastIndexOf('}'), "lightMapColor = color_light(IViewRotMat * Position, lightMapColor);\n").toString();
    }

    private static String EntityInjectionVertexColor(String s) {
        s = s.replace("void main()", "#moj_import <shimmer.glsl>\n\nvoid main()");
        return new StringBuffer(s).insert(s.lastIndexOf('}'), "vertexColor = color_light(IViewRotMat * Position, vertexColor);\n").toString();
    }

    private static String lightShader;

    private static String getLightShader() {
        if (lightShader == null) {
            try {
                lightShader = FileUtility.readInputStream(LightManager.class.getResourceAsStream("/assets/minecraft/shaders/include/shimmer.glsl"));
                lightShader = lightShader.replace("#version 150", "");
            } catch (IOException e) {
                ShimmerConstants.LOGGER.error("error while loading shimmer lighting shader");
                lightShader = "";
            }
        }
        return lightShader;
    }

    public static String RbVFSHInjection(String s) {
        s = new StringBuffer(s).insert(s.lastIndexOf("void main()"), getLightShader()).toString();
        s = new StringBuffer(s).insert(s.lastIndexOf('}'), """
                    v_Color = rb_color_light_uv(position, v_Color, v_LightCoord);
                """).toString();
        return s;
    }

    public void bindRbProgram(int programID) {
        lightUBO.bindToShader(programID, "Lights");
        envUBO.bindToShader(programID, "Env");
    }

    public static void onResourceManagerReload() {
        lightShader = null;
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

    public int leftLightCount() {
        return 2048 - lights.size();
    }

    public FloatBuffer getBuffer() {
        return buffer;
    }

    public void renderLevelPre(int blockLightSize, float camX, float camY, float camZ) {
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
            int uboOffset = Services.PLATFORM.getUniformBufferObjectOffset();

            lightUBO = new ShaderUBO();
            lightUBO.createBufferData(size, GL30.GL_STREAM_DRAW); // stream -- modified each frame

            envUBO = new ShaderUBO();
            envUBO.createBufferData(32, GL30.GL_STREAM_DRAW); // stream -- modified each frame

            lightUBO.blockBinding(uboOffset);
            envUBO.blockBinding(uboOffset+1);
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

    private final Map<Block, BiFunction<BlockState, BlockPos, ColorPointLight.Template>> BLOCK_MAP = Maps.newHashMap();
    private final Map<Fluid, ColorPointLight.Template> FLUID_MAP = Maps.newHashMap();

    public boolean isBlockHasLight(Block block, FluidState fluidState) {
        return BLOCK_MAP.containsKey(block) || (!fluidState.isEmpty() && FLUID_MAP.containsKey(fluidState.getType()));
    }

    @Nullable
    public ColorPointLight getBlockStateLight(BlockAndTintGetter level, BlockPos blockpos, BlockState blockstate, FluidState fluidstate) {
        boolean solid = true;
        for (Direction side : Direction.values()) {
            BlockPos offset = blockpos.relative(side);
            if (!level.getBlockState(offset).isSolidRender(level, offset)) {
                solid = false;
                break;
            }
        }
        if (solid) {
            return null;
        }
        ColorPointLight.Template template = BLOCK_MAP.getOrDefault(blockstate.getBlock(), (s,p) -> null).apply(blockstate,blockpos);
        if (template == null && !fluidstate.isEmpty()){
            template = FLUID_MAP.get(fluidstate.getType());
        }
        return template == null ? null : new ColorPointLight(blockpos, template);
    }

    /**
     * register colored light for a block.
     * @param block block
     * @param supplier light supplier from a BlockState and BlockPos
     */
    public void registerBlockLight(Block block, BiFunction<BlockState, BlockPos, ColorPointLight.Template> supplier) {
        if (block == Blocks.AIR) return;
        BLOCK_MAP.put(block, supplier);
    }

    public void registerBlockLight(Block block, int color, float radius) {
        ColorPointLight.Template template = new ColorPointLight.Template(radius, color);
        registerBlockLight(block, (state, pos) -> template);
    }

    public void registerFluidLight(Fluid fluid, int color, float radius) {
        ColorPointLight.Template template = new ColorPointLight.Template(radius, color);
        FLUID_MAP.put(fluid, template);
    }

    public void loadConfig() {
        JsonElement jsonElement = Configuration.config.get("LightBlock");
        if (jsonElement.isJsonArray()) {
            JsonArray blocks = jsonElement.getAsJsonArray();
            for (JsonElement block : blocks) {
                JsonObject jsonObj = block.getAsJsonObject();
                if (jsonObj.has("block")) {
                    ResourceLocation location = new ResourceLocation(jsonObj.get("block").getAsString());
                    if (!Registry.BLOCK.containsKey(location)) continue;
                    Block bb = Registry.BLOCK.get(location);
                    int a = JsonUtils.getIntOr("a", jsonObj, 0);
                    int r = JsonUtils.getIntOr("r", jsonObj, 0);
                    int g = JsonUtils.getIntOr("g", jsonObj, 0);
                    int b = JsonUtils.getIntOr("b", jsonObj, 0);
                    registerBlockLight(bb, (a << 24) | (r << 16) | (g << 8) | b, jsonObj.get("radius").getAsFloat());
                } else if (jsonObj.has("fluid")) {
                    ResourceLocation location = new ResourceLocation(jsonObj.get("fluid").getAsString());
                    if (!Registry.FLUID.containsKey(location)) continue;
                    Fluid ff = Registry.FLUID.get(location);
                    int a = JsonUtils.getIntOr("a", jsonObj, 0);
                    int r = JsonUtils.getIntOr("r", jsonObj, 0);
                    int g = JsonUtils.getIntOr("g", jsonObj, 0);
                    int b = JsonUtils.getIntOr("b", jsonObj, 0);
                    registerFluidLight(ff, (a << 24) | (r << 16) | (g << 8) | b, jsonObj.get("radius").getAsFloat());
                }
            }
        }
    }

    public int getLight(BlockGetter instance, BlockPos pPos) {
        BlockState blockState = instance.getBlockState(pPos);
        FluidState fluidState = blockState.getFluidState();
        int light = 0;
        if (isBlockHasLight(blockState.getBlock(), fluidState)) {
            ColorPointLight.Template template = BLOCK_MAP.getOrDefault(blockState.getBlock(), (s,p) -> null).apply(blockState, pPos);
            if (template == null && !fluidState.isEmpty()){
                template = FLUID_MAP.get(fluidState.getType());
            }
            if (template != null) {
                light = (int) template.radius;
            }
        }
        for (ColorPointLight colorPointLight : lights) {
            double dist = pPos.distToCenterSqr(colorPointLight.x, colorPointLight.y, colorPointLight.z);
            double r2 = colorPointLight.radius * colorPointLight.radius;
            if (dist < r2) {
                light = (int) Math.max(Math.sqrt(r2) - Math.sqrt(dist), light);
            }
        }
        return light;
    }
}
