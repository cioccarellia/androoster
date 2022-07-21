package com.andreacioccarelli.androoster.tools

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager


/**
 * Created by andrea on 2018/mar.
 * Part of the package com.andreacioccarelli.androoster.ui.network
 */
object ConnectionsManager {

    fun isDataOn(context: Context, connectionsManager: ConnectivityManager): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return try {
            val cmClass = Class.forName(cm.javaClass.name)
            val method = cmClass.getDeclaredMethod("getMobileDataEnabled")
            method.isAccessible = true // Make the method callable
            // get the setting for "mobile data"
            method.invoke(cm) as Boolean
        } catch (e: Exception) {
            connectionsManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)?.isConnected ?: false
        }
    }

    fun isWifiOn(context: Context): Boolean {
        val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wifi.isWifiEnabled
    }

}