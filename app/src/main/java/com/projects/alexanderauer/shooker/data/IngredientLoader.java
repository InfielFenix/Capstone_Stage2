package com.projects.alexanderauer.shooker.data;

import android.content.Context;
import android.content.CursorLoader;
import android.net.Uri;

/**
 * Created by Alex on 29.07.2017.
 */

public class IngredientLoader extends CursorLoader {

    public static IngredientLoader IngredientForRecipeLoader(Context context, long recipeId) {
        return new IngredientLoader(context, IngredientContract.Ingredients.buildDir4RecipeUri(recipeId));
    }

    public static IngredientLoader IngredientForRecipesLoader(Context context, String recipeIds) {
        return new IngredientLoader(context, IngredientContract.Ingredients.buildDir4RecipesUri(recipeIds));
    }

    private IngredientLoader(Context context, Uri uri) {
        super(context, uri, Query.PROJECTION, null, null, IngredientContract.Ingredients.DEFAULT_SORT);
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
