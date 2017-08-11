package com.projects.alexanderauer.shooker.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import com.projects.alexanderauer.shooker.data.DatabaseHelper;
import com.projects.alexanderauer.shooker.data.Ingredient;
import com.projects.alexanderauer.shooker.data.IngredientContract;
import com.projects.alexanderauer.shooker.data.Recipe;
import com.projects.alexanderauer.shooker.data.RecipeContract;
import com.projects.alexanderauer.shooker.data.RecipeProvider;
import com.projects.alexanderauer.shooker.data.Step;
import com.projects.alexanderauer.shooker.data.StepContract;

/**
 * Created by Alex on 03.08.2017.
 */

public class RecipeOperationIntentService extends IntentService {

    public static final String ACTION_CREATE_RECIPE = "create_recipe",
            ACTION_UPDATE_RECIPE = "update_recipe",
            ACTION_DELETE_RECIPE = "delete_recipe",
            ACTION_CREATE_INGREDIENT = "create_ingredient",
            ACTION_UPDATE_INGREDIENT = "update_ingredient",
            ACTION_DELETE_INGREDIENT = "delete_ingredient",
            ACTION_CREATE_STEP = "create_step",
            ACTION_UPDATE_STEP = "update_step",
            ACTION_DELETE_STEP = "delete_step";

    public static final String EXTRA_RECIPE = "recipe",
            EXTRA_RECIPE_ID = "recipe_id",
            EXTRA_INGREDIENT = "ingredient",
            EXTRA_INGREDIENT_ID = "ingredient_id",
            EXTRA_STEP = "step",
            EXTRA_STEP_ID = "step_id";

    private DatabaseHelper mDatabaseHelper;

    public RecipeOperationIntentService() {
        super("RecipeOperationIntentService");

        mDatabaseHelper = new DatabaseHelper(this);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        switch (intent.getAction()) {
            case ACTION_CREATE_RECIPE: {
                Recipe recipe = intent.getParcelableExtra(EXTRA_RECIPE);

                // create new recipe
                recipe.setId(createRecipe(recipe));

                // create ingredients for recipe
                for (Ingredient ingredient : recipe.getIngredients()) {
                    ingredient.setRecipeId(recipe.getId());

                    ingredient.setId(createIngredient(ingredient));
                }

                // create steps for recipe
                for (Step step : recipe.getSteps()) {
                    step.setRecipeId(recipe.getId());

                    step.setId(createStep(step));
                }

                break;
            }
            case ACTION_UPDATE_RECIPE: {
                Recipe recipe = intent.getParcelableExtra(EXTRA_RECIPE);

                if (recipe.getId() > 0)
                    updateRecipe(recipe);

                break;
            }
            case ACTION_DELETE_RECIPE: {
                long recipeId = intent.getLongExtra(EXTRA_RECIPE_ID, 0);

                if (recipeId > 0) {
                    // delete recipe
                    deleteRecipe(recipeId);
                    // delete ingredients of recipe
                    deleteIngredientForRecipeId(recipeId);
                    // delete steps of recipe
                    deleteStepForRecipeId(recipeId);
                }

                break;
            }
            case ACTION_CREATE_INGREDIENT: {
                Ingredient ingredient = intent.getParcelableExtra(EXTRA_INGREDIENT);

                ingredient.setId(createIngredient(ingredient));

                break;
            }
            case ACTION_UPDATE_INGREDIENT: {
                Ingredient ingredient = intent.getParcelableExtra(EXTRA_INGREDIENT);

                if (ingredient.getId() > 0)
                    updateIngredient(ingredient);

                break;
            }
            case ACTION_DELETE_INGREDIENT: {
                long ingredientId = intent.getLongExtra(EXTRA_INGREDIENT_ID, 0);

                if (ingredientId > 0)
                    deleteIngredient(ingredientId);

                break;
            }
            case ACTION_CREATE_STEP: {
                Step step = intent.getParcelableExtra(EXTRA_STEP);

                step.setId(createStep(step));

                break;
            }
            case ACTION_UPDATE_STEP: {
                Step step = intent.getParcelableExtra(EXTRA_STEP);

                if (step.getId() > 0)
                    updateStep(step);

                break;
            }
            case ACTION_DELETE_STEP: {
                long stepId = intent.getLongExtra(EXTRA_STEP_ID, 0);

                if (stepId > 0)
                    deleteStep(stepId);

                break;
            }
        }
    }

