package com.circularblocks.shapes;

import com.circularblocks.shapes.shapeSettings.CylinderShapeSettings;

public class CylinderShape extends Shape {

        public final float repeatFrequency;
        public final boolean centered;
        public final int numSides;
        public final boolean usePlanarMapping;

        public CylinderShape(CylinderShapeSettings settings) {
            super(settings.meshSettings());

            this.repeatFrequency = settings.repeatFrequency();
            this.centered = settings.centered();
            this.numSides = settings.sides();
            this.usePlanarMapping = settings.usePlanarMapping();

        }

}
