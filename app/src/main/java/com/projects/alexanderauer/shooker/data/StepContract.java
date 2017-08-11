package com.projects.alexanderauer.shooker.data;

import android.net.Uri;

/**
 * Created by Alex on 30.07.2017.
 */

public class StepContract {

    public StepContract() {
    }

    interface StepColumns {
        /**
         * Type: INTEGER PRIMARY KEY AUTOINCREMENT
         */
        String _ID = "_id";
        /**
         * Type: INTEGER NOT NULL
         */
        String RECIPE_ID = "recipe_id";
        /**
         * Type: INT NOT NULL
         */
        String SEQUENCE = "sequence";
        /**
         * Type: STRING NOT NULL
         */
        String STEP = "step";
    }

    public static class Steps implements StepColumns {
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.alexanderauer.shooker.steps";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.alexanderauer.shooker.steps";

        public static final String DEFAULT_SORT = SEQUENCE + " ASC";

        /** Matches: /items/ */
        public static Uri buildDirUri() {
            return RecipeProvider.BASE_URI.buildUpon().appendPath("steps").build();
        }

        /** Matches: /items/[_id]/ */
        public static Uri buildItemUri(long _id) {
            return RecipeProvider.BASE_URI.buildUpon().appendPath("steps").appendPath(Long.toString(_id)).build();
        }

        public static Uri buildDir4RecipeUri(long recipeId){
            return RecipeProvider.BASE_URI.buildUpon().appendPath("steps4recipe").appendPath(Long.toString(recipeId)).build();
        }

        /** Read item ID item detail URI. */
        public static long getItemId(Uri itemUri) {
            return Long.parseLong(itemUri.getPathSegments().get(1));
        }
    }
}
