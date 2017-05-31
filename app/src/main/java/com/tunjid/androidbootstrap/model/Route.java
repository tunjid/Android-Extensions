package com.tunjid.androidbootstrap.model;

/**
 * Routes in the sample app
 * <p>
 * Created by tj.dahunsi on 5/30/17.
 */

public class Route {

    private CharSequence destination;
    private CharSequence description;

    public Route(CharSequence destination, CharSequence description) {
        this.destination = destination;
        this.description = description;
    }

    public CharSequence getDestination() {
        return destination;
    }

    public CharSequence getDescription() {
        return description;
    }
}
