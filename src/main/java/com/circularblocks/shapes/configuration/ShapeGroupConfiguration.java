package com.circularblocks.shapes.configuration;

import com.circularblocks.types.Vector3f;

import java.util.List;

public abstract class ShapeGroupConfiguration {

    public final List<Vector3f> sizes;
    public final List<String> appendedNames;

    public ShapeGroupConfiguration(List<Vector3f> sizes, List<String> appendedNames) {
        this.sizes = sizes;
        this.appendedNames = appendedNames;
    }

}
