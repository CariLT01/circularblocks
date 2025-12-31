package com.circularblocks.shapes;

public class CylinderShape extends Shape {

        public final float repeatFrequency;
        public final boolean centered;
        public final int numSides;

        public CylinderShape(CylinderShapeSettings settings) {
            super(settings.meshSettings());

            this.repeatFrequency = settings.repeatFrequency();
            this.centered = settings.centered();
            this.numSides = settings.sides();

        }

}
