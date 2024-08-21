package com.andreacioccarelli.androoster.ui.dashboard;

import android.content.Context;

import com.andreacioccarelli.androoster.dataset.XmlKeys;
import com.andreacioccarelli.androoster.tools.LaunchStruct;
import com.andreacioccarelli.androoster.tools.PreferencesBuilder;

@SuppressWarnings("unused")
public class RecentWidget implements LaunchStruct {

    final static private String SEPARATOR = "$";

    public static void init(Context ctx) {
        PreferencesBuilder mBuilder = new PreferencesBuilder(ctx, PreferencesBuilder.defaultFilename);
        if (mBuilder.getBoolean(XmlKeys.INSTANCE.getRECENTLY_WIDGET_NOT_INITIALIZED(),true)) {
            mBuilder.putBoolean(XmlKeys.INSTANCE.getRECENTLY_WIDGET_NOT_INITIALIZED(),false);
            mBuilder.putString(XmlKeys.INSTANCE.getRECENTLY_WIDGET_DATASET(),"-1$-1$-1");
        }
    }

    private static void updateDatabase(final Context ctx, final String ActivityID) {
        new Thread(() -> {
            PreferencesBuilder mBuilder = new PreferencesBuilder(ctx, PreferencesBuilder.defaultFilename);
            String recently = mBuilder.getString(XmlKeys.INSTANCE.getRECENTLY_WIDGET_DATASET(),"-1$-1$-1").trim();
            if (recently.equals("")) {recently = "-1$-1$-1";}

            String[] recentLaunches = recently.split("\\$");
            String secondLaunch = recentLaunches[0];
            String thirdLaunch = recentLaunches[1];
            String markAsDeleted = recentLaunches[2];

            if (ActivityID.equals(thirdLaunch)) {
                // BATTERY - CPU - RAM ++ CPU
                updateDatabase(build(ActivityID,secondLaunch,markAsDeleted),mBuilder);
                return;
            }

            if (ActivityID.equals(secondLaunch)) {
                // BATTERY - CPU - RAM ++ BATTERY
                return;
            }

            String rebuiltList = ActivityID + SEPARATOR + secondLaunch + SEPARATOR + thirdLaunch;
            updateDatabase(rebuiltList,mBuilder);
        }).start();
    }

    static private String build(String... parts) {
        StringBuilder result = new StringBuilder();
        for (String ID : parts) {
            if (result.toString().equals("")) {
                result = new StringBuilder(ID);
            } else {
                result.append(SEPARATOR).append(ID);
            }
        }
        return result.toString();
    }

    static private void updateDatabase(String rebuiltList, PreferencesBuilder mBuilder) {
        mBuilder.putString(XmlKeys.INSTANCE.getRECENTLY_WIDGET_DATASET(),rebuiltList);
    }

    public static void collect(Context ctx, int ActivityID) {
        updateDatabase(ctx, String.valueOf(ActivityID));
    }

    public static int getFirst(Context ctx) {
        PreferencesBuilder mBuilder = new PreferencesBuilder(ctx, PreferencesBuilder.defaultFilename);
        String recently = mBuilder.getString(XmlKeys.INSTANCE.getRECENTLY_WIDGET_DATASET(),"").trim();
        String ActivityID = recently.split("\\$")[0];
        return Integer.parseInt(ActivityID);
    }


    public static int getSecond(Context ctx) {
        PreferencesBuilder mBuilder = new PreferencesBuilder(ctx, PreferencesBuilder.defaultFilename);
        String recently = mBuilder.getString(XmlKeys.INSTANCE.getRECENTLY_WIDGET_DATASET(),"").trim();
        return Integer.parseInt(recently.split("\\$")[1]);
    }


    public static int getThird(Context ctx) {
        PreferencesBuilder mBuilder = new PreferencesBuilder(ctx, PreferencesBuilder.defaultFilename);
        String recently = mBuilder.getString(XmlKeys.INSTANCE.getRECENTLY_WIDGET_DATASET(),"").trim();
        return Integer.parseInt(recently.split("\\$")[2]);
    }

/*
    public static int getForth(Context ctx) {
        PreferencesBuilder mBuilder = new PreferencesBuilder(ctx,PreferencesBuilder.defaultFilename);
        String recently = mBuilder.getString(XmlKeys.RECENTLY_WIDGET_DATASET,"").trim();
        return Integer.parseInt(recently.split("\\$")[3]) ;
    }


    public static int getFifth(Context ctx) {
        PreferencesBuilder mBuilder = new PreferencesBuilder(ctx,PreferencesBuilder.defaultFilename);
        String recently = mBuilder.getString(XmlKeys.RECENTLY_WIDGET_DATASET,"").trim();
        return Integer.parseInt(recently.split("\\$")[4]) ;
    }


    public static int getSixth(Context ctx) {
        PreferencesBuilder mBuilder = new PreferencesBuilder(ctx,PreferencesBuilder.defaultFilename);
        String recently = mBuilder.getString(XmlKeys.RECENTLY_WIDGET_DATASET,"").trim();
        return Integer.parseInt(recently.split("\\$")[5]) ;
    }*/

}
