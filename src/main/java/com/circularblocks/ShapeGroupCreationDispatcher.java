package com.circularblocks;

import com.circularblocks.shapes.*;
import com.circularblocks.shapes.configuration.CylinderGroupConfiguration;
import com.circularblocks.shapes.configuration.ShapeGroupConfiguration;
import com.circularblocks.shapes.shapeSettings.CylinderShapeSettings;
import com.circularblocks.shapes.shapeSettings.ShapeSettings;
import com.circularblocks.types.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class ShapeGroupCreationDispatcher {

    public static List<Shape> createShapesForGroup(ShapeGroupConfiguration groupType, ShapeAppareance appareance, String baseName) {

        List<Shape> shapeList = new ArrayList<>();

        if (groupType instanceof CylinderGroupConfiguration) {
            for (int i = 0; i < groupType.sizes.size(); i++) {

                CylinderGroupConfiguration cylinderConfig = (CylinderGroupConfiguration) groupType;

                String suffixedName = cylinderConfig.appendedNames.get(i);
                Vector3f size = cylinderConfig.sizes.get(i);
                int numSides = cylinderConfig.numSides.get(i);
                boolean centered = cylinderConfig.centered.get(i);
                float repeatFreq = cylinderConfig.repeatFrequency.get(i);

                shapeList.add(
                        new CylinderShape(
                                new CylinderShapeSettings(
                                        new ShapeSettings(
                                                baseName + suffixedName,
                                                appareance.sideTextureName(),
                                                appareance.topTextureName(),
                                                size,
                                                ShapePlacementBehavior.ROTATED_PILLAR_BLOCK
                                        ),
                                        numSides,
                                        centered,
                                        repeatFreq,
                                        cylinderConfig.usePlanarMapping


                                )
                        )
                );

            }


        } else {
            throw new IllegalArgumentException("Unrecognized configuration type");
        }

        return shapeList;
    }

}
