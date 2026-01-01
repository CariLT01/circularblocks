package com.circularblocks.mimics;

import com.circularblocks.CircularBlocks;
import com.circularblocks.types.Vector3i;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

import static com.circularblocks.ProxyBlock.*;

public class MimicMeshBlockPillar extends RotatedPillarBlock implements EntityBlock {

    public MimicMeshBlockPillar(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MimicMeshBlockEntity(pos, state);
    }

    // 2. Handle Right-Clicking to change the "Mimic" texture
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);

        // Check if player is holding a block (to use as a texture)
        if (stack.getItem() instanceof BlockItem blockItem) {

            BlockState potentialMimicState = blockItem.getBlock().defaultBlockState();

            if (!potentialMimicState.isCollisionShapeFullBlock(level, pos) || !potentialMimicState.canOcclude()) {
                return InteractionResult.FAIL;
            }

            if (!level.isClientSide) { // Only run logic on the server
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof MimicMeshBlockEntity cylinderBE) {
                    // Update the texture
                    System.out.println("Switching mimic to: " + blockItem.getBlock().getName().getString());
                    cylinderBE.setMimic(blockItem.getBlock().defaultBlockState());
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @org.jetbrains.annotations.Nullable LivingEntity placer, ItemStack stack) {
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
