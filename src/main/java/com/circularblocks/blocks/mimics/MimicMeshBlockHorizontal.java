package com.circularblocks.blocks.mimics;

import com.circularblocks.CircularBlocks;
import com.circularblocks.types.Vector3i;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import static com.circularblocks.blocks.ProxyBlock.*;

public class MimicMeshBlockHorizontal extends HorizontalDirectionalBlock implements EntityBlock {

    public MimicMeshBlockHorizontal(Properties properties) {
        super(properties);
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
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @org.jetbrains.annotations.Nullable LivingEntity placer, ItemStack stack) {
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

}
