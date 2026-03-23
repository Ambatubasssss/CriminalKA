package com.example.criminalintent;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

public final class LocaleManager {
    private static final String PREF_NAME = "criminal_intent_prefs";
    private static final String KEY_LANGUAGE_CODE = "language_code";
    private static final String DEFAULT_LANGUAGE_CODE = "en";

    private LocaleManager() {
    }

    public static void applySavedLocale(Context context) {
        applyLocale(context, getSavedLanguageCode(context));
    }

    public static void setLocale(Context context, String languageCode) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        preferences.edit().putString(KEY_LANGUAGE_CODE, languageCode).apply();
        applyLocale(context, languageCode);
    }

    public static String getSavedLanguageCode(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_LANGUAGE_CODE, DEFAULT_LANGUAGE_CODE);
    }

    private static void applyLocale(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Configuration configuration = new Configuration(resources.getConfiguration());
        configuration.setLocale(locale);
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }
}
