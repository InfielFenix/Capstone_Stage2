package com.projects.alexanderauer.shooker.data;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Entity class for Ingredients. It provides getter and setter for all attributes and
 * a constructor which creates Ingredients out of Cursor objects.
 * Furthermore it implements the Parcelable interface to pass the objects between activities.
 */

public class Ingredient implements Parcelable{

    // needed for database
    private long _id, recipeId;

    private double amount;

    private String unit, ingredient;

    private boolean checked;

    public Ingredient() {
    }

    public Ingredient(double amount, String unit, String ingredient) {
        setAmount(amount);
        setUnit(unit);
        setIngredient(ingredient);
    }

    // create Ingredient out of a Cursor object
    public Ingredient(Cursor cursor) {
        setId(cursor.getLong(IngredientLoader.Query._ID));
        setRecipeId(cursor.getLong(IngredientLoader.Query.RECIPE_ID));
        setAmount(cursor.getDouble(IngredientLoader.Query.AMOUNT));
        setUnit(cursor.getString(IngredientLoader.Query.UNIT));
        setIngredient(cursor.getString(IngredientLoader.Query.INGREDIENT));
        setChecked(cursor.getInt(IngredientLoader.Query.CHECKED) == 1);
    }

    protected Ingredient(Parcel in) {
        _id = in.readLong();
        recipeId = in.readLong();
        amount = in.readDouble();
        unit = in.readString();
        ingredient = in.readString();
        checked = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(_id);
        dest.writeLong(recipeId);
        dest.writeDouble(amount);
        dest.writeString(unit);
        dest.writeString(ingredient);
        dest.writeByte((byte) (checked ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Ingredient> CREATOR = new Creator<Ingredient>() {
        @Override
        public Ingredient createFromParcel(Parcel in) {
            return new Ingredient(in);
        }

        @Override
        public Ingredient[] newArray(int size) {
            return new Ingredient[size];
        }
    };

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getIngredient() {
        return ingredient;
    }

    public void setIngredient(String ingredient) {
        this.ingredient = ingredient;
    }

    public long getId() {
        return _id;
    }

    public void setId(long _id) {
        this._id = _id;
    }

    public long getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(long recipeId) {
        this.recipeId = recipeId;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
