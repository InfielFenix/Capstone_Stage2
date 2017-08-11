package com.projects.alexanderauer.shooker.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;

import com.projects.alexanderauer.shooker.R;
import com.projects.alexanderauer.shooker.data.Ingredient;

import java.util.List;

/**
 * Created by Alex on 06.08.2017.
 */

public class ShoppingListAdapter extends ArrayAdapter<Ingredient> {

    public ShoppingListAdapter(@NonNull Context context, @NonNull List<Ingredient> ingredients) {
        super(context,0, ingredients);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Ingredient ingredient = getItem(position);

        if(convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.shopping_list_item,parent,false);

        CheckBox checkBox = convertView.findViewById(R.id.ingredient);

        String amount = "";
        if (ingredient.getAmount() == (int) ingredient.getAmount())
            amount = Integer.toString((int) ingredient.getAmount());
        else
            amount = Double.toString(ingredient.getAmount());

        checkBox.setChecked(ingredient.isChecked());
        checkBox.setText(amount + " " + ingredient.getUnit() + " " + ingredient.getIngredient());

        return convertView;
    }
}
