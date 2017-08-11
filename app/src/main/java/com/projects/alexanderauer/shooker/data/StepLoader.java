package com.projects.alexanderauer.shooker.data;

import android.content.Context;
import android.content.CursorLoader;
import android.net.Uri;

/**
 * Created by Alex on 29.07.2017.
 */

public class StepLoader extends CursorLoader {

    public static StepLoader StepForRecipeLoader(Context context, long recipeId) {
        return new StepLoader(context, StepContract.Steps.buildDir4RecipeUri(recipeId));
    }

    private StepLoader(Context context, Uri uri) {
        super(context, uri, Query.PROJECTION, null, null, StepContract.Steps.DEFAULT_SORT);
    }

    public interface Query {
        String[] PROJECTION = {
                StepContract.StepColumns._ID,
                StepContract.StepColumns.RECIPE_ID,
                StepContract.StepColumns.SEQUENCE,
                StepContract.StepColumns.STEP
        };

        int _ID = 0;
        int RECIPE_ID = 1;
        int SEQUENCE = 2;
        int STEP = 3;
    }
}
