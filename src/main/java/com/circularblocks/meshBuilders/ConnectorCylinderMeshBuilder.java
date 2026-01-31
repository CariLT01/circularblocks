package com.circularblocks.meshBuilders;

import com.circularblocks.loaders.LoaderType;
import com.circularblocks.shapes.AngledCylinderShape;
import com.circularblocks.shapes.ConnectorCylinderShape;
import com.circularblocks.types.Vector2f;
import com.circularblocks.types.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class ConnectorCylinderMeshBuilder implements MeshBuilder {

    // Helper to rotate a point around the X-axis
    private static Vector3f rotateX(float x, float y, float z, double angleRad) {
        float cos = (float) Math.cos(angleRad);
        float sin = (float) Math.sin(angleRad);
        // Rotation around X: x stays same, y and z change
        float newY = y * cos - z * sin;
        float newZ = y * sin + z * cos;
        return new Vector3f(x, newY, newZ);
    }

    private static Vector2f diskToSquareUV(float dx, float dz) {
        double r = Math.hypot(dx, dz);
        if (r == 0) return new Vector2f(0.5f, 0.5f);
        double cos_t = dx / r;
        double sin_t = dz / r;
        double denom = Math.max(Math.abs(cos_t), Math.abs(sin_t));
        double factor = (denom != 0 ? (r / denom) : 0.0);
        double ux = cos_t * factor;
        double uz = sin_t * factor;

        return new Vector2f((float) (0.5 + 0.5 * ux), (float) (0.5 + 0.5 * uz));
    }

    public static String getMtlFileContents(ConnectorCylinderShape cylinder) {

        if (cylinder.loaderType == LoaderType.MIMIC_MESH_LOADER) {
            return "newmtl cylinder_sides\nmap_Kd circularblocks:block/mimic_frame_x\nnewmtl cylinder_caps\nmap_Kd circularblocks:block/mimic_frame_x_top";
        }

        return String.format("newmtl cylinder_sides\nmap_Kd %s\nnewmtl cylinder_caps\nmap_Kd %s",

                cylinder.sideTextureName, cylinder.topTextureName);

    }

    public static String buildMesh(ConnectorCylinderShape cylinder) {
        float radius = 0.25f;
        float extension = radius;
        int sides = cylinder.numSides;
        int ringVertexCount = sides + 1; // Vertices per ring

        List<Vector3f> vertices = new ArrayList<>();
        List<Vector2f> uvs = new ArrayList<>();
        List<Vector3f> normals = new ArrayList<>();

        // 1. GENERATE 4 RINGS
        for (int ring = 0; ring < 4; ring++) {
            for (int i = 0; i <= sides; i++) {
                double angle = 2.0 * Math.PI * i / sides;
                float cos = (float) Math.cos(angle);
                float sin = (float) Math.sin(angle);

                // Ring definitions
                if (ring == 0) { // Vertical Bottom
                    vertices.add(new Vector3f(0.5f + (cos * radius), 0.0f, 0.5f + (sin * radius)));
                    normals.add(new Vector3f(cos, 0, sin));
                } else if (ring == 1) { // Vertical Top
                    vertices.add(new Vector3f(0.5f + (cos * radius), 0.5f, 0.5f + (sin * radius)));
                    normals.add(new Vector3f(cos, 0, sin));
                } else if (ring == 2) { // Horizontal Back (Extension)
                    vertices.add(new Vector3f(0.5f + (cos * radius), 0.5f + (sin * radius), 0.5f - extension));
                    normals.add(new Vector3f(cos, sin, 0));
                } else { // Horizontal Front
                    vertices.add(new Vector3f(0.5f + (cos * radius), 0.5f + (sin * radius), 1.0f));
                    normals.add(new Vector3f(cos, sin, 0));
                }
                uvs.add(new Vector2f((float)i / sides, (float)ring / 3.0f));
            }
        }

        // 2. ADD CAP CENTER VERTICES
        int totalRingVerts = vertices.size();

        vertices.add(new Vector3f(0.5f, 0.0f, 0.5f));             // Center 0: Bottom
        vertices.add(new Vector3f(0.5f, 0.5f, 0.5f));             // Center 1: Top Joint
        vertices.add(new Vector3f(0.5f, 0.5f, 0.5f - extension)); // Center 2: Back Ext
        vertices.add(new Vector3f(0.5f, 0.5f, 1.0f));             // Center 3: Front End

        for(int j=0; j<4; j++) uvs.add(new Vector2f(0.5f, 0.5f));
        normals.add(new Vector3f(0, -1, 0)); // Bottom Normal
        normals.add(new Vector3f(0, 1, 0));  // Top Normal
        normals.add(new Vector3f(0, 0, -1)); // Back Normal
        normals.add(new Vector3f(0, 0, 1));  // Front Normal

        StringBuilder sb = new StringBuilder("# Fixed L-Joint Caps\n");
        // Standard OBJ exports
        for (Vector3f v : vertices) sb.append(String.format("v %.4f %.4f %.4f\n", v.x(), v.y(), v.z()));
        for (Vector2f vt : uvs) sb.append(String.format("vt %.4f %.4f\n", vt.x(), vt.y()));
        for (Vector3f vn : normals) sb.append(String.format("vn %.4f %.4f %.4f\n", vn.x(), vn.y(), vn.z()));

        // 3. SIDES GENERATION
        sb.append("usemtl cylinder_sides\n");
        for (int r = 0; r < 3; r++) {
            int r0 = r * ringVertexCount;
            int r1 = (r + 1) * ringVertexCount;
            for (int i = 0; i < sides; i++) {
                int v1 = r0 + i + 1; int v2 = r0 + i + 2;
                int v3 = r1 + i + 1; int v4 = r1 + i + 2;

                if (r == 0) {
                    // Vertical segment (Ring 0 -> 1): Standard Winding (CCW)
                    // Tri 1: v1 -> v3 -> v4
                    sb.append(String.format("f %d/%d/%d %d/%d/%d %d/%d/%d\n", v1, v1, v1, v3, v3, v3, v4, v4, v4));
                    // Tri 2: v1 -> v4 -> v2
                    sb.append(String.format("f %d/%d/%d %d/%d/%d %d/%d/%d\n", v1, v1, v1, v4, v4, v4, v2, v2, v2));
                } else {
                    // Horizontal segments (Ring 1 -> 2, Ring 2 -> 3): Flipped Winding
                    // Tri 1: v1 -> v4 -> v3
                    sb.append(String.format("f %d/%d/%d %d/%d/%d %d/%d/%d\n", v1, v1, v1, v4, v4, v4, v3, v3, v3));
                    // Tri 2: v1 -> v2 -> v4
                    sb.append(String.format("f %d/%d/%d %d/%d/%d %d/%d/%d\n", v1, v1, v1, v2, v2, v2, v4, v4, v4));
                }
            }
        }
        // 4. CAPS GENERATION (FIXED INDICES)
        sb.append("usemtl cylinder_caps\n");
        int idxBot = totalRingVerts + 1;
        int idxTop = totalRingVerts + 2;
        int idxBck = totalRingVerts + 3;
        int idxFrnt = totalRingVerts + 4;

        for (int i = 0; i < sides; i++) {
            int i1 = i + 1;
            int i2 = i + 2;

            // 1. BOTTOM CAP (Ring 0) -> Faces -Y
            // Correct Winding: Center -> i1 -> i2
            sb.append(String.format("f %d/%d/%d %d/%d/%d %d/%d/%d\n", idxBot, idxBot, idxBot, i1, i1, idxBot, i2, i2, idxBot));

            // 2. VERTICAL TOP (Ring 1) -> Faces +Y
            // Correct Winding: Center -> i2 -> i1
            int vT1 = ringVertexCount + i1; int vT2 = ringVertexCount + i2;
            sb.append(String.format("f %d/%d/%d %d/%d/%d %d/%d/%d\n", idxTop, idxTop, idxTop, vT2, vT2, idxTop, vT1, vT1, idxTop));

            // 3. HORIZONTAL BACK (Ring 2) -> Faces -Z
            // Correct Winding: Center -> i2 -> i1
            int vB1 = (2 * ringVertexCount) + i1; int vB2 = (2 * ringVertexCount) + i2;
            sb.append(String.format("f %d/%d/%d %d/%d/%d %d/%d/%d\n", idxBck, idxBck, idxBck, vB2, vB2, idxBck, vB1, vB1, idxBck));

            // 4. HORIZONTAL FRONT (Ring 3) -> Faces +Z
            // Correct Winding: Center -> i1 -> i2
            int vF1 = (3 * ringVertexCount) + i1; int vF2 = (3 * ringVertexCount) + i2;
            sb.append(String.format("f %d/%d/%d %d/%d/%d %d/%d/%d\n", idxFrnt, idxFrnt, idxFrnt, vF1, vF1, idxFrnt, vF2, vF2, idxFrnt));
        }
        StringBuilder sb1 = new StringBuilder("# Oblique Cylinder\n");
        sb1.append(String.format("o cylinder\nmtllib %s.mtl\n", cylinder.name));
        for (Vector3f v : vertices) sb1.append(String.format("v %.4f %.4f %.4f\n", v.x(), v.y(), v.z()));
        for (Vector2f vt : uvs) sb1.append(String.format("vt %.4f %.4f\n", vt.x(), vt.y()));
        for (Vector3f vn : normals) sb1.append(String.format("vn %.4f %.4f %.4f\n", vn.x(), vn.y(), vn.z()));
        sb1.append("usemtl cylinder_sides\n").append(String.join("\n", sb)).append("\n");
        sb1.append("usemtl cylinder_caps\n").append(String.join("\n", sb)).append("\n");
        return sb1.toString();
    }

    public static String buildMeshFace(AngledCylinderShape cylinder) {
        boolean usePlanarMapping = cylinder.usePlanarMapping;

        float radius = 0.25f;
        int sides = cylinder.numSides;

        // Define the centers of our three rings
        // 0: Bottom Start, 1: The Miter Corner, 2: The Horizontal End
        Vector3f startC = new Vector3f(0.5f, 0.0f, 0.5f);
        Vector3f jointC = new Vector3f(0.5f, 0.5f, 0.5f);
        Vector3f endC   = new Vector3f(0.5f, 0.5f, 1.0f);

        List<Vector3f> vertices = new ArrayList<>();
        List<Vector2f> uvs = new ArrayList<>();
        List<Vector3f> normals = new ArrayList<>();

        // --- 1. GENERATE THE 3 RINGS ---
        for (int ring = 0; ring < 3; ring++) {
            for (int i = 0; i <= sides; i++) {
                double angle = 2.0 * Math.PI * i / sides;
                float cos = (float) Math.cos(angle);
                float sin = (float) Math.sin(angle);

                float vx, vy, vz;
                float nx, ny, nz;

                if (ring == 0) { // BOTTOM RING (Vertical)
                    vx = startC.x() + (cos * radius);
                    vy = startC.y();
                    vz = startC.z() + (sin * radius);
                    nx = cos; ny = 0; nz = sin;
                } else if (ring == 1) { // MITER JOINT (45-degree cut)
                    vx = jointC.x() + (cos * radius);
                    // Transitioning sin(angle) from Z-axis to Y-axis
                    vy = jointC.y() + (sin * radius);
                    vz = jointC.z() + (sin * radius);

                    // Miter normal (bisector of vertical and horizontal)
                    nx = cos;
                    ny = sin * 0.7071f;
                    nz = sin * 0.7071f;
                } else { // END RING (Horizontal)
                    vx = endC.x() + (cos * radius);
                    vy = endC.y() + (sin * radius);
                    vz = endC.z();
                    nx = cos; ny = sin; nz = 0;
                }

                vertices.add(new Vector3f(vx, vy, vz));
                normals.add(new Vector3f(nx, ny, nz));
                uvs.add(new Vector2f((float)i / sides, (float)ring / 2.0f));
            }
        }

        // --- 2. ADD CAP CENTERS (For Solidity) ---
        int sideVertCount = vertices.size();

        // Bottom Center Vertex
        vertices.add(startC);
        uvs.add(new Vector2f(0.5f, 0.5f));
        normals.add(new Vector3f(0, -1, 0));
        int bottomCenterIdx = sideVertCount + 1;

        // End Center Vertex
        vertices.add(endC);
        uvs.add(new Vector2f(0.5f, 0.5f));
        normals.add(new Vector3f(0, 0, 1));
        int endCenterIdx = sideVertCount + 2;

        // --- 3. GENERATE FACES ---
        StringBuilder faceBuilder = new StringBuilder();

        // Side Faces (Quads between rings)
        for (int r = 0; r < 2; r++) {
            int r0 = r * (sides + 1);
            int r1 = (r + 1) * (sides + 1);
            for (int i = 0; i < sides; i++) {
                int v1 = r0 + i + 1;
                int v2 = r0 + i + 2;
                int v3 = r1 + i + 1;
                int v4 = r1 + i + 2;
                faceBuilder.append(String.format("f %d/%d/%d %d/%d/%d %d/%d/%d\n", v1, v1, v1, v3, v3, v3, v4, v4, v4));
                faceBuilder.append(String.format("f %d/%d/%d %d/%d/%d %d/%d/%d\n", v1, v1, v1, v4, v4, v4, v2, v2, v2));
            }
        }

        // Cap Faces (Triangle Fans)
        StringBuilder capBuilder = new StringBuilder();
        for (int i = 0; i < sides; i++) {
            // Bottom Cap (Clockwise)
            int b1 = i + 1;
            int b2 = i + 2;
            capBuilder.append(String.format("f %d/%d/%d %d/%d/%d %d/%d/%d\n", bottomCenterIdx, bottomCenterIdx, bottomCenterIdx, b2, b2, b2, b1, b1, b1));

            // End Cap (Counter-Clockwise)
            int e1 = (2 * (sides + 1)) + i + 1;
            int e2 = (2 * (sides + 1)) + i + 2;
            capBuilder.append(String.format("f %d/%d/%d %d/%d/%d %d/%d/%d\n", endCenterIdx, endCenterIdx, endCenterIdx, e1, e1, e1, e2, e2, e2));
        }

        StringBuilder sb = new StringBuilder("# Oblique Cylinder\n");
        sb.append(String.format("o cylinder\nmtllib %s.mtl\n", cylinder.name));
        for (Vector3f v : vertices) sb.append(String.format("v %.4f %.4f %.4f\n", v.x(), v.y(), v.z()));
        for (Vector2f vt : uvs) sb.append(String.format("vt %.4f %.4f\n", vt.x(), vt.y()));
        for (Vector3f vn : normals) sb.append(String.format("vn %.4f %.4f %.4f\n", vn.x(), vn.y(), vn.z()));
        sb.append("usemtl cylinder_sides\n").append(String.join("\n", faceBuilder)).append("\n");
        sb.append("usemtl cylinder_caps\n").append(String.join("\n", capBuilder)).append("\n");
        return sb.toString();
    }
}
