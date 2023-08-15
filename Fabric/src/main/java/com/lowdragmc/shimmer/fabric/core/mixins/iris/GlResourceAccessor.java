package com.lowdragmc.shimmer.fabric.core.mixins.iris;

import net.coderbot.iris.gl.GlResource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = GlResource.class, remap = false)
public interface GlResourceAccessor {
    @Accessor boolean getIsValid();
}
