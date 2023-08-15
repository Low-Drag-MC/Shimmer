package com.lowdragmc.shimmer.fabric.compact.iris;

import com.lowdragmc.shimmer.ShimmerConstants;
import com.lowdragmc.shimmer.client.shader.RenderUtils;
import com.lowdragmc.shimmer.client.shader.ShaderSSBO;
import com.lowdragmc.shimmer.comp.iris.IrisHandle;
import com.lowdragmc.shimmer.core.mixins.MixinPluginShared;
import com.lowdragmc.shimmer.fabric.core.mixins.iris.ShaderStorageBufferAccessor;
import net.coderbot.iris.gl.buffer.ShaderStorageBuffer;
import net.irisshaders.iris.api.v0.IrisApi;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL46;

@SuppressWarnings("unused")
public class FabricIrisHandle implements IrisHandle {

    /**
     * must have this, this is called by reflect
     */
    public FabricIrisHandle() {
    }

    private boolean available = MixinPluginShared.IS_IRIS_LOAD;
    @Nullable
    private Pair<ShaderSSBO, ShaderSSBO> ssbos;
    /**
     * global ssbo index
     */
    private int lightIndex = -1;
    private int envIndex = -1;

    @Override
    public void updateInfo(Object buffers) {
        if (!available || !ShimmerConstants.IRIS_COMPACT_ENABLE) return;
        if (lightIndex == -1 || envIndex == -1) {
            ShimmerConstants.LOGGER.info("env buffer not set fully, light:{}, env:{}", lightIndex, envIndex);
            ShimmerConstants.LOGGER.info("shimmer shader support for colored light with ssbo is now offline");
            return;
        }
        if (buffers instanceof ShaderStorageBuffer[] suffers) {
            //index in the ShaderStorageBuffer[]
            int lightBufferIndex = -1;
            int envBufferIndex = -1;
            for (int i = 0; i < suffers.length; i++) {
                var buffer = suffers[i];
                if (buffer.getIndex() == lightIndex) {
                    if (buffer.getSize() >= 65536) {
                        lightBufferIndex = i;
                    } else {
                        ShimmerConstants.LOGGER.error("shaders.properties set lights ssbo with index:{}, however its size is{}, less that required", buffer.getIndex(), buffer.getSize());
                    }
                } else if (buffer.getIndex() == envIndex) {
                    if (buffer.getSize() >= 32) {
                        envBufferIndex = i;
                    } else {
                        ShimmerConstants.LOGGER.error("shaders.properties set lights ssbo with index:{}, however its size is{}, less that required", buffer.getIndex(), buffer.getSize());
                    }
                }
            }
            if (lightBufferIndex == -1 || envBufferIndex == -1) {
                ShimmerConstants.LOGGER.error("failed to detect ssbo created by iris");
                return;
            } else {
                ShimmerConstants.LOGGER.info("detect ssbo created by iris success");
            }
            int finalLightBufferIndex = lightBufferIndex;
            int finalEnvBufferIndex = envBufferIndex;
            if (checkIsRelative(suffers[lightBufferIndex], "light") || checkIsRelative(suffers[envBufferIndex],"env")) return;
            RenderUtils.warpGLDebugLabel("initSSBO", () -> {
                var lightBuffer = replaceSSBO(suffers, finalLightBufferIndex);
                var envBuffer = replaceSSBO(suffers, finalEnvBufferIndex);
                ssbos = Pair.of(lightBuffer, envBuffer);
            });
        } else {
            ShimmerConstants.LOGGER.error("expect:{} as ShaderStorageBuffer[],actual:{}", buffers.toString(), buffers.getClass().getName());
        }
    }

    private static boolean checkIsRelative(ShaderStorageBuffer buffer, String name) {
        if (((ShaderStorageBufferAccessor)buffer).getInfo().relative()) {
            ShimmerConstants.LOGGER.error("expect buffer:{} not relative", name);
            return true;
        } {
            return false;
        }
    }

    private static ShaderSSBO replaceSSBO(ShaderStorageBuffer[] suffers, int replaceIndex) {
        var oldBuffer = suffers[replaceIndex];
        //destroy origin
        ((ShaderStorageBufferAccessor) oldBuffer).callDestroy();
        //create ours
        var buffer = new ShaderSSBO();
        buffer.createBufferData(oldBuffer.getSize(), GL46.GL_DYNAMIC_COPY);
        buffer.bindIndex(oldBuffer.getIndex());
        //warp and bind
        var irisSSBO = new ShaderStorageBuffer(buffer.id, ((ShaderStorageBufferAccessor) oldBuffer).getInfo());
        suffers[replaceIndex] = irisSSBO;
        irisSSBO.bind();

        return buffer;
    }

    @Override
    public void onSSBODestroyed() {
        ssbos = null;
    }

    @Override
    public void setLightsIndex(int index) {
        this.lightIndex = index;
    }

    @Override
    public int getLightsIndex() {
        return lightIndex;
    }

    @Override
    public void setEnvIndex(int index) {
        this.envIndex = index;
    }

    @Override
    public int getEnvIndex() {
        return envIndex;
    }

    @Override
    @Nullable
    public Pair<ShaderSSBO, ShaderSSBO> getBuffer() {
        return ssbos;
    }

    @Override
    public boolean underShaderPack() {
        return IrisApi.getInstance().isShaderPackInUse();
    }

    @Override
    public boolean underShadowPass() {
        return IrisApi.getInstance().isRenderingShadowPass();
    }

    @Override
    public void bindWriteMain() {
        GBufferMainRenderTarget.autoBind();
    }

    @Override
    public int getCompositeId() {
        return GBufferMainRenderTarget.getID();
    }
}
