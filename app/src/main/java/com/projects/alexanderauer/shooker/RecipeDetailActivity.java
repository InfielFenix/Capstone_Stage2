package com.projects.alexanderauer.shooker;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.projects.alexanderauer.shooker.data.Ingredient;
import com.projects.alexanderauer.shooker.data.IngredientLoader;
import com.projects.alexanderauer.shooker.data.Recipe;
import com.projects.alexanderauer.shooker.data.Step;
import com.projects.alexanderauer.shooker.data.StepLoader;
import com.projects.alexanderauer.shooker.services.RecipeOperationIntentService;

/**
 * The RecipeDetailActivity works with two fragments: the RecipeDetailFragment which displays the
 * Recipe and the RecipeEditFragment which provides functionality to create/edit a Recipe.
 * It uses a Loader to load Ingredients and Steps of the respective Recipe from a SQLite database.
 */

public class RecipeDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, RecipeEditFragment.OnRecipeSaveListener, RecipeDetailFragment.OnRecipeDeleteListener {
    public static final int FRAGMENT_RECIPE_EDIT = 0,
            FRAGMENT_RECIPE_DETAIL = 1;
    public static final String EXTRA_RECIPE = "recipe_extra",
            EXTRA_CURRENT_FRAGMENT = "current_fragment_extra";
    public static final String BUNDLE_EXTRA_RECIPE_ID = "recipe_id";
    private static final int INGREDIENT_LOADER_ID = 0,
            STEP_LOADER_ID = 1;
    private int mCurrentFragment = FRAGMENT_RECIPE_EDIT;

    private FirebaseAnalytics mFirebaseAnalytics;

    private Recipe mRecipe;

    private boolean ingredientsLoaded,
            stepsLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        // Obtain the FirebaseAnalytics instance
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // distinguish between the different situations
        if (savedInstanceState != null && savedInstanceState.containsKey(EXTRA_RECIPE)) {
            // get data from savedInstanceState
            mRecipe = savedInstanceState.getParcelable(EXTRA_RECIPE);

            if (savedInstanceState.containsKey(EXTRA_CURRENT_FRAGMENT))
                mCurrentFragment = savedInstanceState.getInt(EXTRA_CURRENT_FRAGMENT);
        } else if (getIntent().hasExtra(EXTRA_RECIPE)) {
            // get data from intent extras
            mRecipe = getIntent().getParcelableExtra(EXTRA_RECIPE);

            // we have to navigate to the Recipe detail view if there is a
            // Recipe object in the extras
            mCurrentFragment = FRAGMENT_RECIPE_DETAIL;

            Bundle bundle = new Bundle();
            bundle.putLong(BUNDLE_EXTRA_RECIPE_ID, mRecipe.getId());

            // load Ingredients and Steps with Loaders
            getLoaderManager().initLoader(INGREDIENT_LOADER_ID, bundle, this);
            getLoaderManager().initLoader(STEP_LOADER_ID, bundle, this);
        } else {
            // navigate to the Recipe edit view if there is no Recipe object in the extras
            mCurrentFragment = FRAGMENT_RECIPE_EDIT;
            setCurrentFragment();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // store recipe and current fragment
        outState.putParcelable(EXTRA_RECIPE, mRecipe);
        outState.putInt(EXTRA_CURRENT_FRAGMENT, mCurrentFragment);

        super.onSaveInstanceState(outState);
    }

    private void setCurrentFragment() {
        switch (mCurrentFragment) {
            case FRAGMENT_RECIPE_EDIT:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content, RecipeEditFragment.newInstance(mRecipe))
                        .commit();

                break;
            case FRAGMENT_RECIPE_DETAIL:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content, RecipeDetailFragment.newInstance(mRecipe))
                        .commit();

