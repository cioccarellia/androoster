package com.andreacioccarelli.androoster.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.v7.widget.CardView
import android.support.v7.widget.SwitchCompat
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.TextView
import com.andreacioccarelli.androoster.R
import com.andreacioccarelli.androoster.core.Core
import com.andreacioccarelli.androoster.core.CoreBase
import com.andreacioccarelli.androoster.core.FrameworkSurface
import com.andreacioccarelli.androoster.dataset.KeyStore
import com.andreacioccarelli.androoster.dataset.XmlKeys
import com.andreacioccarelli.androoster.interfaces.Governors
import com.andreacioccarelli.androoster.tools.*
import com.andreacioccarelli.androoster.ui.about.UIAbout
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

class UIDebug : BaseActivity(), NavigationView.OnNavigationItemSelectedListener, Governors, LaunchStruct {

    internal var pro: Boolean = false
    private var adbEnabled: Boolean = false
    private var drawerInitialized = false

    lateinit var UI: UI
    lateinit var DRAWER_SETTINGS: PrimaryDrawerItem
    lateinit var drawer: Drawer
    var menu: Menu? = null

    private var tags: String = ""
    private var isOfficial: Boolean = true
    private var adbString: String = ""

    internal var doubleBackToExitPressedOnce = false

    val fabTop: FloatingActionButton
        get() = findViewById(R.id.fabTop)
    val fabBottom: FloatingActionButton
        get() = findViewById(R.id.fabBottom)

    val CardDebug1: CardView
        get() = findViewById(R.id.CardDebug1)
    val CardDebug2: CardView
        get() = findViewById(R.id.CardDebug2)
    val CardDebug3: CardView
        get() = findViewById(R.id.CardDebug3)
    val CardDebug4: CardView
        get() = findViewById(R.id.CardDebug4)
    val CardDebug5: CardView
        get() = findViewById(R.id.CardDebug5)
    val CardDebug6: CardView
        get() = findViewById(R.id.CardDebug6)


