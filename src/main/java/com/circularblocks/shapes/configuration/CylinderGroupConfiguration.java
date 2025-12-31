package com.circularblocks.shapes.configuration;

import com.circularblocks.types.Vector3;

import java.util.List;

public class CylinderGroupConfiguration extends ShapeGroupConfiguration {

    public final List<Integer> numSides;
    public final List<Boolean> centered;
    public final List<Float> repeatFrequency;

    public CylinderGroupConfiguration(List<Vector3> sizes, List<String> appendedNames, List<Integer> numSides, List<Boolean> centered, List<Float> repeatFrequency) {
        super(sizes, appendedNames);

        this.numSides = numSides;
        this.centered = centered;
        this.repeatFrequency = repeatFrequency;
    }



}
