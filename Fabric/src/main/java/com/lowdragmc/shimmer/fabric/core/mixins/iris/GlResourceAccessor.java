package com.lowdragmc.shimmer.fabric.core.mixins.iris;

import net.coderbot.iris.gl.GlResource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = GlResource.class, remap = false)
public interface GlResourceAccessor {
    @Invoker int callGetGlId();
}
