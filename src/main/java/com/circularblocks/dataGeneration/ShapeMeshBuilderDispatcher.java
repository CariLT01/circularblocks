package com.circularblocks.dataGeneration;

import com.circularblocks.meshBuilders.CylinderMeshBuilder;
import com.circularblocks.meshBuilders.QuarterCylinderMeshBuilder;
import com.circularblocks.shapes.CylinderShape;
import com.circularblocks.shapes.QuarterCylinderShape;
import com.circularblocks.shapes.Shape;

public class ShapeMeshBuilderDispatcher {

    public static ShapeBuiltMeshResult buildMesh(Shape shape) {

        if (shape instanceof CylinderShape) {

            // Cylinder
            String objFileContents = CylinderMeshBuilder.buildMesh((CylinderShape) shape);
            String mtlFileContents = CylinderMeshBuilder.getMtlFileContents((CylinderShape) shape);

            return new ShapeBuiltMeshResult(mtlFileContents, objFileContents);
        } else if (shape instanceof QuarterCylinderShape) {

            // Quarter Cylinder
            String objFileContents = QuarterCylinderMeshBuilder.buildMesh((QuarterCylinderShape) shape);
            String mtlFileContents = QuarterCylinderMeshBuilder.getMtlFileContents((QuarterCylinderShape) shape);

            return new ShapeBuiltMeshResult(mtlFileContents, objFileContents);
        }

        throw new IllegalArgumentException("Shape type not recognized in ShapeMeshBuilderDispatcher");
    }

}
