package com.andreacioccarelli.androoster.ui

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.NavigationView
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.andreacioccarelli.androoster.R
import com.andreacioccarelli.androoster.core.RootFile
import com.andreacioccarelli.androoster.core.TerminalCore
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
import kotlinx.android.synthetic.main.storage.*
import kotlinx.android.synthetic.main.storage_content.*
import java.lang.RuntimeException
import java.util.*
import kotlin.concurrent.schedule

class UIStorage : BaseActivity(), NavigationView.OnNavigationItemSelectedListener, Governors, LaunchStruct {

    internal lateinit var DRAWER_SETTINGS: PrimaryDrawerItem
    internal lateinit var DRAWER_BACKUP: PrimaryDrawerItem

    internal var drwInitialized = false
    internal var pro: Boolean = false

    internal var doubleBackToExitPressedOnce = false
    lateinit var UI: UI
    lateinit var drawer: Drawer
    var ran = false
    var menu: Menu? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.storage)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        UI = UI(this@UIStorage)
        pro = PreferencesBuilder(this, PreferencesBuilder.defaultFilename).getBoolean("pro", false)
        preferencesBuilder = PreferencesBuilder(this@UIStorage)
        animateContent(content as ViewGroup)

        preferencesBuilder.putInt(XmlKeys.LAST_OPENED, LaunchStruct.STORAGE_ACTIVITY)
        RecentWidget.collect(this@UIStorage, LaunchStruct.STORAGE_ACTIVITY)
        setUpDrawer(toolbar)

        FabManager.setup(fabTop, fabBottom, this@UIStorage, drawer, preferencesBuilder)
        createWidget()

        CardROM2.setOnClickListener {
            if (!ran) {
                ran = true
                val dialog = MaterialDialog.Builder(this@UIStorage)
                        .title(R.string.storage_fstrim_dialog_title)
                        .content(R.string.storage_fstrim_dialog_content)
                        .cancelable(false)
                        .progress(true, 0)
                        .progressIndeterminateStyle(true)
                        .show()

                Handler().postDelayed({
                    CoroutineScope(Dispatchers.Main).launch {
                        var isNotInstalled = true
                        try {
                             isNotInstalled = run("fstrim").getStderr().contains("not found")
                        } catch (ise: java.lang.IllegalStateException) {}
                        catch (re: RuntimeException) {}

                        if (isNotInstalled) {
                            CoroutineScope(Dispatchers.Main).launch {
                                ran = false
                                dialog.dismiss()
                                MaterialDialog.Builder(it)
                                        .title(R.string.storage_fstrim_dialog_not_found_title)
                                        .content(R.string.storage_fstrim_dialog_not_found_content)
                                        .positiveText(R.string.action_ok)
                                        .show()
                            }
                            return@doAsync
                        }

                        CoroutineScope(Dispatchers.Main).launch { dialog.setTitle(R.string.storage_fstrim_dialog_progress_title) }
                        TerminalCore.mount()

                        val resultList = ArrayList<String>()
                        val pathList = Arrays.asList("/system", "/data", "/cache")
                        val untrustedPaths = Arrays.asList("/su", "/magisk")


                        for (path in pathList) {
                            CoroutineScope(Dispatchers.Main).launch { dialog.setContent(getString(R.string.storage_fstrim_dialog_progress_prefix) + " " + path) }
                            resultList.add(run("fstrim -v $path").getStdout())
                        }

                        for (path in untrustedPaths) {
                            if (RootFile(path).file.exists()) {
                                CoroutineScope(Dispatchers.Main).launch { dialog.setContent(getString(R.string.storage_fstrim_dialog_progress_prefix) + " " + path) }
                                resultList.add(run("fstrim -v $path").getStdout())
                            }
                        }

                        CoroutineScope(Dispatchers.Main).launch {
                            dialog.dismiss()
                            TitleROM2.setText(R.string.storage_fstrim_dialog_title)
                            ContentROM2.text = ""
                            var showLastSpace = 0
                            for (result in resultList) {
                                showLastSpace++
                                ContentROM2.append(result + (if (showLastSpace == resultList.size) "" else "\n"))
                            }
                        }
                    }
                }, 1000)
            } else {
                UI.warning(getString(R.string.storage_fstrim_already_run))
            }
        }


        CardROM3.setOnClickListener { _ -> SwitchROM3.performClick() }
        CardROM4.setOnClickListener { _ -> SwitchROM4.performClick() }
        CardROM5.setOnClickListener { _ -> SwitchROM5.performClick() }


        SwitchROM3.setOnClickListener { _ ->
            if (SwitchROM3.isChecked) {
                Thread { preferencesBuilder.putBoolean("ROM3", true) }.start()
                UI.on()
            } else {
                Thread { preferencesBuilder.putBoolean("ROM3", true) }.start()
                UI.off()
            }
        }
        SwitchROM4.setOnClickListener { _ ->
            if (SwitchROM4.isChecked) {
                Thread { preferencesBuilder.putBoolean("ROM4", true) }.start()
                UI.on()
            } else {
                Thread { preferencesBuilder.putBoolean("ROM4", false) }.start()
                UI.off()
            }
        }
        SwitchROM5.setOnClickListener { _ ->
            if (SwitchROM5.isChecked) {
                preferencesBuilder.putBoolean("ROM5", true)
                UI.on()
            } else {
                preferencesBuilder.putBoolean("ROM5", false)
                UI.off()
            }
        }

        val mountState = findViewById<TextView>(R.id.ContentROM6)

        if (preferencesBuilder.getBoolean("ext4", false)) {
            mountState.text = getString(R.string.storage_ext4_already_done)
            ButtonROM6.isEnabled = false
        } else {
            ButtonROM6.isEnabled = true
        }

        ButtonROM6.setOnClickListener { _ ->
            val d = MaterialDialog.Builder(this@UIStorage)
                    .title(R.string.storage_mounting_title)
                    .content(R.string.storage_mounting_content)
                    .progress(true, 100)
                    .cancelable(false)
                    .progressIndeterminateStyle(true)
                    .show()

            Handler().postDelayed({
                UI.success(getString(R.string.storage_mounting_success))
                d.dismiss()
                preferencesBuilder.putBoolean("ext4", true)
                ButtonROM6.isEnabled = false
                mountState.text = getString(R.string.storage_ext4_already_did)
            }, 2300)

        }

        ButtonROM7.setOnClickListener {
            if (isPackageInstalled("eu.thedarken.sdm")) {
                try {
                    startActivity(packageManager.getLaunchIntentForPackage("eu.thedarken.sdm"))
                } catch(anf: ActivityNotFoundException) {
                    /*
                    Crashlytics.logException(anf)
                    Crashlytics.log(1, "UIStorage", "eu.thedarken.sdm not found")
                    */
                }
            } else {
                startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=eu.thedarken.sdm")))
            }
        }



        if ((resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE ||
                (resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) {
            appSdmaidLayoutIcon.visibility = View.VISIBLE
        } else {
            appSdmaidLayoutIcon.visibility = View.GONE
        }

        SwitchROM3.isChecked = preferencesBuilder.getBoolean("ROM3", false)
        SwitchROM4.isChecked = preferencesBuilder.getBoolean("ROM4", false)
        SwitchROM5.isChecked = preferencesBuilder.getBoolean("ROM5", false)

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

        ATH.setTint(SwitchROM3, accentColor)
        ATH.setTint(SwitchROM4, accentColor)
        ATH.setTint(SwitchROM5, accentColor)
        ATH.setTint(ButtonROM6, accentColor)
        ATH.setTint(ButtonROM7, ContextCompat.getColor(baseContext,R.color.sd_maid))
        ATH.setTint(storageBase, primaryColor)

        if (isPackageInstalled("eu.thedarken.sdm")) {
            ButtonROM7.text = getString(R.string.action_open)
            TitleROM7.text = getString(R.string.storage_sdmaid_open_title)
            ContentROM7.text = getString(R.string.storage_sdmaid_open_content)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun createWidget() {
        CoroutineScope(Dispatchers.Main).launch {
            val systemPath = Environment.getRootDirectory().absolutePath
            val dataPath = Environment.getDataDirectory().absolutePath
            val storagePath = Environment.getExternalStorageDirectory().path
            val doesSuExist = RootFile("/su").file.exists()
            val doesMagiskExist = RootFile("/magisk").file.exists()

            CoroutineScope(Dispatchers.Main).launch {
                dashboard_rom_content.text =
                        "${getString(R.string.storage_widget_system)}: $systemPath\n" +
                        "${getString(R.string.storage_widget_data)}: $dataPath\n" +
                        "${getString(R.string.storage_widget_cache)}: /cache\n" +
                        "${getString(R.string.storage_widget_storage)}: $storagePath"

                if (doesSuExist) dashboard_rom_content.append("\n${getString(R.string.storage_system_supersu)}: /su")
                if (doesMagiskExist) dashboard_rom_content.append("\n${getString(R.string.storage_system_magisk)}: /magisk")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        FabManager.onResume(fabTop, fabBottom, preferencesBuilder)
        if (pro && drwInitialized) {
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

        CoroutineScope(Dispatchers.Main).launch {
            val updatedState = isPackageInstalled("eu.thedarken.sdm")
            if (isPackageInstalled("eu.thedarken.sdm") == updatedState) return@doAsync
            if (updatedState) {
                CoroutineScope(Dispatchers.Main).launch {
                    ButtonROM7.text = getString(R.string.action_open)
                    TitleROM7.text = getString(R.string.storage_sdmaid_open_title)
                    ContentROM7.text = getString(R.string.storage_sdmaid_open_content)
                }
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    ButtonROM7.text = getString(R.string.action_install)
                    TitleROM7.text = getString(R.string.storage_sdmaid_install_title)
                    ContentROM7.text = getString(R.string.storage_sdmaid_install_content)
                }
            }
        }
    }

    private fun setUpDrawer(toolbar: Toolbar) {
        DrawerBuilder().withActivity(this@UIStorage).build()

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
        val DRAWER_STORAGE = PrimaryDrawerItem().withIdentifier(7).withName(R.string.drawer_storage)
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
            LicenseManager.startProActivity(this@UIStorage, this@UIStorage, drawer)
            false
        }


        DRAWER_BACKUP = PrimaryDrawerItem().withIdentifier(19L).withName(R.string.drawer_backup).withOnDrawerItemClickListener { _, _, _ ->
            startActivity(Intent(this@UIStorage, UIBackup::class.java))
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
        BaseActivity.setDrawerHeader(DrawerHeader.findViewById(R.id.Title), DrawerHeader.findViewById(R.id.Content), DrawerHeader.findViewById(R.id.Image), DrawerHeader.findViewById(R.id.RootLayout), this@UIStorage, pro)


        if (pro) {
            drawer = DrawerBuilder()
                    .withActivity(this@UIStorage)
                    .withToolbar(toolbar)
                    .addDrawerItems(
                            DRAWER_STORAGE,
                            DRAWER_DASHBOARD,
                            DividerDrawerItem(),
                            DRAWER_CPU,
                            DRAWER_RAM,
                            DRAWER_BATTERY,
                            DRAWER_KERNEL,
                            DRAWER_TWEAKS,
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
                    .withActivity(this@UIStorage)
                    .withToolbar(toolbar)
                    .addDrawerItems(
                            DRAWER_STORAGE,
                            DRAWER_DASHBOARD,
                            DividerDrawerItem(),
                            DRAWER_CPU,
                            DRAWER_RAM,
                            DRAWER_BATTERY,
                            DRAWER_KERNEL,
                            DRAWER_TWEAKS,
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

        drwInitialized = true
    }

    internal fun handleIntent(ActivityID: Int) {
        LaunchManager.startActivity(ActivityID, this@UIStorage)
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

        val d: String? = packageManager.getInstallerPackageName(packageName)
        if (d != null) {
            if (CryptoFactory.sha256(d) == "D79B77BC4C48DE2746DE9F43CFB9209C4EA8D27D38B5AD9260FF3F8EA06D4252") {
                Toasty.error(baseContext, getString(R.string.app_toast))
                shutdownApp()
                return false
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when (id) {
            R.id.menu_about -> {
                startActivity(Intent(this@UIStorage, UIAbout::class.java))
                return true
            }
            R.id.menu_dashboard -> {
                startActivity(Intent(this@UIStorage, UIDashboard::class.java))
                return true
            }
            R.id.menu_settings -> {
                startActivity(Intent(this@UIStorage, UISettings::class.java))
                return true
            }
            R.id.menu_drawer -> {
                drawer.openDrawer()
                return true
            }
            R.id.menu_backup -> {
                startActivity(Intent(this@UIStorage, UIBackup::class.java))
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
