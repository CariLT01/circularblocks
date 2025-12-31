package com.circularblocks.shapes.configuration;

import com.circularblocks.types.Vector3f;

import java.util.List;

public class CylinderGroupConfiguration extends ShapeGroupConfiguration {

    public final List<Integer> numSides;
    public final List<Boolean> centered;
    public final List<Float> repeatFrequency;
    public final boolean usePlanarMapping;

    public CylinderGroupConfiguration(List<Vector3f> sizes, List<String> appendedNames, List<Integer> numSides, List<Boolean> centered, List<Float> repeatFrequency, boolean usePlanarMapping) {
        super(sizes, appendedNames);

        this.numSides = numSides;
        this.centered = centered;
        this.repeatFrequency = repeatFrequency;
        this.usePlanarMapping = usePlanarMapping;
    }



}
