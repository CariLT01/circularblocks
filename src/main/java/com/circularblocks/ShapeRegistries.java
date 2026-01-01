package com.circularblocks;

import com.circularblocks.loaders.LoaderType;
import com.circularblocks.mimics.MimicCylinderBlock;
import com.circularblocks.mimics.MimicPolarCylinderBlockEntity;
import com.circularblocks.shapes.*;
import com.circularblocks.shapes.configuration.ShapeGroupConfiguration;
import com.circularblocks.types.Vector3i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.*;

public class ShapeRegistries {

    private final DeferredRegister<Block> BLOCKS;
    private final DeferredRegister<Item> ITEMS;
    private final DeferredRegister<CreativeModeTab> CREATIVE_TABS;
    private final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES;
    private final List<Shape> meshShapes = new ArrayList<>();
    private final Map<String, List<Vector3i>> collisionBoxes = new HashMap<>();


    public ShapeRegistries(DeferredRegister<Block> blocksRegistries, DeferredRegister<Item> itemsRegistries, DeferredRegister<CreativeModeTab> creativeModTab, DeferredRegister<BlockEntityType<?>> blockEntities) {
        this.BLOCKS = blocksRegistries;
        this.ITEMS = itemsRegistries;
        this.BLOCK_ENTITIES = blockEntities;
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

    public void batchedCreateShapeGroup(ShapeGroupConfiguration configuration, List<String> names, String baseTopTextureUnformatted, String baseSideTextureUnformatted, String baseBlockNameUnformatted) {


        for (String s : names) {
            String name = String.format(baseBlockNameUnformatted, s);
            String sideTextureName = String.format(baseSideTextureUnformatted, s);
            String topTextureName = String.format(baseTopTextureUnformatted, s);

            this.createShapeGroup(configuration, new ShapeAppareance(
                    sideTextureName, topTextureName
            ), name);
        }
    }

    public void registerBlocksAndItems() {



        List<RegistryObject<Item>> registeredItems = new ArrayList<>();

        for (Shape shape : meshShapes) {
            RegistryObject<Block> newBlock = null;

            if (shape.loaderType == LoaderType.MESH_LOADER) {
                if (shape.placementBehavior == ShapePlacementBehavior.ROTATED_PILLAR_BLOCK) {
                    newBlock = BLOCKS.register(shape.name, () -> new MeshedBlockPillar(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.QUARTZ)
                            .strength(1.0f)
                            .noOcclusion()));
                } else {
                    newBlock = BLOCKS.register(shape.name, () -> new MeshedBlock(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.QUARTZ)
                            .strength(1.0f)
                            .noOcclusion()));
                }
                RegistryObject<Block> finalNewBlock = newBlock;
            } else {
                newBlock = BLOCKS.register(shape.name, () -> new MimicCylinderBlock(
                        BlockBehaviour.Properties.of()
                                .mapColor(MapColor.QUARTZ)
                                .strength(1.0f)
                                .noOcclusion()
                ));


// 1. Declare the variable as an array or AtomicReference so the lambda can capture it
                final RegistryObject<BlockEntityType<MimicPolarCylinderBlockEntity>>[] typeRef = new RegistryObject[1];

                RegistryObject<Block> finalNewBlock1 = newBlock;
                BLOCK_ENTITIES.register(shape.name, () ->
                        BlockEntityType.Builder.of(
                                MimicPolarCylinderBlockEntity::new, // This now matches (pos, state)
                                finalNewBlock1.get()
                        ).build(null)
                );

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

    public void computeCollisionsForEach() {
        for (Shape shape : meshShapes) {
            if (!(shape instanceof CylinderShape)) continue;

            ShapeCollisionBlocks block = new ShapeCollisionBlocks(shape.size, ((CylinderShape) shape).centered, CollisionType.CYLINDER);

            List<Vector3i> blocks = block.computeCollisionBlocks();

            collisionBoxes.put(shape.name, blocks);
        }
    }

    public Map<String, List<Vector3i>> getCollisionBoxes() {
        return this.collisionBoxes;
    }

    public List<Shape> getShapes() {
        return meshShapes;
    }

}
