package com.circularblocks.dataGeneration;

import com.circularblocks.types.Vector3f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
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

public class BakedMesh extends BakedModelWrapper<BakedModel> {

    public BakedMesh(BakedModel originalModel) {
        super(originalModel);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false; // This stops the "junction" gradients when stacking blocks
    }

    public static float lerp(float start, float stop, float amount) {
        return start * (1.0f - amount) + (stop * amount);
        // An alternative, mathematically identical formula is:
        // return start + amount * (stop - start);
    }


    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType) {
        List<BakedQuad> quads = super.getQuads(state, side, rand, data, renderType);
        List<BakedQuad> smoothed = new ArrayList<>();

        // The "Sun" direction. 1.0 on Y means light comes from directly above.
        // The slight X and Z offsets create the shading on the sides of the cylinder.
        Vector3f lightDir_r = new Vector3f(1.0f, 1.0f, 1.0f);
        Vector3f lightDir = lightDir_r.normalize();

        for (BakedQuad quad : quads) {
            int[] vertexData = quad.getVertices().clone();
            int step = vertexData.length / 4; // Should be 8 ints per vertex

            int unifiedLight = vertexData[6];

            for (int i = 0; i < 4; i++) {
                int offset = i * 8; // Stride of 8 ints

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

            // shade = false tells MC: "Trust my vertex colors, don't apply cardinal shading."
            smoothed.add(new BakedQuad(vertexData, quad.getTintIndex(), quad.getDirection(), quad.getSprite(), false, false));
        }
        return smoothed;
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

