package com.projects.alexanderauer.shooker.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;

import com.projects.alexanderauer.shooker.R;
import com.projects.alexanderauer.shooker.data.Ingredient;
import com.projects.alexanderauer.shooker.services.RecipeOperationIntentService;

import java.util.List;

/**
 * Simple array adapter to display ingredients which got added to the shopping list
 */

public class ShoppingListAdapter extends ArrayAdapter<Ingredient> {

    public ShoppingListAdapter(@NonNull Context context, @NonNull List<Ingredient> ingredients) {
        super(context,0, ingredients);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull final ViewGroup parent) {
        final Ingredient ingredient = getItem(position);

        if(convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.shopping_list_item,parent,false);

        CheckBox checkBox = convertView.findViewById(R.id.ingredient);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ingredient.setChecked(!ingredient.isChecked());

                Intent recipeOperationsIntent = new Intent(parent.getContext(), RecipeOperationIntentService.class);
                recipeOperationsIntent.putExtra(RecipeOperationIntentService.EXTRA_INGREDIENT,ingredient);
                recipeOperationsIntent.setAction(RecipeOperationIntentService.ACTION_UPDATE_INGREDIENT);
                parent.getContext().startService(recipeOperationsIntent);
            }
        });

        // cut of the decimals of the amount if not needed
        String amount;
        if (ingredient.getAmount() == (int) ingredient.getAmount())
            amount = Integer.toString((int) ingredient.getAmount());
        else
            amount = Double.toString(ingredient.getAmount());

        // set checkbox state
        checkBox.setChecked(ingredient.isChecked());
        // set checkbox description (<Amount> <Unit> <Ingredient>)
        checkBox.setText(amount + " " + ingredient.getUnit() + " " + ingredient.getIngredient());

        return convertView;
    }
}
