package com.circularblocks.shapes;

import com.circularblocks.shapes.shapeSettings.AngledCylinderShapeSettings;

public class AngledCylinderShape extends Shape {

        public final float repeatFrequency;
        public final boolean centered;
        public final int numSides;
        public final boolean usePlanarMapping;

        public AngledCylinderShape(AngledCylinderShapeSettings settings) {
            super(settings.meshSettings());

            this.repeatFrequency = settings.repeatFrequency();
            this.centered = settings.centered();
            this.numSides = settings.sides();
            this.usePlanarMapping = settings.usePlanarMapping();

        }

}
