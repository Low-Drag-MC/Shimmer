package com.lowdragmc.shimmer.fabric.core.mixins.iris;

import net.coderbot.iris.gl.buffer.ShaderStorageBufferHolder;
import net.coderbot.iris.pipeline.newshader.NewWorldRenderingPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = NewWorldRenderingPipeline.class, remap = false)
public interface NewWorldRenderingPipelineAccessor {
    @Accessor
    ShaderStorageBufferHolder getShaderStorageBufferHolder();
}
