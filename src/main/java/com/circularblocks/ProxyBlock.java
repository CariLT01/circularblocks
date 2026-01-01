package com.circularblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.NotNull;

public class ProxyBlock extends Block {

    public static final IntegerProperty OFFSET_X = IntegerProperty.create("offset_x", 0, 16);
    public static final IntegerProperty OFFSET_Z = IntegerProperty.create("offset_z", 0, 16);
    public static final IntegerProperty OFFSET_Y = IntegerProperty.create("offset_y", 0, 16);


    public ProxyBlock(Properties props) {
        super(props);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OFFSET_X, OFFSET_Y, OFFSET_Z);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            // Calculate parent position using the stored offsets
            BlockPos parentPos = pos.offset(
                    state.getValue(OFFSET_X) - 8,
                    state.getValue(OFFSET_Y) - 8,
                    state.getValue(OFFSET_Z) - 8
            );

            Block targetBlock = level.getBlockState(parentPos).getBlock();

            if (targetBlock instanceof MeshedBlock || targetBlock instanceof MeshedBlockPillar) {
                System.out.println("Destroy parent block");
                level.destroyBlock(parentPos, !player.isCreative());
            } else {
                System.out.println("Didn't find MeshedBlock, found: " + targetBlock.getClass().getName());
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public @NotNull BlockState updateShape(BlockState state, @NotNull Direction direction, @NotNull BlockState neighborState, LevelAccessor level, BlockPos currentPos, @NotNull BlockPos neighborPos) {
        // Calculate where the parent should be based on our stored offsets
        BlockPos parentPos = currentPos.offset(state.getValue(OFFSET_X) - 8, state.getValue(OFFSET_Y) - 8, state.getValue(OFFSET_Z) - 8);

        // If the block at the parent position is no longer a Cylinder, remove this proxy
        Block targetBlock = level.getBlockState(parentPos).getBlock();

        if (!(targetBlock instanceof MeshedBlock || targetBlock instanceof MeshedBlockPillar)) {

            System.out.println("Destroying, mesh not found, found: " + level.getBlockState(parentPos).getBlock().getClass().getName());
            return Blocks.AIR.defaultBlockState();
        }

        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }
}

