package com.circularblocks.shapes;

import com.circularblocks.shapes.shapeSettings.AngledCylinderShapeSettings;
import com.circularblocks.shapes.shapeSettings.ConnectorCylinderShapeSettings;

public class ConnectorCylinderShape extends Shape {

    public final float repeatFrequency;
    public final boolean centered;
    public final int numSides;
    public final boolean usePlanarMapping;

    public ConnectorCylinderShape(ConnectorCylinderShapeSettings settings) {
        super(settings.meshSettings());

        this.repeatFrequency = settings.repeatFrequency();
        this.centered = settings.centered();
        this.numSides = settings.sides();
        this.usePlanarMapping = settings.usePlanarMapping();

    }

}
