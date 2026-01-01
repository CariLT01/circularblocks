package com.circularblocks.mimics;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public class MimicCylinderBlock extends Block implements EntityBlock {

    public MimicCylinderBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MimicCylinderBlockEntity(pos, state);
    }

    // 2. Handle Right-Clicking to change the "Mimic" texture
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);

        // Check if player is holding a block (to use as a texture)
        if (stack.getItem() instanceof BlockItem blockItem) {
            if (!level.isClientSide) { // Only run logic on the server
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof MimicCylinderBlockEntity cylinderBE) {
                    // Update the texture
                    System.out.println("Switching mimic to: " + blockItem.getBlock().getName().getString());
                    cylinderBE.setMimic(blockItem.getBlock().defaultBlockState());
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    // 3. Optional: Drop the block correctly (so it doesn't leave the entity behind)
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            // We don't need to manually remove the BE,
            // but this is where you'd drop extra inventory items if you had them.
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}
