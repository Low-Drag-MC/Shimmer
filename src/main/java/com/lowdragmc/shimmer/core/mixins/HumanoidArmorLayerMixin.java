package com.lowdragmc.shimmer.core.mixins;

import com.lowdragmc.shimmer.client.ResourceUtils;
import com.lowdragmc.shimmer.client.ShimmerRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
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
            at = @At(value = "RETURN"))
    private void injectRenderModel(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, boolean hasFoil, Model model, float r, float g, float b, ResourceLocation armorResource, CallbackInfo ci) {
        ResourceLocation bloomResource = new ResourceLocation(armorResource.getNamespace(), armorResource.getPath() + "_bloom");
        if (ResourceUtils.isTextureExist(bloomResource)) {
            VertexConsumer vertexconsumer = ItemRenderer.getArmorFoilBuffer(bufferSource, ShimmerRenderTypes.bloomArmor(bloomResource), false, hasFoil);
            model.renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F);
        }
    }

}
