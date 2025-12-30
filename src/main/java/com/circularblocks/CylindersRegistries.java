package com.circularblocks;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

public class CylindersRegistries {

    private final DeferredRegister<Block> BLOCKS;
    private final DeferredRegister<Item> ITEMS;
    private final List<CylinderType> cylinders = new ArrayList<>();

    public CylindersRegistries(DeferredRegister<Block> blocksRegistries, DeferredRegister<Item> itemsRegistries) {
        this.BLOCKS = blocksRegistries;
        this.ITEMS = itemsRegistries;
    }

    public void createCylinderType(CylinderType cylinderType) {
        cylinders.add(cylinderType);
    }

    public void registerBlocksAndItems() {

        for (CylinderType cylinder : cylinders) {
            RegistryObject<Block> newBlock = BLOCKS.register(cylinder.name(), () -> new RotatedPillarBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.QUARTZ)
                    .strength(5.0f)
                    .noOcclusion()));
            ITEMS.register(cylinder.name(), () -> new BlockItem(newBlock.get(), new Item.Properties()));
        }
    }

    public List<CylinderType> getCylinders() {
        return cylinders;
    }

}
