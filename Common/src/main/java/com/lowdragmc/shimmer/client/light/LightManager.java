package com.lowdragmc.shimmer.client.light;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lowdragmc.shimmer.Configuration;
import com.lowdragmc.shimmer.FileUtility;
import com.lowdragmc.shimmer.ShimmerConstants;
import com.lowdragmc.shimmer.Utils;
import com.lowdragmc.shimmer.client.shader.ShaderInjection;
import com.lowdragmc.shimmer.client.shader.ShaderUBO;
import com.lowdragmc.shimmer.platform.Services;
import com.mojang.math.Vector3f;
import com.mojang.realmsclient.util.JsonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author KilaBash
 * @date 2022/05/04
 * @implNote LightManager
 */
public enum LightManager {
    INSTANCE;
    private static final int MAXIMUM_LIGHT_SUPPORT = 2048;
    private static final int MAXIMUM_PALER_LIGHT_SUPPORT = 20;
    private static final int MAXIMUM_BLOCK_LIGHT_SUPPORT = MAXIMUM_LIGHT_SUPPORT - MAXIMUM_PALER_LIGHT_SUPPORT;
    private final List<ColorPointLight> BLOCK_LIGHT = new ArrayList<>(MAXIMUM_BLOCK_LIGHT_SUPPORT);
    private final Map<UUID,ColorPointLight> PLAYER_LIGHT = new HashMap<>(MAXIMUM_PALER_LIGHT_SUPPORT);
    private final FloatBuffer BUFFER = BufferUtils.createFloatBuffer((MAXIMUM_BLOCK_LIGHT_SUPPORT) * ColorPointLight.STRUCT_SIZE);
    private final FloatBuffer PLAYER_BUFFER = BufferUtils.createFloatBuffer(MAXIMUM_PALER_LIGHT_SUPPORT * ColorPointLight.STRUCT_SIZE);

    ShaderUBO lightUBO;
    ShaderUBO envUBO;

    private static String ChunkInjection(String s) {
        s = s.replace("void main()", "#moj_import <shimmer.glsl>\n\nvoid main()");
        return new StringBuffer(s).insert(s.lastIndexOf('}'),
                Services.PLATFORM.useLightMap() ? "vertexColor = color_light_uv(pos, vertexColor,UV2);\n" : "vertexColor = color_light(pos, vertexColor);\n"
        ).toString();
    }

