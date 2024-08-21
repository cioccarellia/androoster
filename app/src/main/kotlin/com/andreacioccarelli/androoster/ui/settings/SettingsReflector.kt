package com.andreacioccarelli.androoster.ui.settings

import android.view.Menu
import android.view.MenuItem

import com.andreacioccarelli.androoster.tools.PreferencesBuilder

object SettingsReflector {
    fun updateMenu(menu: Menu, preferenceBuilder: PreferencesBuilder) {
        try {
            menu.getItem(0).isVisible = preferenceBuilder.getPreferenceBoolean(SettingStore.MENU.SETTINGS, true)
            menu.getItem(1).isVisible = preferenceBuilder.getPreferenceBoolean(SettingStore.MENU.ABOUT, true)
            menu.getItem(2).isVisible = preferenceBuilder.getPreferenceBoolean(SettingStore.MENU.DASHBOARD, true)
            menu.getItem(3).isVisible = preferenceBuilder.getPreferenceBoolean(SettingStore.MENU.OPEN_DRAWER, true)
            menu.getItem(4).isVisible = preferenceBuilder.getPreferenceBoolean(SettingStore.MENU.BACKUP, false)
            menu.getItem(5).isVisible = preferenceBuilder.getPreferenceBoolean(SettingStore.MENU.REBOOT, false)
        } catch (npe: NullPointerException) {}
    }

    fun updateDashboardMenu(menu: Menu?, preferenceBuilder: PreferencesBuilder) {
        if (menu == null) {
            return
        }

        try {
            if (preferenceBuilder.getPreferenceBoolean(SettingStore.GENERAL.SHOW_SETTINGS_IN_TOOLBAR, false)) {
                menu.getItem(0).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            } else {
                menu.getItem(0).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            }
            menu.getItem(0).isVisible = true
            menu.getItem(1).isVisible = preferenceBuilder.getPreferenceBoolean(SettingStore.MENU.ABOUT, true)
            menu.getItem(2).isVisible = false
            menu.getItem(3).isVisible = preferenceBuilder.getPreferenceBoolean(SettingStore.MENU.OPEN_DRAWER, true)
            menu.getItem(4).isVisible = preferenceBuilder.getPreferenceBoolean(SettingStore.MENU.BACKUP, false)
            menu.getItem(5).isVisible = preferenceBuilder.getPreferenceBoolean(SettingStore.MENU.REBOOT, false)
        } catch (_: NullPointerException) {}
    }
}
