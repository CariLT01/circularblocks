package com.circularblocks;

import com.circularblocks.loaders.MeshLoader;
import com.circularblocks.shapes.*;
import com.circularblocks.shapes.configuration.AngledCylinderGroupConfiguration;
import com.circularblocks.shapes.configuration.CylinderGroupConfiguration;
import com.circularblocks.shapes.shapeSettings.QuarterCylinderShapeSettings;
import com.circularblocks.shapes.shapeSettings.ShapeSettings;
import com.circularblocks.types.Vector3f;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.util.List;

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

    public static final RegistryObject<Block> PROXY_BLOCK = BLOCKS.register("proxy_block", () -> new ProxyBlock(BlockBehaviour.Properties.of()
            .strength(1.0f)
            .noOcclusion()
            .noParticlesOnBreak()));


    public static final ShapeRegistries SHAPE_REGISTRIES = new ShapeRegistries(BLOCKS, ITEMS, CREATIVE_MODE_TABS);



    public CircularBlocks()
    {
        final FMLJavaModLoadingContext context = FMLJavaModLoadingContext.get();
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

        CylinderGroupConfiguration cylinderGroupConfiguration = new CylinderGroupConfiguration(
                List.of(new Vector3f(0.25f, 1.0f, 0.25f), new Vector3f(0.5f, 1.0f, 0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(2.0f, 1.0f, 2.0f), new Vector3f(3.0f, 1.0f, 3.0f)),
                List.of("_mini", "_half", "", "_2x2", "_3x3"),
                List.of(16, 16, 32, 32, 32),
                List.of(true, true, false, false, true),
                List.of(1.0f, 2.0f, 4.0f, 8.0f, 8.0f),
                false
        );

        CylinderGroupConfiguration cylinderGroupConfigurationPlanar = new CylinderGroupConfiguration(
                List.of(new Vector3f(0.25f, 1.0f, 0.25f), new Vector3f(0.5f, 1.0f, 0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(2.0f, 1.0f, 2.0f), new Vector3f(3.0f, 1.0f, 3.0f)),
                List.of("_mini", "_half", "", "_2x2", "_3x3"),
                List.of(16, 16, 32, 32, 32),
                List.of(true, true, false, false, true),
                List.of(1.0f, 2.0f, 4.0f, 8.0f, 8.0f),
                true
        );

        AngledCylinderGroupConfiguration angledCylinderGroupConfiguration = new AngledCylinderGroupConfiguration(
                List.of(new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(1.0f, 2.0f, 2.0f), new Vector3f(1.0f, 3.0f, 3.0f),
                        new Vector3f(1.0f, 1.0f, -1.0f), new Vector3f(1.0f, 2.0f, -2.0f), new Vector3f(1.0f, 3.0f, -3.0f)),
                List.of("", "_1x2", "_1x3","r", "_rx2", "_rx3"),
                List.of(32, 32, 32, 32, 32, 32),
                List.of(4.0f, 4.0f, 4.0f, 4.0f, 4.0f, 4.0f),
                true
        );

        SHAPE_REGISTRIES.createShapeGroup(cylinderGroupConfiguration,
                new ShapeAppareance("minecraft:block/quartz_pillar", "minecraft:block/quartz_pillar_top"),
                "quartz_pillar_cylinder"
        );

        SHAPE_REGISTRIES.createShapeGroup(cylinderGroupConfiguration,
                new ShapeAppareance("minecraft:block/iron_block", "minecraft:block/iron_block"),
                "iron_block_cylinder"
        );

        // All log types


        SHAPE_REGISTRIES.batchedCreateShapeGroup(
                cylinderGroupConfiguration,
                List.of("oak", "birch", "jungle", "spruce", "acacia", "dark_oak", "mangrove", "cherry"),
                "minecraft:block/%s_log_top",
                "minecraft:block/%s_log",
                "%s_log_cylinder"
        );

        SHAPE_REGISTRIES.batchedCreateShapeGroup(
                cylinderGroupConfiguration,
                List.of("oak", "birch", "jungle", "spruce", "acacia", "dark_oak", "mangrove", "cherry"),
                "minecraft:block/stripped_%s_log_top",
                "minecraft:block/stripped_%s_log",
                "stripped_%s_log_cylinder"
        );

        // Copper

        SHAPE_REGISTRIES.batchedCreateShapeGroup(
                cylinderGroupConfigurationPlanar,
                List.of("copper_block", "cut_copper", "exposed_copper", "weathered_copper", "oxidized_copper", "exposed_cut_copper", "weathered_cut_copper", "oxidized_cut_copper"),
                "minecraft:block/%s",
                "minecraft:block/%s",
                "%s_cylinder"
        );


        // Deepslate & Mud & Purpur

        SHAPE_REGISTRIES.batchedCreateShapeGroup(
                cylinderGroupConfigurationPlanar,
                List.of("deepslate_bricks", "deepslate_tiles", "deepslate", "cobbled_deepslate", "mud_bricks", "purpur_block"),
                "minecraft:block/%s",
                "minecraft:block/%s",
                "%s_cylinder"
        );


        SHAPE_REGISTRIES.createShapeGroup(cylinderGroupConfigurationPlanar,
                new ShapeAppareance("minecraft:block/cobblestone", "minecraft:block/cobblestone"),
                "cobblestone_cylinder");

        SHAPE_REGISTRIES.createShapeGroup(angledCylinderGroupConfiguration,
                new ShapeAppareance("minecraft:block/quartz_block_side", "minecraft:block/quartz_block_side"),
                "quartz_block_angled_cylinder");

        SHAPE_REGISTRIES.createShapeGroup(cylinderGroupConfigurationPlanar,
                new ShapeAppareance("minecraft:block/quartz_block_side", "minecraft:block/quartz_block_side"),
                "quartz_block_cylinder");

        SHAPE_REGISTRIES.createShapeGroup(cylinderGroupConfigurationPlanar,
                new ShapeAppareance("minecraft:block/stone_bricks", "minecraft:block/stone_bricks"),
                "stone_bricks_cylinder");

        SHAPE_REGISTRIES.createShapeGroup(cylinderGroupConfigurationPlanar,
                new ShapeAppareance("minecraft:block/bricks", "minecraft:block/bricks"),
                "bricks_cylinder");


        SHAPE_REGISTRIES.addShape(
                new QuarterCylinderShape(
                        new QuarterCylinderShapeSettings(
                                new ShapeSettings(
                                        "iron_block_quarter_cylinder",
                                        "minecraft:block/iron_block",
                                        "minecraft:block/iron_block",
                                        new Vector3f(1.0f, 1.0f, 1.0f),
                                        ShapePlacementBehavior.HORIZONTAL_DIRECTIONAL
                                ),
                                32,
                                false,
                                1.0f
                        )
                )
        );



        SHAPE_REGISTRIES.addShape(
                new QuarterCylinderShape(
                        new QuarterCylinderShapeSettings(
                                new ShapeSettings(
                                        "quartz_block_quarter_cylinder",
                                        "minecraft:block/quartz_block_side",
                                        "minecraft:block/quartz_block_side",
                                        new Vector3f(1.0f, 1.0f, 1.0f),
                                        ShapePlacementBehavior.HORIZONTAL_DIRECTIONAL
                                ),
                                32,
                                false,
                                1.0f
                        )
                )
        );


        SHAPE_REGISTRIES.registerBlocksAndItems();
        SHAPE_REGISTRIES.computeCollisionsForEach();



        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        //context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
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
