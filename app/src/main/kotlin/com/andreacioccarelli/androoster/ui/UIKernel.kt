package com.andreacioccarelli.androoster.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.NavigationView
import android.support.v7.widget.AppCompatSeekBar
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.TextView
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
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
import com.kabouzeid.appthemehelper.ATH
import com.kabouzeid.appthemehelper.ThemeStore
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.kernel.*
import kotlinx.android.synthetic.main.kernel_content.*
import org.jetbrains.anko.doAsync
import java.util.*
import kotlin.concurrent.schedule

class UIKernel : BaseActivity(), NavigationView.OnNavigationItemSelectedListener, LaunchStruct {

    private var secondsSeekBar: AppCompatSeekBar? = null
    private var positiveAction: View? = null
    internal var pro: Boolean = false
    internal var drawerInitialized = false
    
    lateinit var UI: UI
    lateinit var DRAWER_SETTINGS: PrimaryDrawerItem
    lateinit var DRAWER_BACKUP: PrimaryDrawerItem
    lateinit var drawer: Drawer
    var menu: Menu? = null

    internal var doubleBackToExitPressedOnce = false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.kernel)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        RecentWidget.collect(this@UIKernel, LaunchStruct.KERNEL_ACTIVITY)

        UI = UI(this@UIKernel)
        pro = PreferencesBuilder(this, PreferencesBuilder.defaultFilename).getBoolean("pro", false)

        preferencesBuilder = PreferencesBuilder(this@UIKernel, PreferencesBuilder.defaultFilename)

        preferencesBuilder.putInt(XmlKeys.LAST_OPENED, LaunchStruct.KERNEL_ACTIVITY)
        setUpDrawer(toolbar)
        FabManager.setup(fabTop, fabBottom, this@UIKernel, drawer, preferencesBuilder)

        createWidget()
        animateContent(content as ViewGroup)

        CardKernel1.setOnClickListener { SwitchKernel1.performClick() }
        CardKernel2.setOnClickListener { SwitchKernel2.performClick() }
        CardKernel3.setOnClickListener { SwitchKernel3.performClick() }
        CardKernel4.setOnClickListener { SwitchKernel4.performClick() }
        CardKernel5.setOnClickListener { ButtonKernel5.performClick() }
        CardKernel6.setOnClickListener { SwitchKernel6.performClick() }
        CardKernel7.setOnClickListener { SwitchKernel7.performClick() }
        CardKernel8.setOnClickListener { SpinnerKernel8.performClick() }

        SwitchKernel1.setOnClickListener { _ ->
            if (SwitchKernel1.isChecked) {
                Core.apply_kernel_tweaks(true)
                preferencesBuilder.putBoolean("Kernel1", true)
                UI.on()
            } else {
                Core.apply_kernel_tweaks(false)
                preferencesBuilder.putBoolean("Kernel1", false)
                UI.off()
            }
        }
        SwitchKernel2.setOnClickListener { _ ->
            if (SwitchKernel2.isChecked) {
                Core.kernel_sleepers_optimization()
                preferencesBuilder.putBoolean("Kernel2", true)
                UI.on()
            } else {
                Core.kernel_sleepers_optimization()
                preferencesBuilder.putBoolean("Kernel2", false)
                UI.off()
            }
        }
        SwitchKernel3.setOnClickListener { _ ->
            if (SwitchKernel3.isChecked) {
                Core.disable_kernel_jni_check(true)
                preferencesBuilder.putBoolean("Kernel3", true)
                UI.on()
            } else {
                Core.disable_kernel_jni_check(false)
                preferencesBuilder.putBoolean("Kernel3", false)
                UI.off()
            }
        }
        SwitchKernel4.setOnClickListener { _ ->
            if (SwitchKernel4.isChecked) {
                Core.set_kernelpanic(60)
                preferencesBuilder.putBoolean("Kernel4", true)
                UI.on()
            } else {
                Core.set_kernelpanic(0)
                preferencesBuilder.putBoolean("Kernel4", false)
                UI.off()
            }
        }


        val d2: String? = packageManager.getInstallerPackageName(packageName)
        if (d2 != null) {
            if (CryptoFactory.sha1(d2) == "d756abfb7665a50be304bae79a0f83db8adffd60") {
                Toasty.error(baseContext, getString(R.string.app_toast))
                throw NullPointerException("null")
            }
        }

        ButtonKernel5.setOnClickListener { _ ->
            val dialog = MaterialDialog.Builder(this@UIKernel)
                    .title(R.string.edit_dialog_target_property)
                    .customView(R.layout.edit_dialog_spinner, true)
                    .positiveText(R.string.action_set)
                    .negativeText(android.R.string.cancel)
                    .onPositive { _, _ ->
                        Core.set_kernelpanic(secondsSeekBar!!.progress)
                        if (!SwitchKernel4.isChecked) SwitchKernel4.performClick()

                        preferencesBuilder.putBoolean("Kernel3", true)
                        preferencesBuilder.putInt("PanicEditCachedText", secondsSeekBar!!.progress)
                        UI.success(getString(R.string.kernel_auto_reboot_success).replace("%s", secondsSeekBar!!.progress.toString()))
                    }
                    .show()

            val editPropTitle = dialog.customView!!.findViewById<TextView>(R.id.targetProperty)
            val defaultValue = dialog.customView!!.findViewById<TextView>(R.id.defaultValue)
            val warningTextView = dialog.customView!!.findViewById<TextView>(R.id.newValue)
            val warningTextViewText = resources.getString(R.string.edit_dialog_new_value)

            defaultValue.text = getString(R.string.kernel_dfu_watchdog)
            positiveAction = dialog.getActionButton(DialogAction.POSITIVE)
            editPropTitle.text = getString(R.string.kernel_auto_reboot_delay)

            secondsSeekBar = dialog.customView!!.findViewById(R.id.input)
            ATH.setTint(secondsSeekBar!!, ThemeStore.accentColor(this@UIKernel))

            val cachedProgress = preferencesBuilder.getInt("PanicEditCachedText", 60)
            secondsSeekBar!!.max = 60
            warningTextView.text = "$warningTextViewText ($cachedProgress${')'}"
            secondsSeekBar!!.progress = cachedProgress
            secondsSeekBar!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    warningTextView.text = "$warningTextViewText ($progress${')'}"
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    warningTextView.text = "$warningTextViewText (${seekBar.progress}${')'}"
                }
            })
        }

        SwitchKernel6.setOnClickListener { _ ->
            if (SwitchKernel6.isChecked) {
                Core.enable_reboot_on_oops(true)
                preferencesBuilder.putBoolean("Kernel6", true)
                UI.on()
            } else {
                Core.enable_reboot_on_oops(false)
                preferencesBuilder.putBoolean("Kernel6", false)
                UI.off()
            }
        }
        SwitchKernel7.setOnClickListener { _ ->
            if (SwitchKernel7.isChecked) {
                Core.enable_entropy_optimization(true)
                preferencesBuilder.putBoolean("Kernel7", true)
                UI.on()
            } else {
                Core.enable_entropy_optimization(false)
                preferencesBuilder.putBoolean("Kernel7", false)
                UI.off()
            }
        }


        val maxThreads = ArrayAdapter.createFromResource(this@UIKernel,
                R.array.max_threads_modifier_items, android.R.layout.simple_spinner_item)
        maxThreads.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        SpinnerKernel8.adapter = maxThreads
        SpinnerKernel8.setSelection(preferencesBuilder.getInt("SpinnerKernel8", 1))


        val r = HardwareCore.ramInGb

        val onCreateItem = intArrayOf(SpinnerKernel8.selectedItemId.toInt())
        SpinnerKernel8.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (onCreateItem[0] != position) {
                    when (position) {
                        0 -> {
                            UI.success(getString(R.string.kernel_max_success))
                            preferencesBuilder.putInt("SpinnerKernel8", 0)
                            onCreateItem[0] = position
                            Core.set_max_threads_number(262144)
                        }
                        1 -> {
                            UI.success(getString(R.string.kernel_max_success))
                            preferencesBuilder.putInt("SpinnerKernel8", 1)
                            onCreateItem[0] = position
                            Core.set_max_threads_number(524288)
                        }
                        2 -> if (r > 2.00) {
                            UI.success(getString(R.string.kernel_max_success))
                            preferencesBuilder.putInt("SpinnerKernel8", 2)
                            onCreateItem[0] = position
                            Core.set_max_threads_number(1048576)
                        } else {
                            UI.error(getString(R.string.kernel_error))
                            SpinnerKernel8.setSelection(onCreateItem[0])
                        }
                        3 -> if (r > 3.00) {
                            UI.success(getString(R.string.kernel_max_success))
                            preferencesBuilder.putInt("SpinnerKernel8", 3)
                            onCreateItem[0] = position
                            Core.set_max_threads_number(2097152)
                        } else {
                            UI.error(getString(R.string.kernel_error))
                            SpinnerKernel8.setSelection(onCreateItem[0])
                        }
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        val d: String? = packageManager.getInstallerPackageName(packageName)
        if (d != null) {
            if (CryptoFactory.sha256(d) == "D79B77BC4C48DE2746DE9F43CFB9209C4EA8D27D38B5AD9260FF3F8EA06D4252") {
                Toasty.error(baseContext, getString(R.string.app_toast))
                shutdownApp()
                return
            }
        }

        SwitchKernel1.isChecked = preferencesBuilder.getBoolean("Kernel1", false)
        SwitchKernel2.isChecked = preferencesBuilder.getBoolean("Kernel2", false)
        SwitchKernel3.isChecked = preferencesBuilder.getBoolean("Kernel3", false)
        SwitchKernel4.isChecked = preferencesBuilder.getBoolean("Kernel4", false)
        SwitchKernel6.isChecked = preferencesBuilder.getBoolean("Kernel6", false)
        SwitchKernel7.isChecked = preferencesBuilder.getBoolean("Kernel7", false)

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

        ATH.setTint(SpinnerKernel8, accentColor)
        ATH.setTint(ButtonKernel5, accentColor)
        ATH.setTint(SwitchKernel1, accentColor)
        ATH.setTint(SwitchKernel2, accentColor)
        ATH.setTint(SwitchKernel3, accentColor)
        ATH.setTint(SwitchKernel4, accentColor)
        ATH.setTint(SwitchKernel6, accentColor)
        ATH.setTint(SwitchKernel7, accentColor)
        ATH.setTint(KernelBase, primaryColor)
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

    private fun createWidget() {
        doAsync {
            dashboard_kernel.text = Core.kernel_info()
        }
    }

    private fun setUpDrawer(toolbar: Toolbar) {
        DrawerBuilder().withActivity(this@UIKernel).build()

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
        val DRAWER_KERNEL = PrimaryDrawerItem().withIdentifier(5).withName(R.string.drawer_kernel)
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
            LicenseManager.startProActivity(this@UIKernel, this@UIKernel, drawer)
            false
        }


        DRAWER_BACKUP = PrimaryDrawerItem().withIdentifier(19L).withName(R.string.drawer_backup).withOnDrawerItemClickListener { _, _, _ ->
            startActivity(Intent(this@UIKernel, UIBackup::class.java))
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
        BaseActivity.setDrawerHeader(DrawerHeader.findViewById(R.id.Title), DrawerHeader.findViewById(R.id.Content), DrawerHeader.findViewById(R.id.Image), DrawerHeader.findViewById(R.id.RootLayout), this@UIKernel, pro)


        if (pro) {
            drawer = DrawerBuilder()
                    .withActivity(this@UIKernel)
                    .withToolbar(toolbar)
                    .addDrawerItems(
                            DRAWER_KERNEL,
                            DRAWER_DASHBOARD,
                            DividerDrawerItem(),
                            DRAWER_CPU,
                            DRAWER_RAM,
                            DRAWER_BATTERY,
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
                    .withActivity(this@UIKernel)
                    .withToolbar(toolbar)
                    .addDrawerItems(
                            DRAWER_KERNEL,
                            DRAWER_DASHBOARD,
                            DividerDrawerItem(),
                            DRAWER_CPU,
                            DRAWER_RAM,
                            DRAWER_BATTERY,
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

    internal fun handleIntent(ActivityID: Int) {
        LaunchManager.startActivity(ActivityID, this@UIKernel)
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
                val UI = UI(this@UIKernel)
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
                startActivity(Intent(this@UIKernel, UIAbout::class.java))
                return true
            }
            R.id.menu_dashboard -> {
                startActivity(Intent(this@UIKernel, UIDashboard::class.java))
                return true
            }
            R.id.menu_settings -> {
                startActivity(Intent(this@UIKernel, UISettings::class.java))
                return true
            }
            R.id.menu_drawer -> {
                drawer.openDrawer()
                return true
            }
            R.id.menu_backup -> {
                startActivity(Intent(this@UIKernel, UIBackup::class.java))
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
