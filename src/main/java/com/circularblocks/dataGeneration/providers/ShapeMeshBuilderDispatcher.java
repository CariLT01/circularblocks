package com.circularblocks.dataGeneration.providers;

import com.circularblocks.meshBuilders.*;
import com.circularblocks.shapes.*;

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
        } else if(shape instanceof ConnectorCylinderShape){
            System.out.println("KKKKK2"+shape.name+":"+shape.getClass().getName());
            String objFileContents;
            objFileContents = ConnectorCylinderMeshBuilder.buildMesh((ConnectorCylinderShape) shape);
            String mtlFileContents = ConnectorCylinderMeshBuilder.getMtlFileContents((ConnectorCylinderShape)shape);

            return new ShapeBuiltMeshResult(mtlFileContents, objFileContents);
        }

        throw new IllegalArgumentException("Shape type not recognized in ShapeMeshBuilderDispatcher");
    }
}
