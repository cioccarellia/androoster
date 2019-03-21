package com.andreacioccarelli.androoster.ui.settings

@Suppress("ClassName")
class SettingStore {
    object GENERAL {
        const val START_PAGE = "default_start_page"
        const val PRESS_TWICE_TO_EXIT = "press_twice_for_exit"
        const val SHOW_OPEN_DRAWER_FAB = "show_open_drawer_fab"
        const val OPEN_DRAWER_FAB_POSITION = "open_drawer_fab_position"
        const val ENABLE_ANIMATIONS = "enable_animations"
        const val SHOW_SETTINGS_IN_TOOLBAR = "settings_in_toolbar"
        const val STICKY_SETTINGS = "sticky_settings"
        const val SHOW_BACKUP = "show_backup_drawer"
        const val HIDE_BOOT_NOTIFICATION = "hide_notification_boot"
    }

    object ANIMATIONS {
        const val ENABLE_ANIMATIONS = "enable_animations"
        const val ANIMATION_ORIENTATION = "animation_orientation"
        const val ANIMATION_SPEED = "animation_speed"
    }

    object THEME {
        const val PRIMARY_COLOR = "primary_color"
        const val ACCENT_COLOR = "accent_color"
        const val THEME = "activity_theme"
        const val SHOW_COLORED_TOASTS = "theme_toasts"
        const val DYNAMICALLY_THEME_TOASTS = "dynamically_theme_toasts"
        const val TINT_NAVIGATION_BAR = "theme_navbar"
        const val BUILT_IN_THEME = "builtin_activity_theme"
    }

    object MENU {
        const val SETTINGS = "menu_item_settings"
        const val ABOUT = "menu_item_about"
        const val DASHBOARD = "menu_item_dashboard"
        const val OPEN_DRAWER = "menu_item_drawer"
        const val BACKUP = "menu_item_backup"
        const val REBOOT = "menu_item_reboot"
    }

    object LOGIN {
        const val LOCK_ENABLED = "lock_enabled"
        const val ALLOW_FINGERPRINT = "allow_fingerprint"
        const val CHANGE_PASSWORD = "change_password"
        const val CHANGE_HINT = "change_hint"
        const val SHOW_PASSWORD_HINT = "show_password_hint"
        const val MASK_PASSWORDS = "mask_password"
    }
}
