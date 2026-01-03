package com.circularblocks.loaders.modelBakery.mimic;

import com.circularblocks.blocks.mimics.MimicMeshBlockEntity;
import com.circularblocks.types.Vector3f;
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
        Vector3f lightDir_r = new Vector3f(1.0f, 1.0f, 1.0f);
        Vector3f lightDir = lightDir_r.normalize();

        int[] vertexData = quad.getVertices().clone();
        // We need the sprite that was ORIGINALLY on the OBJ (e.g., weathered_copper)
        TextureAtlasSprite originalSprite = quad.getSprite();

        int unifiedLight = vertexData[6];

        for (int i = 0; i < 4; i++) {
            int offset = i * 8;

            // 1. Get the current UV (which is on the Atlas)
            float atlasU = Float.intBitsToFloat(vertexData[offset + 4]);
            float atlasV = Float.intBitsToFloat(vertexData[offset + 5]);

            // 2. "Un-interpolate" to get the 0.0 - 16.0 position inside the original sprite
            float localU = originalSprite.getUOffset(atlasU);
            float localV = originalSprite.getVOffset(atlasV);

            // 3. Re-map that local position onto the NEW mimic sprite
            vertexData[offset + 4] = Float.floatToRawIntBits(mimicSprite.getU(localU));
            vertexData[offset + 5] = Float.floatToRawIntBits(mimicSprite.getV(localV));

            // 1. Extract the Packed Normal from index 7
            int packedNormal = vertexData[offset + 7];

            // Unpack bytes (Values are stored as signed bytes from -127 to 127)
            float nx = ((byte) (packedNormal & 0xFF)) / 127.0f;
            float ny = ((byte) ((packedNormal >> 8) & 0xFF)) / 127.0f;
            float nz = ((byte) ((packedNormal >> 16) & 0xFF)) / 127.0f;

            // 2. Calculate Dot Product
            float dot = nx * lightDir.x() + ny * lightDir.y() + nz * lightDir.z();
// 1. Map dot product from [-1, 1] to [0, 1]
// This "wraps" the light around the cylinder.
            int v = getV(dot);

            // 2. Overwrite the lightmap for this vertex
            // This stops Minecraft from trying to "blend" world light across the face
            vertexData[offset + 6] = unifiedLight;

            // 3. Inject into Color Field (Index 3)
            // Format: 0x AA BB GG RR
            vertexData[offset + 3] = 0xFF000000 | (v << 16) | (v << 8) | v;
        }

        return new BakedQuad(vertexData, quad.getTintIndex(), quad.getDirection(), mimicSprite, quad.isShade());
    }

    private static int getV(float dot) {
        // 1. Shift the dot product so the "bright" area is wider
        // We want the light to stay at 100% for longer across the curve.
        float brightnessFactor = dot * 0.5f + 0.5f;

        // 2. The "Pop" Math: Use a higher power (3.0 or 4.0)
        // This creates a "Shoulder" – it stays bright (1.0) for a long time,
        // then dives into the shadow quickly at the edge.
        float contrast = (float) Math.pow(brightnessFactor, 0.6f);

        // 3. The "Shadow Depth":
        // We go from 0.6 (Standard MC Side) to 1.0 (Standard MC Top)
        // But we use a 'Smoothstep' or 'Bias' to make the shadow feel clean.
        float brightness = 0.65f + (contrast * 0.35f);

        // 4. Subtle "Happy" Tint (Optional)
        // If you want it to look less gray, we can keep the Shadow at 0.65
        // but the Highlight at 1.0.

        return (int)(brightness * 255) & 0xFF;
    }
}
