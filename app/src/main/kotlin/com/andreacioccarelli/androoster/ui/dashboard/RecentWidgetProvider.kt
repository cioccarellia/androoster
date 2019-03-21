package com.andreacioccarelli.androoster.ui.dashboard

import android.content.Context
import android.content.Intent
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes

import com.andreacioccarelli.androoster.R
import com.andreacioccarelli.androoster.tools.LaunchStruct
import com.andreacioccarelli.androoster.tools.LaunchManager


class RecentWidgetProvider(internal var ctx: Context) : LaunchStruct {

    @DrawableRes
    fun getIcon(ActivityID: Int): Int {
        when (ActivityID) {
            LaunchStruct.CPU_ACTIVITY -> return R.drawable.cpu
            LaunchStruct.RAM_ACTIVITY -> return R.drawable.ram
            LaunchStruct.BATTERY_ACTIVITY -> return R.drawable.battery
            LaunchStruct.KERNEL_ACTIVITY -> return R.drawable.kernel
            LaunchStruct.GENERAL_ACTIVITY -> return R.drawable.tweaks
            LaunchStruct.INTERNET_ACTIVITY -> return R.drawable.internet
            LaunchStruct.STORAGE_ACTIVITY -> return R.drawable.storage
            LaunchStruct.HARDWARE_ACTIVITY -> return R.drawable.hardware
            LaunchStruct.GRAPHICS_ACTIVITY -> return R.drawable.graphic
            LaunchStruct.DEBUG_ACTIVITY -> return R.drawable.debug
            LaunchStruct.GPS_ACTIVITY -> return R.drawable.gps
            LaunchStruct.BACKUP_ACTIVITY -> return R.drawable.drawer_backup_white
        }

        throw IllegalStateException("Required image does not exist")
    }

    fun getIntent(ActivityID: Int): Intent {
        return Intent(ctx, LaunchManager.getTargetClass(ActivityID))
    }

    fun getTitle(ActivityID: Int): String {
        return when (ActivityID) {
            LaunchStruct.CPU_ACTIVITY -> "CPU"
            LaunchStruct.RAM_ACTIVITY -> "RAM"
            LaunchStruct.BATTERY_ACTIVITY -> "BATTERY"
            LaunchStruct.KERNEL_ACTIVITY -> "KERNEL"
            LaunchStruct.GENERAL_ACTIVITY -> "TWEAKS"
            LaunchStruct.INTERNET_ACTIVITY -> "INTERNET"
            LaunchStruct.STORAGE_ACTIVITY -> "STORAGE"
            LaunchStruct.HARDWARE_ACTIVITY -> "HARDWARE"
            LaunchStruct.GRAPHICS_ACTIVITY -> "GRAPHICS"
            LaunchStruct.DEBUG_ACTIVITY -> "DEBUG"
            LaunchStruct.GPS_ACTIVITY -> "GPS"
            LaunchStruct.BACKUP_ACTIVITY -> "BACKUP"
            else -> ""
        }
    }

    @StringRes
    fun getTitleRes(ActivityID: Int): Int {
        return when (ActivityID) {
            LaunchStruct.CPU_ACTIVITY -> R.string.drawer_cpu
            LaunchStruct.RAM_ACTIVITY -> R.string.drawer_ram
            LaunchStruct.BATTERY_ACTIVITY -> R.string.drawer_battery
            LaunchStruct.KERNEL_ACTIVITY -> R.string.drawer_kernel
            LaunchStruct.GENERAL_ACTIVITY -> R.string.drawer_tweaks
            LaunchStruct.INTERNET_ACTIVITY -> R.string.drawer_net
            LaunchStruct.STORAGE_ACTIVITY -> R.string.drawer_storage
            LaunchStruct.HARDWARE_ACTIVITY -> R.string.drawer_hardware
            LaunchStruct.GRAPHICS_ACTIVITY -> R.string.drawer_graphics
            LaunchStruct.DEBUG_ACTIVITY -> R.string.drawer_debug
            LaunchStruct.GPS_ACTIVITY -> R.string.drawer_gps
            LaunchStruct.BACKUP_ACTIVITY -> R.string.drawer_backup
            else -> R.string.widget_loading
        }
    }


}
