package com.lowdragmc.shimmer.core.mixins;

import com.lowdragmc.shimmer.client.postprocessing.PostProcessing;
import com.lowdragmc.shimmer.client.shader.RenderUtils;
import com.lowdragmc.shimmer.client.ResourceUtils;
import com.lowdragmc.shimmer.client.ShimmerRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author KilaBash
 * @date 2022/05/02
 * @implNote HumanoidArmorLayerMixin, used to inject emissive + bloom armor via custom resource pack.
 */
@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin {

    @Shadow protected abstract ResourceLocation getArmorLocation(ArmorItem armorItem, boolean bl, String string);

    @Inject(method = "renderModel",
            at = @At(value = "RETURN"))
    private void injectRenderModel(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, ArmorItem armorItem, boolean hasFoil, HumanoidModel model, boolean $$6, float r, float g, float b, String resourceLocation, CallbackInfo ci) {
        ResourceLocation armorResource = this.getArmorLocation(armorItem, hasFoil, resourceLocation);
        ResourceLocation bloomResource = new ResourceLocation(armorResource.getNamespace(), armorResource.getPath().replace(".png", "_bloom.png"));
        if (ResourceUtils.isResourceExist(bloomResource)) {
            PoseStack finalStack = RenderUtils.copyPoseStack(poseStack);
            PostProcessing.BLOOM_UNITY.postEntity(sourceConsumer -> model.renderToBuffer(finalStack, sourceConsumer.getBuffer(ShimmerRenderTypes.emissiveArmor(bloomResource)), 0xF000F0, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F));
        }
    }

}
