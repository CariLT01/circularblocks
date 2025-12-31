package com.circularblocks.meshBuilders;

import com.circularblocks.shapes.CylinderShape;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class CylinderMeshBuilder implements  MeshBuilder {

    private static Vector2f diskToSquareUV(float dx, float dz) {
        double r = Math.hypot(dx, dz);

        if (r == 0) return new Vector2f(0.5f, 0.5f);
        double cos_t = dx / r;
        double sin_t = dz / r;
        double denom = Math.max(Math.abs(cos_t), Math.abs(sin_t));
        double factor = (denom != 0 ? (r / denom) : 0.0);
        double ux = cos_t * factor;
        double uz = sin_t * factor;

        return new Vector2f(
                (float) (0.5 + 0.5 * ux),
                (float) (0.5 + 0.5 * uz)
        );
    }

    public static String getMtlFileContents(CylinderShape cylinder) {
        return String.format("newmtl cylinder_sides\nmap_Kd %s\nnewmtl cylinder_caps\nmap_Kd %s", cylinder.sideTextureName, cylinder.topTextureName);
    }

    public static String buildMesh(CylinderShape cylinder) {

        float offsetX = 0.0f;
        float offsetY = 0.0f;
        float offsetZ = 0.0f;

        if (cylinder.centered) {
            offsetX = (float) (0.5 - (cylinder.size.x() / 2.0f));
            offsetY = (float) (0.5 - (cylinder.size.y() / 2.0f));
            offsetZ = (float) (0.5 - (cylinder.size.z() / 2.0f));
        }

        List<Vector3f> vertices = new ArrayList<>();
        List<Vector2f> UVs = new ArrayList<>();
        List<Vector3f> normals = new ArrayList<>();
        List<String> sideFaces = new ArrayList<>();
        List<String> capFaces = new ArrayList<>();

        // Geometry

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
                for (int i = 0; i < t; i++) {
                    globalI += segmentsPerTile[i];
                }
                globalI += k;

                double angle = 2 * Math.PI * globalI / cylinder.numSides;
                double vx = (0.5 + 0.5 * Math.cos(angle)) * cylinder.size.x() + offsetX;
                double vz = (0.5 + 0.5 * Math.sin(angle)) * cylinder.size.z() + offsetZ;
                double uRaw = (angle / (2 * Math.PI)) * cylinder.repeatFrequency;
                double u = (k == segs) ? 1.0 : (uRaw - Math.floor(uRaw));
                double nx = Math.cos(angle);
                double nz = Math.sin(angle);

                vertices.add(
                        new Vector3f(
                                (float) (vx),
                                (float) (0.0 + offsetY),
                                (float) (vz)
                        )
                );

                UVs.add(new Vector2f((float) u, 0.0f));
                normals.add(new Vector3f((float)nx, 0.0f, (float)nz));
                vertices.add(
                        new Vector3f(
                                (float) (vx),
                                (float) (cylinder.size.y() + offsetY),
                                (float) (vz)
                        )
                );
                UVs.add(new Vector2f((float)u, 1.0f));
                normals.add(new Vector3f((float)nx, 0.0f, (float)nz));

            }

            int tileFirst = vertices.size() - 2 * (segs + 1) + 1;
            for (int k = 0; k < segs; k++) {
                int b0 = tileFirst + (k * 2);
                int b1 = tileFirst + (k * 2) + 2;
                int t0 = b0 + 1;
                int t1 = b1 + 1;

                sideFaces.add(
                        String.format("f %s/%s/%s %s/%s/%s %s/%s/%s", b0, b0, b0, t0, t0, t0, t1,t1, t1)
                );
                sideFaces.add(
                        String.format("f %s/%s/%s %s/%s/%s %s/%s/%s", b0, b0, b0, t1, t1, t1, b1, b1, b1)
                );


            }

        }

        // CAPS
        // Bottom cap

        int centerIndex = vertices.size() + 1;
        vertices.add(
                new Vector3f(
                        0.5f * cylinder.size.x() + offsetX, 0.0f + offsetY, 0.5f * cylinder.size.z() + offsetZ
                )
        );

        UVs.add(new Vector2f(0.5f, 0.5f));
        normals.add(new Vector3f(0.0f, -1.0f, 0.0f));

        int ringStart = vertices.size() + 1;
        for (int i = 0; i < cylinder.numSides; i++) {
            double angle = 2.0 * Math.PI * i / cylinder.numSides;
            vertices.add(
                    new Vector3f(
                            (float) ((0.5 + 0.5 * Math.cos(angle)) * cylinder.size.x() + offsetX),
                            (float) (0.0 + offsetY),
                            (float) ((0.5 + 0.5 * Math.sin(angle)) * cylinder.size.z() + offsetZ)
                    )
            );
            UVs.add(
                    diskToSquareUV((float)Math.cos(angle), (float)Math.sin(angle))
            );
            normals.add(new Vector3f(0.0f, -1.0f, 0.0f));

        }

        for (int i = 0; i < cylinder.numSides; i++) {
            int v1 = centerIndex;
            int v2 = ringStart + i;
            int v3 = ringStart + ((i + 1) % cylinder.numSides);
            capFaces.add(String.format("f %s/%s/%s %s/%s/%s %s/%s/%s", v1, v1, v1, v2, v2, v2, v3, v3, v3));
        }

        // Top cap
        int centerIndexTop = vertices.size() + 1;
        vertices.add(
                new Vector3f(
                        0.5f * cylinder.size.x() + offsetX,
                        cylinder.size.y() + offsetY,
                        0.5f * cylinder.size.z() + offsetZ
                )
        );

        UVs.add(
                new Vector2f(0.5f, 0.5f)
        );
        normals.add(new Vector3f(0.0f, 1.0f, 0.0f));
        int ringStartTop = vertices.size() + 1;
        for (int i = 0; i < cylinder.numSides; i++) {
            double angle = 2 * Math.PI * i / cylinder.numSides;
            vertices.add(
                    new Vector3f(
                            (float) ((0.5 + 0.5 * Math.cos(angle)) * cylinder.size.x() + offsetX),
                            (float) (cylinder.size.y() + offsetY),
                            (float) ((0.5 + 0.5 * Math.sin(angle)) * cylinder.size.z() + offsetZ)
                    )
            );
            UVs.add(
                    diskToSquareUV((float)Math.cos(angle), (float)Math.sin(angle))
            );
            normals.add(new Vector3f(0.0f, 1.0f, 0.0f));
        }

        for (int i = 0; i < cylinder.numSides; i++) {
            int v1 = centerIndexTop;
            int v2 = ringStartTop + ((i + 1) % cylinder.numSides);
            int v3 = ringStartTop + i;

            capFaces.add(String.format("f %s/%s/%s %s/%s/%s %s/%s/%s", v1, v1, v1, v2, v2, v2, v3, v3, v3));
        }

        // Write to file

        StringBuilder builder = new StringBuilder();

        builder.append("# Cylinder\n");
        builder.append(String.format("o cylinder\nmtllib %s.mtl\n", cylinder.name));
        for (Vector3f vertex : vertices) {
            builder.append(String.format("v %s %s %s\n", vertex.x(), vertex.y(), vertex.z()));
        }
        for (Vector2f uv : UVs) {
            builder.append(String.format("vt %s %s\n", uv.x(), uv.y()));
        }
        for (Vector3f normal : normals) {
            builder.append(String.format("vn %s %s %s\n", normal.x(), normal.y(), normal.z()));
        }

        builder.append("usemtl cylinder_sides\n");
        for (String face : sideFaces) {
            builder.append(face).append("\n");
        }
        builder.append("usemtl cylinder_caps\n");
        for (String face : capFaces) {
            builder.append(face).append("\n");
        }

        return builder.toString();




    }
}
