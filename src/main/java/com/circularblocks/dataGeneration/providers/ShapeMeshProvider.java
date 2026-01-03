package com.circularblocks.dataGeneration.providers;

import com.circularblocks.registry.ShapeRegistries;
import com.circularblocks.meshBuilders.ShapeBuiltMeshResult;
import com.circularblocks.shapes.Shape;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ShapeMeshProvider implements DataProvider {

    private final PackOutput output;
    private final ShapeRegistries registries;
    private final String modId;

    public ShapeMeshProvider(PackOutput output, String modId, ShapeRegistries registries) {
        this.output = output;
        this.registries = registries;
        this.modId = modId;
    }

    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        Path folderPath = output.getOutputFolder().resolve("assets/" + modId + "/models/item");
        Path recipePath = output.getOutputFolder().resolve("data/" + modId + "/recipes");

        for (Shape shape : registries.getShapes()) {
            // Handle OBJ
            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    ShapeBuiltMeshResult builtMesh = ShapeMeshBuilderDispatcher.buildMesh(shape);

                    saveFile(cache, builtMesh.objFileContents(), folderPath.resolve(shape.name + ".obj"));
                    saveFile(cache, builtMesh.mtlFileContents(), folderPath.resolve(shape.name + ".mtl"));

                    String recipe = RecipeProvider.buildRecipe(shape);
                    saveFile(cache, recipe, recipePath.resolve(shape.name + ".json"));

                } catch (Exception e) {
                    throw new RuntimeException("Failed to generate mesh for " + shape.name, e);
                }
            }));
        }

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private void saveFile(CachedOutput cache, String content, Path path) throws java.io.IOException {
        byte[] bytes = content.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        com.google.common.hash.HashCode hash = com.google.common.hash.Hashing.sha1().hashBytes(bytes);
        cache.writeIfNeeded(path, bytes, hash);
    }



    @Override
    public @NotNull String getName() {
        return "Cylinder Mesh Provider (" + modId + ")";
    }

}
