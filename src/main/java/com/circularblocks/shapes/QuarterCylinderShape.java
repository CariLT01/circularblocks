package com.circularblocks.shapes;


import com.circularblocks.shapes.shapeSettings.QuarterCylinderShapeSettings;

public class QuarterCylinderShape extends Shape {
    public final float repeatFrequency;
    public final boolean centered;
    public final int numSides;

    public QuarterCylinderShape(QuarterCylinderShapeSettings settings) {
        super(settings.meshSettings());

        this.repeatFrequency = settings.repeatFrequency();
        this.centered = settings.centered();
        this.numSides = settings.sides();
    }
}
