package com.projects.alexanderauer.shooker.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Alex on 30.07.2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "shooker.db";
    private static final int DATABASE_VERSION = 2;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // create recipe table
        sqLiteDatabase.execSQL("CREATE TABLE " + RecipeProvider.Tables.RECIPES + " ("
                + RecipeContract.RecipesColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + RecipeContract.RecipesColumns.TITLE + " TEXT NOT NULL,"
                + RecipeContract.RecipesColumns.DURATION + " INTEGER,"
                + RecipeContract.RecipesColumns.DIFFICULTY + " TEXT,"
                + RecipeContract.RecipesColumns.PHOTO_URL + " TEXT,"
                + RecipeContract.RecipesColumns.IN_SHOPPING_LIST + " INTEGER"
                + ")");

        // create ingredient table
        sqLiteDatabase.execSQL("CREATE TABLE " + RecipeProvider.Tables.INGREDIENTS + " ("
                + IngredientContract.IngredientColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + IngredientContract.IngredientColumns.RECIPE_ID + " INTEGER NOT NULL,"
                + IngredientContract.IngredientColumns.AMOUNT + " REAL,"
                + IngredientContract.IngredientColumns.UNIT + " TEXT,"
                + IngredientContract.IngredientColumns.INGREDIENT + " TEXT NOT NULL)");

        // create step table
        sqLiteDatabase.execSQL("CREATE TABLE " + RecipeProvider.Tables.STEPS + " ("
                + StepContract.StepColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + StepContract.StepColumns.RECIPE_ID + " INTEGER NOT NULL,"
                + StepContract.StepColumns.SEQUENCE + " INTEGER NOT NULL,"
                + StepContract.StepColumns.STEP + " TEXT NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + RecipeProvider.Tables.RECIPES);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + RecipeProvider.Tables.INGREDIENTS);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + RecipeProvider.Tables.STEPS);
        onCreate(sqLiteDatabase);
    }
}