package com.circularblocks.meshBuilders;

import com.circularblocks.shapes.QuarterCylinderShape;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class QuarterCylinderMeshBuilder implements MeshBuilder {

    public static String getMtlFileContents(QuarterCylinderShape cylinder) {
        return String.format("newmtl cylinder_sides\nmap_Kd %s\nnewmtl cylinder_caps\nmap_Kd %s",
                cylinder.sideTextureName, cylinder.topTextureName);
    }

    public static String buildMesh(QuarterCylinderShape cylinder) {
        float offsetX = 0.0f;
        float offsetY = 0.0f;
        float offsetZ = 0.0f;

        if (cylinder.centered) {
            offsetX = -(cylinder.size.x() / 2.0f);
            offsetY = -(cylinder.size.y() / 2.0f);
            offsetZ = -(cylinder.size.z() / 2.0f);
        }

        List<Vector3f> vertices = new ArrayList<>();
        List<Vector2f> UVs = new ArrayList<>();
        List<Vector3f> normals = new ArrayList<>();
        List<String> sideFaces = new ArrayList<>();
        List<String> capFaces = new ArrayList<>();

        final double angleRange = Math.PI / 2.0;

        // The "corner" of the quarter cylinder is now at the offset origin
        float centerX = offsetX;
        float centerZ = offsetZ;

        // --- 1. CURVED SIDES ---
        int tiles = Math.max(1, (int) Math.round(cylinder.repeatFrequency));
        int[] segmentsPerTile = new int[tiles];
        int baseValue = cylinder.numSides / tiles;
        int remainder = cylinder.numSides % tiles;

        for (int t = 0; t < tiles; t++) {
            segmentsPerTile[t] = baseValue + (t < remainder ? 1 : 0);
        }

        for (int t = 0; t < segmentsPerTile.length; t++) {
            int segs = segmentsPerTile[t];
            for (int k = 0; k < segs + 1; k++) {
                int globalI = 0;
                for (int i = 0; i < t; i++) globalI += segmentsPerTile[i];
                globalI += k;

                double angle = angleRange * globalI / cylinder.numSides;

                // Radius is now the FULL size.x and size.z
                double vx = Math.cos(angle) * cylinder.size.x() + centerX;
                double vz = Math.sin(angle) * cylinder.size.z() + centerZ;

                double uRaw = (angle / angleRange) * cylinder.repeatFrequency;
                double u = (k == segs) ? 1.0 : (uRaw - Math.floor(uRaw));

                double nx = Math.cos(angle);
                double nz = Math.sin(angle);

                // Bottom Vertex
                vertices.add(new Vector3f((float) vx, offsetY, (float) vz));
                UVs.add(new Vector2f((float) u, 0.0f));
                normals.add(new Vector3f((float) nx, 0.0f, (float) nz));

                // Top Vertex
                vertices.add(new Vector3f((float) vx, cylinder.size.y() + offsetY, (float) vz));
                UVs.add(new Vector2f((float) u, 1.0f));
                normals.add(new Vector3f((float) nx, 0.0f, (float) nz));
            }

            int tileFirst = vertices.size() - 2 * (segs + 1) + 1;
            for (int k = 0; k < segs; k++) {
                int b0 = tileFirst + (k * 2);
                int b1 = tileFirst + (k * 2) + 2;
                int t0 = b0 + 1;
                int t1 = b1 + 1;
                sideFaces.add(String.format("f %d/%d/%d %d/%d/%d %d/%d/%d", b0, b0, b0, t0, t0, t0, t1, t1, t1));
                sideFaces.add(String.format("f %d/%d/%d %d/%d/%d %d/%d/%d", b0, b0, b0, t1, t1, t1, b1, b1, b1));
            }
        }

        // --- 2. FLAT SIDE FACES (Inner Cut) ---
        // Side along X-axis (angle 0)
        addFlatSide(vertices, UVs, normals, sideFaces,
                new Vector3f(centerX, offsetY, centerZ),
                new Vector3f(centerX, offsetY + cylinder.size.y(), centerZ),
                new Vector3f(centerX + cylinder.size.x(), offsetY + cylinder.size.y(), centerZ),
                new Vector3f(centerX + cylinder.size.x(), offsetY, centerZ),
                new Vector3f(0, 0, -1));

        // Side along Z-axis (angle 90)
        addFlatSide(vertices, UVs, normals, sideFaces,
                new Vector3f(centerX, offsetY, centerZ),
                new Vector3f(centerX, offsetY, centerZ + cylinder.size.z()),
                new Vector3f(centerX, offsetY + cylinder.size.y(), centerZ + cylinder.size.z()),
                new Vector3f(centerX, offsetY + cylinder.size.y(), centerZ),
                new Vector3f(-1, 0, 0));

        // --- 3. TOP AND BOTTOM CAPS ---
        // Bottom cap
        int centerIndex = vertices.size() + 1;
        vertices.add(new Vector3f(centerX, offsetY, centerZ));
        UVs.add(new Vector2f(0.0f, 0.0f));
        normals.add(new Vector3f(0.0f, -1.0f, 0.0f));

        int ringStart = vertices.size() + 1;
        for (int i = 0; i <= cylinder.numSides; i++) {
            double angle = angleRange * i / cylinder.numSides;
            float x = (float) (Math.cos(angle) * cylinder.size.x() + centerX);
            float z = (float) (Math.sin(angle) * cylinder.size.z() + centerZ);
            vertices.add(new Vector3f(x, offsetY, z));
            UVs.add(new Vector2f((x - centerX) / cylinder.size.x(), (z - centerZ) / cylinder.size.z()));
            normals.add(new Vector3f(0.0f, -1.0f, 0.0f));
        }
        for (int i = 0; i < cylinder.numSides; i++) {
            int v1 = centerIndex, v2 = ringStart + i, v3 = ringStart + i + 1;
            capFaces.add(String.format("f %d/%d/%d %d/%d/%d %d/%d/%d", v1, v1, v1, v2, v2, v2, v3, v3, v3));
        }

        // Top cap
        int centerIndexTop = vertices.size() + 1;
        vertices.add(new Vector3f(centerX, cylinder.size.y() + offsetY, centerZ));
        UVs.add(new Vector2f(0.0f, 0.0f));
        normals.add(new Vector3f(0.0f, 1.0f, 0.0f));

        int ringStartTop = vertices.size() + 1;
        for (int i = 0; i <= cylinder.numSides; i++) {
            double angle = angleRange * i / cylinder.numSides;
            float x = (float) (Math.cos(angle) * cylinder.size.x() + centerX);
            float z = (float) (Math.sin(angle) * cylinder.size.z() + centerZ);
            vertices.add(new Vector3f(x, cylinder.size.y() + offsetY, z));
            UVs.add(new Vector2f((x - centerX) / cylinder.size.x(), (z - centerZ) / cylinder.size.z()));
            normals.add(new Vector3f(0.0f, 1.0f, 0.0f));
        }
        for (int i = 0; i < cylinder.numSides; i++) {
            int v1 = centerIndexTop, v2 = ringStartTop + i + 1, v3 = ringStartTop + i;
            capFaces.add(String.format("f %d/%d/%d %d/%d/%d %d/%d/%d", v1, v1, v1, v2, v2, v2, v3, v3, v3));
        }

        // --- 4. EXPORT ---
        StringBuilder builder = new StringBuilder();
        builder.append("# Full-Scale Quarter Cylinder\no cylinder_quarter\nmtllib ").append(cylinder.name).append(".mtl\n");
        for (Vector3f v : vertices) builder.append(String.format("v %f %f %f\n", v.x(), v.y(), v.z()));
        for (Vector2f uv : UVs) builder.append(String.format("vt %f %f\n", uv.x(), uv.y()));
        for (Vector3f n : normals) builder.append(String.format("vn %f %f %f\n", n.x(), n.y(), n.z()));

        builder.append("usemtl cylinder_sides\n");
        for (String face : sideFaces) builder.append(face).append("\n");
        builder.append("usemtl cylinder_caps\n");
        for (String face : capFaces) builder.append(face).append("\n");

        return builder.toString();
    }

    private static void addFlatSide(List<Vector3f> verts, List<Vector2f> uvs, List<Vector3f> norms, List<String> faces,
                                    Vector3f v1, Vector3f v2, Vector3f v3, Vector3f v4, Vector3f normal) {
        int start = verts.size() + 1;
        verts.add(v1); verts.add(v2); verts.add(v3); verts.add(v4);
        uvs.add(new Vector2f(0, 0)); uvs.add(new Vector2f(0, 1)); uvs.add(new Vector2f(1, 1)); uvs.add(new Vector2f(1, 0));
        norms.add(normal); norms.add(normal); norms.add(normal); norms.add(normal);

        faces.add(String.format("f %d/%d/%d %d/%d/%d %d/%d/%d", start, start, start, start+1, start+1, start+1, start+2, start+2, start+2));
        faces.add(String.format("f %d/%d/%d %d/%d/%d %d/%d/%d", start, start, start, start+2, start+2, start+2, start+3, start+3, start+3));
    }
}