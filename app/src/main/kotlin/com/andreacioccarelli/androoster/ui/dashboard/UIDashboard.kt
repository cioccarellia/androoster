@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

package com.andreacioccarelli.androoster.ui.dashboard

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v7.widget.CardView
import android.support.v7.widget.Toolbar
import android.view.*
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.andreacioccarelli.androoster.R
import com.andreacioccarelli.androoster.core.HardwareCore
import com.andreacioccarelli.androoster.core.TerminalCore
import com.andreacioccarelli.androoster.dataset.XmlKeys
import com.andreacioccarelli.androoster.tools.*
import com.andreacioccarelli.androoster.ui.about.UIAbout
import com.andreacioccarelli.androoster.ui.backup.UIBackup
import com.andreacioccarelli.androoster.ui.base.BaseActivity
import com.andreacioccarelli.androoster.ui.settings.SettingStore
import com.andreacioccarelli.androoster.ui.settings.SettingsReflector
import com.andreacioccarelli.androoster.ui.settings.UISettings
import com.andreacioccarelli.androoster.ui.upgrade.UIUpgrade
import com.jaredrummler.android.device.DeviceName
import com.kabouzeid.appthemehelper.ATH
import com.kabouzeid.appthemehelper.ThemeStore
import com.kabouzeid.appthemehelper.util.ATHUtil
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import es.dmoral.toasty.Toasty
import java.util.*

class UIDashboard : BaseActivity(), NavigationView.OnNavigationItemSelectedListener, LaunchStruct {

    private var networkAvailable = true
    private lateinit var DRAWER_SETTINGS: PrimaryDrawerItem

    private var drawerInitialized = false
    lateinit var drawer: Drawer
    internal var doubleBackToExitPressedOnce = false
    var menu: Menu? = null


    val fabTop: FloatingActionButton get() = findViewById(R.id.fabTop)
    val fabBottom: FloatingActionButton get() = findViewById(R.id.fabBottom)

    @SuppressLint("HardwareIds", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard)
        setSupportActionBar(findViewById(R.id.toolbar))
        animateContent(findViewById(R.id.content) as ViewGroup)

        val prefs = PreferencesBuilder(baseContext, PreferencesBuilder.defaultFilename)
        RecentWidget.init(this@UIDashboard)

        setUpDrawer(findViewById(R.id.toolbar))

        preferencesBuilder = PreferencesBuilder(baseContext)
        preferencesBuilder.putInt(XmlKeys.LAST_OPENED, LaunchStruct.DASHBOARD_ACTIVITY)

        if (prefs.getBoolean("pro", false)) {
            title = getString(R.string.app_name_pro)
        } else {
            title = getString(R.string.app_name)
        }

        findViewById<ImageView>(R.id.softwareDetailsIcon).setColorFilter(
            ATHUtil.resolveColor(
                this@UIDashboard,
                R.attr.iconColor
            ), PorterDuff.Mode.SRC_IN
        )
        findViewById<ImageView>(R.id.hardwareDetailsIcon).setColorFilter(
            ATHUtil.resolveColor(
                this@UIDashboard,
                R.attr.iconColor
            ), PorterDuff.Mode.SRC_IN
        )

