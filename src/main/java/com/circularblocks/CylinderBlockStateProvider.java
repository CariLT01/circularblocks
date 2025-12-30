package com.circularblocks;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.client.model.generators.loaders.CompositeModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CylinderBlockStateProvider extends BlockStateProvider {

    private final String ModId;

    public CylinderBlockStateProvider(PackOutput output, String modid, ExistingFileHelper exFileHelper) {
        super(output, modid, exFileHelper);

        this.ModId = modid;
    }

    @Override
    protected void registerStatesAndModels() {
        // Replace this loop with your actual block registry objects or names

        List<CylinderType> cylinders = CircularBlocks.cylindersRegistries.getCylinders();

        for (CylinderType cylinder : cylinders) {

            String name = cylinder.name();
            ResourceLocation objectLocation = new ResourceLocation(this.ModId, "models/item/" + name + ".obj");

// 1. Define the Block Model
            BlockModelBuilder compositeModel = models().withExistingParent(name + "_model", "forge:item/default")
                    .renderType("cutout_mipped")
                    .ao(false)
                    .texture("particle", "minecraft:block/quartz_block_side");

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
                    rotation.add(-48);
                    rotation.add(-86);
                    rotation.add(0);
                    gui.add("rotation", rotation);
                    JsonArray scale = new JsonArray();
                    scale.add(0.31);
                    scale.add(0.31);
                    scale.add(0.80);
                    gui.add("scale", scale);
                    display.add("gui", gui);
                    return display;
                }
            }.child("part1", getSmoothMeshPart(name)));

            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(this.ModId, name));
            if (block instanceof RotatedPillarBlock pillar) {
                getVariantBuilder(pillar)
                        .partialState().with(RotatedPillarBlock.AXIS, net.minecraft.core.Direction.Axis.Y)
                        .modelForState().modelFile(compositeModel).addModel()
                        .partialState().with(RotatedPillarBlock.AXIS, net.minecraft.core.Direction.Axis.Z)
                        .modelForState().modelFile(compositeModel).rotationX(90).addModel()
                        .partialState().with(RotatedPillarBlock.AXIS, net.minecraft.core.Direction.Axis.X)
                        .modelForState().modelFile(compositeModel).rotationX(90).rotationY(90).addModel();
            } else {
                simpleBlock(block, compositeModel);
            }

            itemModels().withExistingParent(name, modLoc("block/" + name + "_model"));
        }
    }

    private BlockModelBuilder getSmoothMeshPart(String name) {
        return models().getBuilder(name + "_part")
                .customLoader((partBuilder, partHelper) -> new CustomLoaderBuilder<BlockModelBuilder>(
                        new ResourceLocation("circularblocks", "smooth_mesh_loader"),
                        partBuilder,
                        partHelper) {

                    @Override
                    public JsonObject toJson(JsonObject json) {
                        // Call the super method to include any standard properties
                        super.toJson(json);
                        // Manually inject your custom fields into the JSON
                        json.addProperty("model", ModId + ":models/item/" + name + ".obj");
                        json.addProperty("emissive_ambient", true);
                        return json;
                    }
                })
                .end();
    }

}
