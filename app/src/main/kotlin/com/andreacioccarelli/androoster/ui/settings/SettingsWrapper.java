package com.andreacioccarelli.androoster.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;

import com.andreacioccarelli.androoster.R;

public class SettingsWrapper {

    private static SettingsWrapper sInstance;

    private final SharedPreferences mPreferences;

    private SettingsWrapper(@NonNull final Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static SettingsWrapper getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            sInstance = new SettingsWrapper(context.getApplicationContext());
        }
        return sInstance;
    }

    public void registerOnSharedPreferenceChangedListener(SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener) {
        mPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }

    public void unregisterOnSharedPreferenceChangedListener(SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener) {
        mPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }

    @StyleRes
    public int getGeneralTheme() {
        return getThemeResFromPrefValue(mPreferences.getString(SettingStore.THEME.THEME, "light"));
    }

    public boolean isDark() {
        return (getThemeResFromPrefValue(mPreferences.getString(SettingStore.THEME.THEME, "light")) == R.style.Theme_Androoster_Dark);
    }

    @StyleRes
    public static int getThemeResFromPrefValue(String themePrefValue) {
        switch (themePrefValue) {
            case "dark":
                return R.style.Theme_Androoster_Dark;
            case "light":
                return R.style.Theme_Androoster_Light;
        }
        return R.style.Theme_Androoster_Light;
    }
}
