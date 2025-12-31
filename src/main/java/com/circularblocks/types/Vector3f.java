package com.circularblocks.types;

public record Vector3f(float x, float y, float z) {

    public Vector3f normalize() {
        // Calculate the magnitude: sqrt(x^2 + y^2 + z^2)
        float length = (float) Math.sqrt(x * x + y * y + z * z);

        // Prevent division by zero if the vector is (0,0,0)
        if (length == 0) {
            return new Vector3f(0, 0, 0);
        }

        return new Vector3f(x / length, y / length, z / length);
    }
}
