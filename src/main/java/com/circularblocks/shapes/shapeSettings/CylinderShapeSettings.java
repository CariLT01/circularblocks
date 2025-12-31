package com.circularblocks.shapes.shapeSettings;

public record CylinderShapeSettings(ShapeSettings meshSettings, int sides, boolean centered, float repeatFrequency, boolean usePlanarMapping) {
}
