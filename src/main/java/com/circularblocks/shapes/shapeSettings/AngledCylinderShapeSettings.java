package com.circularblocks.shapes.shapeSettings;

public record AngledCylinderShapeSettings(ShapeSettings meshSettings, int sides, boolean centered, float repeatFrequency, boolean usePlanarMapping) {
}