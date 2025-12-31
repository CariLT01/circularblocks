package com.circularblocks;

import com.circularblocks.shapes.*;
import com.circularblocks.shapes.configuration.ShapeGroupConfiguration;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
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
            RegistryObject<Block> newBlock = null;

            if (shape.placementBehavior == ShapePlacementBehavior.ROTATED_PILLAR_BLOCK) {
                newBlock = BLOCKS.register(shape.name, () -> new RotatedPillarBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.QUARTZ)
                        .strength(5.0f)
                        .noOcclusion()));
            } else {
                newBlock = BLOCKS.register(shape.name, () -> new HorizontalDirectionalBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.QUARTZ)
                        .strength(5.0f)
                        .noOcclusion()) {

                    @Override
                    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                        // Registers the FACING property (limited to horizontal directions)
                        builder.add(FACING);
                    }

                    @Override
                    public BlockState getStateForPlacement(BlockPlaceContext context) {
                        // Sets the direction to face the player, but only on the horizontal plane
                        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
                    }

                    // Set the starting state
                    {
                        registerDefaultState(stateDefinition.any().setValue(FACING, net.minecraft.core.Direction.NORTH));
                    }
                });
            }
            RegistryObject<Block> finalNewBlock = newBlock;
            registeredItems.add(ITEMS.register(shape.name, () -> new BlockItem(finalNewBlock.get(), new Item.Properties())));

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
