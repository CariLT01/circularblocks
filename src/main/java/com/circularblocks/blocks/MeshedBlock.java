package com.circularblocks.blocks;

import com.circularblocks.CircularBlocks;
import com.circularblocks.types.Vector3i;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

import static com.circularblocks.blocks.ProxyBlock.*;

public class MeshedBlock extends HorizontalDirectionalBlock {


    public MeshedBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        String name = BuiltInRegistries.BLOCK.getKey(this).getPath();

        Map<String, List<Vector3i>> boxesAll = CircularBlocks.SHAPE_REGISTRIES.getCollisionBoxes();

        System.out.println("Name: " + name);

        if (!boxesAll.containsKey(name)) return;

        List<Vector3i> positions = boxesAll.get(name);


        for (Vector3i position : positions) {
            BlockPos slavePos = pos.offset(position.x(), position.y(), position.z());
            if (level.isEmptyBlock(slavePos))
                level.setBlock(slavePos, CircularBlocks.PROXY_BLOCK.get().defaultBlockState()
                        .setValue(OFFSET_X, position.x() + 8)
                        .setValue(OFFSET_Z, position.z() + 8)
                        .setValue(OFFSET_Y, position.y() + 8), 3);
        }
    }

    @Override
    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
        super.onRemove(state, level, pos, newState, isMoving);
    }
}