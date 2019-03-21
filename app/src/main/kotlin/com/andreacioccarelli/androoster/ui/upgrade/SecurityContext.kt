package com.andreacioccarelli.androoster.ui.upgrade

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

/**
 * Created by andrea on 2018/May.
 * Part of the package com.andreacioccarelli.androoster.ui.upgrade_v2
 */

private val pirateApps = arrayOf(
        "com.chelpus.lackypatch",
        "com.dimonvideo.luckypatcher",
        "com.forpda.lp",
        "com.android.vending.billing.InAppBillingService",
        "jase.freedom",
        "uret.jasi2169.patcher",
        "madkite.freedom",
        "org.creeplays.hack",
        "com.android.vendinc",
        "apps.zhasik007.hack",
        "com.leo.playcard",
        "com.appsara.app")


fun arePirateAppsInstalled(context: Context): Boolean {
    for (app in context.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)) {
        for (piratePackage in pirateApps) {
            if (app.packageName.contains(piratePackage)) return true
        }
    }
    return false
}



fun openApplicationSettings(activity: Activity) {
    activity.startActivityForResult(Intent(android.provider.Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS), 0)
}