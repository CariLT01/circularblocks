package com.circularblocks.meshBuilders;

import com.circularblocks.shapes.AngledCylinderShape;
import com.circularblocks.shapes.CylinderShape;
import com.circularblocks.types.Vector2f;
import com.circularblocks.types.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class AngledCylinderMeshBuilder implements MeshBuilder {

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

    public static String getMtlFileContents(AngledCylinderShape cylinder) {

        return String.format("newmtl cylinder_sides\nmap_Kd %s\nnewmtl cylinder_caps\nmap_Kd %s",

                cylinder.sideTextureName, cylinder.topTextureName);

    }

    public static String buildMesh(AngledCylinderShape cylinder) {
        boolean usePlanarMapping = cylinder.usePlanarMapping;

        float radius = 0.125f; // Diameter 0.25
        float height = cylinder.size.y();

        // Calculate how much the top ring shifts to achieve a 23-degree lean
        // Shift = height * tan(angle)

        // 1. Calculate the angle in radians
        double radians = Math.atan(cylinder.size.x()/cylinder.size.z());

        // 2. Convert radians to degrees
        double degrees = Math.toDegrees(radians);
        float tiltAngleDeg = (float) degrees;
        float horizontalShift = (float) (height * Math.tan(Math.toRadians(tiltAngleDeg)));

        float offsetX = cylinder.centered ? 0.5f : 0.0f;
        float offsetY = 0.0f;
        float offsetZ = cylinder.centered ? 0.5f : 0.0f;

        List<Vector3f> vertices = new ArrayList<>();
        List<Vector2f> UVs = new ArrayList<>();
        List<Vector3f> normals = new ArrayList<>();
        List<String> sideFaces = new ArrayList<>();
        List<String> capFaces = new ArrayList<>();

        // --- SIDES GEOMETRY ---
        int tiles = Math.max(1, (int) Math.round(cylinder.repeatFrequency));
        int baseValue = cylinder.numSides / tiles;
        int remainder = cylinder.numSides % tiles;

        // Normal for the tilted sides (perpendicular to the lean)
        // A 23 deg lean means the side normal is rotated -23 deg from horizontal
        float cosA = (float) Math.cos(Math.toRadians(tiltAngleDeg));
        float sinA = (float) Math.sin(Math.toRadians(tiltAngleDeg));

        for (int t = 0; t < tiles; t++) {
            int segs = baseValue + (t < remainder ? 1 : 0);
            for (int k = 0; k <= segs; k++) {
                int globalI = 0;
                for (int i = 0; i < t; i++) globalI += (baseValue + (i < remainder ? 1 : 0));
                globalI += k;

                double angle = 2 * Math.PI * globalI / cylinder.numSides;
                float vx = (float) (Math.cos(angle) * radius);
                float vz = (float) (Math.sin(angle) * radius);

                double uRaw = (angle / (2 * Math.PI)) * cylinder.repeatFrequency;
                double u = (k == segs) ? 1.0 : (uRaw - Math.floor(uRaw));

                // Bottom vertex (no shift)
                vertices.add(new Vector3f(vx + offsetX, offsetY, vz + offsetZ));
                UVs.add(new Vector2f((float) u, 0.0f));

                // Top vertex (shifted along Z axis to create the lean, but Y stays flat)
                vertices.add(new Vector3f(vx + offsetX, height + offsetY, vz + horizontalShift + offsetZ));
                UVs.add(new Vector2f((float) u, 1.0f));

                // Side Normals (adjusted for the tilt so lighting looks correct)
                float nx = (float) Math.cos(angle);
                float nz = (float) Math.sin(angle);
                // Rotate normal around X by 23 degrees to stay perpendicular to the slanted wall
                Vector3f tiltedNorm = new Vector3f(nx, -nz * sinA, nz * cosA);
                normals.add(tiltedNorm);
                normals.add(tiltedNorm);
            }

            int tileFirst = vertices.size() - 2 * (segs + 1) + 1;
            for (int k = 0; k < segs; k++) {
                int b0 = tileFirst + (k * 2);
                int b1 = tileFirst + (k * 2) + 2;
                int t0 = b0 + 1;
                int t1 = b1 + 1;
                sideFaces.add(String.format("f %s/%s/%s %s/%s/%s %s/%s/%s", b0, b0, b0, t0, t0, t0, t1, t1, t1));
                sideFaces.add(String.format("f %s/%s/%s %s/%s/%s %s/%s/%s", b0, b0, b0, t1, t1, t1, b1, b1, b1));
            }
        }

        // --- CAPS GEOMETRY ---
        // Bottom Cap (Standard horizontal)
        int centerBot = vertices.size() + 1;
        vertices.add(new Vector3f(offsetX, offsetY, offsetZ));
        UVs.add(new Vector2f(0.5f, 0.5f));
        normals.add(new Vector3f(0, -1, 0));
        int ringBot = vertices.size() + 1;
        for(int i=0; i<cylinder.numSides; i++) {
            double a = 2.0 * Math.PI * i / cylinder.numSides;
            vertices.add(new Vector3f((float)(Math.cos(a)*radius)+offsetX, offsetY, (float)(Math.sin(a)*radius)+offsetZ));
            UVs.add(usePlanarMapping ? new Vector2f((float)(0.5+0.5*Math.cos(a)), (float)(0.5+0.5*Math.sin(a))) : diskToSquareUV((float)Math.cos(a), (float)Math.sin(a)));
            normals.add(new Vector3f(0, -1, 0));
        }

        // Top Cap (Shifted but Y is constant)
        int centerTop = vertices.size() + 1;
        vertices.add(new Vector3f(offsetX, height + offsetY, horizontalShift + offsetZ));
        UVs.add(new Vector2f(0.5f, 0.5f));
        normals.add(new Vector3f(0, 1, 0));
        int ringTop = vertices.size() + 1;
        for(int i=0; i<cylinder.numSides; i++) {
            double a = 2.0 * Math.PI * i / cylinder.numSides;
            vertices.add(new Vector3f((float)(Math.cos(a)*radius)+offsetX, height + offsetY, (float)(Math.sin(a)*radius)+horizontalShift+offsetZ));
            UVs.add(usePlanarMapping ? new Vector2f((float)(0.5+0.5*Math.cos(a)), (float)(0.5+0.5*Math.sin(a))) : diskToSquareUV((float)Math.cos(a), (float)Math.sin(a)));
            normals.add(new Vector3f(0, 1, 0));
        }

        // Face generation for caps
        for (int i = 0; i < cylinder.numSides; i++) {
            int next = (i + 1) % cylinder.numSides;
            capFaces.add(String.format("f %s/%s/%s %s/%s/%s %s/%s/%s", centerBot, centerBot, centerBot, ringBot+i, ringBot+i, ringBot+i, ringBot+next, ringBot+next, ringBot+next));
            capFaces.add(String.format("f %s/%s/%s %s/%s/%s %s/%s/%s", centerTop, centerTop, centerTop, ringTop+next, ringTop+next, ringTop+next, ringTop+i, ringTop+i, ringTop+i));
        }

        StringBuilder sb = new StringBuilder("# Oblique Cylinder\n");
        sb.append(String.format("o cylinder\nmtllib %s.mtl\n", cylinder.name));
        for (Vector3f v : vertices) sb.append(String.format("v %.4f %.4f %.4f\n", v.x(), v.y(), v.z()));
        for (Vector2f vt : UVs) sb.append(String.format("vt %.4f %.4f\n", vt.x(), vt.y()));
        for (Vector3f vn : normals) sb.append(String.format("vn %.4f %.4f %.4f\n", vn.x(), vn.y(), vn.z()));
        sb.append("usemtl cylinder_sides\n").append(String.join("\n", sideFaces)).append("\n");
        sb.append("usemtl cylinder_caps\n").append(String.join("\n", capFaces)).append("\n");
        return sb.toString();
    }
}