package com.andreacioccarelli.androoster.tools;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import com.andreacioccarelli.androoster.R;
import com.andreacioccarelli.androoster.ui.settings.SettingStore;
import com.kabouzeid.appthemehelper.ThemeStore;

import es.dmoral.toasty.Toasty;

public class UI {
    private Context mActivity;
    private boolean shouldColor;
    private boolean shouldTintBasingOnBehavior;

    @ColorInt
    private static int ERROR_COLOR = Color.parseColor("#D50000");
    @ColorInt
    private static int INFO_COLOR = Color.parseColor("#3F51B5");
    @ColorInt
    private static int SUCCESS_COLOR = Color.parseColor("#388E3C");
    @ColorInt
    private static int WARNING_COLOR = Color.parseColor("#FFA900");
    @ColorInt
    private static final int NORMAL_COLOR = Color.parseColor("#353A3E");

    public UI(@NonNull Context context) {
        mActivity = context;

        shouldColor = new PreferencesBuilder(context).getPreferenceBoolean(SettingStore.THEME.SHOW_COLORED_TOASTS,true);
        shouldTintBasingOnBehavior = new PreferencesBuilder(context).getPreferenceBoolean(SettingStore.THEME.DYNAMICALLY_THEME_TOASTS,true);

        Handler handler = new Handler();
    }

    private void updateValues() {
        shouldColor = new PreferencesBuilder(mActivity).getPreferenceBoolean(SettingStore.THEME.SHOW_COLORED_TOASTS,true);
        shouldTintBasingOnBehavior = new PreferencesBuilder(mActivity).getPreferenceBoolean(SettingStore.THEME.DYNAMICALLY_THEME_TOASTS,true);
    }

    public final void success(String text) {
        updateValues();
        if (shouldColor) {
            if (shouldTintBasingOnBehavior) {
                Toasty.success(mActivity, text, 0).show();
            } else {
                Toasty.custom(mActivity, text, R.drawable.on, ThemeStore.accentColor(mActivity),0,true,true).show();
            }
        } else {
            Toasty.normal(mActivity, text, 0).show();
        }
    }

    public final void success(boolean value) {
        String text = String.valueOf(value);
        updateValues();
        if (shouldColor) {
            if (shouldTintBasingOnBehavior) {
                Toasty.success(mActivity, text, 0).show();
            } else {
                Toasty.custom(mActivity, text, R.drawable.on, ThemeStore.accentColor(mActivity),0,true,true).show();
            }
        } else {
            Toasty.normal(mActivity, text, 0).show();
        }
    }

    public final void info(String text) {
        updateValues();
        if (shouldColor) {
            if (shouldTintBasingOnBehavior) {
                Toasty.info(mActivity, text, 0).show();
            } else {
                Toasty.custom(mActivity, text, R.drawable.ic_info_outline_white_48dp, ThemeStore.accentColor(mActivity),0,true,true).show();
            }
        } else {
            Toasty.normal(mActivity, text, 0).show();
        }
    }

    public final void warning(String text) {
        updateValues();
        if (shouldColor) {
            if (shouldTintBasingOnBehavior) {
                Toasty.warning(mActivity, text, 0).show();
            } else {
                Toasty.custom(mActivity, text, R.drawable.icon_warning, ThemeStore.accentColor(mActivity),0,true,true).show();
            }
        } else {
            Toasty.normal(mActivity, text, 0).show();
        }
    }

    public final void normal(String text) {
        updateValues();
        if (shouldColor) {
            if (shouldTintBasingOnBehavior) {
                Toasty.normal(mActivity, text, 0).show();
            } else {
                Toasty.custom(mActivity, text, null, ThemeStore.accentColor(mActivity),0,false,true).show();
            }
        } else {
            Toasty.normal(mActivity, text, 0).show();
        }
    }

    public final void error(String text) {
        updateValues();
        Toasty.error(mActivity, text, 0).show();
    }

    public final void unconditionalError(String text) {
        Toasty.error(mActivity, text, 0).show();
    }

    public final void unconditionalInfo(String text) {
        Toasty.info(mActivity, text, 0).show();
    }

    public final void unconditionalWarn(String text) {
        Toasty.warning(mActivity, text, 0).show();
    }

    public final void unconditionalSuccess(String text) {
        Toasty.success(mActivity, text, 0).show();
    }


    public final void on() {
        updateValues();
        if (shouldColor) {
            if (shouldTintBasingOnBehavior) {
                Toasty.custom(mActivity, mActivity.getString(R.string.state_enabled), R.drawable.on, ContextCompat.getColor(mActivity,R.color.Blue_500),0,true,true).show();
            } else {
                Toasty.custom(mActivity,  mActivity.getString(R.string.state_enabled), R.drawable.on, ThemeStore.accentColor(mActivity),0,true,true).show();
            }
        } else {
            Toasty.custom(mActivity,  mActivity.getString(R.string.state_enabled), R.drawable.on, NORMAL_COLOR, 0, true, true).show();
        }
    }


    public final void off() {
        updateValues();
        if (shouldColor) {
            if (shouldTintBasingOnBehavior) {
                Toasty.custom(mActivity,  mActivity.getString(R.string.state_disabled), R.drawable.off, ContextCompat.getColor(mActivity,R.color.Red_500),0,true,true).show();
            } else {
                Toasty.custom(mActivity,  mActivity.getString(R.string.state_disabled), R.drawable.off, ThemeStore.accentColor(mActivity),0,true,true).show();
            }
        } else {
            Toasty.custom(mActivity,  mActivity.getString(R.string.state_disabled), R.drawable.off, NORMAL_COLOR, 0, true, true).show();
        }
    }


    public final void success(int delay, final String text) {
        new Handler().postDelayed(Toasty.success(mActivity, text, 0)::show, delay);
    }

    public final void info(int delay, final String text) {
        new Handler().postDelayed(() -> info(text), delay);
    }

    public final void warning(int delay, final String text) {
        new Handler().postDelayed(() -> warning(text), delay);
    }

    public final void normal(int delay, final String text) {
        new Handler().postDelayed(() -> normal(text), delay);
    }

    public final void error(int delay, final String text) {
        new Handler().postDelayed(() -> error(text), delay);
    }
}
