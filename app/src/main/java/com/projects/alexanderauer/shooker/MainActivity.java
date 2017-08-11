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
import android.view.MenuItem;
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
        // Create the adapter that will return a fragment for each of the three
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
        } else
            getLoaderManager().initLoader(RECIPE_LOADER_ID, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sort) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // store recipes and shopping list
        outState.putParcelableArrayList(SAVED_INSTANCE_RECIPES, mRecipes);
        outState.putParcelableArrayList(SAVED_INSTANCE_SHOPPING_LIST, mShoppingList);

        super.onSaveInstanceState(outState);
    }

    public void onClickNewRecipe(View view) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        switch (id) {
            case RECIPE_LOADER_ID:
                return RecipeLoader.AllRecipesLoader(this);
            case INGREDIENT_LOADER_ID:
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

                    while (cursor.moveToNext()) {
                        Recipe newRecipe = new Recipe(cursor);

                        // build string of recipe ids for ingredient selection
                        if (newRecipe.isInShoppingList()) {
                            shoppingListRecipesIds += newRecipe.getId() + ",";
                        }

                        mRecipes.add(newRecipe);
                    }

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
                    while (cursor.moveToNext())
                        mShoppingList.add(new Ingredient(cursor));

                    mShoppingList = ShoppingListUtils.compressShoppingList(mShoppingList);
                } finally {
                    cursor.close();
                }

                break;
        }

        mViewPager.setAdapter(mSectionsPagerAdapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRecipes = new ArrayList<>();
        mShoppingList = new ArrayList<>();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mRecipes = new ArrayList<>();
        mShoppingList = new ArrayList<>();
        getLoaderManager().restartLoader(RECIPE_LOADER_ID, null, this);
    }

    public static class ShoppingListFragment extends Fragment {

        public ShoppingListFragment() {
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View shoppingListView = inflater.inflate(R.layout.fragment_shopping_list, container, false);

            ListView shoppingList = shoppingListView.findViewById(R.id.shopping_list);

            shoppingListView.findViewById(R.id.fab_share_shopping_list).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                            .setType("text/plain")
                            .setText(getString(R.string.share_shopping_list))
                            .getIntent(), getString(R.string.share_shopping_list)));
                }
            });

            // set adapter for the grid items
            if (mShoppingList != null && mShoppingList.size() > 0) {
                shoppingList.setAdapter(new ShoppingListAdapter(getContext(), mShoppingList));
                //mEmptyView.setVisibility(View.GONE);
                shoppingList.setVisibility(View.VISIBLE);
            } else {
                shoppingList.setVisibility(View.GONE);
                //mEmptyView.setVisibility(View.VISIBLE);
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

    public static class RecipesFragment extends Fragment implements RecipeCardAdapter.OnRecipeItemClickListener {

        private RecyclerView mRecyclerView;
        private TextView mEmptyView;

        public RecipesFragment() {
        }

        public static RecipesFragment getInstance(Cursor recipesCursor) {
            RecipesFragment recipesFragment = new RecipesFragment();

//            Bundle args = new Bundle();
//            args.put(ARG_RECIPES_CURSOR, recipesCursor);
//            recipesFragment.setArguments(args);

            return recipesFragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setSharedElementReturnTransition(null);
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

            // set adapter for the grid items
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
            Intent intent = new Intent(getActivity(), RecipeDetailActivity.class);

            intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE, recipe);

            startActivity(intent);
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
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
