package com.projects.alexanderauer.shooker;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.projects.alexanderauer.shooker.data.Ingredient;
import com.projects.alexanderauer.shooker.data.Recipe;
import com.projects.alexanderauer.shooker.data.Step;
import com.projects.alexanderauer.shooker.util.ViewUtils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RecipeEditFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecipeEditFragment extends Fragment {
    // the fragment initialization parameters
    private static final String ARG_RECIPE = "recipe";
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private Recipe mRecipe;

    private ImageView mPhoto;
    private String mCurrentPhotoPath;
    private EditText mTitle, mDuration;
    private Spinner mDifficulty;
    private LinearLayout mIngredientContainer, mStepContainer;

    private OnRecipeSaveListener mRecipeSaveListener;

    public RecipeEditFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param recipe Recipe Object.
     * @return A new instance of fragment RecipeEditFragment.
     */
    public static RecipeEditFragment newInstance(@Nullable Recipe recipe) {
        RecipeEditFragment fragment = new RecipeEditFragment();
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
            mRecipe = new Recipe();
            getActivity().setTitle(getString(R.string.create_recipe));
        } else
            getActivity().setTitle(getString(R.string.edit_recipe));

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_recipe_edit, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save: {
                mRecipeSaveListener.onClickSaveRecipe(getData());
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View recipeEditView = inflater.inflate(R.layout.fragment_recipe_edit, container, false);

        getActivityCast().setSupportActionBar(((Toolbar) recipeEditView.findViewById(R.id.toolbar_recipe_edit)));
        getActivityCast().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getActivityCast().getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear_white_24dp);

        recipeEditView.findViewById(R.id.camera_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    File photo = null;
                    try {
                        photo = createImageFile();
                    } catch (IOException e) {
                        Log.e("Photo Error", e.getMessage());
                    }

                    if (photo != null) {
                        Uri photoUri = FileProvider.getUriForFile(getActivity(), "com.projects.alexanderauer.shooker.fileprovider", photo);
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
                    }
                }
            }
        });

        mPhoto = recipeEditView.findViewById(R.id.image);
        mTitle = recipeEditView.findViewById(R.id.recipe_title);
        mDuration = recipeEditView.findViewById(R.id.duration);
        mDifficulty = recipeEditView.findViewById(R.id.difficulty);

        mIngredientContainer = recipeEditView.findViewById(R.id.container_ingredients);
        mStepContainer = recipeEditView.findViewById(R.id.container_steps);

        if (mRecipe != null) {
            // set title
            mTitle.setText(mRecipe.getTitle());

            // set recipe photo
            if (mRecipe.getPhotoUrl() != null && !mRecipe.getPhotoUrl().equals(""))
                Picasso.with(getContext())
                        .load(Uri.fromFile(new File(mRecipe.getPhotoUrl())))
                        .resize(getResources().getInteger(R.integer.photo_pixel_width),
                                getResources().getInteger(R.integer.photo_pixel_height))
                        .centerCrop()
                        .into(mPhoto);
            else
                mPhoto.setImageResource(R.drawable.recipe_default_image);

            // set duration
            if (mRecipe.getDuration() > 0)
                mDuration.setText(String.format(Locale.getDefault(), "%d", mRecipe.getDuration()));

            // set difficulty
            mDifficulty.setSelection(Arrays.asList(getResources().getStringArray(R.array.difficulty_values))
                    .indexOf(mRecipe.getDifficulty()));

            // set ingredients
            for (Ingredient ingredient : mRecipe.getIngredients())
                mIngredientContainer.addView(ViewUtils.getIngredientItemViewFromIngredient(getContext(), ingredient));

            // set steps
            for (Step step : mRecipe.getSteps())
                mStepContainer.addView(ViewUtils.getStepItemViewFromStep(getContext(), step));
        }

        addNewEmptyIngredient();
        addNewEmptyStep();

        return recipeEditView;
    }

    public Recipe getData() {
        if (mTitle.getText().toString().equals(""))
            return null;
        else {
            mRecipe.setTitle(mTitle.getText().toString());
            try {
                mRecipe.setDuration(Integer.parseInt(mDuration.getText().toString()));
            } catch (NumberFormatException nfe) {
                Log.w("NumberFormatException", nfe.getMessage());
            }
            mRecipe.setDifficulty(mDifficulty.getSelectedItem().toString());

            // clear and add new ingredients
            mRecipe.setIngredients(new ArrayList<Ingredient>());
            for (int i = 0; i < mIngredientContainer.getChildCount(); i++) {
                View child = mIngredientContainer.getChildAt(i);

                Ingredient ingredient = new Ingredient();

                String _id = ((TextView) child.findViewById(R.id.id)).getText().toString();
                if(!_id.equals(""))
                    ingredient.setId(Long.valueOf(_id));

                ingredient.setRecipeId(mRecipe.getId());
                try {
                    ingredient.setAmount(Double.parseDouble(((TextView) child.findViewById(R.id.amount)).getText().toString()));
                } catch (NumberFormatException nfe) {
                    Log.w("NumberFormatException", nfe.getMessage());
                }
                ingredient.setUnit(((Spinner) child.findViewById(R.id.unit)).getSelectedItem().toString());
                ingredient.setIngredient(((TextView) child.findViewById(R.id.ingredient)).getText().toString());

                if (ingredient.getAmount() > 0 && !ingredient.getIngredient().equals(""))
                    mRecipe.getIngredients().add(ingredient);
            }

            //clear and add new steps
            mRecipe.setSteps(new ArrayList<Step>());
            for (int i = 0; i < mStepContainer.getChildCount(); i++) {
                View child = mStepContainer.getChildAt(i);

                Step step = new Step(((TextView) child.findViewById(R.id.step)).getText().toString());

                String _id = ((TextView) child.findViewById(R.id.id)).getText().toString();
                if(!_id.equals(""))
                    step.setId(Long.valueOf(_id));

                step.setRecipeId(mRecipe.getId());
                step.setSequence(i);

                if (!step.getStep().equals(""))
                    mRecipe.getSteps().add(step);
            }

            return mRecipe;
        }
    }

    private void addNewEmptyIngredient() {
        View ingredientView = ViewUtils.getIngredientItemView(getContext());

        EditText etAmount = ingredientView.findViewById(R.id.amount);
        etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.toString().equals(""))
                    addNewEmptyIngredient();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mIngredientContainer.addView(ingredientView);
    }

    private void addNewEmptyStep() {
        View stepView = ViewUtils.getStepItemView(getContext());

        EditText etStep = stepView.findViewById(R.id.step);
        etStep.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.toString().equals(""))
                    addNewEmptyStep();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mStepContainer.addView(stepView);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            mRecipe.setPhotoUrl(mCurrentPhotoPath);
            mPhoto.setAlpha(1.0f);
            Picasso.with(mPhoto.getContext())
                    .load(Uri.fromFile(new File(mRecipe.getPhotoUrl())))
                    .into(mPhoto);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private RecipeDetailActivity getActivityCast() {
        return (RecipeDetailActivity) getActivity();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mRecipeSaveListener = (OnRecipeSaveListener) context;
        } catch (ClassCastException cce) {
            throw new ClassCastException(context.toString() + " must implement OnRecipeSaveListener");
        }
    }

    public interface OnRecipeSaveListener {
        void onClickSaveRecipe(Recipe recipe);
    }
}