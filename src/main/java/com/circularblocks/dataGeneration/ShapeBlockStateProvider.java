package com.circularblocks.dataGeneration;

import com.circularblocks.CircularBlocks;
import com.circularblocks.loaders.LoaderType;
import com.circularblocks.shapes.Shape;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.client.model.generators.loaders.CompositeModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ShapeBlockStateProvider extends BlockStateProvider {

    private final String ModId;

    public ShapeBlockStateProvider(PackOutput output, String modid, ExistingFileHelper exFileHelper) {
        super(output, modid, exFileHelper);

        this.ModId = modid;
    }

    @Override
    protected void registerStatesAndModels() {
        // Replace this loop with your actual block registry objects or names

        List<Shape> shapes = CircularBlocks.SHAPE_REGISTRIES.getShapes();

        for (Shape shape : shapes) {

            String name = shape.name;
            ResourceLocation objectLocation = new ResourceLocation(this.ModId, "models/item/" + name + ".obj");

// 1. Define the Block Model
            BlockModelBuilder compositeModel = models().withExistingParent(name + "_model", "forge:item/default")
                    .renderType("cutout_mipped")
                    .ao(false)
                    .texture("particle", shape.sideTextureName);

// 2. Build the Composite Loader with manual JSON override
            compositeModel.customLoader((builder, helper) -> new CompositeModelBuilder<BlockModelBuilder>(builder, helper) {
                @Override
                public JsonObject toJson(JsonObject json) {
                    super.toJson(json); // This adds the "children" and "loader" fields

                    // This manually adds "shade": false to the root of the loader block
                    json.addProperty("shade", false);
                    json.add("display", getDisplayJsonObject());
                    return json;
                }

                private static @NotNull JsonObject getDisplayJsonObject() {
                    JsonObject display = new JsonObject();
                    JsonObject gui = new JsonObject();
                    JsonArray rotation = new JsonArray();
                    rotation.add(-75);
                    rotation.add(-75);
                    rotation.add(0);
                    gui.add("rotation", rotation);
                    JsonArray scale = new JsonArray();
                    scale.add(0.31);
                    scale.add(0.31);
                    scale.add(0.31);
                    gui.add("scale", scale);
                    display.add("gui", gui);

                    JsonObject thirdperson_righthand = new JsonObject();
                    JsonArray rotation2 = new JsonArray();
                    rotation2.add(-45);
                    rotation2.add(-45);
                    rotation2.add(-45);
                    thirdperson_righthand.add("rotation", rotation2);
                    JsonArray scale2 = new JsonArray();
                    scale2.add(0.25);
                    scale2.add(0.25);
                    scale2.add(0.25);
                    thirdperson_righthand.add("scale", scale2);
                    display.add("thirdperson_righthand", thirdperson_righthand);

                    JsonObject thirdperson_lefthand = new JsonObject();
                    thirdperson_lefthand.add("scale", scale2);
                    display.add("thirdperson_lefthand", thirdperson_lefthand);

                    JsonObject firstperson_righthand = new JsonObject();
                    firstperson_righthand.add("scale", scale2);
                    display.add("firstperson_righthand", firstperson_righthand);

                    JsonObject ground = new JsonObject();
                    ground.add("scale", scale2);
                    display.add("ground", ground);


                    return display;
                }
            }.child("part1", getSmoothMeshPart(name, shape.loaderType)));

            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(this.ModId, name));
            if (block instanceof RotatedPillarBlock pillar) {
                getVariantBuilder(pillar)
                        .partialState().with(RotatedPillarBlock.AXIS, net.minecraft.core.Direction.Axis.Y)
                        .modelForState().modelFile(compositeModel).addModel()
                        .partialState().with(RotatedPillarBlock.AXIS, net.minecraft.core.Direction.Axis.Z)
                        .modelForState().modelFile(compositeModel).rotationX(90).addModel()
                        .partialState().with(RotatedPillarBlock.AXIS, net.minecraft.core.Direction.Axis.X)
                        .modelForState().modelFile(compositeModel).rotationX(90).rotationY(90).addModel();
            } else if (block instanceof HorizontalDirectionalBlock horizontal) {
                getVariantBuilder(horizontal)
                        .partialState().with(HorizontalDirectionalBlock.FACING, net.minecraft.core.Direction.NORTH)
                        .modelForState().modelFile(compositeModel).addModel()
                        .partialState().with(HorizontalDirectionalBlock.FACING, net.minecraft.core.Direction.SOUTH)
                        .modelForState().modelFile(compositeModel).rotationY(180).addModel()
                        .partialState().with(HorizontalDirectionalBlock.FACING, net.minecraft.core.Direction.EAST)
                        .modelForState().modelFile(compositeModel).rotationY(90).addModel()
                        .partialState().with(HorizontalDirectionalBlock.FACING, net.minecraft.core.Direction.WEST)
                        .modelForState().modelFile(compositeModel).rotationY(270).addModel();
            } else {
                simpleBlock(block, compositeModel);
            }

            itemModels().withExistingParent(name, modLoc("block/" + name + "_model"));
        }
    }

    private BlockModelBuilder getSmoothMeshPart(String name, LoaderType loaderType) {
        return models().getBuilder(name + "_part")
                .customLoader((partBuilder, partHelper) -> new CustomLoaderBuilder<BlockModelBuilder>(
                        new ResourceLocation("circularblocks", loaderType == LoaderType.MIMIC_MESH_LOADER ? "mimic_mesh_loader" : "smooth_mesh_loader"),
                        partBuilder,
                        partHelper) {

                    @Override
                    public JsonObject toJson(JsonObject json) {
                        // Call the super method to include any standard properties
                        super.toJson(json);
                        // Manually inject your custom fields into the JSON
                        json.addProperty("model", ModId + ":models/item/" + name + ".obj");
                        json.addProperty("emissive_ambient", false);
                        return json;
                    }
                })
                .end();
    }

}
