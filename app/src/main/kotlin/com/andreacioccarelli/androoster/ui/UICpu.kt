package com.andreacioccarelli.androoster.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.NavigationView
import android.support.v7.widget.CardView
import android.support.v7.widget.SwitchCompat
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.andreacioccarelli.androoster.R
import com.andreacioccarelli.androoster.core.Core
import com.andreacioccarelli.androoster.core.HardwareCore
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.schedule

class UICpu : BaseActivity(), NavigationView.OnNavigationItemSelectedListener, Governors, LaunchStruct {

    private var pro: Boolean = false

    private var drawerInitialized = false

    lateinit var UI: UI
    lateinit var DRAWER_SETTINGS: PrimaryDrawerItem
    lateinit var DRAWER_BACKUP: PrimaryDrawerItem
    lateinit var drawer: Drawer
    var menu: Menu? = null
    
    private val governor: String
        get() = run("cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor").getStdout()

    private var cachedGovernor = ""

    private var doubleBackToExitPressedOnce = false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cpu)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        RecentWidget.collect(this@UICpu, LaunchStruct.CPU_ACTIVITY)

        UI = UI(this@UICpu)
        pro = PreferencesBuilder(this, PreferencesBuilder.defaultFilename).getBoolean("pro", false)

        preferencesBuilder = PreferencesBuilder(this@UICpu, PreferencesBuilder.defaultFilename)
        preferencesBuilder.putInt(XmlKeys.LAST_OPENED, LaunchStruct.CPU_ACTIVITY)

        createWidget()
        animateContent(findViewById(R.id.content) as ViewGroup)

        setUpDrawer(toolbar)
        FabManager.setup(findViewById(R.id.fabTop), findViewById(R.id.fabBottom), this@UICpu, drawer, preferencesBuilder)

        findViewById<CardView>(R.id.CardCPU3).setOnClickListener { findViewById<SwitchCompat>(R.id.SwitchCPU3).performClick() }
        findViewById<CardView>(R.id.CardCPU4).setOnClickListener { findViewById<SwitchCompat>(R.id.SwitchCPU4).performClick() }
        findViewById<CardView>(R.id.CardCPU5).setOnClickListener { findViewById<SwitchCompat>(R.id.SwitchCPU5).performClick() }

        findViewById<SwitchCompat>(R.id.SwitchCPU3).setOnClickListener {
            if (findViewById<SwitchCompat>(R.id.SwitchCPU3).isChecked) {
                Core.qcom_tweaks(true)
                Core.set_execution_mode(true)
                UI.on()
                if (preferencesBuilder.getBoolean("show_dialog_cpu3", true)) {
                    MaterialDialog.Builder(this@UICpu)
                            .title(R.string.cpu_cache_dialog_title)
                            .content(getString(R.string.cpu_cache_dialog_content) + if (isPackageInstalled("de.robv.android.xposed.installer")) "\n${getString(R.string.cpu_cache_dialog_content_xposed)}" else "")
                            .positiveText(R.string.cpu_cache_dialog_positive)
                            .negativeText(R.string.action_later)
                            .cancelable(false)
                            .onPositive { dialog, which ->
                                preferencesBuilder.putBoolean("show_dialog_cpu3", !dialog.isPromptCheckBoxChecked)
                                MaterialDialog.Builder(this@UICpu)
                                        .title(R.string.cpu_cache_dialog_working_title)
                                        .content(R.string.cpu_cache_dialog_working_content)
                                        .progress(true, 100)
                                        .show()

                                CoroutineScope(Dispatchers.Main).launch {
                                    Core.clear_dalvik_cache(true)
                                }
                            }
                            .onNegative { dialog, which -> preferencesBuilder.putBoolean("show_dialog_cpu3", !dialog.isPromptCheckBoxChecked) }
                            .checkBoxPromptRes(R.string.dont_ask_again, false, null)
                            .show()
                }
                preferencesBuilder.putBoolean("CPU3", true)
            } else {
                Core.qcom_tweaks(false)
                Core.set_execution_mode(false)
                preferencesBuilder.putBoolean("CPU3", false)
                UI.off()
            }
        }

        findViewById<SwitchCompat>(R.id.SwitchCPU4).setOnClickListener {
            if (findViewById<SwitchCompat>(R.id.SwitchCPU4).isChecked) {
                Core.optimize_cpu_usage(true)
                preferencesBuilder.putBoolean("CPU4", true)
                UI.on()
            } else {
                preferencesBuilder.putBoolean("CPU4", false)
                Core.optimize_cpu_usage(false)
                UI.off()
            }
        }

        findViewById<SwitchCompat>(R.id.SwitchCPU5).setOnClickListener {
            if (findViewById<SwitchCompat>(R.id.SwitchCPU5).isChecked) {
                Core.cpu_boost(true)
                preferencesBuilder.putBoolean("CPU5", true)
                UI.on()
            } else {
                Core.cpu_boost(false)
                preferencesBuilder.putBoolean("CPU5", false)
                UI.off()
            }
        }

        findViewById<SwitchCompat>(R.id.SwitchCPU3).isChecked = preferencesBuilder.getBoolean("CPU3", false)
        findViewById<SwitchCompat>(R.id.SwitchCPU4).isChecked = preferencesBuilder.getBoolean("CPU4", false)
        findViewById<SwitchCompat>(R.id.SwitchCPU5).isChecked = preferencesBuilder.getBoolean("CPU5", false)

        val accentColor = ThemeStore.accentColor(this)
        val primaryColor = ThemeStore.primaryColor(this)
        val primaryDarkColor = ThemeStore.primaryColorDark(this)

        findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout).title = title
        findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout).setStatusBarScrimColor(primaryDarkColor)

        ATH.setActivityToolbarColor(this, toolbar, primaryColor)
        ATH.setBackgroundTint(findViewById(R.id.toolbar_layout), primaryColor)
        ATH.setBackgroundTint(findViewById(R.id.fabTop), accentColor)
        ATH.setBackgroundTint(findViewById(R.id.fabBottom), accentColor)
        toolbar.setBackgroundColor(primaryColor)

        ATH.setTint(findViewById<SwitchCompat>(R.id.SwitchCPU3), accentColor)
        ATH.setTint(findViewById<SwitchCompat>(R.id.SwitchCPU4), accentColor)
        ATH.setTint(findViewById<SwitchCompat>(R.id.SwitchCPU5), accentColor)
        ATH.setTint(findViewById(R.id.CPUBase), primaryColor)
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

            CoroutineScope(Dispatchers.Main).launch {
                if (governor != cachedGovernor) {
                    CoroutineScope(Dispatchers.Main).launch { createWidget() }
                }
            }
        } catch (k: NullPointerException) {}
    }

    private fun setUpDrawer(toolbar: Toolbar) {
        DrawerBuilder().withActivity(this@UICpu).build()

        val DRAWER_DASHBOARD = PrimaryDrawerItem().withIdentifier(1).withName(R.string.drawer_dashboard).withIcon(R.drawable.dashboard).withOnDrawerItemClickListener { _, _, _ ->
            handleIntent(LaunchStruct.DASHBOARD_ACTIVITY)
            false
        }
        val DRAWER_CPU = PrimaryDrawerItem().withIdentifier(2).withName(R.string.drawer_cpu)
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
        val DRAWER_TWEAKS = PrimaryDrawerItem().withIdentifier(6).withName(R.string.drawer_tweaks).withIcon(R.drawable.tweaks).withOnDrawerItemClickListener { _, _, _ ->
            handleIntent(LaunchStruct.GENERAL_ACTIVITY)
            false
        }
        val DRAWER_STORAGE = PrimaryDrawerItem().withIdentifier(7).withName(R.string.drawer_storage).withIcon(R.drawable.storage).withOnDrawerItemClickListener { _, _, _ ->
            handleIntent(LaunchStruct.STORAGE_ACTIVITY)
            false
        }
        val DRAWER_INTERNET = PrimaryDrawerItem().withIdentifier(8).withName(R.string.drawer_net).withIcon(R.drawable.internet).withOnDrawerItemClickListener { _, _, _ ->
            handleIntent(LaunchStruct.INTERNET_ACTIVITY)
            false
        }
        val DRAWER_DEBUG = PrimaryDrawerItem().withIdentifier(9).withName(R.string.drawer_debug).withIcon(R.drawable.debug).withOnDrawerItemClickListener { _, _, _ ->
            handleIntent(LaunchStruct.DEBUG_ACTIVITY)
            false
        }
        val DRAWER_GPS = PrimaryDrawerItem().withIdentifier(11).withName(R.string.drawer_gps).withIcon(R.drawable.gps).withOnDrawerItemClickListener { _, _, _ ->
            handleIntent(LaunchStruct.GPS_ACTIVITY)
            false
        }
        val DRAWER_HARDWARE = PrimaryDrawerItem().withIdentifier(12).withName(R.string.drawer_hardware).withIcon(R.drawable.hardware).withOnDrawerItemClickListener { _, _, _ ->
            handleIntent(LaunchStruct.HARDWARE_ACTIVITY)
            false
        }
        val DRAWER_GRAPHICS = PrimaryDrawerItem().withIdentifier(13).withName(R.string.drawer_graphics).withIcon(R.drawable.gpu).withOnDrawerItemClickListener { _, _, _ ->
            handleIntent(LaunchStruct.GRAPHICS_ACTIVITY)
            false
        }
        val DRAWER_ABOUT = PrimaryDrawerItem().withIdentifier(14).withName(R.string.drawer_about).withOnDrawerItemClickListener { _, _, _ ->
            handleIntent(LaunchStruct.ABOUT_ACTIVITY)
            false
        }
        val DRAWER_BUY_PRO_VERSION = PrimaryDrawerItem().withIdentifier(15).withName(R.string.drawer_pro).withOnDrawerItemClickListener { _, _, _ ->
            LicenseManager.startProActivity(this@UICpu, this@UICpu, drawer)
            false
        }


        DRAWER_BACKUP = PrimaryDrawerItem().withIdentifier(19L).withName(R.string.drawer_backup).withOnDrawerItemClickListener { _, _, _ ->
            startActivity(Intent(this@UICpu, UIBackup::class.java))
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
        BaseActivity.setDrawerHeader(DrawerHeader.findViewById(R.id.Title), DrawerHeader.findViewById(R.id.Content), DrawerHeader.findViewById(R.id.Image), DrawerHeader.findViewById(R.id.RootLayout), this@UICpu, pro)


        if (pro) {
            drawer = DrawerBuilder()
                    .withActivity(this@UICpu)
                    .withToolbar(toolbar)
                    .addDrawerItems(
                            DRAWER_CPU,
                            DRAWER_DASHBOARD,
                            DividerDrawerItem(),
                            DRAWER_RAM,
                            DRAWER_BATTERY,
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
                    .withActivity(this@UICpu)
                    .withToolbar(toolbar)
                    .addDrawerItems(
                            DRAWER_CPU,
                            DRAWER_DASHBOARD,
                            DividerDrawerItem(),
                            DRAWER_RAM,
                            DRAWER_BATTERY,
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

        val d: String? = packageManager.getInstallerPackageName(packageName)
        if (d != null) {
            if (d == "com.android.packageinstaller") {
                Toasty.error(baseContext, getString(R.string.app_toast))
                shutdownApp()
                return
            }
        }

        drawerInitialized = true
    }

    private fun handleIntent(ActivityID: Int) {
        LaunchManager.startActivity(ActivityID, this@UICpu)
        Handler().postDelayed({ drawer.closeDrawer() }, 50)
    }

    private fun createWidget() {
        CoroutineScope(Dispatchers.Main).launch {
            val arch = HardwareCore.arch
            val cores = HardwareCore.cores
            cachedGovernor = governor

            CoroutineScope(Dispatchers.Main).launch {
                findViewById<TextView>(R.id.dashboard_cpu_content).text = "${getString(R.string.dashboard_widget_hardware_cpu)}: $arch\n" +
                        "${getString(R.string.dashboard_widget_hardware_cpu_cores)}: $cores\n" +
                        "${getString(R.string.dashboard_widget_hardware_title)}: ${Build.MANUFACTURER}"
                grabGovernorInfo()
            }
        }
    }

    private fun grabGovernorInfo() {
        findViewById<TextView>(R.id.TitleCPU1).text = cachedGovernor.capitalize()
        findViewById<TextView>(R.id.ContentCPU1).text = getString(CPUGovernorDocs.grab(cachedGovernor))
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
                val UI = UI(this@UICpu)
                UI.normal(getString(R.string.click_again_to_exit))

                Timer().schedule(1500){ doubleBackToExitPressedOnce = false }
            } else {
                super.onBackPressed()
            }
        }
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
            R.id.menu_settings -> {
                startActivity(Intent(this@UICpu, UISettings::class.java))
                return true
            }
            R.id.menu_dashboard -> {
                startActivity(Intent(this@UICpu, UIDashboard::class.java))
                return true
            }
            R.id.menu_about -> {
                startActivity(Intent(this@UICpu, UIAbout::class.java))
                return true
            }
            R.id.menu_drawer -> {
                drawer.openDrawer()
                return true
            }
            R.id.menu_backup -> {
                startActivity(Intent(this@UICpu, UIBackup::class.java))
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