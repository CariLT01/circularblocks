package com.circularblocks;

public record CylinderType(
        String name,
        String sideTextureName,
        String topTextureName,
        float repeatFrequency,
        float sizeX,
        float sizeY,
        float sizeZ,
        boolean centered,
        int numSides
) { }
