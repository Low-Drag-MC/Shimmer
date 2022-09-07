package com.lowdragmc.shimmer.fabric.core.mixins;

import com.lowdragmc.shimmer.fabric.core.IQuadViewImpl;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.QuadViewImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author KilaBash
 * @date 2022/06/15
 */
@Mixin(QuadViewImpl.class)
public abstract class QuadViewImplMixin implements IQuadViewImpl {

    private boolean isBloom;

    @Inject(method = "lightmap", at = @At(value = "HEAD"), cancellable = true, remap = false)
    public void injectBaked(int vertexIndex, CallbackInfoReturnable<Integer> cir) {
        if (isBloom) {
            cir.setReturnValue(0x1000100);
        }
    }

    public boolean isBloom() {
        return isBloom;
    }

    public void setBloom(boolean isBloom) {
        this.isBloom = isBloom;
    }
}
