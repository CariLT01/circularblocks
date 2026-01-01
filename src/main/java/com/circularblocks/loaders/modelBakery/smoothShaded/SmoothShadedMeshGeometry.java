package com.circularblocks.loaders.modelBakery.smoothShaded;

import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import net.minecraftforge.client.model.obj.ObjLoader;
import net.minecraftforge.client.model.obj.ObjModel;

import java.util.function.Function;

public class SmoothShadedMeshGeometry implements IUnbakedGeometry<SmoothShadedMeshGeometry> {

    public final ResourceLocation objLocation;

    public SmoothShadedMeshGeometry(ResourceLocation objLocation) {
        this.objLocation = objLocation;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation) {
// 1. Create the ModelSettings object required by loadModel
        // Signature: ModelSettings(ResourceLocation modelLocation, boolean automaticCulling, boolean shadeQuads, boolean flipV, boolean emissiveAmbient, @Nullable ResourceLocation mtllibOverride)
        ObjModel.ModelSettings settings = new ObjModel.ModelSettings(
                objLocation,
                true,  // automaticCulling
                true,  // shadeQuads (We'll override this in the wrapper anyway)
                true,  // flipV (Commonly needed for Blender/OBJ exports)
                true,  // emissiveAmbient
                null   // mtllibOverride
        );

        // 2. Load the model using the settings object
        ObjModel objModel = ObjLoader.INSTANCE.loadModel(settings);

        // 3. Bake the OBJ model into the base BakedModel
        BakedModel baseModel = objModel.bake(context, baker, spriteGetter, modelState, overrides, modelLocation);

        // 4. Wrap it in your BakedMesh to inject the smooth shading gradients
        return new SmoothShadedBakedMesh(baseModel);
    }
}
