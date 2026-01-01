package com.circularblocks.dataGeneration;

import com.circularblocks.meshBuilders.AngledCylinderMeshBuilder;
import com.circularblocks.meshBuilders.CylinderMeshBuilder;
import com.circularblocks.meshBuilders.QuarterCylinderMeshBuilder;
import com.circularblocks.shapes.AngledCylinderShape;
import com.circularblocks.shapes.CylinderShape;
import com.circularblocks.shapes.QuarterCylinderShape;
import com.circularblocks.shapes.Shape;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

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
        } else if (shape instanceof AngledCylinderShape) {
            System.out.println("KKKKK"+shape.name+":"+shape.getClass().getName());

            // Angled Cylinder
            String objFileContents;
            if(shape.name.contains("face")){
                objFileContents = AngledCylinderMeshBuilder.buildMeshFace((AngledCylinderShape) shape);
            }
            else{
                objFileContents = AngledCylinderMeshBuilder.buildMesh((AngledCylinderShape) shape);
            }
            String mtlFileContents = AngledCylinderMeshBuilder.getMtlFileContents((AngledCylinderShape) shape);

            return new ShapeBuiltMeshResult(mtlFileContents, objFileContents);
        }

        throw new IllegalArgumentException("Shape type not recognized in ShapeMeshBuilderDispatcher");
    }
}
