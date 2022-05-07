package com.lowdragmc.shimmer.test;

import com.lowdragmc.shimmer.ShimmerMod;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

/**
 * @author KilaBash
 * @date 2022/5/5
 * @implNote ColoredFireBlock
 */
public class ColoredFireBlock extends BaseFireBlock {
    public int color;

    public ColoredFireBlock(String name, int color) {
        super(Properties.of(Material.FIRE, MaterialColor.FIRE).noCollission().instabreak().sound(SoundType.WOOL), 2);
        this.color = color;
        setRegistryName(new ResourceLocation(ShimmerMod.MODID, "fire_" + name));
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return 4;
    }

    @Override
    protected boolean canBurn(BlockState pState) {
        return !pState.hasProperty(BlockStateProperties.WATERLOGGED);
    }
}
