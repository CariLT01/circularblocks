package com.circularblocks;

import com.circularblocks.shapes.*;
import com.circularblocks.shapes.configuration.ShapeGroupConfiguration;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ShapeRegistries {

    private final DeferredRegister<Block> BLOCKS;
    private final DeferredRegister<Item> ITEMS;
    private final DeferredRegister<CreativeModeTab> CREATIVE_TABS;
    private final List<Shape> meshShapes = new ArrayList<>();

    public ShapeRegistries(DeferredRegister<Block> blocksRegistries, DeferredRegister<Item> itemsRegistries, DeferredRegister<CreativeModeTab> creativeModTab) {
        this.BLOCKS = blocksRegistries;
        this.ITEMS = itemsRegistries;
        this.CREATIVE_TABS = creativeModTab;
    }

    public void addShape(Shape shape) {
        meshShapes.add(shape);
    }

    public void createShapeGroup(ShapeGroupConfiguration groupType, ShapeAppareance appareance, String baseName) {

        List<Shape> createdShapes = ShapeGroupCreationDispatcher.createShapesForGroup(groupType, appareance, baseName);
        for (Shape shape : createdShapes) {
            this.addShape(shape);
        }
    }

    public void registerBlocksAndItems() {



        List<RegistryObject<Item>> registeredItems = new ArrayList<>();

        for (Shape shape : meshShapes) {
            RegistryObject<Block> newBlock = BLOCKS.register(shape.name, () -> new RotatedPillarBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.QUARTZ)
                    .strength(5.0f)
                    .noOcclusion()));
            registeredItems.add(ITEMS.register(shape.name, () -> new BlockItem(newBlock.get(), new Item.Properties())));

        }


        CREATIVE_TABS.register("circular_blocks", () ->
                CreativeModeTab.builder()
                        .title(Component.literal("Circular Blocks"))
                        .icon(() -> new ItemStack(registeredItems.get(0).get()))
                        .displayItems((displayParameters, output) -> {
                            for (RegistryObject<Item> item : registeredItems) {
                                output.accept(item.get());
                            }
                        })
                        .build()
        );
    }

    public List<Shape> getShapes() {
        return meshShapes;
    }

}
