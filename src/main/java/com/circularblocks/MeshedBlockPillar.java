package com.circularblocks;

import com.circularblocks.types.Vector3i;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.circularblocks.ProxyBlock.*;

public class MeshedBlockPillar extends RotatedPillarBlock {

    public MeshedBlockPillar(Properties properties) {
        super(properties);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        String name = BuiltInRegistries.BLOCK.getKey(this).getPath();
        List<Vector3i> positions = CircularBlocks.SHAPE_REGISTRIES.getCollisionBoxes().get(name);
        if (positions == null) return;

        Direction.Axis axis = state.getValue(AXIS);

        for (Vector3i position : positions) {
            // 1. Rotate the relative offset based on the block's placement axis
            Vector3i rotated = rotateOffset(position, axis);

            // 2. Determine the world position for the slave
            BlockPos slavePos = pos.offset(rotated.x(), rotated.y(), rotated.z());

            if (level.isEmptyBlock(slavePos)) {
                // 3. Store the NEGATIVE of the rotated offset, shifted by 8
                // This tells the proxy: "Your parent is at -rotated.x, -rotated.y, -rotated.z"
                level.setBlock(slavePos, CircularBlocks.PROXY_BLOCK.get().defaultBlockState()
                        .setValue(OFFSET_X, (-rotated.x()) + 8)
                        .setValue(OFFSET_Y, (-rotated.y()) + 8)
                        .setValue(OFFSET_Z, (-rotated.z()) + 8), 3);
            }
        }
    }

    @Override
    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            String name = BuiltInRegistries.BLOCK.getKey(this).getPath();
            List<Vector3i> positions = CircularBlocks.SHAPE_REGISTRIES.getCollisionBoxes().get(name);

            if (positions != null) {
                Direction.Axis axis = state.getValue(AXIS);
                for (Vector3i offset : positions) {
                    // Must rotate the removal search to match the placement rotation
                    Vector3i rotated = rotateOffset(offset, axis);
                    BlockPos target = pos.offset(rotated.x(), rotated.y(), rotated.z());

                    if (level.getBlockState(target).is(CircularBlocks.PROXY_BLOCK.get())) {
                        level.removeBlock(target, false);
                    }
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    private Vector3i rotateOffset(Vector3i original, Direction.Axis axis) {
        return switch (axis) {
            case Y -> original; // Vertical: Up stays Up
            case X -> new Vector3i(original.y(), original.x(), original.z()); // Side: Y becomes X
            case Z -> new Vector3i(original.x(), original.z(), original.y()); // Side: Y becomes Z
        };
    }
}