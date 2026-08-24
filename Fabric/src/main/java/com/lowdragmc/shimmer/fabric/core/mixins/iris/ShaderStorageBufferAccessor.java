package com.lowdragmc.shimmer.fabric.core.mixins.iris;

import net.irisshaders.iris.gl.buffer.ShaderStorageBuffer;
import net.irisshaders.iris.gl.buffer.ShaderStorageInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = ShaderStorageBuffer.class, remap = false)
public interface ShaderStorageBufferAccessor {
    @Invoker void callDestroy();
    @Accessor ShaderStorageInfo getInfo();
}
