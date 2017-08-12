package com.projects.alexanderauer.shooker.services;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.projects.alexanderauer.shooker.R;
import com.projects.alexanderauer.shooker.data.Ingredient;
import com.projects.alexanderauer.shooker.data.IngredientContract;
import com.projects.alexanderauer.shooker.data.RecipeContract;
import com.projects.alexanderauer.shooker.data.RecipeLoader;
import com.projects.alexanderauer.shooker.util.ShoppingListUtils;

import java.util.ArrayList;

/**
 * Class to create the single ListView items (Ingredients) for the widget
 */

public class ShoppingListWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ShoppingListViewItemRemoteFactory(this.getApplicationContext());
    }
}

class ShoppingListViewItemRemoteFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context mContext;
    private ArrayList<Ingredient> mShoppingList = new ArrayList<>();

    public ShoppingListViewItemRemoteFactory(Context context) {
        mContext = context;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        // query all Recipes in the shopping list
        Cursor cursor = mContext.getContentResolver().query(
                RecipeContract.Recipes.buildDirUri(),
                new String[]{RecipeContract.Recipes._ID},
                RecipeContract.Recipes.IN_SHOPPING_LIST + " = ? ",
                new String[]{"1"},
                RecipeContract.Recipes.DEFAULT_SORT
        );

        if (cursor == null || cursor.getCount() == 0)
            return;

        // build String of Recipe-Ids
        String recipeIds = "(";
        while (cursor.moveToNext()) {
            recipeIds += cursor.getLong(RecipeLoader.Query._ID) + ",";
        }
        recipeIds = recipeIds.substring(0, recipeIds.length() - 1) + ")";

        cursor.close();

        // query all Ingredients for the selected Recipes
        cursor = mContext.getContentResolver().query(
                IngredientContract.Ingredients.buildDir4RecipesUri(recipeIds),
                null,
                null,
                null,
                IngredientContract.Ingredients.DEFAULT_SORT
        );

        while (cursor.moveToNext()) {
            mShoppingList.add(new Ingredient(cursor));
        }

        // compress the shopping list (combine equal Ingredients)
        mShoppingList = ShoppingListUtils.compressShoppingList(mShoppingList);

        cursor.close();
    }

    @Override
    public void onDestroy() {
        mShoppingList = null;
    }

    @Override
    public int getCount() {
        return mShoppingList.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        // fill RemoteView object with Ingredient data
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_shopping_list_item);

        Ingredient ingredient = mShoppingList.get(position);

        String amount;
        if (ingredient.getAmount() == (int) ingredient.getAmount())
            amount = Integer.toString((int) ingredient.getAmount());
        else
            amount = Double.toString(ingredient.getAmount());

        rv.setTextViewText(R.id.ingredient, amount + " " + ingredient.getUnit() + " " + ingredient.getIngredient());

        return rv;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
