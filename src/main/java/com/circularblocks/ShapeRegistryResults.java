package com.circularblocks;

import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

import java.rmi.registry.Registry;
import java.util.List;

public record ShapeRegistryResults(List<RegistryObject<Block>> blockEntitiesToRegister) {



}
