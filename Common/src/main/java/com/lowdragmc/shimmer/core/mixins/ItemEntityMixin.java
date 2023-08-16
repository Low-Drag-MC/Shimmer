package com.lowdragmc.shimmer.core.mixins;

import com.lowdragmc.shimmer.client.light.ItemEntityLightSourceManager;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {

    @SuppressWarnings("DataFlowIssue")
    @Inject(method = "tick", at = @At("HEAD"))
    private void injectItemTick(CallbackInfo ci) {
        ItemEntityLightSourceManager.tickItemEntity(((ItemEntity) (Object) this));
    }
}
