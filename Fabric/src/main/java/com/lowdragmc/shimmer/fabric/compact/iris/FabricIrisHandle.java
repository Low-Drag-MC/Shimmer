package com.lowdragmc.shimmer.fabric.compact.iris;

import com.lowdragmc.shimmer.ShimmerConstants;
import com.lowdragmc.shimmer.client.shader.RenderUtils;
import com.lowdragmc.shimmer.client.shader.ShaderSSBO;
import com.lowdragmc.shimmer.comp.iris.IrisHandle;
import com.lowdragmc.shimmer.core.mixins.MixinPluginShared;
import net.coderbot.iris.gl.buffer.ShaderStorageBuffer;
import net.irisshaders.iris.api.v0.IrisApi;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL46;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public class FabricIrisHandle implements IrisHandle {

    public FabricIrisHandle() {
    }

    private boolean available = MixinPluginShared.IS_IRIS_LOAD;
    @Nullable
    private Pair<ShaderSSBO, ShaderSSBO> ssbos;
    private int lightIndex = -1;
    private int envIndex = -1;

    @Override
    public void updateInfo(Object buffers) {
        if (!available || !ShimmerConstants.IRIS_COMPACT_ENABLE) return;
        if (buffers instanceof ShaderStorageBuffer[] suffers) {
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
            if (lightBufferIndex == -1 || envBufferIndex == -1) return;
            int finalLightBufferIndex = lightBufferIndex;
            int finalEnvBufferIndex = envBufferIndex;
            RenderUtils.warpGLDebugLabel("initSSBO", () -> {
                var lightBuffer = new ShaderSSBO();
                //no need to call glShaderStorageBlockBinding here, as we set layout in shader explicitly
                {
                    var oldBuffer = suffers[finalLightBufferIndex];
                    lightBuffer.createBufferData(oldBuffer.getSize(), GL46.GL_DYNAMIC_COPY);
                    lightBuffer.bindIndex(oldBuffer.getIndex());
                    oldBuffer.destroy();
                    suffers[finalLightBufferIndex] = new ShaderStorageBuffer(lightBuffer.id, oldBuffer.getIndex(), oldBuffer.getSize());
                }
                var envBuffer = new ShaderSSBO();
                {
                    var oldBuffer = suffers[finalEnvBufferIndex];
                    envBuffer.createBufferData(oldBuffer.getSize(), GL46.GL_DYNAMIC_COPY);
                    envBuffer.bufferSubData(0,new int[8]);
                    envBuffer.bindIndex(oldBuffer.getIndex());
                    oldBuffer.destroy();
                    suffers[finalEnvBufferIndex] = new ShaderStorageBuffer(envBuffer.id, oldBuffer.getIndex(), oldBuffer.getSize());
                }
                suffers[finalLightBufferIndex].bind();
                suffers[finalEnvBufferIndex].bind();
                ssbos = Pair.of(lightBuffer, envBuffer);
            });
        } else {
            ShimmerConstants.LOGGER.error("expect:{} as ShaderStorageBuffer[],actual:{}", buffers.toString(), buffers.getClass().getName());
        }
    }

    @Override
    public void onSSBODestroyed() {
        if (ssbos != null) {
            ssbos.getLeft().close();
            ssbos.getRight().close();
            ssbos = null;
        }
        lightIndex = -1;
        envIndex = -1;
    }

    @Override
    public void setLightsIndex(int index) {
        this.lightIndex = index;
    }

    @Override
    public void setEnvIndex(int index) {
        this.envIndex = index;
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
}
