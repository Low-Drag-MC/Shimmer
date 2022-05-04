package com.lowdragmc.shimmer;

import com.lowdragmc.shimmer.client.light.LightManager;
import com.mojang.math.Vector3f;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2022/05/02
 * @implNote TestBlock
 */
public class PistonBlock extends Block {
    public PistonBlock(ResourceLocation registryName) {
        super(Properties
                .of(Material.METAL)
                .sound(SoundType.STONE)
                .strength(1)
                .noOcclusion());
        this.setRegistryName(registryName);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
        if (pLevel.isClientSide) {
            LightManager.INSTANCE.addLight(new Vector3f(pPos.getX() + 0.5f, pPos.getY() + 0.5f, pPos.getZ() + 0.5f), 0xffff0000, 5);
        }
    }
}
