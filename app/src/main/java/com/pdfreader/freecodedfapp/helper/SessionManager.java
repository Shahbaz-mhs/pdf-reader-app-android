package com.pdfreader.freecodedfapp.helper;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pdfreader.freecodedfapp.ads.Const;
import com.pdfreader.freecodedfapp.models.PdfModel;

import java.lang.reflect.Type;
import java.util.ArrayList;


public class SessionManager {
    public static final String KEY_APP_LANGUAGE = "en";
    private static final String PREFER_NAME = "AppLangPref";
    Context _context;
    int PRIVATE_MODE = 0;
    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    @SuppressLint("CommitPrefEdits")
    public SessionManager(Context context) {
        this.pref = context.getSharedPreferences(Const.PREF_NAME, MODE_PRIVATE);
        this.editor = this.pref.edit();
    }

    public void savePdf(ArrayList<PdfModel> allPdfModels) {
        Gson gson = new Gson();
        String json = gson.toJson(allPdfModels);
        editor.putString(Const.pdf, json);
        editor.apply();

    }

    public ArrayList<PdfModel> getPdf() {

        String json = pref.getString(Const.pdf, null);

        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<PdfModel>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public void saveBooleanValue(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean getBooleanValue(String key) {
        return pref.getBoolean(key, false);
    }

    public void saveStringValue(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }

    public String getStringValue(String key) {
        return pref.getString(key, "");
    }


}
