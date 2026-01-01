package com.circularblocks.shapes.shapeSettings;

import com.circularblocks.loaders.LoaderType;
import com.circularblocks.shapes.ShapePlacementBehavior;
import com.circularblocks.types.Vector3f;

public record ShapeSettings(
        String name,
        String sideTextureName,
        String topTextureName,
        Vector3f size,
        ShapePlacementBehavior placementBehavior,
        LoaderType loaderType
) {
}
