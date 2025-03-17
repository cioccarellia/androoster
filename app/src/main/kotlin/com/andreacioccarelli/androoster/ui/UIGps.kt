package com.andreacioccarelli.androoster.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.v7.widget.AppCompatButton
import android.support.v7.widget.CardView
import android.support.v7.widget.SwitchCompat
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.ImageView
import com.afollestad.assent.Assent
import com.andreacioccarelli.androoster.tools.LicenseManager
import com.andreacioccarelli.androoster.R
import com.andreacioccarelli.androoster.core.Core
import com.andreacioccarelli.androoster.dataset.KeyStore
import com.andreacioccarelli.androoster.ui.settings.SettingStore
import com.andreacioccarelli.androoster.dataset.XmlKeys
import com.andreacioccarelli.androoster.tools.LaunchStruct
import com.andreacioccarelli.androoster.interfaces.Governors
import com.andreacioccarelli.androoster.tools.*
import com.andreacioccarelli.androoster.ui.about.UIAbout
import com.andreacioccarelli.androoster.ui.base.BaseActivity
import com.andreacioccarelli.androoster.ui.dashboard.RecentWidget
import com.andreacioccarelli.androoster.ui.dashboard.UIDashboard
import com.andreacioccarelli.androoster.tools.FabManager
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
import java.io.File
import java.util.*
import kotlin.concurrent.schedule

class UIGps : BaseActivity(), NavigationView.OnNavigationItemSelectedListener, Governors, LaunchStruct {

    internal var pro: Boolean = false

    lateinit var UI: UI
    lateinit var DRAWER_SETTINGS: PrimaryDrawerItem
    lateinit var drawer: Drawer
    var menu: Menu? = null

    internal var drawerInitialized = false

    internal var doubleBackToExitPressedOnce = false




