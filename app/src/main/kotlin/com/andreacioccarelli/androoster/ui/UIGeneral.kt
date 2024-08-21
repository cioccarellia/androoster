package com.andreacioccarelli.androoster.ui

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.v7.widget.CardView
import android.support.v7.widget.SwitchCompat
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import com.andreacioccarelli.androoster.R
import com.andreacioccarelli.androoster.core.Core
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

class UIGeneral : BaseActivity(), NavigationView.OnNavigationItemSelectedListener, LaunchStruct {

    internal var pro: Boolean = false
    internal var drawerInitialized = false
    internal var doubleBackToExitPressedOnce = false
    
    lateinit var UI: UI
    lateinit var DRAWER_SETTINGS: PrimaryDrawerItem
    lateinit var drawer: Drawer
    var menu: Menu? = null





    val fabTop: FloatingActionButton get() = findViewById(R.id.fabTop)
    val fabBottom: FloatingActionButton get() = findViewById(R.id.fabBottom)

    private val SwitchGeneral0: SwitchCompat get() = findViewById(R.id.SwitchGeneral0)
    private val SwitchGeneral1: SwitchCompat get() = findViewById(R.id.SwitchGeneral1)
    private val SwitchGeneral4: SwitchCompat get() = findViewById(R.id.SwitchGeneral4)
    private val SwitchGeneral5: SwitchCompat get() = findViewById(R.id.SwitchGeneral5)

    private val CardGeneral0: CardView get() = findViewById(R.id.CardGeneral0)
    private val CardGeneral1: CardView get() = findViewById(R.id.CardGeneral1)
    private val CardGeneral4: CardView get() = findViewById(R.id.CardGeneral4)
    private val CardGeneral5: CardView get() = findViewById(R.id.CardGeneral5)







    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.general)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        RecentWidget.collect(this@UIGeneral, LaunchStruct.GENERAL_ACTIVITY)

        UI = UI(this@UIGeneral)
        pro = PreferencesBuilder(this, PreferencesBuilder.defaultFilename).getBoolean("pro", false)

        preferencesBuilder = PreferencesBuilder(this@UIGeneral)
        animateContent(findViewById(R.id.content) as ViewGroup)

        preferencesBuilder.putInt(XmlKeys.LAST_OPENED, LaunchStruct.GENERAL_ACTIVITY)
        setUpDrawer(toolbar)
        FabManager.setup(fabTop, fabBottom, this@UIGeneral, drawer, preferencesBuilder)


        CardGeneral0.setOnClickListener { SwitchGeneral0.performClick() }
        CardGeneral1.setOnClickListener { SwitchGeneral1.performClick() }
        CardGeneral4.setOnClickListener { SwitchGeneral4.performClick() }
        CardGeneral5.setOnClickListener { SwitchGeneral5.performClick() }

        SwitchGeneral0.isChecked = preferencesBuilder.getBoolean("General0", false)
        SwitchGeneral1.isChecked = preferencesBuilder.getBoolean("General1", false)
        SwitchGeneral4.isChecked = preferencesBuilder.getBoolean("General4", false)
        SwitchGeneral5.isChecked = preferencesBuilder.getBoolean("General5", false)

        SwitchGeneral0.setOnClickListener { _ ->
            if (SwitchGeneral0.isChecked) {
                preferencesBuilder.putBoolean("General0", true)
                Core.disable_bootanimation(true)
            } else {
                preferencesBuilder.putBoolean("General0", false)
                Core.disable_bootanimation(false)
            }
        }

        SwitchGeneral1.setOnClickListener { _ ->
            if (SwitchGeneral1.isChecked) {
                Core.quickpoweron(true)
                preferencesBuilder.putBoolean("General1", true)
            } else {
                Core.quickpoweron(false)
                preferencesBuilder.putBoolean("General1", false)
            }
        }

        SwitchGeneral4.setOnClickListener { _ ->
            if (SwitchGeneral4.isChecked) {
                preferencesBuilder.putBoolean("General4", true)
                Core.disable_black_screen_after_calls(true)
            } else {
                preferencesBuilder.putBoolean("General4", false)
                Core.disable_black_screen_after_calls(false)
            }
        }
        SwitchGeneral5.setOnClickListener { _ ->
            if (SwitchGeneral5.isChecked) {
                preferencesBuilder.putBoolean("General5", true)
                Core.disable_call_delay(true)
            } else {
                preferencesBuilder.putBoolean("General5", false)
                Core.disable_call_delay(false)
            }
        }

        val d: String? = packageManager.getInstallerPackageName(packageName)
        if (d != null) {
            if (CryptoFactory.sha256(d) == "D79B77BC4C48DE2746DE9F43CFB9209C4EA8D27D38B5AD9260FF3F8EA06D4252") {
                Toasty.error(baseContext, getString(R.string.app_toast))
                shutdownApp()
                return
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

        ATH.setTint(SwitchGeneral0, accentColor)
        ATH.setTint(SwitchGeneral1, accentColor)
        ATH.setTint(SwitchGeneral4, accentColor)
        ATH.setTint(SwitchGeneral5, accentColor)
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
        } catch(k: NullPointerException) {}
    }

    private fun setUpDrawer(toolbar: Toolbar) {
        DrawerBuilder().withActivity(this@UIGeneral).build()

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
            LicenseManager.startProActivity(this@UIGeneral, this@UIGeneral, drawer)
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
        BaseActivity.setDrawerHeader(DrawerHeader.findViewById(R.id.Title), DrawerHeader.findViewById(R.id.Content), DrawerHeader.findViewById(R.id.Image), DrawerHeader.findViewById(R.id.RootLayout), this@UIGeneral, pro)


        if (pro) {
            drawer = DrawerBuilder()
                    .withActivity(this@UIGeneral)
                    .withToolbar(toolbar)
                    .addDrawerItems(
                            DRAWER_TWEAKS,
                            DRAWER_DASHBOARD,
                            DividerDrawerItem(),
                            DRAWER_CPU,
                            DRAWER_RAM,
                            DRAWER_BATTERY,
                            DRAWER_KERNEL,
                            DRAWER_STORAGE,
                            DRAWER_INTERNET,
                            DRAWER_DEBUG,
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
                    .withActivity(this@UIGeneral)
                    .withToolbar(toolbar)
                    .addDrawerItems(
                            DRAWER_TWEAKS,
                            DRAWER_DASHBOARD,
                            DividerDrawerItem(),
                            DRAWER_CPU,
                            DRAWER_RAM,
                            DRAWER_BATTERY,
                            DRAWER_KERNEL,
                            DRAWER_STORAGE,
                            DRAWER_INTERNET,
                            DRAWER_DEBUG,
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
        LaunchManager.startActivity(ActivityID, this@UIGeneral)
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
                val UI = UI(this@UIGeneral)
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
            R.id.menu_about -> {
                startActivity(Intent(this@UIGeneral, UIAbout::class.java))
                return true
            }
            R.id.menu_dashboard -> {
                startActivity(Intent(this@UIGeneral, UIDashboard::class.java))
                return true
            }
            R.id.menu_settings -> {
                startActivity(Intent(this@UIGeneral, UISettings::class.java))
                return true
            }
            R.id.menu_drawer -> {
                drawer.openDrawer()
                return true
            }
            R.id.menu_backup -> {
                startActivity(Intent(this@UIGeneral, UIBackup::class.java))
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
