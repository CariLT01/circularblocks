package com.circularblocks.shapes.configuration;

import com.circularblocks.loaders.LoaderType;
import com.circularblocks.types.Vector3f;

import java.util.List;

public abstract class ShapeGroupConfiguration {

    public final List<Vector3f> sizes;
    public final List<String> appendedNames;
    public final LoaderType loaderType;

    public ShapeGroupConfiguration(List<Vector3f> sizes, List<String> appendedNames, LoaderType loaderType) {
        this.sizes = sizes;
        this.appendedNames = appendedNames;
        this.loaderType = loaderType;
    }

}
