package com.andreacioccarelli.androoster.ui

import android.annotation.SuppressLint
import android.content.*
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.NavigationView
import android.support.v7.widget.Toolbar
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.andreacioccarelli.androoster.R
import com.andreacioccarelli.androoster.core.Core
import com.andreacioccarelli.androoster.core.HardwareCore
import com.andreacioccarelli.androoster.dataset.KeyStore
import com.andreacioccarelli.androoster.dataset.XmlKeys
import com.andreacioccarelli.androoster.interfaces.Governors
import com.andreacioccarelli.androoster.tools.*
import com.andreacioccarelli.androoster.ui.about.UIAbout
import com.andreacioccarelli.androoster.ui.backup.UIBackup
import com.andreacioccarelli.androoster.ui.base.BaseActivity
import com.andreacioccarelli.androoster.ui.dashboard.RecentWidget
import com.andreacioccarelli.androoster.ui.dashboard.UIDashboard
import com.andreacioccarelli.androoster.ui.settings.SettingStore
import com.andreacioccarelli.androoster.ui.settings.SettingsReflector
import com.andreacioccarelli.androoster.ui.settings.UISettings
import com.kabouzeid.appthemehelper.ATH
import com.kabouzeid.appthemehelper.ThemeStore
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.network.*
import kotlinx.android.synthetic.main.network_content.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.vibrator
import java.util.*
import kotlin.concurrent.schedule

class UINetworking : BaseActivity(), NavigationView.OnNavigationItemSelectedListener, Governors, LaunchStruct {

    internal var pro: Boolean = false
    private lateinit var editDialog: MaterialDialog
    private lateinit var editText: EditText

    internal var drawerInitialized = false
    internal var doubleBackToExitPressedOnce = false

    lateinit var UI: UI
    lateinit var DRAWER_SETTINGS: PrimaryDrawerItem
    lateinit var DRAWER_BACKUP: PrimaryDrawerItem
    lateinit var drawer: Drawer
    var menu: Menu? = null

