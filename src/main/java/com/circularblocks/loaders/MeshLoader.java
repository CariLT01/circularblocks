package com.circularblocks.loaders;

import com.circularblocks.dataGeneration.MeshGeometry;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryLoader;

public class MeshLoader implements IGeometryLoader<MeshGeometry> {
    public static final MeshLoader INSTANCE = new MeshLoader();

    @Override
    public MeshGeometry read(JsonObject jsonObject, JsonDeserializationContext context) {
        // Here you load the OBJ path from the JSON
        ResourceLocation modelLocation = new ResourceLocation(jsonObject.get("model").getAsString());
        return new MeshGeometry(modelLocation);
    }
}
