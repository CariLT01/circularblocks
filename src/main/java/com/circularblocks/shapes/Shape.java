package com.circularblocks.shapes;

import com.circularblocks.shapes.shapeSettings.ShapeSettings;
import com.circularblocks.types.Vector3;

public abstract class Shape {

    public final String name;
    public final String sideTextureName;
    public final String topTextureName;
    public final Vector3 size;
    public final ShapePlacementBehavior placementBehavior;

    public Shape(ShapeSettings settings) {
        this.name = settings.name();
        this.sideTextureName = settings.sideTextureName();
        this.topTextureName = settings.topTextureName();
        this.size = settings.size();
        this.placementBehavior = settings.placementBehavior();

    }

}
