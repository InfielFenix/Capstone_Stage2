package com.projects.alexanderauer.shooker.data;

import android.content.Context;
import android.content.CursorLoader;
import android.net.Uri;

/**
 * CursorLoader for Ingredient objects
 */

public class IngredientLoader extends CursorLoader {

    private IngredientLoader(Context context, Uri uri) {
        super(context, uri, Query.PROJECTION, null, null, IngredientContract.Ingredients.DEFAULT_SORT);
    }

    // method to load all Ingredients for a specific Recipe
    public static IngredientLoader IngredientForRecipeLoader(Context context, long recipeId) {
        return new IngredientLoader(context, IngredientContract.Ingredients.buildDir4RecipeUri(recipeId));
    }

    // method to load all Ingredients for a list of Recipes (<recipe_id1>,<recipe_id2>,...,<recipe_idn>)
    public static IngredientLoader IngredientForRecipesLoader(Context context, String recipeIds) {
        return new IngredientLoader(context, IngredientContract.Ingredients.buildDir4RecipesUri(recipeIds));
    }

    public interface Query {
        String[] PROJECTION = {
                IngredientContract.Ingredients._ID,
                IngredientContract.Ingredients.RECIPE_ID,
                IngredientContract.Ingredients.AMOUNT,
                IngredientContract.Ingredients.UNIT,
                IngredientContract.Ingredients.INGREDIENT,
                IngredientContract.Ingredients.CHECKED
        };

        int _ID = 0;
        int RECIPE_ID = 1;
        int AMOUNT = 2;
        int UNIT = 3;
        int INGREDIENT = 4;
        int CHECKED = 5;
    }
}
