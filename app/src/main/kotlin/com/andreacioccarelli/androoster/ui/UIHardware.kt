package com.andreacioccarelli.androoster.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatButton
import android.support.v7.widget.AppCompatSpinner
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
import android.widget.LinearLayout
import android.widget.TextView
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.internal.MDTintHelper
import com.afollestad.materialdialogs.internal.ThemeSingleton
import com.andreacioccarelli.androoster.tools.LicenseManager
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

class UIHardware : BaseActivity(), NavigationView.OnNavigationItemSelectedListener, Governors, LaunchStruct {

    internal var pro: Boolean = false
    lateinit var UI: UI
    lateinit var DRAWER_SETTINGS: PrimaryDrawerItem
    lateinit var drawer: Drawer
    var menu: Menu? = null
    var isInBatterySavingMode = false

    internal var drawerInitialized = false

    internal var doubleBackToExitPressedOnce = false









    val fabTop: FloatingActionButton get() = findViewById(R.id.fabTop)
    val fabBottom: FloatingActionButton get() = findViewById(R.id.fabBottom)
    val toolbar_layout: CollapsingToolbarLayout get() = findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout)

    private val SwitchHardware1: SwitchCompat get() = findViewById(R.id.SwitchHardware1)
    private val SwitchHardware2: SwitchCompat get() = findViewById(R.id.SwitchHardware2)
    private val SwitchHardware4: SwitchCompat get() = findViewById(R.id.SwitchHardware4)
    private val SwitchHardware5: SwitchCompat get() = findViewById(R.id.SwitchHardware5)
    private val SwitchHardware6: SwitchCompat get() = findViewById(R.id.SwitchHardware6)
    private val SwitchHardware7: SwitchCompat get() = findViewById(R.id.SwitchHardware7)

    private val CardHardware1: CardView get() = findViewById(R.id.CardHardware1)
    private val CardHardware2: CardView get() = findViewById(R.id.CardHardware2)
    private val CardHardware4: CardView get() = findViewById(R.id.CardHardware4)
    private val CardHardware5: CardView get() = findViewById(R.id.CardHardware5)
    private val CardHardware6: CardView get() = findViewById(R.id.CardHardware6)
    private val CardHardware7: CardView get() = findViewById(R.id.CardHardware7)
    private val CardHardware8: CardView get() = findViewById(R.id.CardHardware8)

    private val SpinnerRAM0: AppCompatSpinner get() = findViewById(R.id.SpinnerRAM0)
    private val spinnerRAM1: AppCompatSpinner get() = findViewById(R.id.spinnerRAM1)

    private val HardwareBase: ImageView get() = findViewById(R.id.HardwareBase)

    private val render1RAM: TextView get() = findViewById(R.id.render1RAM)
    private val render2RAM: TextView get() = findViewById(R.id.render2RAM)
    private val render3RAM: TextView get() = findViewById(R.id.render3RAM)
    private val render4RAM: TextView get() = findViewById(R.id.render4RAM)
    private val render5RAM: TextView get() = findViewById(R.id.render5RAM)
    private val render6RAM: TextView get() = findViewById(R.id.render6RAM)


    private val ButtonHardware8: AppCompatButton get() = findViewById(R.id.ButtonHardware8)

    private val dashboard_hardware_content: TextView get() = findViewById(R.id.dashboard_hardware_content)











    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hardware)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        RecentWidget.collect(this@UIHardware, LaunchStruct.HARDWARE_ACTIVITY)

        UI = UI(this@UIHardware)
        pro = PreferencesBuilder(this, PreferencesBuilder.defaultFilename).getBoolean("pro", false)

        preferencesBuilder = PreferencesBuilder(this@UIHardware, PreferencesBuilder.defaultFilename)

        preferencesBuilder.putInt(XmlKeys.LAST_OPENED, LaunchStruct.HARDWARE_ACTIVITY)
        setUpDrawer(toolbar)
        FabManager.setup(fabTop, fabBottom, this@UIHardware, drawer, preferencesBuilder)

        updateDashboard()
        animateContent(findViewById(R.id.content) as ViewGroup)

        CardHardware1.setOnClickListener { _ -> SwitchHardware1.performClick() }
        CardHardware2.setOnClickListener { _ -> SwitchHardware2.performClick() }
        CardHardware4.setOnClickListener { _ -> SwitchHardware4.performClick() }
        CardHardware5.setOnClickListener { _ -> SwitchHardware5.performClick() }
        CardHardware6.setOnClickListener { _ -> SwitchHardware6.performClick() }
        CardHardware7.setOnClickListener { _ -> SwitchHardware7.performClick() }

        SwitchHardware1.setOnClickListener { _ ->
            if (SwitchHardware1.isChecked) {
                preferencesBuilder.putBoolean("HW1", true)
                Core.hardware_rendering(true)
                UI.on()
            } else {
                preferencesBuilder.putBoolean("HW1", false)
                Core.hardware_rendering(false)
                UI.off()
            }
        }

        SwitchHardware2.setOnClickListener { _ ->
            if (SwitchHardware2.isChecked) {
                preferencesBuilder.putBoolean("HW2", true)
                Core.set_16bit_alpha(true)
                UI.on()
            } else {
                preferencesBuilder.putBoolean("HW2", false)
                Core.set_16bit_alpha(false)
                UI.off()
            }
        }


        if (Build.MANUFACTURER.lowercase().contains("samsung") && Build.VERSION.SDK_INT >= 21) {
            CardHardware4.visibility = View.GONE
            CardHardware5.visibility = View.GONE
        } else {
            SwitchHardware4.setOnClickListener { _ ->
                if (SwitchHardware4.isChecked) {
                    preferencesBuilder.putBoolean("HW4", true)
                    Core.set_180_rot(true)
                    UI.on()
                } else {
                    preferencesBuilder.putBoolean("HW4", false)
                    Core.set_180_rot(false)
                    UI.off()
                }
            }

            SwitchHardware5.setOnClickListener { _ ->
                if (SwitchHardware5.isChecked) {
                    preferencesBuilder.putBoolean("HW5", true)
                    Core.set_lockscreen_rotation(true)
                    UI.on()
                } else {
                    preferencesBuilder.putBoolean("HW5", false)
                    Core.set_lockscreen_rotation(false)
                    UI.off()
                }
            }
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                isInBatterySavingMode = Integer.valueOf(
                    CoreBase.SETTINGS.get(
                        FrameworkSurface.GLOBAL,
                        "low_power"
                    )
                ) == 1
            } catch (e: RuntimeException) {

            }
        }

        SwitchHardware6.setOnClickListener { _ ->
            if (isInBatterySavingMode) {
                if (SwitchHardware6.isChecked) {
                    UI.on()
                    preferencesBuilder.putBoolean("HW6", true)
                    Core.set_always_on_backlights(true)
                } else {
                    UI.off()
                    preferencesBuilder.putBoolean("HW6", false)
                    Core.set_always_on_backlights(false)
                }
            } else {
                if (SwitchHardware6.isChecked) {
                    UI.on()
                    preferencesBuilder.putBoolean("HW6", true)
                    Core.set_always_on_backlights(true)
                } else {
                    UI.off()
                    preferencesBuilder.putBoolean("HW6", false)
                    Core.set_always_on_backlights(false)
                }
            }
        }


        SwitchHardware7.setOnClickListener { _ ->
            if (SwitchHardware7.isChecked) {
                preferencesBuilder.putBoolean("HW7", true)
                Core.set_wakeup_method_volume(true)
                UI.on()
            } else {
                preferencesBuilder.putBoolean("HW7", false)
                Core.set_wakeup_method_volume(false)
                UI.off()
            }
        }

        ButtonHardware8.setOnClickListener {
            var value = "0"

            val editDialog = MaterialDialog.Builder(this@UIHardware)
                    .title(R.string.edit_dialog_target_property)
                    .customView(R.layout.edit_dialog, true)
                    .positiveText(R.string.action_set)
                    .autoDismiss(true)
                    .negativeText(android.R.string.cancel)
                    .onPositive { _, _ ->
                        CoreBase.buildprop("ro.sf.lcd_density", value)
                        CoreBase.setprop("ro.sf.lcd_density", value)
                        UI.info(getString(R.string.graphic_dpi_change_success).replace("%dpi", value))
                        preferencesBuilder.putString("cachedDpi", value)
                    }
                    .onNegative { dialog1, _ -> dialog1.dismiss() }
                    .build()


            val warning = editDialog!!.customView!!.findViewById(R.id.newValue) as TextView

            editDialog.customView!!.findViewById<TextView>(R.id.targetProperty).text = getString(R.string.dialog_dpi_changer_property)
            editDialog.customView!!.findViewById<TextView>(R.id.defaultValue).text = getString(R.string.dialog_dpi_changer_dfu)
            editDialog.customView!!.findViewById<LinearLayout>(R.id.warningLayout).visibility = View.VISIBLE
            editDialog.customView!!.findViewById<TextView>(R.id.warning).text = getString(R.string.hardware_dialog_warning)


            val positiveAction = editDialog.getActionButton(DialogAction.POSITIVE)
            val input = editDialog.customView!!.findViewById(R.id.input) as EditText

            val dfu = CoreBase.scanbuild("ro.sf.lcd_density").trim()
            input.hint = dfu

            ATH.setTint(input, ThemeStore.accentColor(this@UIHardware))
            input.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                    val numericInput = try {
                        Integer.parseInt(s.toString())
                    } catch (n: NumberFormatException) {
                        0
                    }

                    val isEmpty = s.toString().trim().isEmpty()

                    when {
                        isEmpty -> warning.text = getString(R.string.dialog_dpi_changer_empty)
                        numericInput > 600 -> warning.text = getString(R.string.dialog_dpi_changer_large)
                        numericInput < 300 -> warning.text = getString(R.string.dialog_dpi_changer_low)
                        else -> warning.text = getString(R.string.edit_dialog_new_value)
                    }

                    value = numericInput.toString()
                    positiveAction!!.isEnabled = !value.trim().isEmpty()
                    positiveAction.isEnabled = value.trim() != "0"
                }

                override fun afterTextChanged(s: Editable) {}
            })

            val widgetColor = ThemeSingleton.get().widgetColor

            //MDTintHelper.setTint(input,
            //        if (widgetColor == 0) ContextCompat.getColor(this@UIHardware, R.color.accent) else widgetColor)

            editDialog.show()
            positiveAction!!.isEnabled = false
        }


        SwitchHardware1.isChecked = preferencesBuilder.getBoolean("HW1", false)
        SwitchHardware2.isChecked = preferencesBuilder.getBoolean("HW2", false)
        SwitchHardware4.isChecked = preferencesBuilder.getBoolean("HW4", false)
        SwitchHardware5.isChecked = preferencesBuilder.getBoolean("HW5", false)
        SwitchHardware6.isChecked = preferencesBuilder.getBoolean("HW6", false)
        SwitchHardware7.isChecked = preferencesBuilder.getBoolean("HW7", false)

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

        ATH.setTint(SwitchHardware1, accentColor)
        ATH.setTint(SwitchHardware2, accentColor)
        ATH.setTint(SwitchHardware4, accentColor)
        ATH.setTint(SwitchHardware5, accentColor)
        ATH.setTint(SwitchHardware6, accentColor)
        ATH.setTint(SwitchHardware7, accentColor)
        ATH.setTint(HardwareBase, primaryColor)
        ATH.setTint(ButtonHardware8, accentColor)

        val d: String? = packageManager.getInstallerPackageName(packageName)
        if (d != null) {
            if (CryptoFactory.sha256(d) == "D79B77BC4C48DE2746DE9F43CFB9209C4EA8D27D38B5AD9260FF3F8EA06D4252") {
                Toasty.error(baseContext, getString(R.string.app_toast))
                shutdownApp()
                return
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateDashboard() {
        dashboard_hardware_content.text =
                "${getString(R.string.hardware_widget_device)}: ${Build.DEVICE}\n" +
                "${getString(R.string.hardware_widget_manufacturer)}: ${Build.MANUFACTURER}\n" +
                "${getString(R.string.hardware_widget_model)}: ${Build.MODEL}"
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

        DrawerBuilder().withActivity(this@UIHardware).build()

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
        val DRAWER_HARDWARE = PrimaryDrawerItem().withIdentifier(12).withName(R.string.drawer_hardware)
        val DRAWER_GRAPHICS = PrimaryDrawerItem().withIdentifier(13).withName(R.string.drawer_graphics).withOnDrawerItemClickListener { _, _, _ ->
            handleIntent(LaunchStruct.GRAPHICS_ACTIVITY)
            false
        }
        val DRAWER_ABOUT = PrimaryDrawerItem().withIdentifier(14).withName(R.string.drawer_about).withOnDrawerItemClickListener { _, _, _ ->
            handleIntent(LaunchStruct.ABOUT_ACTIVITY)
            false
        }
        val DRAWER_BUY_PRO_VERSION = PrimaryDrawerItem().withIdentifier(15).withName(R.string.drawer_pro).withOnDrawerItemClickListener { _, _, _ ->
            LicenseManager.startProActivity(this@UIHardware, this@UIHardware, drawer)
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
        BaseActivity.setDrawerHeader(DrawerHeader.findViewById(R.id.Title), DrawerHeader.findViewById(R.id.Content), DrawerHeader.findViewById(R.id.Image), DrawerHeader.findViewById(R.id.RootLayout), this@UIHardware, pro)


        if (pro) {
            drawer = DrawerBuilder()
                    .withActivity(this@UIHardware)
                    .withToolbar(toolbar)
                    .addDrawerItems(
                            DRAWER_HARDWARE,
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
                            DRAWER_GRAPHICS,
                            DividerDrawerItem(),
                            DRAWER_ABOUT,
                            DRAWER_SETTINGS
                    )
                    .withHeader(DrawerHeader)
                    .build()
        } else {
            drawer = DrawerBuilder()
                    .withActivity(this@UIHardware)
                    .withToolbar(toolbar)
                    .addDrawerItems(
                            DRAWER_HARDWARE,
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
        LaunchManager.startActivity(ActivityID, this@UIHardware)
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
                val UI = UI(this@UIHardware)
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
                startActivity(Intent(this@UIHardware, UIAbout::class.java))
                return true
            }
            R.id.menu_dashboard -> {
                startActivity(Intent(this@UIHardware, UIDashboard::class.java))
                return true
            }
            R.id.menu_settings -> {
                startActivity(Intent(this@UIHardware, UISettings::class.java))
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

