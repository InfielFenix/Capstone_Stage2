package com.projects.alexanderauer.shooker.data;

import android.content.Context;
import android.content.CursorLoader;
import android.net.Uri;

/**
 * CursorLoader for Recipe objects
 */

public class RecipeLoader extends CursorLoader {
    public static RecipeLoader AllRecipesLoader(Context context) {
        return new RecipeLoader(context, RecipeContract.Recipes.buildDirUri());
    }

    public static RecipeLoader RecipeByIdLoader(Context context, long itemId) {
        return new RecipeLoader(context, RecipeContract.Recipes.buildItemUri(itemId));
    }

    private RecipeLoader(Context context, Uri uri) {
        super(context, uri, Query.PROJECTION, null, null, RecipeContract.Recipes.DEFAULT_SORT);
    }

    public interface Query {
        String[] PROJECTION = {
                RecipeContract.Recipes._ID,
                RecipeContract.Recipes.TITLE,
                RecipeContract.Recipes.DURATION,
                RecipeContract.Recipes.DIFFICULTY,
                RecipeContract.Recipes.PHOTO_URL,
                RecipeContract.Recipes.IN_SHOPPING_LIST,
        };

        int _ID = 0;
        int TITLE = 1;
        int DURATION = 2;
        int DIFFICULTY = 3;
        int PHOTO_URL = 4;
        int IN_SHOPPING_LIST = 5;
    }
}
