package com.pdfreader.freecodedfapp.models;

import java.io.Serializable;

public class ConfigModel implements Serializable {
    private int id;
    private boolean isRewarded;
    private int key;
    private int value;

    public ConfigModel() {
    }

    public ConfigModel(int i, int i2, boolean z) {
        this.key = i;
        this.value = i2;
        this.isRewarded = z;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int i) {
        this.id = i;
    }

    public int getKey() {
        return this.key;
    }

    public void setKey(int i) {
        this.key = i;
    }

    public int getValue() {
        return this.value;
    }

    public void setValue(int i) {
        this.value = i;
    }

    public boolean isRewarded() {
        return this.isRewarded;
    }

    public void setRewarded(boolean z) {
        this.isRewarded = z;
    }
}
