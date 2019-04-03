package com.andreacioccarelli.androoster.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import com.andreacioccarelli.androoster.R
import com.andreacioccarelli.androoster.core.TerminalCore
import com.andreacioccarelli.androoster.interfaces.Governors
import com.andreacioccarelli.androoster.tools.PreferencesBuilder
import com.andreacioccarelli.androoster.ui.boot.UIBoot
import com.andreacioccarelli.androoster.ui.settings.SettingStore
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread


/**
 * Created by andrea on 2018/May.
 * Part of the package com.andreacioccarelli.androoster.service
 */

class BootService : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        doAsync {
            TerminalCore.mount()
            val preferencesBuilder = PreferencesBuilder(context)


            val appsToKill = listOf("com.paget96.lspeed", "com.androidvip.hebf", "com.androidvip.hebfpro")
            for (packageName in appsToKill) {
                TerminalCore.arun("am force-stop $packageName")
            }

            if (!preferencesBuilder.getPreferenceBoolean(SettingStore.GENERAL.HIDE_BOOT_NOTIFICATION, false)) {
                uiThread {
                    createNotificationChannel(context)

                    val pendingIntent = Intent(context, UIBoot::class.java)
                    pendingIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    val notificationIntent = PendingIntent.getActivity(context, 0, pendingIntent, 0)

                    val notificationBuilder = NotificationCompat.Builder(context, "boot_notification")
                            .setSmallIcon(R.drawable.notification_default)
                            .setContentTitle(context.getString(R.string.service_boot_title))
                            .setContentText(context.getString(R.string.service_boot_content_small))
                            .setStyle(NotificationCompat.BigTextStyle()
                                    .bigText(context.getString(R.string.service_boot_content_large)))
                            .setContentIntent(notificationIntent)

                    val notificationManager = NotificationManagerCompat.from(context)
                    notificationManager.notify(0, notificationBuilder.build())
                }
            }
        }
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification_channel_boot)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("boot_notification", name, importance)
            val notificationManager: NotificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getGovernorInt(governor: String): Int {
        if (governor.toLowerCase().contains(Governors.INTERACTIVE)) return 0
        if (governor.toLowerCase().contains(Governors.POWERSAVE)) return 1
        if (governor.toLowerCase().contains(Governors.PERFORMANCES)) return 2
        return if (governor.toLowerCase().contains(Governors.USERSPACE)) 3 else 0
    }

}