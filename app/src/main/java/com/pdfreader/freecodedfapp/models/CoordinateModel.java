package com.pdfreader.freecodedfapp.models;

public class CoordinateModel {
    private float x;
    private float y;

    public CoordinateModel(float f, float f2) {
        this.x = f;
        this.y = f2;
    }

    public CoordinateModel() {
    }

    public float getX() {
        return this.x;
    }

    public void setX(float f) {
        this.x = f;
    }

    public float getY() {
        return this.y;
    }

    public void setY(float f) {
        this.y = f;
    }
}