    val SwitchDebug1: SwitchCompat
        get() = findViewById(R.id.SwitchDebug1)
    val SwitchDebug2: SwitchCompat
        get() = findViewById(R.id.SwitchDebug2)
    val SwitchDebug3: SwitchCompat
        get() = findViewById(R.id.SwitchDebug3)
    val SwitchDebug4: SwitchCompat
        get() = findViewById(R.id.SwitchDebug4)
    val SwitchDebug5: SwitchCompat
        get() = findViewById(R.id.SwitchDebug5)
    val SwitchDebug6: SwitchCompat
        get() = findViewById(R.id.SwitchDebug6)

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.debug)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        RecentWidget.collect(this@UIDebug, LaunchStruct.DEBUG_ACTIVITY)

        UI = UI(this@UIDebug)
        pro = PreferencesBuilder(this, PreferencesBuilder.defaultFilename).getBoolean("pro", false)

        preferencesBuilder = PreferencesBuilder(this@UIDebug, PreferencesBuilder.defaultFilename)
        preferencesBuilder.putInt(XmlKeys.LAST_OPENED, LaunchStruct.DEBUG_ACTIVITY)

        createWidget()
        animateContent(findViewById(R.id.content) as ViewGroup)

        setUpDrawer(toolbar)
        FabManager.setup(fabTop, fabBottom, this@UIDebug, drawer, preferencesBuilder)



        CardDebug1.setOnClickListener { SwitchDebug1.performClick() }
        CardDebug2.setOnClickListener { SwitchDebug2.performClick() }
        CardDebug3.setOnClickListener { SwitchDebug3.performClick() }
        CardDebug4.setOnClickListener { SwitchDebug4.performClick() }
        CardDebug5.setOnClickListener { SwitchDebug5.performClick() }
        CardDebug6.setOnClickListener { SwitchDebug6.performClick() }

        SwitchDebug1.isChecked = preferencesBuilder.getBoolean("Debug1", adbEnabled)
        SwitchDebug2.isChecked = preferencesBuilder.getBoolean("Debug2", false)
        SwitchDebug3.isChecked = preferencesBuilder.getBoolean("Debug3", false)
        SwitchDebug4.isChecked = preferencesBuilder.getBoolean("Debug4", false)
        SwitchDebug5.isChecked = preferencesBuilder.getBoolean("Debug5", false)
        SwitchDebug6.isChecked = preferencesBuilder.getBoolean("Debug6", false)

        SwitchDebug1.setOnClickListener { _ ->
            adbEnabled = SwitchDebug1.isChecked

            if (adbEnabled) {
                CoreBase.SETTINGS.put(FrameworkSurface.GLOBAL, "adb_enabled", 1)
            } else {
                CoreBase.SETTINGS.put(FrameworkSurface.GLOBAL, "adb_enabled", 0)
            }

            preferencesBuilder.putBoolean("Debug1", adbEnabled)
            findViewById<TextView>(R.id.dashboard_debug_content).text =
                    "ADB: ${if (adbEnabled) getString(R.string.state_enabled) else getString(R.string.state_disabled)}\n" +
                    "Tags: $tags\n"+
                    getString(R.string.debug_widget_official) + " $isOfficial"

        }
        SwitchDebug2.setOnClickListener { _ ->
            if (SwitchDebug2.isChecked) {
                preferencesBuilder.putBoolean("Debug2", true)
                Core.set_adb_notification(true)
            } else {
                preferencesBuilder.putBoolean("Debug2", false)
                Core.set_adb_notification(false)
            }

        }
        SwitchDebug3.setOnClickListener { _ ->
            if (SwitchDebug3.isChecked) {
                preferencesBuilder.putBoolean("Debug3", true)
                Core.disable_error_logging(true)
            } else {
                preferencesBuilder.putBoolean("Debug3", false)
                Core.disable_error_logging(false)
            }
        }
        SwitchDebug4.setOnClickListener { _ ->
            if (SwitchDebug4.isChecked) {
                preferencesBuilder.putBoolean("Debug4", true)
                Core.disable_error_sent(true)
            } else {
                preferencesBuilder.putBoolean("Debug4", false)
                Core.disable_error_sent(false)
            }

        }
        SwitchDebug5.setOnClickListener { _ ->
            if (SwitchDebug5.isChecked) {
                preferencesBuilder.putBoolean("Debug5", true)
                Core.disable_anr_history(true)
            } else {
                preferencesBuilder.putBoolean("Debug5", false)
                Core.disable_anr_history(false)
            }

        }
        SwitchDebug6.setOnClickListener { _ ->
            if (SwitchDebug6.isChecked) {
                preferencesBuilder.putBoolean("Debug6", true)
                Core.set_debug_optimization(true)
            } else {
                preferencesBuilder.putBoolean("Debug6", false)
                Core.set_debug_optimization(false)
            }
        }

        animateContent(findViewById(R.id.content) as ViewGroup)

        val accentColor = ThemeStore.accentColor(this)
        val primaryColor = ThemeStore.primaryColor(this)
        val primaryDarkColor = ThemeStore.primaryColorDark(this)

        findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout).title = title
        findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout).setStatusBarScrimColor(primaryDarkColor)

        ATH.setActivityToolbarColor(this, toolbar, primaryColor)
        ATH.setBackgroundTint(findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout), primaryColor)
        ATH.setBackgroundTint(fabTop, accentColor)
        ATH.setBackgroundTint(fabBottom, accentColor)
        toolbar.setBackgroundColor(primaryColor)

        ATH.setTint(SwitchDebug1, accentColor)
        ATH.setTint(SwitchDebug2, accentColor)
        ATH.setTint(SwitchDebug3, accentColor)
        ATH.setTint(SwitchDebug4, accentColor)
        ATH.setTint(SwitchDebug5, accentColor)
        ATH.setTint(SwitchDebug6, accentColor)
        ATH.setTint(findViewById(R.id.DebugBase), primaryColor)
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
            } else {
                drawer.removeItem(19)
            }
        }
        try {
            SettingsReflector.updateMenu(menu!!, preferencesBuilder)
        } catch (k: NullPointerException) {}
    }

    @SuppressLint("SetTextI18n")
    private fun createWidget() {
        CoroutineScope(Dispatchers.Main).launch {
            tags = Build.TAGS
            isOfficial = tags.contains("release-keys")

            adbEnabled = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                Settings.Secure.getInt(contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
            } else {
                Settings.Secure.getInt(contentResolver, Settings.Secure.ADB_ENABLED, 0) == 1
            }
            adbString = if (adbEnabled) getString(R.string.state_enabled) else getString(R.string.state_disabled)

            CoroutineScope(Dispatchers.Main).launch {
                SwitchDebug1.isChecked = adbEnabled
                findViewById<TextView>(R.id.dashboard_debug_content).text =
                        "${getString(R.string.debug_widget_adb)}: $adbString\n" +
                        "${getString(R.string.debug_widget_tags)}: $tags\n" +
                        "${getString(R.string.debug_widget_official)}: $isOfficial"
            }
        }
    }

    private fun setUpDrawer(toolbar: Toolbar) {
        DrawerBuilder().withActivity(this@UIDebug).build()

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
        val DRAWER_INTERNET = PrimaryDrawerItem().withIdentifier(8).withName(R.string.drawer_net).withOnDrawerItemClickListener { _, _, _ ->
            handleIntent(LaunchStruct.INTERNET_ACTIVITY)
            false
        }
        val DRAWER_DEBUG = PrimaryDrawerItem().withIdentifier(9).withName(R.string.drawer_debug).withOnDrawerItemClickListener { _, _, _ ->
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
            LicenseManager.startProActivity(this@UIDebug, this@UIDebug, drawer)
            false
        }

        val d: String? = packageManager.getInstallerPackageName(packageName)
        if (d != null) {
            if (d == "com.android.packageinstaller") {
                Toasty.error(baseContext, getString(R.string.app_toast))
                shutdownApp()
                return
            }
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
            DRAWER_ABOUT.withIcon(R.drawable.drawer_white_about)
        }


        val factory = layoutInflater
        val DrawerHeader = factory.inflate(R.layout.drawer_header, null)
        BaseActivity.setDrawerHeader(DrawerHeader.findViewById(R.id.Title), DrawerHeader.findViewById(R.id.Content), DrawerHeader.findViewById(R.id.Image), DrawerHeader.findViewById(R.id.RootLayout), this@UIDebug, pro)


        if (pro) {
            drawer = DrawerBuilder()
                    .withActivity(this@UIDebug)
                    .withToolbar(toolbar)
                    .addDrawerItems(
                            DRAWER_DEBUG,
                            DRAWER_DASHBOARD,
                            DividerDrawerItem(),
                            DRAWER_CPU,
                            DRAWER_RAM,
                            DRAWER_BATTERY,
                            DRAWER_KERNEL,
                            DRAWER_TWEAKS,
                            DRAWER_STORAGE,
                            DRAWER_INTERNET,
                            DRAWER_GPS,
                            DRAWER_HARDWARE,
                            DRAWER_GRAPHICS,
                            DividerDrawerItem(),
                            DRAWER_ABOUT,
                            DRAWER_SETTINGS
                    )
                    .withHeader(DrawerHeader)
                    .build()
        } else {
            drawer = DrawerBuilder()
                    .withActivity(this@UIDebug)
                    .withToolbar(toolbar)
                    .addDrawerItems(
                            DRAWER_DEBUG,
                            DRAWER_DASHBOARD,
                            DividerDrawerItem(),
                            DRAWER_CPU,
                            DRAWER_RAM,
                            DRAWER_BATTERY,
                            DRAWER_KERNEL,
                            DRAWER_TWEAKS,
                            DRAWER_STORAGE,
                            DRAWER_INTERNET,
                            DRAWER_GPS,
                            DRAWER_HARDWARE,
                            DRAWER_GRAPHICS,
                            DividerDrawerItem(),
                            DRAWER_ABOUT,
                            DRAWER_SETTINGS
                    )
                    .addStickyDrawerItems(DRAWER_BUY_PRO_VERSION)
                    .withHeader(DrawerHeader)
                    .build()
        }

        drawerInitialized = true
    }

    internal fun handleIntent(ActivityID: Int) {
        LaunchManager.startActivity(ActivityID, this@UIDebug)
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
                val UI = UI(this@UIDebug)
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
        menu.getItem(4).isVisible = preferencesBuilder.getPreferenceBoolean(SettingStore.MENU.REBOOT, false)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when (id) {
            R.id.menu_about -> {
                startActivity(Intent(this@UIDebug, UIAbout::class.java))
                return true
            }
            R.id.menu_dashboard -> {
                startActivity(Intent(this@UIDebug, UIDashboard::class.java))
                return true
            }
            R.id.menu_settings -> {
                startActivity(Intent(this@UIDebug, UISettings::class.java))
                return true
            }
            R.id.menu_drawer -> {
                drawer.openDrawer()
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
