package com.circularblocks.shapes;

import com.circularblocks.loaders.LoaderType;
import com.circularblocks.shapes.shapeSettings.ShapeSettings;
import com.circularblocks.types.Vector3f;

public abstract class Shape {

    public final String name;
    public final String sideTextureName;
    public final String topTextureName;
    public final Vector3f size;
    public final ShapePlacementBehavior placementBehavior;
    public final LoaderType loaderType;

    public Shape(ShapeSettings settings) {
        this.name = settings.name();
        this.sideTextureName = settings.sideTextureName();
        this.topTextureName = settings.topTextureName();
        this.size = settings.size();
        this.placementBehavior = settings.placementBehavior();
        this.loaderType = settings.loaderType();

    }

}
