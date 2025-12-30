import math

def generate_minecraft_cylinder(sides=32, filename="cylinder.obj", side_uv_repeat=1.0, wrap_uv=False, size=(1.0, 1.0, 1.0)):
    sx, sy, sz = size
    vertices = []
    uvs = []
    normals = []  # Added normals list
    side_faces = []
    cap_faces = []

    def disk_to_square_uv(dx, dz):
        r = math.hypot(dx, dz)
        if r == 0: return 0.5, 0.5
        cos_t, sin_t = dx / r, dz / r
        denom = max(abs(cos_t), abs(sin_t))
        factor = (r / denom) if denom != 0 else 0.0
        ux, uz = cos_t * factor, sin_t * factor
        return 0.5 + 0.5 * ux, 0.5 + 0.5 * uz

    # --- SIDES ---
    if not wrap_uv:
        for y_val in (0.0, 1.0):
            for i in range(sides + 1):
                angle = 2 * math.pi * i / sides
                vx = (0.5 + 0.5 * math.cos(angle)) * sx
                vz = (0.5 + 0.5 * math.sin(angle)) * sz
                vertices.append((vx, y_val * sy, vz))
                uvs.append(((angle / (2 * math.pi)) * side_uv_repeat, y_val))
                # Normal points outward from center (0.5, 0.5)
                normals.append((math.cos(angle), 0.0, math.sin(angle)))
                
        for i in range(sides):
            bl, br = i + 1, i + 2
            tl, tr = bl + (sides + 1), br + (sides + 1)
            # Added //norm_idx to the face format
            side_faces.append(f"f {bl}/{bl}/{bl} {tl}/{tl}/{tl} {tr}/{tr}/{tr}")
            side_faces.append(f"f {bl}/{bl}/{bl} {tr}/{tr}/{tr} {br}/{br}/{br}")
    else:
        tiles = max(1, int(round(side_uv_repeat)))
        segments_per_tile = [sides // tiles + (1 if t < sides % tiles else 0) for t in range(tiles)]

        for t, segs in enumerate(segments_per_tile):
            for k in range(segs + 1):
                global_i = sum(segments_per_tile[:t]) + k
                angle = 2 * math.pi * global_i / sides
                vx = (0.5 + 0.5 * math.cos(angle)) * sx
                vz = (0.5 + 0.5 * math.sin(angle)) * sz
                u_raw = (angle / (2 * math.pi)) * side_uv_repeat
                u = 1.0 if k == segs else (u_raw - math.floor(u_raw))
                nx, nz = math.cos(angle), math.sin(angle)

                vertices.append((vx, 0.0, vz)); uvs.append((u, 0.0)); normals.append((nx, 0.0, nz))
                vertices.append((vx, sy, vz)); uvs.append((u, 1.0)); normals.append((nx, 0.0, nz))

            tile_first = len(vertices) - 2 * (segs + 1) + 1
            for k in range(segs):
                b0, b1 = tile_first + (k * 2), tile_first + (k * 2) + 2
                t0, t1 = b0 + 1, b1 + 1
                side_faces.append(f"f {b0}/{b0}/{b0} {t0}/{t0}/{t0} {t1}/{t1}/{t1}")
                side_faces.append(f"f {b0}/{b0}/{b0} {t1}/{t1}/{t1} {b1}/{b1}/{b1}")

    # --- CAPS ---
    # Bottom cap (Normal is -Y)
    center_idx = len(vertices) + 1
    vertices.append((0.5 * sx, 0.0, 0.5 * sz)); uvs.append((0.5, 0.5)); normals.append((0, -1, 0))
    ring_start = len(vertices) + 1
    for i in range(sides):
        angle = 2 * math.pi * i / sides
        vertices.append(((0.5 + 0.5 * math.cos(angle)) * sx, 0.0, (0.5 + 0.5 * math.sin(angle)) * sz))
        uvs.append(disk_to_square_uv(math.cos(angle), math.sin(angle)))
        normals.append((0, -1, 0))
    for i in range(sides):
        v1, v2, v3 = center_idx, ring_start + i, ring_start + ((i + 1) % sides)
        cap_faces.append(f"f {v1}/{v1}/{v1} {v2}/{v2}/{v2} {v3}/{v3}/{v3}")

    # Top cap (Normal is +Y)
    center_idx_top = len(vertices) + 1
    vertices.append((0.5 * sx, sy, 0.5 * sz)); uvs.append((0.5, 0.5)); normals.append((0, 1, 0))
    ring_start_top = len(vertices) + 1
    for i in range(sides):
        angle = 2 * math.pi * i / sides
        vertices.append(((0.5 + 0.5 * math.cos(angle)) * sx, sy, (0.5 + 0.5 * math.sin(angle)) * sz))
        uvs.append(disk_to_square_uv(math.cos(angle), math.sin(angle)))
        normals.append((0, 1, 0))
    for i in range(sides):
        v1, v2, v3 = center_idx_top, ring_start_top + ((i + 1) % sides), ring_start_top + i
        cap_faces.append(f"f {v1}/{v1}/{v1} {v2}/{v2}/{v2} {v3}/{v3}/{v3}")

    # --- WRITE OBJ ---
    with open(filename, "w") as f:
        f.write(f"# Cylinder - Size: {size}\n")
        f.write("o cylinder\nmtllib cylinder.mtl\n")
        for v in vertices: f.write(f"v {v[0]:.6f} {v[1]:.6f} {v[2]:.6f}\n")
        for vt in uvs: f.write(f"vt {vt[0]:.6f} {vt[1]:.6f}\n")
        for vn in normals: f.write(f"vn {vn[0]:.4f} {vn[1]:.4f} {vn[2]:.4f}\n")
        f.write("usemtl cylinder_sides\n")
        for face in side_faces: f.write(face + "\n")
        f.write("usemtl cylinder_caps\n")
        for face in cap_faces: f.write(face + "\n")

generate_minecraft_cylinder(32, filename="src/main/resources/assets/circularblocks/models/item/cylinder_3x3.obj", side_uv_repeat=8.0, wrap_uv=True, size=(3.0, 1.0, 3.0))