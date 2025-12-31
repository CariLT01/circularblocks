package com.circularblocks.dataGeneration;

import com.circularblocks.CircularBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CircularBlocks.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        // This adds the BlockState & Model generator
        generator.addProvider(event.includeClient(),
                new ShapeMeshProvider(output, CircularBlocks.MODID, CircularBlocks.SHAPE_REGISTRIES));
        generator.addProvider(event.includeClient(),
                new ShapeBlockStateProvider(output, CircularBlocks.MODID, existingFileHelper));
    }
}
