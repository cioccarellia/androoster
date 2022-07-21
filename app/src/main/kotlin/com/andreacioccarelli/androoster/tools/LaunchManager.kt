package com.andreacioccarelli.androoster.tools

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import com.andreacioccarelli.androoster.dataset.KeyStore
import com.andreacioccarelli.androoster.ui.*
import com.andreacioccarelli.androoster.ui.about.UIAbout
import com.andreacioccarelli.androoster.ui.backup.UIBackup
import com.andreacioccarelli.androoster.ui.dashboard.UIDashboard
import com.andreacioccarelli.androoster.ui.settings.UISettings
import com.andreacioccarelli.androoster.ui.upgrade.UIUpgrade
import es.dmoral.toasty.Toasty

class LaunchManager : LaunchStruct {
    companion object {
        fun startActivity(i: Int, ctx: Context) {
            val prefs = PreferencesBuilder(ctx, PreferencesBuilder.defaultFilename)
            val pro = prefs.getBoolean("pro", false)
            if (canLaunch(i, pro)) {
                try {
                    val i2 = Intent(ctx, getTargetClass(i))
                    ctx.startActivity(i2)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                    // Crashlytics.logException(e)
                    // Crashlytics.log("Error, class not set as target in launchStruct")
                    Toasty.error(ctx, "Error, class not set as target in launchStruct", 1).show()
                }

            } else {
                ctx.startActivity(Intent(ctx, UIUpgrade::class.java))
            }
        }

        fun canLaunch(i: Int, pro: Boolean): Boolean {
            if (pro) return true
            return when (i) {
                LaunchStruct.DASHBOARD_ACTIVITY,
                LaunchStruct.SETTINGS_ACTIVITY,
                LaunchStruct.CPU_ACTIVITY,
                LaunchStruct.RAM_ACTIVITY,
                LaunchStruct.BATTERY_ACTIVITY,
                LaunchStruct.UPGRADE_ACTIVITY,
                LaunchStruct.KERNEL_ACTIVITY,
                LaunchStruct.ABOUT_ACTIVITY -> true
                else -> false
            }
        }

        fun getTargetClass(i: Int): Class<*>? {
            when (i) {
                LaunchStruct.DASHBOARD_ACTIVITY -> return UIDashboard::class.java
                LaunchStruct.CPU_ACTIVITY -> return UICpu::class.java
                LaunchStruct.RAM_ACTIVITY -> return UIRam::class.java
                LaunchStruct.BATTERY_ACTIVITY -> return UIBattery::class.java
                LaunchStruct.KERNEL_ACTIVITY -> return UIKernel::class.java
                LaunchStruct.GENERAL_ACTIVITY -> return UIGeneral::class.java
                LaunchStruct.INTERNET_ACTIVITY -> return UINetworking::class.java
                LaunchStruct.STORAGE_ACTIVITY -> return UIStorage::class.java
                LaunchStruct.HARDWARE_ACTIVITY -> return UIHardware::class.java
                LaunchStruct.GRAPHICS_ACTIVITY -> return UIGraphic::class.java
                LaunchStruct.DEBUG_ACTIVITY -> return UIDebug::class.java
                LaunchStruct.GPS_ACTIVITY -> return UIGps::class.java
                LaunchStruct.SETTINGS_ACTIVITY -> return UISettings::class.java
                LaunchStruct.ABOUT_ACTIVITY -> return UIAbout::class.java
                LaunchStruct.BACKUP_ACTIVITY -> return UIBackup::class.java
            }
            return null
        }
    }
}
