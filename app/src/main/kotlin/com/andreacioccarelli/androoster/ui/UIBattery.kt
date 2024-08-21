package com.andreacioccarelli.androoster.ui

import android.annotation.SuppressLint
import android.content.*
import android.content.res.Configuration
import android.graphics.Color
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatButton
import android.support.v7.widget.CardView
import android.support.v7.widget.SwitchCompat
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import az.plainpie.PieView
import az.plainpie.animation.PieStrokeWidthAnimation
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.internal.MDTintHelper
import com.afollestad.materialdialogs.internal.ThemeSingleton
import com.andreacioccarelli.androoster.R
import com.andreacioccarelli.androoster.core.Core
import com.andreacioccarelli.androoster.core.HardwareCore
import com.andreacioccarelli.androoster.dataset.KeyStore
import com.andreacioccarelli.androoster.dataset.XmlKeys
import com.andreacioccarelli.androoster.tools.*
import com.andreacioccarelli.androoster.ui.about.UIAbout
import com.andreacioccarelli.androoster.ui.backup.UIBackup
import com.andreacioccarelli.androoster.ui.base.BaseActivity
import com.andreacioccarelli.androoster.ui.dashboard.RecentWidget
import com.andreacioccarelli.androoster.ui.dashboard.UIDashboard
import com.andreacioccarelli.androoster.ui.settings.SettingStore
import com.andreacioccarelli.androoster.ui.settings.SettingsReflector
import com.andreacioccarelli.androoster.ui.settings.UISettings
import com.jrummyapps.android.shell.Shell
import com.kabouzeid.appthemehelper.ATH
import com.kabouzeid.appthemehelper.ThemeStore
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.schedule

class UIBattery : BaseActivity(), NavigationView.OnNavigationItemSelectedListener, LaunchStruct {


    lateinit var UI: UI
    lateinit var DRAWER_SETTINGS: PrimaryDrawerItem
    lateinit var DRAWER_BACKUP: PrimaryDrawerItem
    lateinit var drawer: Drawer
    var menu: Menu? = null

    internal var chargePlug: Int = 0
    internal var BatteryLevel: Int = 0

    internal var screenSize: Int = 0

    private var SecondsInput: EditText? = null
    private var positiveAction: View? = null
    internal var pro: Boolean = false
    internal var pluggedIn: Boolean = false

    private var warningTextView: TextView? = null
    private var warningTextViewText: String? = null
    private var editPropTitle: TextView? = null
    private var defaultValue: TextView? = null
    private var editDialog: MaterialDialog? = null
    private val errorColor = Color.parseColor("#F44336")




    private val BatteryReceiver = object : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(mContext: Context, intent: Intent) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
            when {
                level <= 20 -> findViewById<PieView>(R.id.batteryGraphic).setPercentageBackgroundColor(red)
                level <= 35 -> findViewById<PieView>(R.id.batteryGraphic).setPercentageBackgroundColor(yellow)
                else -> findViewById<PieView>(R.id.batteryGraphic).setPercentageBackgroundColor(green)
            }

            findViewById<PieView>(R.id.batteryGraphic).percentage = level.toFloat()

            val accentColor = ThemeStore.accentColor(mContext)

