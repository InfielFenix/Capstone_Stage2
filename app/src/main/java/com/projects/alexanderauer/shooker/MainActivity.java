package com.projects.alexanderauer.shooker;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ShareCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.projects.alexanderauer.shooker.adapters.RecipeCardAdapter;
import com.projects.alexanderauer.shooker.adapters.ShoppingListAdapter;
import com.projects.alexanderauer.shooker.data.Ingredient;
import com.projects.alexanderauer.shooker.data.IngredientLoader;
import com.projects.alexanderauer.shooker.data.Recipe;
import com.projects.alexanderauer.shooker.data.RecipeLoader;
import com.projects.alexanderauer.shooker.util.ShoppingListUtils;

import java.util.ArrayList;

/**
 * The MainActivity contains a ViewPager containing two fragments, the list of Recipes and
 * the shopping list.
 * It uses a Loader to load recipes from a SQLite database.
 */

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int RECIPE_LOADER_ID = 0,
            INGREDIENT_LOADER_ID = 1;

    private static final String BUNDLE_EXTRA_RECIPE_IDS = "recipe_ids",
            SAVED_INSTANCE_RECIPES = "saved_instance_recipes",
            SAVED_INSTANCE_SHOPPING_LIST = "saved_instance_shopping_list";

    static ArrayList<Recipe> mRecipes;
    static ArrayList<Ingredient> mShoppingList;

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the two
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        // get stored data or load it
        if (savedInstanceState != null && savedInstanceState.containsKey(SAVED_INSTANCE_RECIPES)) {
            mRecipes = savedInstanceState.getParcelableArrayList(SAVED_INSTANCE_RECIPES);

            // get stored shopping list
            if (savedInstanceState.containsKey(SAVED_INSTANCE_SHOPPING_LIST))
                mShoppingList = savedInstanceState.getParcelableArrayList(SAVED_INSTANCE_SHOPPING_LIST);

            mViewPager.setAdapter(mSectionsPagerAdapter);
        } else {
            // initialize the Loader which loads the Recipes
            getLoaderManager().initLoader(RECIPE_LOADER_ID, null, this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // store recipes and shopping list
        outState.putParcelableArrayList(SAVED_INSTANCE_RECIPES, mRecipes);
        outState.putParcelableArrayList(SAVED_INSTANCE_SHOPPING_LIST, mShoppingList);

        super.onSaveInstanceState(outState);
    }

    public void onClickNewRecipe(View view) {
        // start the intent which opens the Recipe detail activity
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        switch (id) {
            case RECIPE_LOADER_ID:
                // load all stored Recipes
                return RecipeLoader.AllRecipesLoader(this);
            case INGREDIENT_LOADER_ID:
                // load all Ingredients for a list of Recipes
                return IngredientLoader.IngredientForRecipesLoader(this, bundle.getString(BUNDLE_EXTRA_RECIPE_IDS));
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case RECIPE_LOADER_ID:
                mRecipes = new ArrayList<>();

                try {
                    String shoppingListRecipesIds = "";

                    // during the processing of the retrieved Cursor, we check if the Recipe
                    // got added to the shopping list and build up a String of Recipe-Ids
                    while (cursor.moveToNext()) {
                        Recipe newRecipe = new Recipe(cursor);

                        // build string of recipe ids for ingredient selection
                        if (newRecipe.isInShoppingList()) {
                            shoppingListRecipesIds += newRecipe.getId() + ",";
                        }

                        mRecipes.add(newRecipe);
                    }

                    // load Ingredients in case there are Recipes in the shopping list
                    if (!shoppingListRecipesIds.equals("")) {
                        Bundle bundle = new Bundle();
                        bundle.putString(BUNDLE_EXTRA_RECIPE_IDS, "(" + shoppingListRecipesIds.substring(0, shoppingListRecipesIds.length() - 1) + ")");
                        getLoaderManager().restartLoader(INGREDIENT_LOADER_ID, bundle, this);
                    }
                } finally {
                    cursor.close();
                }

                break;
            case INGREDIENT_LOADER_ID:
                mShoppingList = new ArrayList<>();

                try {
                    // process Cursor of Ingredients
                    while (cursor.moveToNext())
                        mShoppingList.add(new Ingredient(cursor));

                    mShoppingList = ShoppingListUtils.compressShoppingList(mShoppingList);
                } finally {
                    cursor.close();
                }

                break;
        }

        // refresh the ViewPager
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // clear the lists
        mRecipes = new ArrayList<>();
        mShoppingList = new ArrayList<>();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        // clear lists and restart the Loader
        mRecipes = new ArrayList<>();
        mShoppingList = new ArrayList<>();
        getLoaderManager().restartLoader(RECIPE_LOADER_ID, null, this);
    }

    /**
     * Fragment that contains a ListView to display the Ingredient items
     */
    public static class ShoppingListFragment extends Fragment {

        private TextView mEmptyView;

        public ShoppingListFragment() {
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View shoppingListView = inflater.inflate(R.layout.fragment_shopping_list, container, false);

            ListView shoppingList = shoppingListView.findViewById(R.id.shopping_list);

            // start the share intent when pressing the share fab
            shoppingListView.findViewById(R.id.fab_share_shopping_list).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                            .setType("text/plain")
                            .setText(ShoppingListUtils.shoppingListToString(mShoppingList))
                            .getIntent(), getString(R.string.share_shopping_list)));
                }
            });

            mEmptyView = shoppingListView.findViewById(R.id.empty_view);

            // set adapter and set visibility
            if (mShoppingList != null && mShoppingList.size() > 0) {
                shoppingList.setAdapter(new ShoppingListAdapter(getContext(), mShoppingList));
                mEmptyView.setVisibility(View.GONE);
                shoppingList.setVisibility(View.VISIBLE);
            } else {
                shoppingList.setVisibility(View.GONE);
                mEmptyView.setVisibility(View.VISIBLE);
            }

            AdView mAdView = shoppingListView.findViewById(R.id.adView);

            // create the AdRequest
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .build();
            mAdView.loadAd(adRequest);

            return shoppingListView;
        }
    }

    /**
     * Fragment that contains a GridView to display Recipes
     */
    public static class RecipesFragment extends Fragment implements RecipeCardAdapter.OnRecipeItemClickListener {

        private RecyclerView mRecyclerView;
        private TextView mEmptyView;

        public RecipesFragment() {
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View recipesView = inflater.inflate(R.layout.fragment_recipe_list, container, false);

            mRecyclerView = recipesView.findViewById(R.id.recipe_card_recycler);
            mEmptyView = recipesView.findViewById(R.id.empty_view);

            // set a grid layout in the recycler view
            StaggeredGridLayoutManager gridManager = new StaggeredGridLayoutManager(
                    getResources().getInteger(R.integer.grid_column_count),
                    StaggeredGridLayoutManager.VERTICAL);

            mRecyclerView.setLayoutManager(gridManager);

            // set adapter and visibility
            if (mRecipes != null && mRecipes.size() > 0) {
                mRecyclerView.setAdapter(new RecipeCardAdapter(mRecipes, this));
                mEmptyView.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            } else {
                mRecyclerView.setVisibility(View.GONE);
                mEmptyView.setVisibility(View.VISIBLE);
            }

            return recipesView;
        }

        @Override
        public void onRecipeItemClick(Recipe recipe, ImageView recipePhoto) {
            // start RecipeDetail activity when clicking on a Recipe
            Intent intent = new Intent(getActivity(), RecipeDetailActivity.class);

            intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE, recipe);

            startActivity(intent);
        }
    }

    /**
     * Simple PagerAdapter for the ViewPager
     * It creates instances of the RecipesFragment and the ShoppingListFragment
     */
    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new RecipesFragment();
                case 1:
                    return new ShoppingListFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.tab_recipes);
                case 1:
                    return getString(R.string.tab_shopping_list);
            }
            return null;
        }
    }
}
