package com.projects.alexanderauer.shooker.data;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Entity class for Recipes. It provides getter and setter for all attributes and
 * a constructor which creates Recipes out of Cursor objects. It contains two ArrayLists
 * containing the Ingredients and Steps of the recipe.
 * Furthermore it implements the Parcelable interface to pass the objects between activities.
 */

public class Recipe implements Parcelable {

    private long id;
    private String title, difficulty, photoUrl;
    private int duration;
    private boolean inShoppingList;
    private ArrayList<Ingredient> ingredients;
    private ArrayList<Step> steps;

    public Recipe() {
    }

    public Recipe(String title, String difficulty, String photoUrl, int duration, boolean in_shopping_list) {
        this.title = title;
        this.difficulty = difficulty;
        this.photoUrl = photoUrl;
        this.duration = duration;
        this.inShoppingList = in_shopping_list;

        this.ingredients = new ArrayList<>();
        this.steps = new ArrayList<>();
    }

    // create Recipe out of a Cursor object
    public Recipe(Cursor cursor) {
        setId(cursor.getLong(RecipeLoader.Query._ID));
        setTitle(cursor.getString(RecipeLoader.Query.TITLE));
        setDifficulty(cursor.getString(RecipeLoader.Query.DIFFICULTY));
        setPhotoUrl(cursor.getString(RecipeLoader.Query.PHOTO_URL));
        setDuration(cursor.getInt(RecipeLoader.Query.DURATION));
        setInShoppingList(cursor.getInt(RecipeLoader.Query.IN_SHOPPING_LIST) == 1);
    }

    protected Recipe(Parcel in) {
        id = in.readLong();
        title = in.readString();
        difficulty = in.readString();
        photoUrl = in.readString();
        duration = in.readInt();
        inShoppingList = in.readByte() != 0;
        ingredients = in.createTypedArrayList(Ingredient.CREATOR);
        steps = in.createTypedArrayList(Step.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(difficulty);
        dest.writeString(photoUrl);
        dest.writeInt(duration);
        dest.writeByte((byte) (inShoppingList ? 1 : 0));
        dest.writeTypedList(ingredients);
        dest.writeTypedList(steps);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Recipe> CREATOR = new Creator<Recipe>() {
        @Override
        public Recipe createFromParcel(Parcel in) {
            return new Recipe(in);
        }

        @Override
        public Recipe[] newArray(int size) {
            return new Recipe[size];
        }
    };

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photo_url) {
        this.photoUrl = photo_url;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public boolean isInShoppingList() {
        return inShoppingList;
    }

    public void setInShoppingList(boolean inShoppingList) {
        this.inShoppingList = inShoppingList;
    }

    public ArrayList<Ingredient> getIngredients() {
        if(ingredients == null)
            ingredients = new ArrayList<>();

        return ingredients;
    }

    public void setIngredients(ArrayList<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public ArrayList<Step> getSteps() {
        if (steps == null)
            steps = new ArrayList<>();

        return steps;
    }

    public void setSteps(ArrayList<Step> steps) {
        this.steps = steps;
    }

}