                break;
        }

        // redraw toolbar
        invalidateOptionsMenu();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();

        return true;
    }

    public void onClickEditRecipe(View view) {
        // switch the fragment when the edit button gets pressed
        mCurrentFragment = FRAGMENT_RECIPE_EDIT;

        setCurrentFragment();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        switch (id) {
            case INGREDIENT_LOADER_ID: {
                // load Ingredients of a specific Recipe
                ingredientsLoaded = false;
                long recipeId = bundle.getLong(BUNDLE_EXTRA_RECIPE_ID);
                if (recipeId != 0)
                    return IngredientLoader.IngredientForRecipeLoader(this, recipeId);

                break;
            }
            case STEP_LOADER_ID: {
                // load Steps of a specific Recipe
                stepsLoaded = false;
                long recipeId = bundle.getLong(BUNDLE_EXTRA_RECIPE_ID);
                if (recipeId != 0)
                    return StepLoader.StepForRecipeLoader(this, recipeId);

                break;
            }
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case INGREDIENT_LOADER_ID:
                try {
                    // process retrieved Cursor object and add Ingredients to the Recipe object
                    while (cursor.moveToNext())
                        mRecipe.getIngredients().add(new Ingredient(cursor));

                    ingredientsLoaded = true;
                } finally {
                    cursor.close();
                }

                break;
            case STEP_LOADER_ID:
                try {
                    // process retrieved Cursor object and add Steps to the Recipe object
                    while (cursor.moveToNext())
                        mRecipe.getSteps().add(new Step(cursor));

                    stepsLoaded = true;
                } finally {
                    cursor.close();
                }

                break;
        }

        // set the fragment as soon as everything is loaded
        if (ingredientsLoaded && stepsLoaded)
            setCurrentFragment();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // clear flags
        ingredientsLoaded = stepsLoaded = false;
    }

    /**
     * Method to track adding Ingredients to the shopping list via Analytics
     *
     * @param recipe
     */
    public void trackAddRecipeToShoppingList(Recipe recipe) {
        for (Ingredient ingredient : recipe.getIngredients()) {
            Bundle bundle = new Bundle();

            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, ingredient.getIngredient());
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, getString(R.string.ingredient));

            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.ADD_TO_CART, bundle);
        }
    }

    /**
     * Method to track removing Ingredients from the shopping list via Analytics
     *
     * @param recipe
     */
    public void trackRemoveRecipeFromShoppingList(Recipe recipe) {
        for (Ingredient ingredient : recipe.getIngredients()) {
            Bundle bundle = new Bundle();

            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, ingredient.getIngredient());
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, getString(R.string.ingredient));

            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.REMOVE_FROM_CART, bundle);
        }
    }

    /**
     * Method to save a Recipe
     *
     * @param recipe
     */
    @Override
    public void onClickSaveRecipe(Recipe recipe) {
        // get current data from UI
        mRecipe = recipe;

        // create intent service to update sqlite tables
        Intent recipeOperationsIntent = new Intent(this, RecipeOperationIntentService.class);

        recipeOperationsIntent.putExtra(RecipeOperationIntentService.EXTRA_RECIPE, mRecipe);
        if (mRecipe.getId() == 0) {
            recipeOperationsIntent.setAction(RecipeOperationIntentService.ACTION_CREATE_RECIPE);
        } else {
            recipeOperationsIntent.setAction(RecipeOperationIntentService.ACTION_UPDATE_RECIPE);
        }

        // create/update recipe via the IntentService
        startService(recipeOperationsIntent);

        // create/update ingredients & steps
        if (mRecipe.getId() != 0) {
            for (Ingredient ingredient : mRecipe.getIngredients()) {
                recipeOperationsIntent = new Intent(this, RecipeOperationIntentService.class);
                recipeOperationsIntent.putExtra(RecipeOperationIntentService.EXTRA_INGREDIENT, ingredient);
                if (ingredient.getId() == 0) {
                    recipeOperationsIntent.setAction(RecipeOperationIntentService.ACTION_CREATE_INGREDIENT);
                } else {
                    recipeOperationsIntent.setAction(RecipeOperationIntentService.ACTION_UPDATE_INGREDIENT);
                }

                // create/update Ingredient via the IntentService
                startService(recipeOperationsIntent);
            }

            for (Step step : mRecipe.getSteps()) {
                recipeOperationsIntent = new Intent(this, RecipeOperationIntentService.class);
                recipeOperationsIntent.putExtra(RecipeOperationIntentService.EXTRA_STEP, step);

                if (step.getId() == 0)
                    recipeOperationsIntent.setAction(RecipeOperationIntentService.ACTION_CREATE_STEP);
                else
                    recipeOperationsIntent.setAction(RecipeOperationIntentService.ACTION_UPDATE_STEP);

                // create/update Step via the IntentService
                startService(recipeOperationsIntent);
            }
        }

        Toast.makeText(this, R.string.recipe_saved, Toast.LENGTH_SHORT).show();

        // do we come from the main activity or from the detail fragment?
        if (recipe.getId() == 0)
            // go back to main activity
            onBackPressed();
        else {
            // go back to detail fragment
            this.mCurrentFragment = FRAGMENT_RECIPE_DETAIL;
            setCurrentFragment();
        }
    }

    /**
     * Method to delete a Recipe
     */
    @Override
    public void onClickDeleteRecipe() {
        // delete Recipe via the IntentService
        Intent recipeOperationsIntent = new Intent(this, RecipeOperationIntentService.class);
        recipeOperationsIntent.putExtra(RecipeOperationIntentService.EXTRA_RECIPE_ID, mRecipe.getId());
        recipeOperationsIntent.setAction(RecipeOperationIntentService.ACTION_DELETE_RECIPE);
        startService(recipeOperationsIntent);

        Toast.makeText(this, R.string.recipe_deleted, Toast.LENGTH_SHORT).show();

        onBackPressed();
    }
}
