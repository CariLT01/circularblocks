package com.circularblocks.shapes.shapeSettings;

import com.circularblocks.shapes.ShapePlacementBehavior;
import com.circularblocks.types.Vector3;

public record ShapeSettings(String name, String sideTextureName, String topTextureName, Vector3 size, ShapePlacementBehavior placementBehavior) {
}
