package com.lowdragmc.shimmer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * @author KilaBash
 * @date 2022/05/02
 * @implNote com.lowdragmc.shimmer.CommonProxy
 */
public class CommonProxy {
    public static TestBlock TEST_BLOCK;
    public CommonProxy() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.register(this);
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> registry = event.getRegistry();
        TEST_BLOCK = new TestBlock(new ResourceLocation(ShimmerMod.MODID, "test_block"));
        registry.register(TEST_BLOCK);
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        registry.register(new BlockItem(TEST_BLOCK, new Item.Properties().tab(CreativeModeTab.TAB_REDSTONE))
                .setRegistryName(TEST_BLOCK.getRegistryName()));
    }
}
