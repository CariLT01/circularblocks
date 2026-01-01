package com.circularblocks.loaders.modelBakery.mimic;

import com.circularblocks.mimics.MimicMeshBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MimicBakedModel extends BakedModelWrapper<BakedModel> {

    public MimicBakedModel(BakedModel originalModel) {
        super(originalModel);
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType) {
        BlockState mimicState = data.get(MimicMeshBlockEntity.MIMIC_PROPERTY);
        if (mimicState == null || mimicState.isAir()) {
            return originalModel.getQuads(state, side, rand, data, renderType);
        }

        List<BakedQuad> templateQuads = originalModel.getQuads(state, side, rand, data, renderType);
        List<BakedQuad> modifiedQuads = new ArrayList<>();

        // 1. Get the sprites for the mimic block (e.g., Log)
        // We'll grab North for sides and Up for caps
        TextureAtlasSprite mimicSide = getSpriteForDirection(mimicState, Direction.NORTH, rand, data, renderType);
        TextureAtlasSprite mimicCap = getSpriteForDirection(mimicState, Direction.UP, rand, data, renderType);

        for (BakedQuad quad : templateQuads) {
            // 2. Look at the template sprite's name from your OBJ
            String templateName = quad.getSprite().contents().name().getPath();
            System.out.println("Quad Sprite: " + templateName); // Check your console for this!

            // 3. Decide which mimic sprite to use based on your material name
            // Replace "cylinder_sides" and "cylinder_caps" with your actual .mtl names
            TextureAtlasSprite targetSprite = mimicSide;
            if (templateName.contains("top")) {
                targetSprite = mimicCap;
            }

            modifiedQuads.add(remapQuad(quad, targetSprite));
        }

        return modifiedQuads;
    }

    private TextureAtlasSprite getSpriteForDirection(BlockState state, Direction dir, RandomSource rand, ModelData data, RenderType renderType) {
        return Minecraft.getInstance()
                .getBlockRenderer()
                .getBlockModel(state)
                .getQuads(state, dir, rand, data, renderType)
                .get(0)
                .getSprite();
    }

    private BakedQuad remapQuad(BakedQuad quad, TextureAtlasSprite mimicSprite) {
        int[] vertices = quad.getVertices().clone();
        // We need the sprite that was ORIGINALLY on the OBJ (e.g., weathered_copper)
        TextureAtlasSprite originalSprite = quad.getSprite();

        for (int i = 0; i < 4; i++) {
            int offset = i * 8;

            // 1. Get the current UV (which is on the Atlas)
            float atlasU = Float.intBitsToFloat(vertices[offset + 4]);
            float atlasV = Float.intBitsToFloat(vertices[offset + 5]);

            // 2. "Un-interpolate" to get the 0.0 - 16.0 position inside the original sprite
            float localU = originalSprite.getUOffset(atlasU);
            float localV = originalSprite.getVOffset(atlasV);

            // 3. Re-map that local position onto the NEW mimic sprite
            vertices[offset + 4] = Float.floatToRawIntBits(mimicSprite.getU(localU));
            vertices[offset + 5] = Float.floatToRawIntBits(mimicSprite.getV(localV));
        }

        return new BakedQuad(vertices, quad.getTintIndex(), quad.getDirection(), mimicSprite, quad.isShade());
    }
}
