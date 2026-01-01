package com.circularblocks.shapes;

import com.circularblocks.types.Vector3f;
import com.circularblocks.types.Vector3i;

import java.util.ArrayList;
import java.util.List;

public class ShapeCollisionBlocks {

    private final Vector3f size;
    private final boolean centered;
    private final CollisionType collisionType;

    public ShapeCollisionBlocks(Vector3f size, boolean centered, CollisionType collisionType) {
        this.size = size;
        this.centered = centered;
        this.collisionType = collisionType;
    }

    public List<Vector3i> computeCollisionBlocks() {

        // Simple estimation

        List<Vector3i> offsets = new ArrayList<>();

        if (this.centered) {
            for (int x = 0; x < this.size.x(); x++) {
                for (int z = 0; z < this.size.z(); z++) {

                    int halfX = (int) Math.floor(this.size.x() / 2.0);
                    int halfZ = (int) Math.floor(this.size.z() / 2.0);

                    Vector3i offset = new Vector3i(x - halfX, 0, z - halfZ);
                    if (offset.x() == 0 && offset.z() == 0) continue; // Always skip the parent pos

                    offsets.add(offset);

                }
            }

             return offsets;
        } else {

            for (int x = 0; x < this.size.x(); x++) {
                for (int z = 0; z < this.size.z(); z++) {
                    Vector3i offset = new Vector3i(x, 0, z);

                    offsets.add(offset);
                }
            }

            return offsets;
        }
    }

}
