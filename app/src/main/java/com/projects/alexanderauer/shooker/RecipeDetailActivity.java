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

    private RecipeEditFragment mRecipeEditFragment;

    private boolean ingredientsLoaded,
            stepsLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        if (savedInstanceState != null && savedInstanceState.containsKey(EXTRA_RECIPE)) {
            mRecipe = savedInstanceState.getParcelable(EXTRA_RECIPE);

            if (savedInstanceState.containsKey(EXTRA_CURRENT_FRAGMENT))
                mCurrentFragment = savedInstanceState.getInt(EXTRA_CURRENT_FRAGMENT);
        } else if (getIntent().hasExtra(EXTRA_RECIPE)) {
            mRecipe = getIntent().getParcelableExtra(EXTRA_RECIPE);
            mCurrentFragment = FRAGMENT_RECIPE_DETAIL;

            Bundle bundle = new Bundle();
            bundle.putLong(BUNDLE_EXTRA_RECIPE_ID, mRecipe.getId());

            getLoaderManager().initLoader(INGREDIENT_LOADER_ID, bundle, this);
            getLoaderManager().initLoader(STEP_LOADER_ID, bundle, this);
        } else {
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
                mRecipeEditFragment = RecipeEditFragment.newInstance(mRecipe);

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content, mRecipeEditFragment)
                        .commit();

                break;
            case FRAGMENT_RECIPE_DETAIL:
                RecipeDetailFragment recipeDetailFragment = RecipeDetailFragment.newInstance(mRecipe);

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content, recipeDetailFragment)
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
        mCurrentFragment = FRAGMENT_RECIPE_EDIT;

        setCurrentFragment();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        switch (id) {
            case INGREDIENT_LOADER_ID: {
                ingredientsLoaded = false;
                long recipeId = bundle.getLong(BUNDLE_EXTRA_RECIPE_ID);
                if (recipeId != 0)
                    return IngredientLoader.IngredientForRecipeLoader(this, recipeId);

                break;
            }
            case STEP_LOADER_ID: {
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
                    while (cursor.moveToNext())
                        mRecipe.getIngredients().add(new Ingredient(cursor));

                    ingredientsLoaded = true;
                } finally {
                    cursor.close();
                }

                break;
            case STEP_LOADER_ID:
                try {
                    while (cursor.moveToNext())
                        mRecipe.getSteps().add(new Step(cursor));

                    stepsLoaded = true;
                } finally {
                    cursor.close();
                }

                break;
        }

        if (ingredientsLoaded && stepsLoaded)
            setCurrentFragment();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        ingredientsLoaded = stepsLoaded = false;
    }

    public void trackAddRecipeToShoppingList(Recipe recipe) {
        for (Ingredient ingredient : recipe.getIngredients()) {
            Bundle bundle = new Bundle();

            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, ingredient.getIngredient());
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, getString(R.string.ingredient));

            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.ADD_TO_CART, bundle);
        }
    }

    public void trackRemoveRecipeFromShoppingList(Recipe recipe) {
        for (Ingredient ingredient : recipe.getIngredients()) {
            Bundle bundle = new Bundle();

            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, ingredient.getIngredient());
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, getString(R.string.ingredient));

            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.REMOVE_FROM_CART, bundle);
        }
    }

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
        // create/update recipe
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

                startService(recipeOperationsIntent);
            }

            for (Step step : mRecipe.getSteps()) {
                recipeOperationsIntent = new Intent(this, RecipeOperationIntentService.class);
                recipeOperationsIntent.putExtra(RecipeOperationIntentService.EXTRA_STEP, step);

                if (step.getId() == 0)
                    recipeOperationsIntent.setAction(RecipeOperationIntentService.ACTION_CREATE_STEP);
                else
                    recipeOperationsIntent.setAction(RecipeOperationIntentService.ACTION_UPDATE_STEP);

                startService(recipeOperationsIntent);
            }
        }

        Toast.makeText(this, R.string.recipe_saved, Toast.LENGTH_SHORT).show();

        this.mCurrentFragment = FRAGMENT_RECIPE_DETAIL;
        setCurrentFragment();
    }

    @Override
    public void onClickDeleteRecipe() {
        Intent recipeOperationsIntent = new Intent(this, RecipeOperationIntentService.class);
        recipeOperationsIntent.putExtra(RecipeOperationIntentService.EXTRA_RECIPE_ID, mRecipe.getId());
        recipeOperationsIntent.setAction(RecipeOperationIntentService.ACTION_DELETE_RECIPE);
        startService(recipeOperationsIntent);

        Toast.makeText(this, R.string.recipe_deleted, Toast.LENGTH_SHORT).show();

        onBackPressed();
    }
}
