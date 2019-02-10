package com.tunjid.androidbootstrap.model;

import android.graphics.Color;

import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

public class Tile implements Differentiable {

    private static final int[] colors = {
            Color.RED,
            Color.BLACK,
            Color.BLUE,
            Color.CYAN,
            Color.MAGENTA,
            Color.GREEN,
            Color.LTGRAY,
            Color.DKGRAY
    };

    private final int number;
    @ColorInt
    private final int color;

    private Tile(int number, int color) {
        this.number = number;
        this.color = color;
    }

    public static Tile generate(int id) {
        return new Tile(id, colors[randomIndex()]);
    }

    public int getNumber() {
        return number;
    }

    @ColorInt
    public int getColor() {
        return color;
    }

    @Override
    public String getId() {
        return String.valueOf(number);
    }

    @Override
    public boolean areContentsTheSame(Differentiable other) {
        if (!(other instanceof Tile)) return false;

        Tile otherTile = (Tile) other;
        return getId().equals(otherTile.getId()) && getColor() == otherTile.getColor();
    }

    @Override
    public Object getChangePayload(Differentiable other) {
        return other;
    }

    @NonNull
    @Override
    public String toString() {
        return getId();
    }

    private static int randomIndex() {
        return (int) (Math.random() * colors.length);
    }
}
