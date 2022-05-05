package com.lowdragmc.shimmer;

import com.lowdragmc.shimmer.test.ColoredFireBlock;
import com.lowdragmc.shimmer.test.ColoredFlintItem;
import com.lowdragmc.shimmer.test.PistonBlock;
import com.lowdragmc.shimmer.test.TestBlock;
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

import java.awt.*;

/**
 * @author KilaBash
 * @date 2022/05/02
 * @implNote com.lowdragmc.shimmer.CommonProxy
 */
public class CommonProxy {
    public static TestBlock TEST_BLOCK;
    public static ColoredFireBlock[] FIRE_BLOCKS;
    public static PistonBlock PISTON_BLOCK;
    public CommonProxy() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.register(this);
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> registry = event.getRegistry();
        TEST_BLOCK = new TestBlock(new ResourceLocation(ShimmerMod.MODID, "test_block"));
        PISTON_BLOCK = new PistonBlock(new ResourceLocation(ShimmerMod.MODID, "piston_block"));
        FIRE_BLOCKS = new ColoredFireBlock[]{
                new ColoredFireBlock("orange", Color.ORANGE.getRGB()),
                new ColoredFireBlock("cyan", Color.CYAN.getRGB()),
                new ColoredFireBlock("green", Color.GREEN.getRGB()),
                new ColoredFireBlock("purple", Color.MAGENTA.getRGB()),
        };
        registry.register(PISTON_BLOCK);
        registry.register(TEST_BLOCK);
        for (ColoredFireBlock block : FIRE_BLOCKS) {
            registry.register(block);
        }
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        registry.register(new BlockItem(TEST_BLOCK, new Item.Properties().tab(CreativeModeTab.TAB_REDSTONE))
                .setRegistryName(TEST_BLOCK.getRegistryName()));
        registry.register(new BlockItem(PISTON_BLOCK, new Item.Properties().tab(CreativeModeTab.TAB_REDSTONE))
                .setRegistryName(PISTON_BLOCK.getRegistryName()));
        for (ColoredFireBlock fireBlock : FIRE_BLOCKS) {
            registry.register(new ColoredFlintItem(fireBlock));
        }
    }
}
