package com.projects.alexanderauer.shooker.data;

import android.net.Uri;

/**
 * Created by Alex on 30.07.2017.
 */

public class RecipeContract {

    public RecipeContract() {
    }

    interface RecipesColumns {
        /**
         * Type: INTEGER PRIMARY KEY AUTOINCREMENT
         */
        String _ID = "_id";
        /**
         * Type: TEXT NOT NULL
         */
        String TITLE = "title";
        /**
         * Type: TEXT NOT NULL
         */
        String DURATION = "duration";
        /**
         * Type: TEXT NOT NULL
         */
        String DIFFICULTY = "difficulty";
        /**
         * Type: TEXT NOT NULL
         */
        String PHOTO_URL = "photo_url";
        /**
         * Type: TEXT NOT NULL
         */
        String IN_SHOPPING_LIST = "in_shopping_list";
    }

    public static class Recipes implements RecipesColumns {
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.alexanderauer.shooker.recipes";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.alexanderauer.shooker.recipes";

        public static final String DEFAULT_SORT = TITLE + " ASC";

        /** Matches: /items/ */
        public static Uri buildDirUri() {
            return RecipeProvider.BASE_URI.buildUpon().appendPath("recipes").build();
        }

        /** Matches: /items/[_id]/ */
        public static Uri buildItemUri(long _id) {
            return RecipeProvider.BASE_URI.buildUpon().appendPath("recipes").appendPath(Long.toString(_id)).build();
        }

        /** Read item ID item detail URI. */
        public static long getItemId(Uri itemUri) {
            return Long.parseLong(itemUri.getPathSegments().get(1));
        }
    }
}
