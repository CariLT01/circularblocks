package com.circularblocks.mimics;

import com.circularblocks.CircularBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;

public class MimicCylinderBlockEntity extends BlockEntity {

    private BlockState mimic = Blocks.OAK_LOG.defaultBlockState();

    public static final ModelProperty<BlockState> MIMIC_PROPERTY = new ModelProperty<>();

    public MimicCylinderBlockEntity(BlockPos pos, BlockState state) {
        super(null, pos, state);
    }

    public void setMimic(BlockState newBlockState) {
        this.mimic = newBlockState;

        this.setChanged();

        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            requestModelDataUpdate();
        }
    }

    public BlockState getMimic() {
        return this.mimic;
    }

    // --- MODEL DATA BRIDGE ---
    @Override
    public @NotNull ModelData getModelData() {
        return ModelData.builder()
                .with(MIMIC_PROPERTY, mimic)
                .build();
    }

    @Override
    public void load(CompoundTag tag) { // 1.20.1 uses 'load', not 'loadAdditional'
        super.load(tag);
        if (tag.contains("mimic", 10)) {
            // 1.20.1 uses BuiltInRegistries for lookup
            this.mimic = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag.getCompound("mimic"));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) { // 1.20.1 takes ONLY 1 argument
        super.saveAdditional(tag);
        tag.put("mimic", NbtUtils.writeBlockState(this.mimic));
    }

    // --- FIXING THE NETWORKING ---

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() { // 1.20.1 returns CompoundTag with no arguments
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag); // Pack our mimic data into the sync tag
        return tag;
    }

    @Override
    public void onDataPacket(net.minecraft.network.Connection net, net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            // Load the new mimic state from the server's packet
            this.load(tag);

            // CRITICAL: Tell the renderer that the model data has changed
            requestModelDataUpdate();

            // Force a chunk redraw so the new texture appears instantly
            if (level != null) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }
}