    private long createRecipe(Recipe recipe) {
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();

        ContentValues contentValues = getContentValuesOfRecipe(recipe);

        long _id = db.insert(RecipeProvider.Tables.RECIPES, null, contentValues);

        db.close();

        return _id;
    }

    private boolean updateRecipe(Recipe recipe) {
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();

        ContentValues contentValues = getContentValuesOfRecipe(recipe);

        boolean success = db.update(RecipeProvider.Tables.RECIPES, contentValues, RecipeContract.Recipes._ID + " = " + recipe.getId(), null) > 0;

        db.close();

        return success;
    }

    private boolean deleteRecipe(long recipeId) {
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();

        boolean success =  db.delete(RecipeProvider.Tables.RECIPES, RecipeContract.Recipes._ID + " = " + recipeId, null) > 0;

        db.close();

        return success;
    }

    private long createIngredient(Ingredient ingredient) {
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();

        ContentValues contentValues = getContentValuesOfIngredient(ingredient);

        long _id = db.insert(RecipeProvider.Tables.INGREDIENTS, null, contentValues);

        db.close();

        return _id;
    }

    private boolean updateIngredient(Ingredient ingredient) {
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();

        ContentValues contentValues = getContentValuesOfIngredient(ingredient);

        boolean success =  db.update(RecipeProvider.Tables.INGREDIENTS, contentValues, IngredientContract.Ingredients._ID + " = " + ingredient.getId(), null) > 0;

        db.close();

        return success;
    }

    private boolean deleteIngredient(long ingredientId) {
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();

        boolean success =  db.delete(RecipeProvider.Tables.INGREDIENTS, IngredientContract.Ingredients._ID + " = " + ingredientId, null) > 0;

        db.close();

        return success;
    }

    private boolean deleteIngredientForRecipeId(long recipeId) {
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();

        boolean success =  db.delete(RecipeProvider.Tables.INGREDIENTS, IngredientContract.Ingredients.RECIPE_ID + " = " + recipeId, null) > 0;

        db.close();

        return success;
    }

    private long createStep(Step step) {
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();

        ContentValues contentValues = getContentValuesOfStep(step);

        long _id = db.insert(RecipeProvider.Tables.STEPS, null, contentValues);

        return _id;
    }

    private boolean updateStep(Step step) {
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();

        ContentValues contentValues = getContentValuesOfStep(step);

        boolean success =  db.update(RecipeProvider.Tables.STEPS, contentValues, StepContract.Steps._ID + " = " + step.getId(), null) > 0;

        db.close();

        return success;
    }

    private boolean deleteStep(long stepId) {
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();

        boolean success =  db.delete(RecipeProvider.Tables.STEPS, StepContract.Steps._ID + " = " + stepId, null) > 0;

        db.close();

        return success;
    }

    private boolean deleteStepForRecipeId(long recipeId) {
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();

        boolean success =  db.delete(RecipeProvider.Tables.STEPS, StepContract.Steps.RECIPE_ID + " = " + recipeId, null) > 0;

        db.close();

        return success;
    }

    private ContentValues getContentValuesOfRecipe(Recipe recipe) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(RecipeContract.Recipes.TITLE, recipe.getTitle());
        contentValues.put(RecipeContract.Recipes.DURATION, recipe.getDuration());
        contentValues.put(RecipeContract.Recipes.DIFFICULTY, recipe.getDifficulty());
        contentValues.put(RecipeContract.Recipes.PHOTO_URL, recipe.getPhotoUrl());
        contentValues.put(RecipeContract.Recipes.IN_SHOPPING_LIST, recipe.isInShoppingList());

        return contentValues;
    }

    private ContentValues getContentValuesOfIngredient(Ingredient ingredient) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(IngredientContract.Ingredients.RECIPE_ID, ingredient.getRecipeId());
        contentValues.put(IngredientContract.Ingredients.AMOUNT, ingredient.getAmount());
        contentValues.put(IngredientContract.Ingredients.UNIT, ingredient.getUnit());
        contentValues.put(IngredientContract.Ingredients.INGREDIENT, ingredient.getIngredient());

        return contentValues;
    }

    private ContentValues getContentValuesOfStep(Step step) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(StepContract.Steps.RECIPE_ID, step.getRecipeId());
        contentValues.put(StepContract.Steps.SEQUENCE, step.getSequence());
        contentValues.put(StepContract.Steps.STEP, step.getStep());

        return contentValues;
    }
}
