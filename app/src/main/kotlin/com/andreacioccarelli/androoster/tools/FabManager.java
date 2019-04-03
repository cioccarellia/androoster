package com.andreacioccarelli.androoster.tools;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.andreacioccarelli.androoster.R;
import com.andreacioccarelli.androoster.tools.PreferencesBuilder;
import com.andreacioccarelli.androoster.ui.settings.SettingStore;
import com.mikepenz.materialdrawer.Drawer;

public class FabManager {
    public static void setup(final FloatingActionButton fabTop, final FloatingActionButton fabBottom, final Context baseContext, final Drawer drw, final PreferencesBuilder mBuilder) {
        fabBottom.setOnClickListener(view -> {
            drw.openDrawer();
            if (mBuilder.getPreferenceBoolean(SettingStore.GENERAL.ENABLE_ANIMATIONS,true)){
                Animation fabRotation = AnimationUtils.loadAnimation(baseContext, R.anim.fab_full);
                fabBottom.startAnimation(fabRotation);
            }
        });
        fabTop.setOnClickListener(view -> {
            drw.openDrawer();
            if (mBuilder.getPreferenceBoolean(SettingStore.GENERAL.ENABLE_ANIMATIONS,true)){
                Animation fabRotation = AnimationUtils.loadAnimation(baseContext, R.anim.fab_full);
                fabTop.startAnimation(fabRotation);
            }
        });
        if (mBuilder.getPreferenceBoolean(SettingStore.GENERAL.SHOW_OPEN_DRAWER_FAB,true)) {
            switch (mBuilder.getPreferenceString(SettingStore.GENERAL.OPEN_DRAWER_FAB_POSITION,"toolbar")) {
                case "toolbar":
                    fabBottom.setVisibility(View.GONE);
                    fabTop.setVisibility(View.VISIBLE);
                    break;
                case "bottom":
                    fabBottom.setVisibility(View.VISIBLE);
                    fabTop.setVisibility(View.GONE);
                    break;
            }
        } else {
            fabTop.setVisibility(View.GONE);
            fabBottom.setVisibility(View.GONE);
        }
    }

    public static void onResume(final FloatingActionButton fabTop, final FloatingActionButton fabBottom, final PreferencesBuilder mBuilder) {
        if (mBuilder.getPreferenceBoolean(SettingStore.GENERAL.SHOW_OPEN_DRAWER_FAB,true)) {
            switch (mBuilder.getPreferenceString(SettingStore.GENERAL.OPEN_DRAWER_FAB_POSITION,"toolbar")) {
                case "toolbar":
                    fabBottom.setVisibility(View.GONE);
                    fabTop.setVisibility(View.VISIBLE);
                    break;
                case "bottom":
                    fabBottom.setVisibility(View.VISIBLE);
                    fabTop.setVisibility(View.GONE);
                    break;
            }
        } else {
            fabTop.setVisibility(View.GONE);
            fabBottom.setVisibility(View.GONE);
        }
    }

    public static void animate(FloatingActionButton fab, Context ctx, boolean checkIfAnimationIsNeeded) {
        if (new PreferencesBuilder(ctx).getPreferenceBoolean(SettingStore.GENERAL.ENABLE_ANIMATIONS,true) && checkIfAnimationIsNeeded) {
            Animation fabRotation = AnimationUtils.loadAnimation(ctx, R.anim.fab_full);
            fab.startAnimation(fabRotation);
        }
    }

// --Commented out by Inspection START (08/02/18, 19:44):
//    public static void animate(FloatingActionButton fab1, FloatingActionButton fab2, Context ctx, boolean checkIfAnimationIsNeeded) {
//        if (new PreferencesBuilder(ctx).getPreferenceBoolean(SettingStore.GENERAL.ENABLE_ANIMATIONS,true) && checkIfAnimationIsNeeded) {
//            Animation fabRotation1 = AnimationUtils.loadAnimation(ctx, R.anim.fab_full);
//            fab1.startAnimation(fabRotation1);
//            Animation fabRotation2 = AnimationUtils.loadAnimation(ctx, R.anim.fab_full);
//            fab2.startAnimation(fabRotation2);
//        }
//    }
// --Commented out by Inspection STOP (08/02/18, 19:44)


// --Commented out by Inspection START (08/02/18, 19:44):
//    public static void onActivityLaunch(FloatingActionButton fab1, FloatingActionButton fab2, Context ctx) {
//        fab1.hide();
//        fab2.hide();
//    }
// --Commented out by Inspection STOP (08/02/18, 19:44)
}
