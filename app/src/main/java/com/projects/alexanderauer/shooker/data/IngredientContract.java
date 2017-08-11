package com.projects.alexanderauer.shooker.data;

import android.net.Uri;

/**
 * Created by Alex on 30.07.2017.
 */

public class IngredientContract {

    public IngredientContract() {
    }

    interface IngredientColumns {
        /**
         * Type: INTEGER PRIMARY KEY AUTOINCREMENT
         */
        String _ID = "_id";
        /**
         * Type: INTEGER NOT NULL
         */
        String RECIPE_ID = "recipe_id";
        /**
         * Type: DOUBLE NOT NULL
         */
        String AMOUNT = "amount";
        /**
         * Type: TEXT NOT NULL
         */
        String UNIT = "unit";
        /**
         * Type: TEXT NOT NULL
         */
        String INGREDIENT = "ingredient";
    }

    public static class Ingredients implements IngredientColumns {
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.alexanderauer.shooker.ingredients";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.alexanderauer.shooker.ingredients";

        public static final String DEFAULT_SORT = INGREDIENT + " ASC";

        /** Matches: /items/ */
        public static Uri buildDirUri() {
            return RecipeProvider.BASE_URI.buildUpon().appendPath("ingredients").build();
        }

        /** Matches: /items/[_id]/ */
        public static Uri buildItemUri(long _id) {
            return RecipeProvider.BASE_URI.buildUpon().appendPath("ingredients").appendPath(Long.toString(_id)).build();
        }

        public static Uri buildDir4RecipeUri(long recipeId){
            return RecipeProvider.BASE_URI.buildUpon().appendPath("ingredients4recipe").appendPath(Long.toString(recipeId)).build();
        }

        public static Uri buildDir4RecipesUri(String recipeIds){
            return RecipeProvider.BASE_URI.buildUpon().appendPath("ingredients4recipes").appendPath(recipeIds).build();
        }

        /** Read item ID item detail URI. */
        public static long getItemId(Uri itemUri) {
            return Long.parseLong(itemUri.getPathSegments().get(1));
        }
    }
}
