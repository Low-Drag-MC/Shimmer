package com.lowdragmc.shimmer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

/**
 * @author KilaBash
 * @date 2022/05/02
 * @implNote TestBlock
 */
public class TestBlock extends Block {
    public TestBlock(ResourceLocation registryName) {
        super(BlockBehaviour.Properties
                .of(Material.METAL)
                .sound(SoundType.STONE)
                .strength(1)
                .noOcclusion());
        this.setRegistryName(registryName);
    }

}
