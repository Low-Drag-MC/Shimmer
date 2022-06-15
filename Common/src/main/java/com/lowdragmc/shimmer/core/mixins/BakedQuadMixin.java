package com.lowdragmc.shimmer.core.mixins;

import com.lowdragmc.shimmer.client.model.ShimmerMetadataSection;
import com.lowdragmc.shimmer.core.IBakedQuad;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author KilaBash
 * @date 2022/05/02
 * @implNote BakedQuadMixin, inject tinitIndex for block bloom.
 */
@Mixin(BakedQuad.class)
public abstract class BakedQuadMixin implements IBakedQuad {
    private boolean bloom;

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void injectResize(int[] pVertices, int pTintIndex, Direction pDirection, TextureAtlasSprite pSprite, boolean pShade, CallbackInfo ci) {
        bloom = pTintIndex < -100 || ShimmerMetadataSection.isBloom(pSprite);
    }

    @Override
    public boolean isBloom() {
        return bloom;
    }
}
