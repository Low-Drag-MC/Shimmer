package com.lowdragmc.shimmer.fabric.core.mixins;

import com.lowdragmc.shimmer.client.postprocessing.PostProcessing;
import com.lowdragmc.shimmer.core.IBakedQuad;
import com.lowdragmc.shimmer.fabric.core.IQuadViewImpl;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MutableQuadViewImpl;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author KilaBash
 * @date 2022/06/15
 */
@Mixin(MutableQuadViewImpl.class)
public abstract class MutableQuadViewImplMixin {

    @Inject(method = "fromVanilla(Lnet/minecraft/client/renderer/block/model/BakedQuad;Lnet/fabricmc/fabric/api/renderer/v1/material/RenderMaterial;Lnet/minecraft/core/Direction;)Lnet/fabricmc/fabric/impl/client/indigo/renderer/mesh/MutableQuadViewImpl;",
            at = @At(value = "HEAD"))
    private void injectBaked(BakedQuad quad, RenderMaterial material, Direction cullFace, CallbackInfoReturnable<MutableQuadViewImpl> cir) {
        ((IQuadViewImpl) this).setBloom(((IBakedQuad)quad).isBloom() || PostProcessing.isBlockBloom());
    }

}
