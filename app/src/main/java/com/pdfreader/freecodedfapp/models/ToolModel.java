package com.pdfreader.freecodedfapp.models;

public class ToolModel {
    private String bgColor;
    private int drawable;
    private int id;
    private String title;

    public ToolModel(int i, String str, String str2, int i2) {
        this.id = i;
        this.title = str;
        this.bgColor = str2;
        this.drawable = i2;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int i) {
        this.id = i;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String str) {
        this.title = str;
    }

    public String getBgColor() {
        return this.bgColor;
    }

    public void setBgColor(String str) {
        this.bgColor = str;
    }

    public int getDrawable() {
        return this.drawable;
    }

    public void setDrawable(int i) {
        this.drawable = i;
    }
}