    val fabTop: FloatingActionButton get() = findViewById(R.id.fabTop)
    val fabBottom: FloatingActionButton get() = findViewById(R.id.fabBottom)
    val toolbar_layout: CollapsingToolbarLayout get() = findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout)

    private val SwitchGPS2: SwitchCompat get() = findViewById(R.id.SwitchGPS2)

    private val CardGPS2: CardView get() = findViewById(R.id.CardGPS2)

    private val ButtonGPS1: AppCompatButton get() = findViewById(R.id.ButtonGPS1)
    private val ButtonGPS3: AppCompatButton get() = findViewById(R.id.ButtonGPS3)

    private val GPSBase: ImageView get() = findViewById(R.id.GPSBase)





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gps)
        pro = PreferencesBuilder(this, PreferencesBuilder.defaultFilename).getBoolean("pro", false)
        Assent.setActivity(this@UIGps, this@UIGps)

        val d: String? = packageManager.getInstallerPackageName(packageName)
        if (d != null) {
            if (CryptoFactory.sha256(d) == "D79B77BC4C48DE2746DE9F43CFB9209C4EA8D27D38B5AD9260FF3F8EA06D4252") {
                Toasty.error(baseContext, getString(R.string.app_toast))
                shutdownApp()
                return
            }
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        RecentWidget.collect(this@UIGps, LaunchStruct.GPS_ACTIVITY)

        UI = UI(this@UIGps)
        preferencesBuilder = PreferencesBuilder(this@UIGps, PreferencesBuilder.defaultFilename)

        preferencesBuilder.putInt(XmlKeys.LAST_OPENED, LaunchStruct.GPS_ACTIVITY)
        setUpDrawer(toolbar)
        animateContent(findViewById(R.id.content) as ViewGroup)

        FabManager.setup(fabTop, fabBottom, this@UIGps, drawer, preferencesBuilder)

        CardGPS2.setOnClickListener { SwitchGPS2.performClick() }

        CoroutineScope(Dispatchers.Main).launch {
            SwitchGPS2.isChecked = (Settings.Secure.getInt(contentResolver, Settings.Secure.ALLOW_MOCK_LOCATION, 0)) == 1
            preferencesBuilder.putBoolean("GPS2", SwitchGPS2.isChecked)
        }

        ButtonGPS1.setOnClickListener { _ ->
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                    startActivityForResult(Intent(Settings.ACTION_SETTINGS), 0)
                }
            }
        }

        SwitchGPS2.setOnClickListener { _ ->
            if (SwitchGPS2.isChecked) {
                preferencesBuilder.putBoolean("GPS2", true)
                Core.set_mock_location(true)
                UI.on()
            } else {
                preferencesBuilder.putBoolean("GPS2", false)
                Core.set_mock_location(false)
                UI.off()
            }
            preferencesBuilder.putBoolean("GPS2", SwitchGPS2.isChecked)
        }

        ButtonGPS3.setOnClickListener { _ ->
            try {
                val i = Intent()
                val gps = File("/system/etc/gps.conf")
                i.setDataAndType(Uri.fromFile(gps), "text/*")
                startActivity(i)
            } catch (r: RuntimeException) {
                r.printStackTrace()
                UI.error(getString(R.string.gps_config_error))
            }
        }

        SwitchGPS2.isChecked = preferencesBuilder.getBoolean("GPS2", false)

        val accentColor = ThemeStore.accentColor(this)
        val primaryColor = ThemeStore.primaryColor(this)
        val primaryDarkColor = ThemeStore.primaryColorDark(this)

        toolbar_layout.title = title
        toolbar_layout.setStatusBarScrimColor(primaryDarkColor)

        ATH.setActivityToolbarColor(this, toolbar, primaryColor)
        ATH.setBackgroundTint(toolbar_layout, primaryColor)
        ATH.setBackgroundTint(fabTop, accentColor)
        ATH.setBackgroundTint(fabBottom, accentColor)
        ATH.setBackgroundTint(ButtonGPS1, accentColor)
        ATH.setBackgroundTint(ButtonGPS3, accentColor)
        ATH.setTint(GPSBase, primaryColor)
        toolbar.setBackgroundColor(primaryColor)

        ATH.setTint(SwitchGPS2, ThemeStore.accentColor(this))
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

    private fun setUpDrawer(toolbar: Toolbar) {
        DrawerBuilder().withActivity(this@UIGps).build()

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
            LicenseManager.startProActivity(this@UIGps, this@UIGps, drawer)
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
        BaseActivity.setDrawerHeader(DrawerHeader.findViewById(R.id.Title), DrawerHeader.findViewById(R.id.Content), DrawerHeader.findViewById(R.id.Image), DrawerHeader.findViewById(R.id.RootLayout), this@UIGps, pro)


        if (pro) {
            drawer = DrawerBuilder()
                    .withActivity(this@UIGps)
                    .withToolbar(toolbar)
                    .addDrawerItems(
                            DRAWER_GPS,
                            DRAWER_DASHBOARD,
                            DividerDrawerItem(),
                            DRAWER_CPU,
                            DRAWER_RAM,
                            DRAWER_BATTERY,
                            DRAWER_KERNEL,
                            DRAWER_TWEAKS,
                            DRAWER_STORAGE,
                            DRAWER_INTERNET,
                            DRAWER_DEBUG,
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
                    .withActivity(this@UIGps)
                    .withToolbar(toolbar)
                    .addDrawerItems(
                            DRAWER_GPS,
                            DRAWER_DASHBOARD,
                            DividerDrawerItem(),
                            DRAWER_CPU,
                            DRAWER_RAM,
                            DRAWER_BATTERY,
                            DRAWER_KERNEL,
                            DRAWER_TWEAKS,
                            DRAWER_STORAGE,
                            DRAWER_INTERNET,
                            DRAWER_DEBUG,
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
        LaunchManager.startActivity(ActivityID, this@UIGps)
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen) {
            closeApp()
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed()
                return
            }
            if (preferencesBuilder.getPreferenceBoolean(SettingStore.GENERAL.PRESS_TWICE_TO_EXIT, false)) {
                this.doubleBackToExitPressedOnce = true
                val UI = UI(this@UIGps)
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
                startActivity(Intent(this@UIGps, UIAbout::class.java))
                return true
            }
            R.id.menu_dashboard -> {
                startActivity(Intent(this@UIGps, UIDashboard::class.java))
                return true
            }
            R.id.menu_settings -> {
                startActivity(Intent(this@UIGps, UISettings::class.java))
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Lets Assent handle permission results, and contact your callbacks
        Assent.handleResult(permissions, grantResults)
    }
}
