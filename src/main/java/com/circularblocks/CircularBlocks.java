package com.circularblocks;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(CircularBlocks.MODID)
public class CircularBlocks
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "circularblocks";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "examplemod" namespace
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "examplemod" namespace
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "examplemod" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);




    public static final RegistryObject<Block> CYLINDER_BLOCK = BLOCKS.register(
            "cylinder", () -> new RotatedPillarBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.QUARTZ)
                    .strength(5.0f)
                    .noOcclusion()));

    public static final RegistryObject<Block> CYLINDER_BLOCK_2X2 = BLOCKS.register(
            "cylinder_2x2", () -> new RotatedPillarBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.QUARTZ)
                    .strength(5.0f)
                    .noOcclusion()));
    public static final RegistryObject<Block> CYLINDER_BLOCK_3X3 = BLOCKS.register(
            "cylinder_3x3", () -> new RotatedPillarBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.QUARTZ)
                    .strength(5.0f)
                    .noOcclusion()));

    public static final RegistryObject<Item> CYLINDER_ITEM = ITEMS.register("cylinder",
            () -> new BlockItem(CYLINDER_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Item> CYLINDER_ITEM_2X2 = ITEMS.register("cylinder_2x2",
            () -> new BlockItem(CYLINDER_BLOCK_2X2.get(), new Item.Properties()));

    public static final RegistryObject<Item> CYLINDER_ITEM_3X3 = ITEMS.register("cylinder_3x3",
            () -> new BlockItem(CYLINDER_BLOCK_3X3.get(), new Item.Properties()));

    public static final RegistryObject<Block> IRON_CYLINDER_BLOCK = BLOCKS.register(
            "ironcylinder", () -> new RotatedPillarBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.RAW_IRON)
                    .strength(5.0f)
                    .noOcclusion()));

    public static final RegistryObject<Block> IRON_CYLINDER_BLOCK_2X2 = BLOCKS.register(
            "ironcylinder_2x2", () -> new RotatedPillarBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.RAW_IRON)
                    .strength(5.0f)
                    .noOcclusion()));
    public static final RegistryObject<Block> IRON_CYLINDER_BLOCK_3X3 = BLOCKS.register(
            "ironcylinder_3x3", () -> new RotatedPillarBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.RAW_IRON)
                    .strength(5.0f)
                    .noOcclusion()));

    public static final RegistryObject<Item> IRON_CYLINDER_ITEM = ITEMS.register("ironcylinder",
            () -> new BlockItem(IRON_CYLINDER_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Item> IRON_CYLINDER_ITEM_2X2 = ITEMS.register("ironcylinder_2x2",
            () -> new BlockItem(IRON_CYLINDER_BLOCK_2X2.get(), new Item.Properties()));

    public static final RegistryObject<Item> IRON_CYLINDER_ITEM_3X3 = ITEMS.register("ironcylinder_3x3",
            () -> new BlockItem(IRON_CYLINDER_BLOCK_3X3.get(), new Item.Properties()));


    public CircularBlocks(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);


        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        LOGGER.info("RESOURCES CHECK: {}",
                getClass().getResourceAsStream("/assets/circularblocks/models/block/cylinder_model.json") != null
                        ? "FOUND" : "NOT FOUND");
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.logDirtBlock)
            LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }


    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }

        @SubscribeEvent
        public static void registerGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
            // This ID "smooth_cylinder" is what you use in your JSON
            event.register("smooth_mesh_loader", MeshLoader.INSTANCE);
        }

    }
}
