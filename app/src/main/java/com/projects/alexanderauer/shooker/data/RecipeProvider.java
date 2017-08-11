package com.projects.alexanderauer.shooker.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Alex on 30.07.2017.
 */

public class RecipeProvider extends ContentProvider {

    public static final String CONTENT_AUTHORITY = "com.alexanderauer.shooker";
    public static final Uri BASE_URI = Uri.parse("content://com.alexanderauer.shooker");

    private static final int RECIPES = 0,
            RECIPES_ID = 1,
            INGREDIENTS = 2,
            INGREDIENTS_ID = 3,
            INGREDIENTS_FOR_RECIPE = 4,
            INGREDIENTS_FOR_RECIPES = 5,
            STEPS = 6,
            STEPS_ID = 7,
            STEPS_FOR_RECIPE = 8;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private SQLiteOpenHelper mOpenHelper;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        matcher.addURI(CONTENT_AUTHORITY, "recipes", RECIPES);
        matcher.addURI(CONTENT_AUTHORITY, "recipes/#", RECIPES_ID);
        matcher.addURI(CONTENT_AUTHORITY, "ingredients", INGREDIENTS);
        matcher.addURI(CONTENT_AUTHORITY, "ingredients4recipe/#", INGREDIENTS_FOR_RECIPE);
        matcher.addURI(CONTENT_AUTHORITY, "ingredients4recipes/*", INGREDIENTS_FOR_RECIPES);
        matcher.addURI(CONTENT_AUTHORITY, "ingredients/#", INGREDIENTS_ID);
        matcher.addURI(CONTENT_AUTHORITY, "steps", STEPS);
        matcher.addURI(CONTENT_AUTHORITY, "steps4recipe/#", STEPS_FOR_RECIPE);
        matcher.addURI(CONTENT_AUTHORITY, "steps/#", STEPS_ID);

        return matcher;
    }

    @Nullable
    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor;

        switch (sUriMatcher.match(uri)) {
            case RECIPES_ID: {
                String recipeId = uri.getLastPathSegment();
                String[] selectionArguments = new String[]{recipeId};

                cursor = mOpenHelper.getReadableDatabase().query(
                        Tables.RECIPES,
                        projection,
                        RecipeContract.Recipes._ID + " = ? ",
                        selectionArguments,
                        null,
                        null,
                        sortOrder);

                break;
            }
            case RECIPES: {
                cursor = mOpenHelper.getReadableDatabase().query(
                        Tables.RECIPES,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);

                break;
            }
            case INGREDIENTS_FOR_RECIPE: {
                String recipeId = uri.getLastPathSegment();
                String[] selectionArguments = new String[]{recipeId};

                cursor = mOpenHelper.getReadableDatabase().query(
                        Tables.INGREDIENTS,
                        projection,
                        IngredientContract.Ingredients.RECIPE_ID + " = ? ",
                        selectionArguments,
                        null,
                        null,
                        sortOrder
                );

                break;
            }
            case INGREDIENTS_FOR_RECIPES: {
                String recipeIds = uri.getLastPathSegment();

                cursor = mOpenHelper.getReadableDatabase().query(
                        Tables.INGREDIENTS,
                        projection,
                        IngredientContract.Ingredients.RECIPE_ID + " IN " + recipeIds,
                        null,
                        null,
                        null,
                        sortOrder
                );

                break;
            }
            case STEPS_FOR_RECIPE: {
                String recipeId = uri.getLastPathSegment();
                String[] selectionArguments = new String[]{recipeId};

                cursor = mOpenHelper.getReadableDatabase().query(
                        Tables.STEPS,
                        projection,
                        IngredientContract.Ingredients.RECIPE_ID + " = ? ",
                        selectionArguments,
                        null,
                        null,
                        sortOrder
                );

                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case RECIPES:
                return RecipeContract.Recipes.CONTENT_TYPE;
            case RECIPES_ID:
                return RecipeContract.Recipes.CONTENT_ITEM_TYPE;
            case INGREDIENTS:
            case INGREDIENTS_FOR_RECIPE:
                return IngredientContract.Ingredients.CONTENT_TYPE;
            case INGREDIENTS_ID:
                return IngredientContract.Ingredients.CONTENT_ITEM_TYPE;
            case STEPS:
            case STEPS_FOR_RECIPE:
                return StepContract.Steps.CONTENT_TYPE;
            case STEPS_ID:
                return StepContract.Steps.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    public interface Tables {
        String RECIPES = "recipes";
        String INGREDIENTS = "ingredients";
        String STEPS = "steps";
    }
}
