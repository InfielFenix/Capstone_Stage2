package com.projects.alexanderauer.shooker.adapters;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v13.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.projects.alexanderauer.shooker.R;
import com.projects.alexanderauer.shooker.RecipeDetailActivity;
import com.projects.alexanderauer.shooker.data.Recipe;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Alex on 02.08.2017.
 */

public class RecipeCardAdapter extends RecyclerView.Adapter<RecipeCardAdapter.ViewHolder> {

    private ArrayList<Recipe> mRecipes;
    private OnRecipeItemClickListener mRecipeItemClickListener;

    public RecipeCardAdapter(ArrayList<Recipe> recipes, OnRecipeItemClickListener recipeItemClickListener) {
        mRecipes = recipes;
        mRecipeItemClickListener = recipeItemClickListener;
    }

    @Override
    public RecipeCardAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe_item, parent, false);

        final ViewHolder viewHolder = new ViewHolder(view);

        final ImageView recipePhoto = view.findViewById(R.id.image);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRecipeItemClickListener.onRecipeItemClick(mRecipes.get(viewHolder.getAdapterPosition()),recipePhoto);
            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecipeCardAdapter.ViewHolder holder, int position) {
        Recipe recipe = mRecipes.get(position);

        holder.mTitle.setText(recipe.getTitle());

        String duration = recipe.getDuration() + " " + holder.mContext.getString(R.string.minutes_short);
        holder.mDuration.setText(duration);
        holder.mDifficulty.setText(recipe.getDifficulty());

        String photoUrl = recipe.getPhotoUrl();
        if (photoUrl != null && !photoUrl.equals(""))
            Picasso.with(holder.mContext)
                    .load(Uri.fromFile(new File(photoUrl)))
                    .resize(holder.mContext.getResources().getInteger(R.integer.photo_pixel_width),
                            holder.mContext.getResources().getInteger(R.integer.photo_pixel_height))
                    .centerCrop()
                    .into(holder.mImage);

        holder.mImage.setTransitionName(holder.mContext.getString(R.string.recipe_photo_transition) + recipe.getId());

        holder.mRecipe = recipe;
    }

    @Override
    public int getItemCount() {
        return mRecipes.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        final View mView;
        final TextView mTitle, mDuration, mDifficulty;
        final ImageView mImage;
        Recipe mRecipe;
        private Context mContext;

        ViewHolder(View itemView) {
            super(itemView);
            mContext = itemView.getContext();

            mView = itemView;
            mImage = itemView.findViewById(R.id.image);
            mTitle = itemView.findViewById(R.id.title);
            mDuration = itemView.findViewById(R.id.duration);
            mDifficulty = itemView.findViewById(R.id.difficulty);
            mRecipe = null;
        }
    }

    public interface OnRecipeItemClickListener{
        void onRecipeItemClick(Recipe recipe, ImageView recipePhoto);
    }
}

