package com.lowdragmc.shimmer.core.mixins;

import com.lowdragmc.shimmer.client.ResourceUtils;
import com.lowdragmc.shimmer.client.ShimmerRenderTypes;
import com.lowdragmc.shimmer.client.postprocessing.PostProcessing;
import com.lowdragmc.shimmer.client.shader.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
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
 * @implNote HumanoidArmorLayerMixin, used to inject emissive + bloom armor via custom resource pack.
 */
@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin {

    @Inject(method = "renderModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IZLnet/minecraft/client/model/Model;FFFLnet/minecraft/resources/ResourceLocation;)V",
            at = @At(value = "RETURN"), remap = false)
    private void injectRenderModel(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, boolean hasFoil, Model model, float r, float g, float b, ResourceLocation armorResource, CallbackInfo ci) {
        ResourceLocation bloomResource = new ResourceLocation(armorResource.getNamespace(), armorResource.getPath().replace(".png", "_bloom.png"));
        if (ResourceUtils.isResourceExist(bloomResource)) {
            PoseStack finalStack = RenderUtils.copyPoseStack(poseStack);
            PostProcessing.BLOOM_UNITY.postEntity(sourceConsumer -> model.renderToBuffer(finalStack, sourceConsumer.getBuffer(ShimmerRenderTypes.emissiveArmor(bloomResource)), 0xF000F0, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F));
        }
    }

}
