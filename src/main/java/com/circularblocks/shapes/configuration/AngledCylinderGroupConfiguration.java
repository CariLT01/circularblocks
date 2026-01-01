package com.circularblocks.shapes.configuration;

import com.circularblocks.loaders.LoaderType;
import com.circularblocks.types.Vector3f;

import java.util.List;

public class AngledCylinderGroupConfiguration extends ShapeGroupConfiguration {

    public final List<Integer> numSides;
    public final List<Float> repeatFrequency;
    public final boolean usePlanarMapping;
    public final boolean faceDirection;

    public AngledCylinderGroupConfiguration(List<Vector3f> sizes, List<String> appendedNames, List<Integer> numSides, List<Float> repeatFrequency, boolean usePlanarMapping, LoaderType loaderType,
                                            boolean faceDirection) {
        super(sizes, appendedNames, loaderType);

        this.numSides = numSides;
        this.repeatFrequency = repeatFrequency;
        this.usePlanarMapping = usePlanarMapping;
        this.faceDirection = faceDirection;
    }



}
