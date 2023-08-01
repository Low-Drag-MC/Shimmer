package com.lowdragmc.shimmer.fabric.core.mixins.iris;

import com.lowdragmc.shimmer.comp.iris.IrisHandle;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import net.coderbot.iris.gl.buffer.ShaderStorageBuffer;
import net.coderbot.iris.gl.buffer.ShaderStorageBufferHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(value = ShaderStorageBufferHolder.class, remap = false)
public abstract class ShaderStorageBufferHolderMixin {
    @Shadow private boolean destroyed;

    @Shadow private ShaderStorageBuffer[] buffers;

    @Inject(method = "setupBuffers", at = @At("TAIL"))
    private void updateIrisHandle(CallbackInfo ci) {
        if (!this.destroyed){
            IrisHandle.INSTANCE.updateInfo(buffers);
        }
    }

    @Inject(method = "destroyBuffers", at = @At("TAIL"))
    private void destroy(CallbackInfo ci) {
        IrisHandle.INSTANCE.onSSBODestroyed();
    }
}
