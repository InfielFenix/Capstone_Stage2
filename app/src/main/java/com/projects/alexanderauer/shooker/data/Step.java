package com.projects.alexanderauer.shooker.data;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Alex on 04.08.2017.
 */

public class Step implements Parcelable{

    // needed for database
    private long _id, recipeId;

    private int sequence;

    private String step;

    public Step(){}

    public Step(String step) {
        setStep(step);
    }

    public Step(Cursor cursor) {
        setId(cursor.getLong(StepLoader.Query._ID));
        setRecipeId(cursor.getLong(StepLoader.Query.RECIPE_ID));
        setSequence(cursor.getInt(StepLoader.Query.SEQUENCE));
        setStep(cursor.getString(StepLoader.Query.STEP));
    }

    protected Step(Parcel in) {
        _id = in.readLong();
        recipeId = in.readLong();
        sequence = in.readInt();
        step = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(_id);
        dest.writeLong(recipeId);
        dest.writeInt(sequence);
        dest.writeString(step);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Step> CREATOR = new Creator<Step>() {
        @Override
        public Step createFromParcel(Parcel in) {
            return new Step(in);
        }

        @Override
        public Step[] newArray(int size) {
            return new Step[size];
        }
    };

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
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

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }
}
