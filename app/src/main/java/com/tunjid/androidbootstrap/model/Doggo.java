package com.tunjid.androidbootstrap.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.DrawableRes;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Model class for G O O D  B O Y E S  A N D  G I R L E S  B R O N T
 * <p>
 * Created by tj.dahunsi on 5/20/17.
 */

public class Doggo implements Parcelable {

    private @DrawableRes int imageRes;
    private String name;

    private static final List<String> DOGGO_NOUNS = Arrays.asList(
            "F R E N",
            "B O Y E",
            "G I R L E"
    );

    private static final List<String> DOGGO_ADJECTIVES = Arrays.asList(
            "F L Y",
            "L A Z Y",
            "G O O D",
            "F L O O F",
            "H E C K I N",
            "B A M B O O Z L E"
    );

    public Doggo(@DrawableRes int imageRes) {
        this.imageRes = imageRes;
        name = DOGGO_ADJECTIVES.get(new Random().nextInt(DOGGO_ADJECTIVES.size()))
                + "  " + DOGGO_NOUNS.get(new Random().nextInt(DOGGO_NOUNS.size()));
    }

    public String getName() {
        return name;
    }

    @DrawableRes
    public int getImageRes() {
        return imageRes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Doggo)) return false;

        Doggo doggo = (Doggo) o;

        return imageRes == doggo.imageRes && name.equals(doggo.name);
    }

    @Override
    public int hashCode() {
        return imageRes;
    }

    @Override
    public String toString() {
        return getName() + "-" + hashCode();
    }

    private Doggo(Parcel in) {
        imageRes = in.readInt();
        name = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(imageRes);
        dest.writeString(name);
    }

    public static final Parcelable.Creator<Doggo> CREATOR = new Parcelable.Creator<Doggo>() {
        @Override
        public Doggo createFromParcel(Parcel in) {
            return new Doggo(in);
        }

        @Override
        public Doggo[] newArray(int size) {
            return new Doggo[size];
        }
    };
}
