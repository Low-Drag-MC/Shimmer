package com.lowdragmc.shimmer.core.mixins;

import com.lowdragmc.shimmer.client.ResourceUtils;
import com.lowdragmc.shimmer.client.ShimmerRenderTypes;
import com.lowdragmc.shimmer.client.bloom.Bloom;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author KilaBash
 * @date 2022/05/02
 * @implNote LevelRendererMixin
 */
@Mixin(HumanoidArmorLayer.class)
public class HumanoidArmorLayerMixin {

    @Inject(method = "renderModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IZLnet/minecraft/client/model/Model;FFFLnet/minecraft/resources/ResourceLocation;)V",
            at = @At(value = "RETURN"), remap = false)
    private void injectRenderModel(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, boolean hasFoil, Model model, float r, float g, float b, ResourceLocation armorResource, CallbackInfo ci) {
        ResourceLocation bloomResource = new ResourceLocation(armorResource.getNamespace(), armorResource.getPath().replace(".png", "_bloom.png"));
        if (ResourceUtils.isResourceExist(bloomResource)) {
            RenderType renderType = ShimmerRenderTypes.bloomArmor(bloomResource);
            PoseStack finalStack = new PoseStack();
            finalStack.setIdentity();
            finalStack.mulPoseMatrix(poseStack.last().pose());
            Bloom.Entity_LAST_BLOOM.postBloom(renderType, vertexConsumer -> model.renderToBuffer(finalStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F));
        }
    }

}