            if (level == 100) {
                ATH.setTint(findViewById(R.id.ButtonBattery2), accentColor)
            } else {
                ATH.setTint(findViewById(R.id.ButtonBattery2), errorColor)
            }
            BatteryLevel = level

            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus = this@UIBattery.registerReceiver(null, filter)
            chargePlug = batteryStatus!!.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)

            val status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

            val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0).toFloat() / 10
            val temperature = "$temp C"

            pluggedIn = isCharging

            findViewById<TextView>(R.id.ContentBattery1).text =
                    "${getString(R.string.battery_widget_status)}: ${
                    if (isCharging)
                        getString(R.string.battery_widget_status_charging)
                    else
                        getString(R.string.battery_widget_status_discharging)} " +
                    "${detectPowerSource()}\n" +
                    "${getString(R.string.battery_widget_health)}: ${detectBatteryHealth()}\n" +
                    "${getString(R.string.battery_widget_temperature)}: $temperature\n" +
                    "${getString(R.string.battery_widget_capacity)}: $capacity"

        }
    }

    private var wiped = false
    internal var capacity = ""

    internal var drawerInitialized = false

    internal var doubleBackToExitPressedOnce = false

    internal fun detectPowerSource(): String {
        return when (chargePlug) {
            BatteryManager.BATTERY_PLUGGED_USB -> getString(R.string.battery_widget_charging_usb)
            BatteryManager.BATTERY_PLUGGED_AC -> getString(R.string.battery_widget_charging_ac)
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> getString(R.string.battery_widget_charging_wireless)
            else -> ""
        }
    }

    internal fun detectBatteryHealth(): String {
        when (chargePlug) {
            BatteryManager.BATTERY_HEALTH_GOOD -> return getString(R.string.battery_widget_state_good)
            BatteryManager.BATTERY_HEALTH_COLD -> return getString(R.string.battery_widget_state_cold)
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> return getString(R.string.battery_widget_state_over_voltage)
            BatteryManager.BATTERY_HEALTH_DEAD -> return getString(R.string.battery_widget_state_dead)
            BatteryManager.BATTERY_HEALTH_UNKNOWN -> return getString(R.string.battery_widget_state_unknown)
        }
        return getString(R.string.battery_widget_state_good)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.battery)
        pro = PreferencesBuilder(this, PreferencesBuilder.defaultFilename).getBoolean("pro", false)

        RecentWidget.collect(this@UIBattery, LaunchStruct.BATTERY_ACTIVITY)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        preferencesBuilder = PreferencesBuilder(this@UIBattery)
        setSupportActionBar(toolbar)

        val errorColor = ContextCompat.getColor(baseContext, R.color.Red_500)

        capacity = HardwareCore.getBatteryCapacity(this@UIBattery)

        findViewById<PieView>(R.id.batteryGraphic).setMaxPercentage(100f)

        if (preferencesBuilder.getPreferenceBoolean(SettingStore.GENERAL.ENABLE_ANIMATIONS, true)) {
            val animation = PieStrokeWidthAnimation(findViewById<PieView>(R.id.batteryGraphic))
            animation.duration = 800
            findViewById<PieView>(R.id.batteryGraphic).startAnimation(animation)
        }

        screenSize = resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
        if (screenSize == Configuration.SCREENLAYOUT_SIZE_SMALL) {
            findViewById<PieView>(R.id.batteryGraphic).setPercentageTextSize(16f)
        } else if (screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE || screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE) {
            findViewById<PieView>(R.id.batteryGraphic).setPercentageTextSize(24f)
        }

        registerReceiver(BatteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        UI = UI(this@UIBattery)

        preferencesBuilder.putInt(XmlKeys.LAST_OPENED, LaunchStruct.BATTERY_ACTIVITY)

        animateContent(findViewById<PieView>(R.id.content) as ViewGroup)
        setUpDrawer(toolbar)
        FabManager.setup(findViewById(R.id.fabTop), findViewById(R.id.fabBottom), this@UIBattery, drawer, preferencesBuilder)

        findViewById<SwitchCompat>(R.id.SwitchBattery3).isChecked = preferencesBuilder.getBoolean("Battery3", false)
        findViewById<SwitchCompat>(R.id.SwitchBattery4).isChecked = preferencesBuilder.getBoolean("Battery4", false)
        findViewById<SwitchCompat>(R.id.SwitchBattery6).isChecked = preferencesBuilder.getBoolean("Battery6", false)
        findViewById<SwitchCompat>(R.id.SwitchBattery9).isChecked = preferencesBuilder.getBoolean("Battery9", false)


        findViewById<AppCompatButton>(R.id.ButtonBattery2).setOnClickListener { _ ->
            if (wiped) {
                UI.warning(getString(R.string.battery_statistics_just_wiped))
            } else {
                if (BatteryLevel != 100) {
                    MaterialDialog.Builder(this@UIBattery)
                            .iconRes(R.drawable.warning_red)
                            .title(R.string.battery_stats_title_not_100)
                            .content(R.string.battery_stats_content_not_100)
                            .positiveText(R.string.action_continue)
                            .negativeText(android.R.string.cancel)
                            .positiveColorRes(R.color.Red_500)
                            .onPositive { dialog, which ->
                                wiped = true
                                Core.erase_battery_stats()
                                UI.success(getString(R.string.battery_statistics_success))
                            }
                            .show()
                } else {
                    MaterialDialog.Builder(this@UIBattery)
                            .iconRes(R.drawable.wipe_stats)
                            .title(R.string.battery_stats_title_100)
                            .content(R.string.battery_stats_content_100)
                            .positiveText(R.string.action_continue)
                            .negativeText(android.R.string.cancel)
                            .positiveColorRes(R.color.Red_500)
                            .onPositive { dialog, which ->
                                wiped = true
                                Core.erase_battery_stats()
                                UI.success(getString(R.string.battery_statistics_success))
                            }
                            .show()
                }
            }
        }


        findViewById<AppCompatButton>(R.id.ButtonBattery5).setOnClickListener { _ ->
            var scanInterval = 0
            editDialog = MaterialDialog.Builder(this@UIBattery)
                    .title(R.string.edit_dialog_target_property)
                    .customView(R.layout.edit_dialog, true)
                    .positiveText(R.string.action_set)
                    .autoDismiss(false)
                    .negativeText(android.R.string.cancel)
                    .onPositive { _, _ ->
                        if (scanInterval > 0) {
                            Core.set_wifi_scan(scanInterval)
                            UI.success(getString(R.string.battery_wifi_success).replace("%s", scanInterval.toString()))
                            editDialog!!.dismiss()
                        } else {
                            UI.warning(getString(R.string.battery_wifi_error))
                        }
                    }
                    .onNegative { dialog1, _ -> dialog1.dismiss() }
                    .build()


            warningTextView = editDialog!!.customView!!.findViewById(R.id.newValue)
            warningTextViewText = resources.getString(R.string.edit_dialog_new_value)

            editPropTitle = editDialog!!.customView!!.findViewById(R.id.targetProperty)
            defaultValue = editDialog!!.customView!!.findViewById(R.id.defaultValue)

            editPropTitle!!.text = getString(R.string.battery_wifi_autoscan)
            defaultValue!!.text = getString(R.string.battery_wifi_dfu_seconds)

            positiveAction = editDialog!!.getActionButton(DialogAction.POSITIVE)
            SecondsInput = editDialog!!.customView!!.findViewById(R.id.input)
            ATH.setTint(SecondsInput!!, ThemeStore.accentColor(this@UIBattery))
            SecondsInput!!.setText(preferencesBuilder.getString(XmlKeys.WIFI_EDITED_TEXT_CACHE, ""))
            SecondsInput!!.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    val isEmpty = s.toString().trim().isEmpty()
                    val input = s.toString()
                    val numericInput = try {
                        Integer.parseInt(s.toString())
                    } catch (n: NumberFormatException) {
                        0
                    }

                    try {
                        if (isEmpty) {
                            positiveAction!!.isEnabled = false
                            warningTextView!!.text = warningTextViewText
                        } else if (s.toString().contains(",") || s.toString().contains(".") || Integer.parseInt(s.toString()) == 0) {
                            positiveAction!!.isEnabled = false
                            warningTextView!!.text = getString(R.string.battery_wifi_error)
                        } else if (numericInput > 300 || input.contains("-")) {
                            positiveAction!!.isEnabled = false
                            warningTextView!!.text = getString(R.string.battery_wifi_max)
                        } else if (numericInput <= 10) {
                            warningTextView!!.text = getString(R.string.battery_wifi_low)
                            positiveAction!!.isEnabled = s.toString().trim { it <= ' ' }.isNotEmpty()
                        } else {
                            scanInterval = Integer.valueOf(input)!!
                            warningTextView!!.text = warningTextViewText
                            positiveAction!!.isEnabled = s.toString().trim { it <= ' ' }.isNotEmpty()
                            preferencesBuilder.putString(XmlKeys.WIFI_EDITED_TEXT_CACHE, s.toString())
                        }
                    } catch (e: NumberFormatException) {
                        positiveAction!!.isEnabled = false
                        warningTextView!!.text = getString(R.string.battery_wifi_max)
                        warningTextView!!.setTextColor(red)
                    }

                }

                override fun afterTextChanged(s: Editable) {}
            })

            val widgetColor = ThemeSingleton.get().widgetColor

            //MDTintHelper.setTint(SecondsInput!!,
            //        if (widgetColor == 0) ContextCompat.getColor(this@UIBattery, R.color.accent) else widgetColor)

            editDialog!!.show()
            positiveAction!!.isEnabled = true
        }


        findViewById<CardView>(R.id.CardBattery3).setOnClickListener { _ -> findViewById<SwitchCompat>(R.id.SwitchBattery3).performClick() }
        findViewById<CardView>(R.id.CardBattery4).setOnClickListener { _ -> findViewById<SwitchCompat>(R.id.SwitchBattery4).performClick() }
        findViewById<CardView>(R.id.CardBattery6).setOnClickListener { _ -> findViewById<SwitchCompat>(R.id.SwitchBattery6).performClick() }
        findViewById<CardView>(R.id.CardBattery9).setOnClickListener { _ -> findViewById<SwitchCompat>(R.id.SwitchBattery9).performClick() }

        findViewById<SwitchCompat>(R.id.SwitchBattery3).setOnClickListener { _ ->
            if (findViewById<SwitchCompat>(R.id.SwitchBattery3).isChecked) {
                preferencesBuilder.putBoolean("Battery3", true)
                UI.on()
                Core.set_internal_async_battery(true)
                Core.optimize_battery_services(true)
            } else {
                preferencesBuilder.putBoolean("Battery3", false)
                UI.off()
                Core.set_internal_async_battery(false)
                Core.optimize_battery_services(false)
            }
        }

        findViewById<SwitchCompat>(R.id.SwitchBattery4).setOnClickListener { _ ->
            if (findViewById<SwitchCompat>(R.id.SwitchBattery4).isChecked) {
                preferencesBuilder.putBoolean("Battery4", true)
                UI.on()
            } else {
                preferencesBuilder.putBoolean("Battery4", false)
                UI.off()
            }
        }


        findViewById<SwitchCompat>(R.id.SwitchBattery6).setOnClickListener { _ ->
            if (findViewById<SwitchCompat>(R.id.SwitchBattery6).isChecked) {
                preferencesBuilder.putBoolean("Battery6", true)
                UI.on()
                Core.set_sleep_mode(true)
            } else {
                preferencesBuilder.putBoolean("Battery6", false)
                UI.off()
                Core.set_sleep_mode(false)
            }
        }

        val d: String? = packageManager.getInstallerPackageName(packageName)
        if (d != null) {
            if (d == "com.android.packageinstaller") {
                Toasty.error(baseContext, getString(R.string.app_toast))
                shutdownApp()
                return
            }
        }

        /*
        SwitchBattery7.setOnClickListener { _ ->
            if (!pluggedIn) {
                if (SwitchBattery7.isChecked) {
                    preferencesBuilder.putBoolean("Battery7", true)
                    Core.set_fast_charging(true)
                } else {
                    preferencesBuilder.putBoolean("Battery7", false)
                    Core.set_fast_charging(false)
                }
            } else {
                SwitchBattery7.isChecked = !SwitchBattery7.isChecked
                UI.warning(getString(R.string.battery_unplug))
            }
        }

        SwitchBattery8.setOnClickListener { _ ->
            if (!pluggedIn) {
                if (SwitchBattery8.isChecked) {
                    preferencesBuilder.putBoolean("Battery8", true)
                    Core.wireless_fast_charging(true)
                } else {
                    preferencesBuilder.putBoolean("Battery8", false)
                    Core.wireless_fast_charging(false)
                }
            } else {
                SwitchBattery8.isChecked = !SwitchBattery8.isChecked
                UI.warning(getString(R.string.battery_unplug))
            }
        }*/

        findViewById<SwitchCompat>(R.id.SwitchBattery9).setOnClickListener { _ ->
            Thread.sleep(500)
            if (findViewById<SwitchCompat>(R.id.SwitchBattery9).isChecked) {
                preferencesBuilder.putBoolean("Battery9", true)
                CoroutineScope(Dispatchers.Main).launch { 
                    fixBatteryDrain()
                }
                UI.on()
            } else {
                preferencesBuilder.putBoolean("Battery9", false)
                UI.off()
            }
        }



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            findViewById<CardView>(R.id.cardBatteryDoze).visibility = View.VISIBLE
            findViewById<AppCompatButton>(R.id.buttonBatteryDoze).setOnClickListener {
                try {
                    startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                } catch (anf: ActivityNotFoundException) {
                    // Crashlytics.logException(anf)
                    UI.unconditionalError(getString(R.string.error_activity_settings_not_found))
                    startActivity(Intent(Settings.ACTION_SETTINGS))
                }
            }
        } else {
            findViewById<CardView>(R.id.cardBatteryDoze).visibility = View.GONE
        }


        val accentColor = ThemeStore.accentColor(this)
        val primaryColor = ThemeStore.primaryColor(this)
        val primaryDarkColor = ThemeStore.primaryColorDark(this)

        findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout).title = title
        findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout).setStatusBarScrimColor(primaryDarkColor)

        ATH.setActivityToolbarColor(this, toolbar, primaryColor)
        ATH.setBackgroundTint(findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout), primaryColor)
        ATH.setBackgroundTint(findViewById(R.id.fabTop), accentColor)
        ATH.setBackgroundTint(findViewById(R.id.fabBottom), accentColor)
        toolbar.setBackgroundColor(primaryColor)

        ATH.setTint(findViewById<AppCompatButton>(R.id.buttonBatteryDoze), accentColor)
        ATH.setTint(findViewById<AppCompatButton>(R.id.ButtonBattery2), errorColor)
        ATH.setTint(findViewById<AppCompatButton>(R.id.ButtonBattery5), accentColor)
        ATH.setTint(findViewById<SwitchCompat>(R.id.SwitchBattery3), accentColor)
        ATH.setTint(findViewById<SwitchCompat>(R.id.SwitchBattery4), accentColor)
        ATH.setTint(findViewById<SwitchCompat>(R.id.SwitchBattery6), accentColor)
        ATH.setTint(findViewById<SwitchCompat>(R.id.SwitchBattery9), accentColor)
    }


    override fun onResume() {
        super.onResume()
        FabManager.onResume(findViewById(R.id.fabTop), findViewById(R.id.fabBottom), preferencesBuilder)
        if (pro && drawerInitialized) {
            if (preferencesBuilder.getPreferenceBoolean(SettingStore.GENERAL.STICKY_SETTINGS, false)) {
                drawer.removeAllStickyFooterItems()
                drawer.removeItem(20)
                drawer.addStickyFooterItem(DRAWER_SETTINGS)
            } else {
                drawer.removeAllStickyFooterItems()
                drawer.removeItem(20)
                drawer.addItem(DRAWER_SETTINGS)
            }

            if (preferencesBuilder.getPreferenceBoolean(SettingStore.GENERAL.SHOW_BACKUP, false)) {
                drawer.removeItem(19)
                drawer.addItemAtPosition(DRAWER_BACKUP, 16)
            } else {
                drawer.removeItem(19)
            }
        }
        try {
            SettingsReflector.updateMenu(menu!!, preferencesBuilder)
        } catch (k: NullPointerException) {}
        
        val accentColor = ThemeStore.primaryColor(this)
        
        ATH.setTint(findViewById<AppCompatButton>(R.id.buttonBatteryDoze), accentColor)
        ATH.setTint(findViewById<AppCompatButton>(R.id.ButtonBattery2), errorColor)
        ATH.setTint(findViewById<AppCompatButton>(R.id.ButtonBattery5), accentColor)
    }

    private fun setUpDrawer(toolbar: Toolbar) {
        DrawerBuilder().withActivity(this@UIBattery).build()

        val DRAWER_DASHBOARD = PrimaryDrawerItem().withIdentifier(1).withName(R.string.drawer_dashboard).withIcon(R.drawable.dashboard).withOnDrawerItemClickListener { _, _, _ ->
            handleIntent(LaunchStruct.DASHBOARD_ACTIVITY)
            false
        }
        val DRAWER_CPU = PrimaryDrawerItem().withIdentifier(2).withName(R.string.drawer_cpu).withOnDrawerItemClickListener { _, _, _ ->
            handleIntent(LaunchStruct.CPU_ACTIVITY)
            false
        }
        val DRAWER_RAM = PrimaryDrawerItem().withIdentifier(3).withName(R.string.drawer_ram).withOnDrawerItemClickListener { _, _, _ ->
            handleIntent(LaunchStruct.RAM_ACTIVITY)
            false
        }
        val DRAWER_BATTERY = PrimaryDrawerItem().withIdentifier(4).withName(R.string.drawer_battery)
        val DRAWER_KERNEL = PrimaryDrawerItem().withIdentifier(5).withName(R.string.drawer_kernel).withOnDrawerItemClickListener { _, _, _ ->
            handleIntent(LaunchStruct.KERNEL_ACTIVITY)
            false
        }
        val DRAWER_TWEAKS = PrimaryDrawerItem().withIdentifier(6).withName(R.string.drawer_tweaks).withOnDrawerItemClickListener { _, _, _ ->
            handleIntent(LaunchStruct.GENERAL_ACTIVITY)
            false
        }
        val DRAWER_STORAGE = PrimaryDrawerItem().withIdentifier(7).withName(R.string.drawer_storage).withOnDrawerItemClickListener { _, _, _ ->
            handleIntent(LaunchStruct.STORAGE_ACTIVITY)
            false
        }
        val DRAWER_INTERNET = PrimaryDrawerItem().withIdentifier(8).withName(R.string.drawer_net).withOnDrawerItemClickListener { _, _, _ ->
            handleIntent(LaunchStruct.INTERNET_ACTIVITY)
            false
        }
        val DRAWER_DEBUG = PrimaryDrawerItem().withIdentifier(9).withName(R.string.drawer_debug).withOnDrawerItemClickListener { _, _, _ ->
            handleIntent(LaunchStruct.DEBUG_ACTIVITY)
            false
        }
        val DRAWER_GPS = PrimaryDrawerItem().withIdentifier(11).withName(R.string.drawer_gps).withOnDrawerItemClickListener { _, _, _ ->
            handleIntent(LaunchStruct.GPS_ACTIVITY)
            false
        }
        val DRAWER_HARDWARE = PrimaryDrawerItem().withIdentifier(12).withName(R.string.drawer_hardware).withOnDrawerItemClickListener { _, _, _ ->
            handleIntent(LaunchStruct.HARDWARE_ACTIVITY)
            false
        }
        val DRAWER_GRAPHICS = PrimaryDrawerItem().withIdentifier(13).withName(R.string.drawer_graphics).withOnDrawerItemClickListener { _, _, _ ->
            handleIntent(LaunchStruct.GRAPHICS_ACTIVITY)
            false
        }
        val DRAWER_ABOUT = PrimaryDrawerItem().withIdentifier(14).withName(R.string.drawer_about).withOnDrawerItemClickListener { _, _, _ ->
            handleIntent(LaunchStruct.ABOUT_ACTIVITY)
            false
        }
        val DRAWER_BUY_PRO_VERSION = PrimaryDrawerItem().withIdentifier(15).withName(R.string.drawer_pro).withOnDrawerItemClickListener { _, _, _ ->
            LicenseManager.startProActivity(this@UIBattery, this@UIBattery, drawer)
            false
        }
        DRAWER_BACKUP = PrimaryDrawerItem().withIdentifier(19L).withName(R.string.drawer_backup).withOnDrawerItemClickListener { _, _, _ ->
            startActivity(Intent(this@UIBattery, UIBackup::class.java))
            false
        }

        DRAWER_SETTINGS = PrimaryDrawerItem().withIdentifier(20).withName(R.string.drawer_settings).withOnDrawerItemClickListener { _, _, _ ->
            handleIntent(LaunchStruct.SETTINGS_ACTIVITY)
            false
        }


        if (!preferencesBuilder.getBoolean(XmlKeys.DARK_THEME_APPLIED, false)) {
            DRAWER_DASHBOARD.withIcon(R.drawable.drawer_black_dashboard)
            DRAWER_CPU.withIcon(R.drawable.drawer_black_cpu)
            DRAWER_RAM.withIcon(R.drawable.drawer_black_ram)
            DRAWER_BATTERY.withIcon(R.drawable.drawer_black_battery_100)
            DRAWER_KERNEL.withIcon(R.drawable.drawer_black_kernel)
            DRAWER_TWEAKS.withIcon(R.drawable.drawer_black_tweaks)
            DRAWER_STORAGE.withIcon(R.drawable.drawer_black_storage)
            DRAWER_INTERNET.withIcon(R.drawable.drawer_black_internet)
            DRAWER_DEBUG.withIcon(R.drawable.drawer_black_debug)
            DRAWER_GPS.withIcon(R.drawable.drawer_black_gps)
            DRAWER_HARDWARE.withIcon(R.drawable.drawer_black_hardware)
            DRAWER_GRAPHICS.withIcon(R.drawable.drawer_black_graphic)
            DRAWER_SETTINGS.withIcon(R.drawable.drawer_black_settings)
            DRAWER_BUY_PRO_VERSION.withIcon(R.drawable.drawer_black_buy)
            DRAWER_BACKUP.withIcon(R.drawable.drawer_backup_black)
            DRAWER_ABOUT.withIcon(R.drawable.drawer_black_about)
        } else {
            DRAWER_DASHBOARD.withIcon(R.drawable.drawer_white_dashboard)
            DRAWER_CPU.withIcon(R.drawable.drawer_white_cpu)
            DRAWER_RAM.withIcon(R.drawable.drawer_white_ram)
            DRAWER_BATTERY.withIcon(R.drawable.drawer_white_battery_100)
            DRAWER_KERNEL.withIcon(R.drawable.drawer_white_kernel)
            DRAWER_TWEAKS.withIcon(R.drawable.drawer_white_tweaks)
            DRAWER_STORAGE.withIcon(R.drawable.drawer_white_storage)
            DRAWER_INTERNET.withIcon(R.drawable.drawer_white_internet)
            DRAWER_DEBUG.withIcon(R.drawable.drawer_white_debug)
            DRAWER_GPS.withIcon(R.drawable.drawer_white_gps)
            DRAWER_HARDWARE.withIcon(R.drawable.drawer_white_hardware)
            DRAWER_GRAPHICS.withIcon(R.drawable.drawer_white_graphic)
            DRAWER_SETTINGS.withIcon(R.drawable.drawer_white_settings)
            DRAWER_BUY_PRO_VERSION.withIcon(R.drawable.drawer_white_buy)
            DRAWER_BACKUP.withIcon(R.drawable.drawer_backup_white)
            DRAWER_ABOUT.withIcon(R.drawable.drawer_white_about)
        }


        val factory = layoutInflater
        val DrawerHeader = factory.inflate(R.layout.drawer_header, null)
        BaseActivity.setDrawerHeader(DrawerHeader.findViewById(R.id.Title), DrawerHeader.findViewById(R.id.Content), DrawerHeader.findViewById(R.id.Image), DrawerHeader.findViewById(R.id.RootLayout), this@UIBattery, pro)


        if (pro) {
            drawer = DrawerBuilder()
                    .withActivity(this@UIBattery)
                    .withToolbar(toolbar)
                    .addDrawerItems(
                            DRAWER_BATTERY,
                            DRAWER_DASHBOARD,
                            DividerDrawerItem(),
                            DRAWER_CPU,
                            DRAWER_RAM,
                            DRAWER_KERNEL,
                            DRAWER_TWEAKS,
                            DRAWER_STORAGE,
                            DRAWER_INTERNET,
                            DRAWER_DEBUG,
                            DRAWER_GPS,
                            DRAWER_HARDWARE,
                            DRAWER_GRAPHICS,
                            DividerDrawerItem(),
                            DRAWER_BACKUP,
                            DRAWER_ABOUT,
                            DRAWER_SETTINGS
                    )
                    .withHeader(DrawerHeader)
                    .build()
        } else {
            drawer = DrawerBuilder()
                    .withActivity(this@UIBattery)
                    .withToolbar(toolbar)
                    .addDrawerItems(
                            DRAWER_BATTERY,
                            DRAWER_DASHBOARD,
                            DividerDrawerItem(),
                            DRAWER_CPU,
                            DRAWER_RAM,
                            DRAWER_KERNEL,
                            DRAWER_TWEAKS,
                            DRAWER_STORAGE,
                            DRAWER_INTERNET,
                            DRAWER_DEBUG,
                            DRAWER_GPS,
                            DRAWER_HARDWARE,
                            DRAWER_GRAPHICS,
                            DividerDrawerItem(),
                            DRAWER_BACKUP,
                            DRAWER_ABOUT,
                            DRAWER_SETTINGS
                    )
                    .addStickyDrawerItems(DRAWER_BUY_PRO_VERSION)
                    .withHeader(DrawerHeader)
                    .build()

        }

        drawerInitialized = true
    }

    fun handleIntent(ActivityID: Int) {
        LaunchManager.startActivity(ActivityID, this@UIBattery)
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen) {
            drawer.closeDrawer()
        } else {
            if (doubleBackToExitPressedOnce) {
                closeApp()
                return
            }
            if (preferencesBuilder.getPreferenceBoolean(SettingStore.GENERAL.PRESS_TWICE_TO_EXIT, false)) {
                this.doubleBackToExitPressedOnce = true
                val UI = UI(this@UIBattery)
                UI.normal(getString(R.string.click_again_to_exit))

                Timer().schedule(1500){ doubleBackToExitPressedOnce = false }
            } else {
                super.onBackPressed()
            }
        }
    }

    private fun fixBatteryDrain() {
        Shell.SU.run("pm enable com.google.android.gms/.ads.settings.AdsSettingsActivity")
        Shell.SU.run("pm enable com.google.android.gms/com.google.android.location.places.ui.aliaseditor.AliasEditorActivity")
        Shell.SU.run("pm enable com.google.android.gms/com.google.android.location.places.ui.aliaseditor.AliasEditorMapActivity")
        Shell.SU.run("pm enable com.google.android.gms/com.google.android.location.settings.ActivityRecognitionPermissionActivity")
        Shell.SU.run("pm enable com.google.android.gms/com.google.android.location.settings.GoogleLocationSettingsActivity")
        Shell.SU.run("pm enable com.google.android.gms/com.google.android.location.settings.LocationHistorySettingsActivity")
        Shell.SU.run("pm enable com.google.android.gms/com.google.android.location.settings.LocationSettingsCheckerActivity")
        Shell.SU.run("pm enable com.google.android.gms/.usagereporting.settings.UsageReportingActivity")
        Shell.SU.run("pm enable com.google.android.gms/.ads.adinfo.AdvertisingInfoContentProvider")
        Shell.SU.run("pm enable com.google.android.gms/com.google.android.location.internal.LocationContentProvider")
        Shell.SU.run("pm enable com.google.android.gms/.common.stats.net.contentprovider.NetworkUsageContentProvider")
        Shell.SU.run("pm enable com.google.android.gms/com.google.android.gms.ads.config.GServicesChangedReceiver")
        Shell.SU.run("pm enable com.google.android.gms/com.google.android.contextmanager.systemstate.SystemStateReceiver")
        Shell.SU.run("pm enable com.google.android.gms/.ads.jams.SystemEventReceiver")
        Shell.SU.run("pm enable com.google.android.gms/.ads.config.FlagsReceiver")
        Shell.SU.run("pm enable com.google.android.gms/.ads.social.DoritosReceiver")
        Shell.SU.run("pm enable com.google.android.gms/.analytics.AnalyticsReceiver")
        Shell.SU.run("pm enable com.google.android.gms/.analytics.internal.GServicesChangedReceiver")
        Shell.SU.run("pm enable com.google.android.gms/.common.analytics.CoreAnalyticsReceiver")
        Shell.SU.run("pm enable com.google.android.gms/.common.stats.GmsCoreStatsServiceLauncher")
        Shell.SU.run("pm enable com.google.android.gms/com.google.android.location.internal.AnalyticsSamplerReceiver")
        Shell.SU.run("pm enable com.google.android.gms/.checkin.CheckinService\$ActiveReceiver")
        Shell.SU.run("pm enable com.google.android.gms/.checkin.CheckinService\$ClockworkFallbackReceiver")
        Shell.SU.run("pm enable com.google.android.gms/.checkin.CheckinService\$ImposeReceiver")
        Shell.SU.run("pm enable com.google.android.gms/.checkin.CheckinService\$SecretCodeReceiver")
        Shell.SU.run("pm enable com.google.android.gms/.checkin.CheckinService\$TriggerReceiver")
        Shell.SU.run("pm enable com.google.android.gms/.checkin.EventLogService\$Receiver")
        Shell.SU.run("pm enable com.google.android.gms/com.google.android.location.reporting.service.ExternalChangeReceiver")
        Shell.SU.run("pm enable com.google.android.gms/com.google.android.location.reporting.service.GcmRegistrationReceiver")
        Shell.SU.run("pm enable com.google.android.gms/com.google.android.location.copresence.GcmRegistrationReceiver")
        Shell.SU.run("pm enable com.google.android.gms/com.google.android.location.copresence.GservicesBroadcastReceiver")
        Shell.SU.run("pm enable com.google.android.gms/com.google.android.location.internal.LocationProviderEnabler")
        Shell.SU.run("pm enable com.google.android.gms/com.google.android.location.internal.NlpNetworkProviderSettingsUpdateReceiver")
        Shell.SU.run("pm enable com.google.android.gms/com.google.android.location.network.ConfirmAlertActivity\$LocationModeChangingReceiver")
        Shell.SU.run("pm enable com.google.android.gms/com.google.android.location.places.ImplicitSignalsReceiver")
        Shell.SU.run("pm enable com.google.android.gms/com.google.android.libraries.social.mediamonitor.MediaMonitor")
        Shell.SU.run("pm enable com.google.android.gms/.location.copresence.GcmBroadcastReceiver")
        Shell.SU.run("pm enable com.google.android.gms/.location.reporting.service.GcmBroadcastReceiver")
        Shell.SU.run("pm enable com.google.android.gms/.social.location.GservicesBroadcastReceiver")
        Shell.SU.run("pm enable com.google.android.gms/.update.SystemUpdateService\$Receiver")
        Shell.SU.run("pm enable com.google.android.gms/.update.SystemUpdateService\$OtaPolicyReceiver")
        Shell.SU.run("pm enable com.google.android.gms/.update.SystemUpdateService\$SecretCodeReceiver")
        Shell.SU.run("pm enable com.google.android.gms/.update.SystemUpdateService\$ActiveReceiver")
        Shell.SU.run("pm enable com.google.android.gms/com.google.android.contextmanager.service.ContextManagerService")
        Shell.SU.run("pm enable com.google.android.gms/.ads.AdRequestBrokerService")
        Shell.SU.run("pm enable com.google.android.gms/.ads.GservicesValueBrokerService")
        Shell.SU.run("pm enable com.google.android.gms/.ads.identifier.service.AdvertisingIdNotificationService")
        Shell.SU.run("pm enable com.google.android.gms/.ads.identifier.service.AdvertisingIdService")
        Shell.SU.run("pm enable com.google.android.gms/.ads.jams.NegotiationService")
        Shell.SU.run("pm enable com.google.android.gms/.ads.pan.PanService")
        Shell.SU.run("pm enable com.google.android.gms/.ads.social.GcmSchedulerWakeupService")
        Shell.SU.run("pm enable com.google.android.gms/.analytics.AnalyticsService")
        Shell.SU.run("pm enable com.google.android.gms/.analytics.internal.PlayLogReportingService")
        Shell.SU.run("pm enable com.google.android.gms/.analytics.service.AnalyticsService")
        Shell.SU.run("pm enable com.google.android.gms/.analytics.service.PlayLogMonitorIntervalService")
        Shell.SU.run("pm enable com.google.android.gms/.analytics.service.RefreshEnabledStateService")
        Shell.SU.run("pm enable com.google.android.gms/.auth.be.proximity.authorization.userpresence.UserPresenceService")
        Shell.SU.run("pm enable com.google.android.gms/.common.analytics.CoreAnalyticsIntentService")
        Shell.SU.run("pm enable com.google.android.gms/.common.stats.GmsCoreStatsService")
        Shell.SU.run("pm enable com.google.android.gms/.backup.BackupStatsService")
        Shell.SU.run("pm enable com.google.android.gms/.deviceconnection.service.DeviceConnectionAsyncService")
        Shell.SU.run("pm enable com.google.android.gms/.deviceconnection.service.DeviceConnectionServiceBroker")
        Shell.SU.run("pm enable com.google.android.gms/.wallet.service.analytics.AnalyticsIntentService")
        Shell.SU.run("pm enable com.google.android.gms/.checkin.CheckinService")
        Shell.SU.run("pm enable com.google.android.gms/.checkin.EventLogService")
        Shell.SU.run("pm enable com.google.android.gms/com.google.android.location.internal.AnalyticsUploadIntentService")
        Shell.SU.run("pm enable com.google.android.gms/com.google.android.location.reporting.service.DeleteHistoryService")
        Shell.SU.run("pm enable com.google.android.gms/com.google.android.location.reporting.service.DispatchingService")
        Shell.SU.run("pm enable com.google.android.gms/com.google.android.location.reporting.service.InternalPreferenceServiceDoNotUse")
        Shell.SU.run("pm enable com.google.android.gms/com.google.android.location.reporting.service.LocationHistoryInjectorService")
        Shell.SU.run("pm enable com.google.android.gms/com.google.android.location.reporting.service.ReportingAndroidService")
        Shell.SU.run("pm enable com.google.android.gms/com.google.android.location.reporting.service.ReportingSyncService")
        Shell.SU.run("pm enable com.google.android.gms/com.google.android.location.activity.HardwareArProviderService")
        Shell.SU.run("pm enable com.google.android.gms/com.google.android.location.fused.FusedLocationService")
        Shell.SU.run("pm enable com.google.android.gms/com.google.android.location.fused.service.FusedProviderService")
        Shell.SU.run("pm enable com.google.android.gms/com.google.android.location.geocode.GeocodeService")
        Shell.SU.run("pm enable com.google.android.gms/com.google.android.location.geofencer.service.GeofenceProviderService")
        Shell.SU.run("pm enable com.google.android.gms/com.google.android.location.internal.GoogleLocationManagerService")
        Shell.SU.run("pm enable com.google.android.gms/com.google.android.location.places.PlaylogService")
        Shell.SU.run("pm enable com.google.android.gms/com.google.android.location.places.service.GeoDataService")
        Shell.SU.run("pm enable com.google.android.gms/com.google.android.location.places.service.PlaceDetectionService")
        Shell.SU.run("pm enable com.google.android.gms/com.google.android.libraries.social.mediamonitor.MediaMonitorIntentService")
        Shell.SU.run("pm enable com.google.android.gms/.config.ConfigService")
        Shell.SU.run("pm enable com.google.android.gms/.stats.PlatformStatsCollectorService")
        Shell.SU.run("pm enable com.google.android.gms/.usagereporting.service.UsageReportingService")
        Shell.SU.run("pm enable com.google.android.gms/.update.SystemUpdateService")
        Shell.SU.run("pm enable com.google.android.gms/com.google.android.location.network.ConfirmAlertActivity")
        Shell.SU.run("pm enable com.google.android.gms/com.google.android.location.network.LocationProviderChangeReceiver")
        Shell.SU.run("pm enable com.google.android.gms/com.google.android.location.internal.server.GoogleLocationService")
        Shell.SU.run("pm enable com.google.android.gms/com.google.android.location.internal.PendingIntentCallbackService")
        Shell.SU.run("pm enable com.google.android.gms/com.google.android.location.network.NetworkLocationService")
        Shell.SU.run("pm enable com.google.android.gms/com.google.android.location.util.PreferenceService")
        Shell.SU.run("pm enable com.google.android.gsf/.update.SystemUpdateActivity")
        Shell.SU.run("pm enable com.google.android.gsf/.update.SystemUpdatePanoActivity")
        Shell.SU.run("pm enable com.google.android.gsf/com.google.android.gsf.checkin.CheckinService\\\$Receiver")
        Shell.SU.run("pm enable com.google.android.gsf/com.google.android.gsf.checkin.CheckinService\\\$SecretCodeReceiver")
        Shell.SU.run("pm enable com.google.android.gsf/com.google.android.gsf.checkin.CheckinService\\\$TriggerReceiver")
        Shell.SU.run("pm enable com.google.android.gsf/.checkin.EventLogService\$Receiver")
        Shell.SU.run("pm enable com.google.android.gsf/.update.SystemUpdateService\$Receiver")
        Shell.SU.run("pm enable com.google.android.gsf/.update.SystemUpdateService\$SecretCodeReceiver")
        Shell.SU.run("pm enable com.google.android.gsf/.checkin.CheckinService")
        Shell.SU.run("pm enable com.google.android.gsf/.checkin.EventLogService")
        Shell.SU.run("pm enable com.google.android.gsf/.update.SystemUpdateService")
    }
    
    override fun onDestroy() {
        unregisterReceiver(BatteryReceiver)
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.overflow, menu)
        this.menu = menu
        menu.getItem(0).isVisible = true
        menu.getItem(1).isVisible = preferencesBuilder.getPreferenceBoolean(SettingStore.MENU.ABOUT, true)
        menu.getItem(2).isVisible = preferencesBuilder.getPreferenceBoolean(SettingStore.MENU.DASHBOARD, true)
        menu.getItem(3).isVisible = preferencesBuilder.getPreferenceBoolean(SettingStore.MENU.OPEN_DRAWER, true)
        menu.getItem(4).isVisible = preferencesBuilder.getPreferenceBoolean(SettingStore.MENU.BACKUP, false)
        menu.getItem(5).isVisible = preferencesBuilder.getPreferenceBoolean(SettingStore.MENU.REBOOT, false)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when (id) {
            R.id.menu_about -> {
                startActivity(Intent(this@UIBattery, UIAbout::class.java))
                return true
            }
            R.id.menu_dashboard -> {
                startActivity(Intent(this@UIBattery, UIDashboard::class.java))
                return true
            }
            R.id.menu_settings -> {
                startActivity(Intent(this@UIBattery, UISettings::class.java))
                return true
            }
            R.id.menu_drawer -> {
                drawer.openDrawer()
                return true
            }
            R.id.menu_backup -> {
                startActivity(Intent(this@UIBattery, UIBackup::class.java))
                return true
            }
            R.id.menu_reboot -> {
                RebootDialog.show(this)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return false
    }
}
