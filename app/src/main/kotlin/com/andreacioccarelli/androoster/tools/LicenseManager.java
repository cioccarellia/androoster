package com.andreacioccarelli.androoster.tools;

import android.content.Context;
import android.content.Intent;

import com.andreacioccarelli.androoster.dataset.KeyStore;
import com.andreacioccarelli.androoster.ui.upgrade.UIUpgrade;
import com.mikepenz.materialdrawer.Drawer;

@SuppressWarnings({"unused", "SimplifiableIfStatement"})
public class LicenseManager {

    final static private String default_key = "YW5kcmVh";

    public static void startProActivity(Context ctx, Context Activity, Drawer drawer) {
        try {
            if (drawer.isDrawerOpen()) drawer.closeDrawer();
            drawer.deselect();
        } catch (RuntimeException wt) {
            wt.printStackTrace();
        }
        ctx.startActivity(new Intent(ctx, UIUpgrade.class));
    }
    public static boolean isPro(Context ctx) {
        //if (BuildConfig.TESTING_RELEASE) return true;
        final PreferencesBuilder mBuilder = new PreferencesBuilder(ctx, PreferencesBuilder.defaultFilename);

        return mBuilder.getBoolean("pro", false);
    }

    public static void finishProUpgrade(Context ctx) {
        final PreferencesBuilder mBuilder = new PreferencesBuilder(ctx, PreferencesBuilder.defaultFilename);
        final PreferencesBuilder hashBuilder = new PreferencesBuilder(ctx, PreferencesBuilder.Hashes, CryptoFactory.INSTANCE.md5(KeyStore.INSTANCE.getHashedDecryptionKey()));

        mBuilder.putBoolean("just_bought", true);
        mBuilder.putBoolean("pro",true);
    }

    public static void clearHashes(Context ctx) {
        final PreferencesBuilder hashBuilder = new PreferencesBuilder(ctx, PreferencesBuilder.Hashes, CryptoFactory.INSTANCE.md5(KeyStore.INSTANCE.getHashedDecryptionKey()));
        hashBuilder.erasePreferences();
    }

}