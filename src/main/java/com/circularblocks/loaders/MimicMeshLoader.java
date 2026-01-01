package com.circularblocks.loaders;

import com.circularblocks.loaders.modelBakery.mimic.MimicMeshGeometry;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryLoader;

public class MimicMeshLoader implements IGeometryLoader<MimicMeshGeometry> {
    public static final MimicMeshLoader INSTANCE = new MimicMeshLoader();

    @Override
    public MimicMeshGeometry read(JsonObject jsonObject, JsonDeserializationContext context) {
        // Here you load the OBJ path from the JSON
        ResourceLocation modelLocation = new ResourceLocation(jsonObject.get("model").getAsString());
        return new MimicMeshGeometry(modelLocation);
    }
}
