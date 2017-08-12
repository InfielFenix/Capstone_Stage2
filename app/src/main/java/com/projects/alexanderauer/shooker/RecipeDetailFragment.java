package com.projects.alexanderauer.shooker;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.projects.alexanderauer.shooker.data.Ingredient;
import com.projects.alexanderauer.shooker.data.Recipe;
import com.projects.alexanderauer.shooker.services.RecipeOperationIntentService;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.util.Locale;

/**
 * The fragment that contains the Recipe details.
 * It provides functionality to switch to the Recipe editing fragment
 * and to delete the Recipe.
 *
 * Use the {@link RecipeDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecipeDetailFragment extends Fragment {
    // the fragment initialization parameters
    private static final String ARG_RECIPE = "recipe_id";

    private Recipe mRecipe;

    private OnRecipeDeleteListener mRecipeDeleteListener;

    public RecipeDetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param recipe recipe object.
     * @return A new instance of fragment RecipeDetailFragment.
     */
    public static RecipeDetailFragment newInstance(Recipe recipe) {
        RecipeDetailFragment fragment = new RecipeDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_RECIPE, recipe);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mRecipe = getArguments().getParcelable(ARG_RECIPE);
        }

        if (mRecipe == null) {
            Log.e("DEV", "Empty Recipe (Detail)");
            return;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View recipeDetailView = inflater.inflate(R.layout.fragment_recipe_detail, container, false);

        // set own toolbar
        getActivityCast().setSupportActionBar(((Toolbar) recipeDetailView.findViewById(R.id.toolbar_recipe_detail)));
        getActivityCast().getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final ImageView photo = recipeDetailView.findViewById(R.id.photo);

        if (mRecipe != null) {
            // set recipe title
            final CollapsingToolbarLayout collapsingToolbarLayout = recipeDetailView
                    .findViewById(R.id.collapsing_toolbar_recipe_detail);
            collapsingToolbarLayout.setTitle(mRecipe.getTitle());
            collapsingToolbarLayout.setExpandedTitleColor(Color.WHITE);
            collapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);

            // PaletteAsyncListener which is used to set suitable colors to the
            // CollapsingToolbarLayout after Picasso has loaded the image
            final Palette.PaletteAsyncListener paletteListener = new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(Palette palette) {
                    int darkMutedColor, mutedColor;

                    if(!isAdded())
                        return;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        mutedColor = palette.getMutedColor(getResources().getColor(R.color.colorPrimary, null));
                        darkMutedColor = palette.getDarkMutedColor(getResources().getColor(R.color.colorPrimary, null));
                    } else {
                        mutedColor = palette.getMutedColor(getResources().getColor(R.color.colorPrimary));
                        darkMutedColor = palette.getDarkMutedColor(getResources().getColor(R.color.colorPrimary));
                    }

                    collapsingToolbarLayout.setContentScrimColor(mutedColor);
                    collapsingToolbarLayout.setStatusBarScrimColor(darkMutedColor);
                }
            };

            // load photo via Picasso
            if (mRecipe.getPhotoUrl() != null && !mRecipe.getPhotoUrl().equals("")) {
                Picasso.with(getContext())
                        .load(Uri.fromFile(new File(mRecipe.getPhotoUrl())))
                        .resize(getResources().getInteger(R.integer.photo_pixel_width),
                                getResources().getInteger(R.integer.photo_pixel_height))
                        .centerCrop()
                        .into(new Target() {
                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                photo.setImageBitmap(bitmap);

                                Palette.from(bitmap).generate(paletteListener);
                            }

                            @Override
                            public void onBitmapFailed(Drawable errorDrawable) {
                            }

                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {
                            }
                        });
            } else {
                photo.setImageResource(R.drawable.recipe_default_image);
                Palette.from(BitmapFactory.decodeResource(getResources(), R.drawable.recipe_default_image)).generate(paletteListener);
            }

            // set duration
            TextView duration = recipeDetailView.findViewById(R.id.duration);
            String concatenatedDuration = String.format(Locale.getDefault(), "%d", mRecipe.getDuration())
                    + " " + getString(R.string.minutes_short);
            duration.setText(concatenatedDuration);

            // set difficulty
            TextView difficulty = recipeDetailView.findViewById(R.id.difficulty);
            difficulty.setText(mRecipe.getDifficulty());

            // handle adding to shopping list
            ImageView shoppingCart = recipeDetailView.findViewById(R.id.add_to_shopping_list);
            setShoppingCartColor(shoppingCart, mRecipe.isInShoppingList());
            shoppingCart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mRecipe.setInShoppingList(!mRecipe.isInShoppingList());

                    if (mRecipe.isInShoppingList())
                        getActivityCast().trackAddRecipeToShoppingList(mRecipe);
                    else
                        getActivityCast().trackRemoveRecipeFromShoppingList(mRecipe);

                    setShoppingCartColor(((ImageView) view), mRecipe.isInShoppingList());

                    // create and start intent to update the flag
                    Intent recipeOperationsIntent = new Intent(getContext(), RecipeOperationIntentService.class);

                    recipeOperationsIntent.putExtra(RecipeOperationIntentService.EXTRA_RECIPE, mRecipe);
                    recipeOperationsIntent.setAction(RecipeOperationIntentService.ACTION_UPDATE_RECIPE);

                    getContext().startService(recipeOperationsIntent);
                }
            });

            // set ingredients
            TextView ingredients = recipeDetailView.findViewById(R.id.ingredients);
            for (Ingredient ingredient : mRecipe.getIngredients()) {
                String amount = "";
                if (ingredient.getAmount() == (int) ingredient.getAmount())
                    amount = Integer.toString((int) ingredient.getAmount());
                else
                    amount = Double.toString(ingredient.getAmount());

                ingredients.append(amount + " " +
                        ingredient.getUnit() + "   " +
                        ingredient.getIngredient() + "\n");
            }

            // set steps
            TextView steps = recipeDetailView.findViewById(R.id.steps);
            for (int i = 0; i < mRecipe.getSteps().size(); i++) {
                steps.append((i + 1) + ". " + mRecipe.getSteps().get(i).getStep() + "\n");
            }
        }

        setHasOptionsMenu(true);

        return recipeDetailView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // inflate fragment specific menu
        inflater.inflate(R.menu.menu_recipe_detail, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete: {
                // pass Recipe deletion event to the activity
                mRecipeDeleteListener.onClickDeleteRecipe();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void setShoppingCartColor(ImageView shoppingCart, boolean active) {
        // set color of shopping cart image
        if (active) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                shoppingCart.setColorFilter(getActivity().getColor(R.color.colorAccent));
            else
                shoppingCart.setColorFilter(getResources().getColor(R.color.colorAccent));
        } else {
            shoppingCart.setColorFilter(Color.GRAY);
        }
    }

    private RecipeDetailActivity getActivityCast() {
        return (RecipeDetailActivity) getActivity();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mRecipeDeleteListener = (OnRecipeDeleteListener) context;
        } catch (ClassCastException cce) {
            throw new ClassCastException(context.toString() + " must implement OnRecipeDeleteListener");
        }
    }

    /**
     * Interface which has to be implemented by the Activity to get the
     * Recipe deletion events
     */
    public interface OnRecipeDeleteListener {
        void onClickDeleteRecipe();
    }
}