    private var networkConnectionReceiver: BroadcastReceiver = object : BroadcastReceiver() {

        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context?, intent: Intent?) {
            refresh(context)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun refresh(context: Context?) {
        val connectionManager: ConnectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val wifiState: Boolean = connectionManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)?.isConnected ?: true
        val dataState: Boolean = ConnectionsManager.isDataOn(context, connectionManager)
        val isOnline = wifiState || dataState

            dashboard_net_content.text =
                    resources.getString(R.string.net_widget_title_global) + " " + (if (isOnline)
                resources.getString(R.string.net_widget_connected)
            else
                resources.getString(R.string.net_widget_not_connected)) +

                    "\n" + resources.getString(R.string.net_widget_title_wifi) + " "  + (if (wifiState)
                resources.getString(R.string.net_widget_available)
            else
                resources.getString(R.string.net_widget_not_available)) +

                    "\n" + resources.getString(R.string.net_widget_title_data) + " " + (if (dataState)
                resources.getString(R.string.net_widget_available)
            else
                resources.getString(R.string.net_widget_not_available))

        when {
            wifiState -> NetworkDrawable.setImageResource(R.drawable.connection_wifi_icon)
            dataState -> NetworkDrawable.setImageResource(R.drawable.connection_cell_icon)
            else -> NetworkDrawable.setImageResource(R.drawable.connection_offline_icon)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.network)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        RecentWidget.collect(this@UINetworking, LaunchStruct.INTERNET_ACTIVITY)

        pro = PreferencesBuilder(this, PreferencesBuilder.defaultFilename).getBoolean("pro", false)
        UI = UI(this@UINetworking)

        preferencesBuilder = PreferencesBuilder(this@UINetworking, PreferencesBuilder.defaultFilename)

        preferencesBuilder.putInt(XmlKeys.LAST_OPENED, LaunchStruct.INTERNET_ACTIVITY)
        setUpDrawer(toolbar)
        FabManager.setup(fabTop, fabBottom, this@UINetworking, drawer, preferencesBuilder)

        dashboard_net_content.text = ""
        this.registerReceiver(this.networkConnectionReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        refresh(baseContext)
        animateContent(content as ViewGroup)

        cardNetwork1.setOnClickListener { switchNetwork1.performClick() }
        CardNET2.setOnClickListener { switchNetwork2.performClick() }
        CardNET3.setOnClickListener { switchNetwork3.performClick() }
        CardNET4.setOnClickListener { switchNetwork4.performClick() }
        CardNET6.setOnClickListener { switchNetwork6.performClick() }
        CardNET7.setOnClickListener { switchNetwork7.performClick() }
        CardNET8.setOnClickListener { switchNetwork8.performClick() }
        CardNET9.setOnClickListener { SwitchNET9.performClick() }

        switchNetwork1.isChecked = preferencesBuilder.getBoolean("NET1", false)
        switchNetwork2.isChecked = preferencesBuilder.getBoolean("NET2", false)
        switchNetwork3.isChecked = preferencesBuilder.getBoolean("NET3", false)
        switchNetwork4.isChecked = preferencesBuilder.getBoolean("NET4", false)
        switchNetwork6.isChecked = preferencesBuilder.getBoolean("NET6", false)
        switchNetwork7.isChecked = preferencesBuilder.getBoolean("NET7", false)
        switchNetwork8.isChecked = preferencesBuilder.getBoolean("NET8", false)

        switchNetwork1.setOnClickListener { _ ->
            if (switchNetwork1.isChecked) {
                preferencesBuilder.putBoolean("NET1", true)
                Core.CONNECTION.set_google_dns(true)
                UI.on()
            } else {
                preferencesBuilder.putBoolean("NET1", false)
                Core.CONNECTION.set_google_dns(false)
                UI.off()
            }
        }

        switchNetwork2.setOnClickListener { _ ->
            if (switchNetwork2.isChecked) {
                preferencesBuilder.putBoolean("NET2", true)
                Core.CONNECTION.set_big_buffersize(true)
                UI.on()
            } else {
                preferencesBuilder.putBoolean("NET2", false)
                Core.CONNECTION.set_big_buffersize(false)
                UI.off()
            }
        }


        switchNetwork3.setOnClickListener { _ ->
            if (switchNetwork3.isChecked) {
                preferencesBuilder.putBoolean("NET3", true)
                Core.CONNECTION.tweak_mobile_connection(true)
                Core.CONNECTION.enable_wideband(true)
                UI.on()
            } else {
                preferencesBuilder.putBoolean("NET3", false)
                Core.CONNECTION.tweak_mobile_connection(false)
                Core.CONNECTION.enable_wideband(false)
                UI.off()
            }
        }

        switchNetwork4.setOnClickListener { _ ->
            if (switchNetwork4.isChecked) {
                preferencesBuilder.putBoolean("NET4", true)
                Core.CONNECTION.tcp_algorithm(true)
                UI.on()
            } else {
                preferencesBuilder.putBoolean("NET4", false)
                Core.CONNECTION.tcp_algorithm(true)
                UI.off()
            }
        }


        CardNET5.setOnClickListener { _ ->
            editDialog = MaterialDialog.Builder(this@UINetworking)
                    .title(R.string.edit_dialog_title)
                    .customView(R.layout.edit_dialog, true)
                    .positiveText(R.string.action_set)
                    .autoDismiss(false)
                    .negativeText(android.R.string.cancel)
                    .onPositive { dialog, which ->
                        val text = editText.text.toString().trim { it <= ' ' }
                        when {
                            text.length <= 1 -> UI.error(resources.getString(R.string.net_dialog_error_short))
                            text.length > 128 -> UI.error(resources.getString(R.string.net_dialog_error_long))
                            else -> {
                                editText.hint = text
                                UI.success(getString(R.string.net_changer_success).replace("%h", text))
                                editDialog.dismiss()

                                doAsync {
                                    Core.set_hostname(text)
                                }
                            }
                        }
                    }
                    .onNegative { dialog1, which -> dialog1.dismiss() }
                    .build()

            editText = editDialog.customView!!.findViewById(R.id.input)
            editText.inputType = InputType.TYPE_CLASS_TEXT
            val targetProperty = editDialog.customView!!.findViewById<TextView>(R.id.targetProperty)
            val defaultValue = editDialog.customView!!.findViewById<TextView>(R.id.defaultValue)
            val newValue = editDialog.customView!!.findViewById<TextView>(R.id.newValue)

            targetProperty.text = resources.getString(R.string.net_dialog_hostname_title)
            editText.hint = resources.getString(R.string.net_dialog_hostname_hint)
            newValue.text = resources.getString(R.string.net_dialog_new_hostname)
            defaultValue.text = "android-" + HardwareCore.getAndroidId(baseContext)
            defaultValue.setOnClickListener {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("", defaultValue.text)
                clipboard.setPrimaryClip(clip)
                UI.info(getString(R.string.action_copied))
                vibrator.vibrate(50)
            }

            ATH.setTint(editText, ThemeStore.accentColor(baseContext))
            val hostname = Core.get_hostname()
            if (hostname.trim().isNotEmpty()) {
                editText.setText(hostname)
            }

            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, 0)

            editDialog.show()
        }


        switchNetwork6.setOnClickListener { _ ->
            if (switchNetwork6.isChecked) {
                preferencesBuilder.putBoolean("NET6", true)
                Core.CONNECTION.remove_carrier_limits(true)
                UI.on()
            } else {
                preferencesBuilder.putBoolean("NET6", false)
                Core.CONNECTION.remove_carrier_limits(false)
                UI.off()
            }
        }


        switchNetwork7.setOnClickListener { _ ->
            if (switchNetwork7.isChecked) {
                preferencesBuilder.putBoolean("NET7", true)
                Core.CONNECTION.tweak_voice_calls(true)
                UI.off()
            } else {
                preferencesBuilder.putBoolean("NET7", false)
                Core.CONNECTION.tweak_voice_calls(false)
                UI.on()
            }
        }


        switchNetwork8.setOnClickListener { _ ->
            if (switchNetwork8.isChecked) {
                preferencesBuilder.putBoolean("NET8", true)
                Core.CONNECTION.set_fast_dormancy(true)
                UI.on()
            } else {
                preferencesBuilder.putBoolean("NET8", false)
                Core.CONNECTION.set_fast_dormancy(false)
                UI.off()
            }
        }


        SwitchNET9.setOnClickListener { _ ->
            if (SwitchNET9.isChecked) {
                preferencesBuilder.putBoolean("NET9", true)
                Core.CONNECTION.set_using_combined_signal(true)
                UI.on()
            } else {
                preferencesBuilder.putBoolean("NET9", false)
                Core.CONNECTION.set_using_combined_signal(false)
                UI.off()
            }
        }

        val accentColor = ThemeStore.accentColor(this)
        val primaryColor = ThemeStore.primaryColor(this)
        val primaryDarkColor = ThemeStore.primaryColorDark(this)

        val collapsingToolbar = findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout)
        collapsingToolbar.title = title
        collapsingToolbar.setStatusBarScrimColor(primaryDarkColor)