    private static String PositionInjection(String s) {
        //TODO fix armor lighting. what the hell!!!!!
        s = s.replace("void main()", "#moj_import <shimmer.glsl>\n\nvoid main()");
        return new StringBuffer(s).insert(s.lastIndexOf('}'),
                Services.PLATFORM.useLightMap() ? "vertexColor = color_light_uv(Position, vertexColor,UV2);\n" : "vertexColor = color_light(Position, vertexColor);\n"
        ).toString();
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
        s = new StringBuffer(s).insert(s.lastIndexOf('}'), Services.PLATFORM.useLightMap() ? """
                    v_Color = rb_color_light_uv(position, v_Color, v_LightCoord);
                """ : """
                    v_Color = color_light(position, v_Color);
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
        for (ColorPointLight light : INSTANCE.BLOCK_LIGHT) {
            light.lightManager = null;
        }
        INSTANCE.BLOCK_LIGHT.clear();
    }

    public int leftBlockLightCount() {
        return MAXIMUM_BLOCK_LIGHT_SUPPORT - BLOCK_LIGHT.size();
    }

    public FloatBuffer getBuffer() {
        return BUFFER;
    }

    public void renderLevelPre(int blockLightSize, float camX, float camY, float camZ) {
        if (blockLightSize > 0) {
            lightUBO.bufferSubData(getOffset(BLOCK_LIGHT.size()), BUFFER);
        }
        updatePlayerHeldItemLight();
        lightUBO.bufferSubData(getOffset(BLOCK_LIGHT.size() + blockLightSize),PLAYER_BUFFER);

        envUBO.bufferSubData(0, new int[]{BLOCK_LIGHT.size() + blockLightSize});
        envUBO.bufferSubData(4,new int[]{PLAYER_LIGHT_COUNT});
        envUBO.bufferSubData(16, new float[]{camX, camY, camZ});
    }

    public void renderLevelPost() {
        envUBO.bufferSubData(0, new int[]{0,0});
    }

    public void reloadShaders() {
        if (lightUBO == null) {
            int size = getOffset(MAXIMUM_LIGHT_SUPPORT);
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
        if (BLOCK_LIGHT.size() == MAXIMUM_BLOCK_LIGHT_SUPPORT) return null;
        ColorPointLight light = new ColorPointLight(this, pos, color, radius, getOffset(BLOCK_LIGHT.size()));
        BLOCK_LIGHT.add(light);
        lightUBO.bufferSubData(light.offset, light.getData());
        return light;
    }

    private int getOffset(int index) {
        return (index * ColorPointLight.STRUCT_SIZE) << 2;
    }

    void removeLight(ColorPointLight removed) {
        int index = BLOCK_LIGHT.indexOf(removed);
        if (index >= 0) {
            for (int i = index + 1; i < BLOCK_LIGHT.size(); i++) {
                BLOCK_LIGHT.get(i).offset = getOffset(i - 1);
            }
            BLOCK_LIGHT.remove(index);
            if (index < BLOCK_LIGHT.size()) {
                Minecraft.getInstance().execute(() -> {
                    BUFFER.clear();
                    for (int i = index; i < BLOCK_LIGHT.size(); i++) {
                        BLOCK_LIGHT.get(i).uploadBuffer(BUFFER);
                    }
                    BUFFER.flip();
                    lightUBO.bufferSubData(getOffset(index), BUFFER);
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
        if (BLOCK_MAP.containsKey(block)) {
            BiFunction<BlockState, BlockPos, ColorPointLight.Template> exist = BLOCK_MAP.get(block);
            BiFunction<BlockState, BlockPos, ColorPointLight.Template> current = supplier;
            supplier = (blockState, pos) -> {
                ColorPointLight.Template template = current.apply(blockState, pos);
                if (template == null) {
                    template = exist.apply(blockState, pos);
                }
                return template;
            };
        }
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
                int color = (JsonUtils.getIntOr("a", jsonObj, 0) << 24) |
                        (JsonUtils.getIntOr("r", jsonObj, 0) << 16) |
                        (JsonUtils.getIntOr("g", jsonObj, 0) << 8) |
                        JsonUtils.getIntOr("b", jsonObj, 0);
                float radius = jsonObj.get("radius").getAsFloat();
                if (jsonObj.has("block")) {
                    ResourceLocation location = new ResourceLocation(jsonObj.get("block").getAsString());
                    if (!Registry.BLOCK.containsKey(location)) continue;
                    Block bb = Registry.BLOCK.get(location);
                    if (jsonObj.has("state") && jsonObj.get("state").isJsonObject()) {
                        Set<BlockState> available = Utils.getAllPossibleStates(jsonObj, bb);
                        if (!available.isEmpty()) {
                            registerBlockLight(bb, (bs, pos) -> {
                                if (available.contains(bs)) {
                                    return new ColorPointLight.Template(radius, color);
                                }
                                return null;
                            });
                        }
                    } else {
                        registerBlockLight(bb, color, radius);
                    }
                } else if (jsonObj.has("fluid")) {
                    ResourceLocation location = new ResourceLocation(jsonObj.get("fluid").getAsString());
                    if (!Registry.FLUID.containsKey(location)) continue;
                    Fluid ff = Registry.FLUID.get(location);
                    registerFluidLight(ff, color, radius);
                } else if (jsonObj.has("item_id")){
                    ResourceLocation itemResourceLocation = new ResourceLocation(jsonObj.get("item_id").getAsString());
                    if (Registry.ITEM.containsKey(itemResourceLocation)){
                        Item item = Registry.ITEM.get(itemResourceLocation);
                        registerItemLight(item,(itemStack -> new ColorPointLight.Template(radius,color)));
                    }
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
        for (ColorPointLight colorPointLight : BLOCK_LIGHT) {
            double dist = pPos.distToCenterSqr(colorPointLight.x, colorPointLight.y, colorPointLight.z);
            double r2 = colorPointLight.radius * colorPointLight.radius;
            if (dist < r2) {
                light = (int) Math.max(Math.sqrt(r2) - Math.sqrt(dist), light);
            }
        }
        return light;
    }

    // *********************** held item light *********************** //
    private final Map<Item,Function<ItemStack,ColorPointLight.Template>> ITEM_MAP = new HashMap<>();
//    private final ColorPointLight LOCAL_PLAYER_ITEM_LIGHT = new ColorPointLight(BlockPos.ZERO,new ColorPointLight.Template(0,0));
    @Nullable
    public ColorPointLight getItemLight(ItemStack itemStack, Vec3 pos){
        Function<ItemStack,ColorPointLight.Template> function = ITEM_MAP.get(itemStack.getItem());
        if (function !=null){
            ColorPointLight.Template template = function.apply(itemStack);
            return new ColorPointLight(pos,template);
        }else return null;
    }

    private int PLAYER_LIGHT_COUNT = 0;

    @Nullable
    public void updatePlayerHeldItemLight(){
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        PLAYER_BUFFER.clear();
        PLAYER_LIGHT_COUNT = 0;
        Vec3 localPlayerPosition = localPlayer.position();
        List<AbstractClientPlayer> players = Minecraft.getInstance().level.players();
        for (int index = 0 ; index < Math.min(MAXIMUM_PALER_LIGHT_SUPPORT,players.size()) ;index++){
            var player = players.get(index);
            UUID uuid = player.getUUID();
            if (PLAYER_LIGHT.containsKey(uuid)){
                PLAYER_LIGHT.get(uuid).uploadBuffer(PLAYER_BUFFER);
                PLAYER_LIGHT_COUNT++;
                continue;
            }
            Vec3 position = player.position();
            if (player == localPlayer || position.distanceTo(localPlayerPosition) < 32){
                ColorPointLight light = getItemLight(player.getMainHandItem(),position);
                if (light != null){
                    PLAYER_LIGHT_COUNT++;
//                    PLAYER_LIGHT.add(light);
                    light.uploadBuffer(PLAYER_BUFFER);
                    continue;
                }
                light = getItemLight(player.getOffhandItem(),position);
                if (light!=null){
                    PLAYER_LIGHT_COUNT++;
                    light.uploadBuffer(PLAYER_BUFFER);
//                    PLAYER_LIGHT.add(light);
                }
            }
        }
        PLAYER_BUFFER.flip();
    }

    public void registerItemLight(Item item,Function<ItemStack,ColorPointLight.Template> function){
        ITEM_MAP.put(item,function);
    }

    public ColorPointLight addPlayerItemLight(Vector3f pos, int color, float radius, UUID playerUUID) {
        if (PLAYER_LIGHT.size() == MAXIMUM_PALER_LIGHT_SUPPORT) return null;
        ColorPointLight light = new ColorPointLight(this,pos,color,radius,getOffset(PLAYER_LIGHT.size()));
        PLAYER_LIGHT.put(playerUUID,light);
        return light;
    }

    public boolean removePlayerLight(UUID playerUUID){
        return PLAYER_LIGHT.remove(playerUUID) != null;
    }

    public boolean removePlayerLight(ColorPointLight removeLight){
        Set<UUID> set = PLAYER_LIGHT.keySet();
        Iterator<UUID> iterator = set.iterator();
        while (iterator.hasNext()){
            UUID uuid = iterator.next();
            ColorPointLight light = PLAYER_LIGHT.get(uuid);
            if (light == removeLight){
                PLAYER_LIGHT.remove(uuid,light);
                return true;
            }
        }
        return false;
    }

}
