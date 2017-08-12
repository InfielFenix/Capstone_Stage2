package com.projects.alexanderauer.shooker.util;

import com.projects.alexanderauer.shooker.data.Ingredient;

import java.util.ArrayList;

/**
 * Utility class to build the shopping list
 */

public class ShoppingListUtils {

    // compress shopping list: combine equal Ingredients and sum up their amounts
    public static ArrayList<Ingredient> compressShoppingList(ArrayList<Ingredient> shoppingList) {

        for (int i = 0; i < shoppingList.size(); i++) {
            Ingredient ingredient1 = shoppingList.get(i);
            for (int j = i + 1; j < shoppingList.size(); ) {
                Ingredient ingredient2 = shoppingList.get(j);

                if (ingredient2.getIngredient().trim().toLowerCase().equals(ingredient1.getIngredient().trim().toLowerCase())) {
                    if (ingredient2.getUnit().equals(ingredient1.getUnit())) {
                        ingredient1.setAmount(ingredient1.getAmount() + ingredient2.getAmount());
                        shoppingList.remove(j);
                    } else if (isUnitConvertible(ingredient2.getUnit(), ingredient1.getUnit())) {
                        ingredient1.setAmount(ingredient1.getAmount() +
                                convertUnit(ingredient2.getUnit(), ingredient1.getUnit(), ingredient2.getAmount()));
                        shoppingList.remove(j);
                    } else {
                        j++;
                    }
                } else
                    j++;
            }
        }

        return shoppingList;
    }

    // convert one unit to another
    private static double convertUnit(String fromUnit, String toUnit, double value) {
        if (fromUnit.equals("KG") && toUnit.equals("G") || fromUnit.equals("L") && toUnit.equals("ML"))
            return value * 1000;
        else if (fromUnit.equals("G") && toUnit.equals("KG") || fromUnit.equals("ML") && toUnit.equals("L"))
            return value / 1000;

        return value;
    }

    // check if unit conversion is possible at all
    private static boolean isUnitConvertible(String fromUnit, String toUnit) {
        return (fromUnit.equals("G") && toUnit.equals("KG") ||
                fromUnit.equals("KG") && toUnit.equals("G") ||
                fromUnit.equals("ML") && toUnit.equals("L") ||
                fromUnit.equals("L") && toUnit.equals("ML"));
    }

    // make a String out of a shopping list (needed for sharing)
    public static String shoppingListToString(ArrayList<Ingredient> shoppingList) {
        String shoppingListString = "Shopping List:\n";

        for (Ingredient ingredient : shoppingList) {
            shoppingListString += ingredient.getAmount() + " " + ingredient.getUnit() + " " + ingredient.getIngredient() + "\n";
        }

        return shoppingListString;
    }

}
