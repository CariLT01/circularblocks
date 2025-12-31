package com.circularblocks.dataGeneration;

import com.circularblocks.shapes.CylinderShape;
import com.circularblocks.shapes.Shape;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class RecipeProvider {

    public static String buildRecipe(Shape shape)
    {
        if (shape instanceof CylinderShape) {
            JsonObject json = new JsonObject();
            json.addProperty("type", "minecraft:crafting_shapeless");
            JsonArray ingredients = new JsonArray();
            if(shape.name.contains("2x2")){
                String blockname = "circularblocks:" + shape.name.replace("_2x2", "");
                JsonObject block1 = new JsonObject();
                block1.addProperty("item", blockname);
                JsonObject block2 = new JsonObject();
                block2.addProperty("item", blockname);
                ingredients.add(block1);
                ingredients.add(block2);

            } else if(shape.name.contains("3x3")){
                String blockname = "circularblocks:" + shape.name.replace("_3x3", "");
                JsonObject block1 = new JsonObject();
                block1.addProperty("item", blockname);
                JsonObject block2 = new JsonObject();
                block2.addProperty("item", blockname);
                JsonObject block3 = new JsonObject();
                block3.addProperty("item", blockname);
                ingredients.add(block1);
                ingredients.add(block2);
                ingredients.add(block3);

            }else{
                String blockname = shape.sideTextureName.replace("minecraft:block/", "minecraft:");
                JsonObject block1 = new JsonObject();
                block1.addProperty("item", "circularblocks:cobblestone_cylinder");
                JsonObject block2 = new JsonObject();
                block2.addProperty("item", blockname);
                ingredients.add(block1);
                ingredients.add(block2);
            }
            json.add("ingredients", ingredients);
            JsonObject result = new JsonObject();
            result.addProperty("item", "circularblocks:" + shape.name);
            result.addProperty("count", 1);
            json.add("result", result);
            return json.toString();

        }
        return "";
    }
}