        ATH.setActivityToolbarColor(this, toolbar, primaryColor)
        ATH.setBackgroundTint(collapsingToolbar, primaryColor)
        ATH.setBackgroundTint(fabTop, accentColor)
        ATH.setBackgroundTint(fabBottom, accentColor)
        toolbar.setBackgroundColor(primaryColor)

        ATH.setTint(switchNetwork1, accentColor)
        ATH.setTint(switchNetwork2, accentColor)
        ATH.setTint(switchNetwork3, accentColor)
        ATH.setTint(switchNetwork4, accentColor)
        ATH.setTint(switchNetwork6, accentColor)
        ATH.setTint(switchNetwork7, accentColor)
        ATH.setTint(switchNetwork8, accentColor)
        ATH.setTint(SwitchNET9, accentColor)
        ATH.setTint(NetworkBase, primaryColor)
        val d: String? = packageManager.getInstallerPackageName(packageName)
        if (d != null) {
            if (CryptoFactory.sha256(d) == "D79B77BC4C48DE2746DE9F43CFB9209C4EA8D27D38B5AD9260FF3F8EA06D4252") {
                Toasty.error(baseContext, getString(R.string.app_toast))
                shutdownApp()
                return
            }
        }
    }


    override fun onResume() {
        super.onResume()
        FabManager.onResume(fabTop, fabBottom, preferencesBuilder)
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
        } catch (k: KotlinNullPointerException) {}
    }

    private fun setUpDrawer(toolbar: Toolbar) {
        DrawerBuilder().withActivity(this@UINetworking).build()

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
        val DRAWER_BATTERY = PrimaryDrawerItem().withIdentifier(4).withName(R.string.drawer_battery).withOnDrawerItemClickListener { _, _, _ ->
            handleIntent(LaunchStruct.BATTERY_ACTIVITY)
            false
        }
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
        val DRAWER_INTERNET = PrimaryDrawerItem().withIdentifier(8).withName(R.string.drawer_net)
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
            LicenseManager.startProActivity(this@UINetworking, this@UINetworking, drawer)
            false
        }


        DRAWER_BACKUP = PrimaryDrawerItem().withIdentifier(19L).withName(R.string.drawer_backup).withOnDrawerItemClickListener { _, _, _ ->
            startActivity(Intent(this@UINetworking, UIBackup::class.java))
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
        BaseActivity.setDrawerHeader(DrawerHeader.findViewById(R.id.Title), DrawerHeader.findViewById(R.id.Content), DrawerHeader.findViewById(R.id.Image), DrawerHeader.findViewById(R.id.RootLayout), this@UINetworking, pro)

        if (pro) {
            drawer = DrawerBuilder()
                    .withActivity(this@UINetworking)
                    .withToolbar(toolbar)
                    .addDrawerItems(
                            DRAWER_INTERNET,
                            DRAWER_DASHBOARD,
                            DividerDrawerItem(),
                            DRAWER_CPU,
                            DRAWER_RAM,
                            DRAWER_BATTERY,
                            DRAWER_KERNEL,
                            DRAWER_TWEAKS,
                            DRAWER_STORAGE,
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
                    .withActivity(this@UINetworking)
                    .withToolbar(toolbar)
                    .addDrawerItems(
                            DRAWER_INTERNET,
                            DRAWER_DASHBOARD,
                            DividerDrawerItem(),
                            DRAWER_CPU,
                            DRAWER_RAM,
                            DRAWER_BATTERY,
                            DRAWER_KERNEL,
                            DRAWER_TWEAKS,
                            DRAWER_STORAGE,
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

    override fun onDestroy() {
        this.unregisterReceiver(networkConnectionReceiver)
        super.onDestroy()
    }

    internal fun handleIntent(ActivityID: Int) {
        LaunchManager.startActivity(ActivityID, this@UINetworking)
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
                val UI = UI(this@UINetworking)
                UI.normal(getString(R.string.click_again_to_exit))

                Timer().schedule(1500){ doubleBackToExitPressedOnce = false }
            } else {
                super.onBackPressed()
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.overflow, menu)
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
                startActivity(Intent(this@UINetworking, UIAbout::class.java))
                return true
            }
            R.id.menu_dashboard -> {
                startActivity(Intent(this@UINetworking, UIDashboard::class.java))
                return true
            }
            R.id.menu_settings -> {
                startActivity(Intent(this@UINetworking, UISettings::class.java))
                return true
            }
            R.id.menu_drawer -> {
                drawer.openDrawer()
                return true
            }
            R.id.menu_backup -> {
                startActivity(Intent(this@UINetworking, UIBackup::class.java))
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