        val playServiceState = try {
            "Play Services: ${
                packageManager.getPackageInfo(
                    "com.google.android.gms",
                    0
                )?.versionName
            }\n"
        } catch (r: Exception) {
            ""
        }

        findViewById<TextView>(R.id.softwareDetails).text =
            "${getString(R.string.dashboard_widget_software_android_version)}: ${Build.VERSION.RELEASE}\n" +
                    "${getString(R.string.dashboard_widget_software_bootloader)}: ${Build.BOOTLOADER}\n" +
                    "${getString(R.string.dashboard_widget_software_fingerprint)}: ${Build.FINGERPRINT}\n" +
                    "${getString(R.string.dashboard_widget_software_root)}: ${
                        preferencesBuilder.getString(
                            "rootManagerDetails",
                            getString(R.string.dashboard_widget_software_root_installed)
                        )
                    }\n" +
                    "${getString(R.string.dashboard_widget_software_build)}: ${Build.ID}\n" +
                    "${getString(R.string.dashboard_widget_software_android_id)}: ${
                        HardwareCore.getAndroidId(
                            baseContext
                        )
                    }\n" +
                    playServiceState +
                    "${getString(R.string.dashboard_widget_software_kernel)}: ${
                        preferencesBuilder.getString(
                            "kernelDetails",
                            "Linux"
                        )
                    }"


        findViewById<TextView>(R.id.hardwareDetails).text = "RAM: ${HardwareCore.ram}\n" +
                "${getString(R.string.dashboard_widget_hardware_cpu)}: " +
                "${HardwareCore.cores} ${getString(R.string.dashboard_widget_hardware_cpu_cores)}\n" +
                "${getString(R.string.dashboard_widget_hardware_battery)}: ${
                    HardwareCore.getBatteryCapacity(
                        this@UIDashboard
                    ).replace(".0", "")
                }\n" +
                "${getString(R.string.dashboard_widget_hardware_serial)}: ${
                    getDeviceSerial(
                        baseContext
                    )
                }\n" +
                "${getString(R.string.dashboard_widget_hardware_brand)}: ${Build.MANUFACTURER}\n" +
                "${getString(R.string.ram_widget_check)}: ${Build.HARDWARE}\n" +
                "${getString(R.string.dashboard_widget_hardware_device)}: ${DeviceName.getDeviceName()}\n" +
                getString(R.string.graphic_widget_resolution) + " ${resources.displayMetrics.heightPixels}${'x'}${resources.displayMetrics.widthPixels}\n" +
                getString(R.string.graphic_widget_density) + " ${(resources.displayMetrics.density * 160f).toInt()}dpi"


        val accentColor = ThemeStore.accentColor(this)
        val primaryColor = ThemeStore.primaryColor(this)
        val primaryDarkColor = ThemeStore.primaryColorDark(this)

        FabManager.setup(fabTop, fabBottom, this@UIDashboard, drawer, preferencesBuilder)

        val collapsingToolbar = findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout)
        collapsingToolbar.title = title
        collapsingToolbar.setStatusBarScrimColor(primaryDarkColor)

        ATH.setActivityToolbarColor(this, findViewById(R.id.toolbar), primaryColor)
        ATH.setBackgroundTint(collapsingToolbar, primaryColor)
        ATH.setBackgroundTint(fabTop, accentColor)
        ATH.setBackgroundTint(fabBottom, accentColor)
        ATH.setTint(findViewById(R.id.DashboardBase), primaryColor)
        findViewById<Toolbar>(R.id.toolbar).setBackgroundColor(primaryColor)

        // Free pack
        if (!prefs.getBoolean("notified_pro_version", false) && !prefs.getBoolean("pro", false)) {
            Toasty.success(this, "Pro version available!", Toast.LENGTH_LONG).show()

            MaterialDialog.Builder(this)
                .title("Androoster Pro")
                .content(
                    """Androoster has just become free. 
                        |You can enable pro version by visiting the upgrade page, and clicking the upgrade button, no purchase is required anymore. 
                        |This software has been around for a long time, and I felt it was time to make it available for everyone.\n
                        |Thank you very much to everybody who supported my work through purchases, and I wish you all happy tweaking and exploring!""".trimMargin()
                )
                .negativeText("GOT IT")
                .positiveText("OPEN UPGRADE")
                .positiveColorRes(R.color.Green_500)
                .onNegative() { dialog, which ->
                    dialog.dismiss()
                    prefs.putBoolean("notified_pro_version", true)
                }
                .onPositive() { dialog, which ->
                    prefs.putBoolean("notified_pro_version", true)
                    startActivity(Intent(this, UIUpgrade::class.java))
                }
                .autoDismiss(false)
                .cancelable(false)
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        FabManager.onResume(fabTop, fabBottom, preferencesBuilder)
        if (PreferencesBuilder(this, PreferencesBuilder.defaultFilename).getBoolean(
                "pro",
                false
            ) && drawerInitialized
        ) {
            if (preferencesBuilder.getPreferenceBoolean(
                    SettingStore.GENERAL.STICKY_SETTINGS,
                    false
                )
            ) {
                drawer.removeAllStickyFooterItems()
                drawer.removeItem(20)
                drawer.addStickyFooterItem(DRAWER_SETTINGS)
            } else {
                drawer.removeAllStickyFooterItems()
                drawer.removeItem(20)
                drawer.addItem(DRAWER_SETTINGS)
            }
            if (preferencesBuilder.getPreferenceBoolean(SettingStore.GENERAL.SHOW_BACKUP, true)) {
                drawer.removeItem(19)
            } else {
                drawer.removeItem(19)
            }
        }
        try {
            SettingsReflector.updateDashboardMenu(menu!!, preferencesBuilder)
        } catch (_: NullPointerException) {
        }

        val dispatcher = RecentWidgetProvider(this@UIDashboard)

        val first = RecentWidget.getFirst(this@UIDashboard)
        val second = RecentWidget.getSecond(this@UIDashboard)
        val third = RecentWidget.getThird(this@UIDashboard)

        if (first == LaunchStruct.NULL || second == LaunchStruct.NULL || third == LaunchStruct.NULL) {
            findViewById<CardView>(R.id.recentWidget).visibility = View.GONE
        } else {
            val locale = Locale.getDefault()

            try {
                findViewById<ImageView>(R.id.recentIcon1).apply {
                    setImageResource(dispatcher.getIcon(first))
                    setColorFilter(ThemeStore.accentColor(this@UIDashboard))
                }

                findViewById<RelativeLayout>(R.id.recentLayout1).setOnClickListener { _ ->
                    startActivity(
                        dispatcher.getIntent(first)
                    )
                }
                findViewById<TextView>(R.id.recentText1).text =
                    getString(dispatcher.getTitleRes(first)).toUpperCase(locale)


                findViewById<ImageView>(R.id.recentIcon2).apply {
                    setImageResource(dispatcher.getIcon(second))
                    setColorFilter(ThemeStore.accentColor(this@UIDashboard))
                }

                findViewById<RelativeLayout>(R.id.recentLayout2).setOnClickListener { _ ->
                    startActivity(
                        dispatcher.getIntent(second)
                    )
                }
                findViewById<TextView>(R.id.recentText2).text =
                    getString(dispatcher.getTitleRes(second)).toUpperCase(locale)


                findViewById<ImageView>(R.id.recentIcon3).apply {
                    setImageResource(dispatcher.getIcon(third))
                    setColorFilter(ThemeStore.accentColor(this@UIDashboard))
                }

                findViewById<RelativeLayout>(R.id.recentLayout3).setOnClickListener { _ -> startActivity(dispatcher.getIntent(third)) }
                findViewById<TextView>(R.id.recentText3).text = getString(dispatcher.getTitleRes(third)).toUpperCase(locale)
            } catch (rnf: IllegalStateException) {
                findViewById<CardView>(R.id.recentWidget).visibility = View.GONE
            }
        }
    }

    override fun onKeyLongPress(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return if (drawer.isDrawerOpen) {
                drawer.closeDrawer()
                FabManager.animate(fabTop, this@UIDashboard, true)
                FabManager.animate(fabBottom, this@UIDashboard, true)
                true
            } else {
                drawer.openDrawer()
                FabManager.animate(fabTop, this@UIDashboard, true)
                FabManager.animate(fabBottom, this@UIDashboard, true)
                true
            }
        }
        return super.onKeyLongPress(keyCode, event)
    }

    private fun setUpDrawer(toolbar: Toolbar) {
        val DRAWER_DASHBOARD =
            PrimaryDrawerItem().withIdentifier(1).withName(R.string.drawer_dashboard)
                .withIcon(R.drawable.dashboard)
        val DRAWER_CPU = PrimaryDrawerItem().withIdentifier(2).withName(R.string.drawer_cpu)
            .withOnDrawerItemClickListener { _, _, _ ->
                handleIntent(LaunchStruct.CPU_ACTIVITY)
                false
            }
        val DRAWER_RAM = PrimaryDrawerItem().withIdentifier(3).withName(R.string.drawer_ram)
            .withOnDrawerItemClickListener { _, _, _ ->
                handleIntent(LaunchStruct.RAM_ACTIVITY)
                false
            }
        val DRAWER_BATTERY = PrimaryDrawerItem().withIdentifier(4).withName(R.string.drawer_battery)
            .withOnDrawerItemClickListener { _, _, _ ->
                handleIntent(LaunchStruct.BATTERY_ACTIVITY)
                false
            }
        val DRAWER_KERNEL = PrimaryDrawerItem().withIdentifier(5).withName(R.string.drawer_kernel)
            .withOnDrawerItemClickListener { _, _, _ ->
                handleIntent(LaunchStruct.KERNEL_ACTIVITY)
                false
            }
        val DRAWER_TWEAKS = PrimaryDrawerItem().withIdentifier(6).withName(R.string.drawer_tweaks)
            .withOnDrawerItemClickListener { _, _, _ ->
                handleIntent(LaunchStruct.GENERAL_ACTIVITY)
                false
            }
        val DRAWER_STORAGE = PrimaryDrawerItem().withIdentifier(7).withName(R.string.drawer_storage)
            .withOnDrawerItemClickListener { _, _, _ ->
                handleIntent(LaunchStruct.STORAGE_ACTIVITY)
                false
            }
        val DRAWER_INTERNET = PrimaryDrawerItem().withIdentifier(8).withName(R.string.drawer_net)
            .withOnDrawerItemClickListener { _, _, _ ->
                handleIntent(LaunchStruct.INTERNET_ACTIVITY)
                false
            }
        val DRAWER_DEBUG = PrimaryDrawerItem().withIdentifier(9).withName(R.string.drawer_debug)
            .withOnDrawerItemClickListener { _, _, _ ->
                handleIntent(LaunchStruct.DEBUG_ACTIVITY)
                false
            }
        val DRAWER_GPS = PrimaryDrawerItem().withIdentifier(11).withName(R.string.drawer_gps)
            .withOnDrawerItemClickListener { _, _, _ ->
                handleIntent(LaunchStruct.GPS_ACTIVITY)
                false
            }
        val DRAWER_HARDWARE =
            PrimaryDrawerItem().withIdentifier(12).withName(R.string.drawer_hardware)
                .withOnDrawerItemClickListener { _, _, _ ->
                    handleIntent(LaunchStruct.HARDWARE_ACTIVITY)
                    false
                }
        val DRAWER_GRAPHICS =
            PrimaryDrawerItem().withIdentifier(13).withName(R.string.drawer_graphics)
                .withOnDrawerItemClickListener { _, _, _ ->
                    handleIntent(LaunchStruct.GRAPHICS_ACTIVITY)
                    false
                }
        val DRAWER_ABOUT = PrimaryDrawerItem().withIdentifier(14).withName(R.string.drawer_about)
            .withOnDrawerItemClickListener { _, _, _ ->
                handleIntent(LaunchStruct.ABOUT_ACTIVITY)
                false
            }
        val DRAWER_BUY_PRO_VERSION =
            PrimaryDrawerItem().withIdentifier(15).withName(R.string.drawer_pro)
                .withOnDrawerItemClickListener { _, _, _ ->
                    LicenseManager.startProActivity(this@UIDashboard, this@UIDashboard, drawer)
                    false
                }

        DRAWER_SETTINGS = PrimaryDrawerItem().withIdentifier(20).withName(R.string.drawer_settings)
            .withOnDrawerItemClickListener { _, _, _ ->
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
        BaseActivity.setDrawerHeader(
            DrawerHeader.findViewById(R.id.Title),
            DrawerHeader.findViewById(R.id.Content),
            DrawerHeader.findViewById(R.id.Image),
            DrawerHeader.findViewById(R.id.RootLayout),
            this@UIDashboard,
            PreferencesBuilder(this, PreferencesBuilder.defaultFilename).getBoolean("pro", false)
        )

        if (PreferencesBuilder(this, PreferencesBuilder.defaultFilename).getBoolean("pro", false)) {
            drawer = DrawerBuilder()
                .withActivity(this@UIDashboard)
                .withToolbar(toolbar)
                .addDrawerItems(
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
                .withActivity(this@UIDashboard)
                .withToolbar(toolbar)
                .addDrawerItems(
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

    fun handleIntent(ActivityID: Int) {
        LaunchManager.startActivity(ActivityID, this@UIDashboard)
    }

   
    override fun onBackPressed() {
        if (drawer.isDrawerOpen) {
            drawer.closeDrawer()
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed()
                return
            }
            if (preferencesBuilder.getPreferenceBoolean(SettingStore.GENERAL.PRESS_TWICE_TO_EXIT, false)) {
                this.doubleBackToExitPressedOnce = true
                val UI = UI(this@UIDashboard)
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

        if (preferencesBuilder.getPreferenceBoolean(
                SettingStore.GENERAL.SHOW_SETTINGS_IN_TOOLBAR,
                false
            )
        ) {
            menu.getItem(0).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }
        menu.getItem(0).isVisible = true
        menu.getItem(1).isVisible =
            preferencesBuilder.getPreferenceBoolean(SettingStore.MENU.ABOUT, true)
        menu.getItem(2).isVisible = false
        menu.getItem(3).isVisible =
            preferencesBuilder.getPreferenceBoolean(SettingStore.MENU.OPEN_DRAWER, true)
        menu.getItem(4).isVisible =
            preferencesBuilder.getPreferenceBoolean(SettingStore.MENU.BACKUP, false)
        menu.getItem(5).isVisible =
            preferencesBuilder.getPreferenceBoolean(SettingStore.MENU.REBOOT, false)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when (id) {
            R.id.menu_settings -> {
                startActivity(Intent(this@UIDashboard, UISettings::class.java))
                return true
            }

            R.id.menu_about -> {
                startActivity(Intent(this@UIDashboard, UIAbout::class.java))
                return true
            }

            R.id.menu_dashboard -> return true
            R.id.menu_drawer -> {
                drawer.openDrawer()
                return true
            }

            R.id.menu_backup -> {
                startActivity(Intent(this@UIDashboard, UIBackup::class.java))
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
