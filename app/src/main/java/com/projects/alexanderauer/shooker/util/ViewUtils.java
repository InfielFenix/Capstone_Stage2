package com.projects.alexanderauer.shooker.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import com.projects.alexanderauer.shooker.R;
import com.projects.alexanderauer.shooker.data.Ingredient;
import com.projects.alexanderauer.shooker.data.Step;

import java.util.Arrays;

/**
 * Utility class to convert Ingredient and Step objects into Views.
 * This is needed in the Recipe-Edit fragment.
 */

public class ViewUtils {

    public static View getIngredientItemView(Context context) {
        return LayoutInflater.from(context).inflate(R.layout.ingredient_item, null);
    }

    public static View getIngredientItemViewFromIngredient(Context context, Ingredient ingredient) {
        View ingredientItemView = getIngredientItemView(context);

        // set id (technical field)
        TextView tvId = ingredientItemView.findViewById(R.id.id);
        tvId.setText(Long.toString(ingredient.getId()));

        // set amount (try to convert it to an integer first, for a prettier appearance)
        TextView tvAmount = ingredientItemView.findViewById(R.id.amount);
        try {
            tvAmount.setText(Integer.toString((int) ingredient.getAmount()));
        } catch (NumberFormatException nfe) {
            tvAmount.setText(Double.toString(ingredient.getAmount()));
        }

        // set unit
        Spinner sUnit = ingredientItemView.findViewById(R.id.unit);
        sUnit.setSelection(Arrays.asList(context.getResources().getStringArray(R.array.unit_values))
                .indexOf(ingredient.getUnit()));

        // set Ingredient name
        TextView tvIngredient = ingredientItemView.findViewById(R.id.ingredient);
        tvIngredient.setText(ingredient.getIngredient());

        return ingredientItemView;
    }

    public static View getStepItemView(Context context) {
        return LayoutInflater.from(context).inflate(R.layout.step_item, null);
    }

    public static View getStepItemViewFromStep(Context context, Step step) {
        View stepItemView = getStepItemView(context);

        // set id (technical field)
        TextView tvId = stepItemView.findViewById(R.id.id);
        tvId.setText(Long.toString(step.getId()));

        // set Step description
        TextView tvStep = stepItemView.findViewById(R.id.step);
        tvStep.setText(step.getStep());

        return stepItemView;
    }

}
