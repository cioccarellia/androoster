package com.andreacioccarelli.androoster.ui

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.v7.widget.AppCompatButton
import android.support.v7.widget.AppCompatSpinner
import android.support.v7.widget.CardView
import android.support.v7.widget.SwitchCompat
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.andreacioccarelli.androoster.tools.LicenseManager
import com.andreacioccarelli.androoster.R
import com.andreacioccarelli.androoster.core.Core
import com.andreacioccarelli.androoster.core.FrameworkSurface
import com.andreacioccarelli.androoster.core.HardwareCore
import com.andreacioccarelli.androoster.dataset.KeyStore
import com.andreacioccarelli.androoster.ui.settings.SettingStore
import com.andreacioccarelli.androoster.dataset.XmlKeys
import com.andreacioccarelli.androoster.tools.LaunchStruct
import com.andreacioccarelli.androoster.tools.*
import com.andreacioccarelli.androoster.ui.about.UIAbout
import com.andreacioccarelli.androoster.ui.backup.UIBackup
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
import java.util.*
import kotlin.concurrent.schedule

class UIRam : BaseActivity(), NavigationView.OnNavigationItemSelectedListener, FrameworkSurface, LaunchStruct {

    private var pro: Boolean = false
    private var ram: Float = 0F
    private var ramString: String = ""

    lateinit var UI: UI
    lateinit var DRAWER_SETTINGS: PrimaryDrawerItem
    lateinit var DRAWER_BACKUP: PrimaryDrawerItem
    lateinit var drawer: Drawer
    var menu: Menu? = null

    private var drawerInitialized = false

    private var doubleBackToExitPressedOnce = false





    val fabTop: FloatingActionButton get() = findViewById(R.id.fabTop)
    val fabBottom: FloatingActionButton get() = findViewById(R.id.fabBottom)
    val toolbar_layout: CollapsingToolbarLayout get() = findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout)

    private val Switch1RAM3: SwitchCompat get() = findViewById(R.id.Switch1RAM3)
    private val Switch2RAM3: SwitchCompat get() = findViewById(R.id.Switch2RAM3)
    private val SwitchRAM2: SwitchCompat get() = findViewById(R.id.SwitchRAM2)
    private val SwitchRAM6: SwitchCompat get() = findViewById(R.id.SwitchRAM6)
    private val SwitchRAM7: SwitchCompat get() = findViewById(R.id.SwitchRAM7)

    private val CardRAM0: CardView get() = findViewById(R.id.CardRAM0)
    private val CardRAM1: CardView get() = findViewById(R.id.CardRAM1)
    private val CardRAM2: CardView get() = findViewById(R.id.CardRAM2)
    private val CardRAM3_1: CardView get() = findViewById(R.id.CardRAM3_1)
    private val CardRAM3_2: CardView get() = findViewById(R.id.CardRAM3_2)
    private val CardRAM4: CardView get() = findViewById(R.id.CardRAM4)
    private val CardRAM6: CardView get() = findViewById(R.id.CardRAM6)
    private val CardRAM7: CardView get() = findViewById(R.id.CardRAM7)

    private val SpinnerRAM0: AppCompatSpinner get() = findViewById(R.id.SpinnerRAM0)
    private val spinnerRAM1: AppCompatSpinner get() = findViewById(R.id.spinnerRAM1)

    private val RAMBase: ImageView get() = findViewById(R.id.RAMBase)

    private val render1RAM: TextView get() = findViewById(R.id.render1RAM)
    private val render2RAM: TextView get() = findViewById(R.id.render2RAM)
    private val render3RAM: TextView get() = findViewById(R.id.render3RAM)
    private val render4RAM: TextView get() = findViewById(R.id.render4RAM)
    private val render5RAM: TextView get() = findViewById(R.id.render5RAM)
    private val render6RAM: TextView get() = findViewById(R.id.render6RAM)
    private val lmk_state: TextView get() = findViewById(R.id.lmk_state)
    private val dashboard_ram_content: TextView get() = findViewById(R.id.dashboard_ram_content)







    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ram)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        RecentWidget.collect(this@UIRam, LaunchStruct.RAM_ACTIVITY)

        CoroutineScope(Dispatchers.Main).launch {
            ramString = HardwareCore.ram
            ram = HardwareCore.ramInGb
            updateWidget()
        }

        preferencesBuilder = PreferencesBuilder(this@UIRam)
        UI = UI(this@UIRam)
        pro = PreferencesBuilder(this, PreferencesBuilder.defaultFilename).getBoolean("pro", false)
        animateContent(findViewById(R.id.content) as ViewGroup)

        preferencesBuilder.putInt(XmlKeys.LAST_OPENED, LaunchStruct.RAM_ACTIVITY)
        setUpDrawer(toolbar)
        FabManager.setup(fabTop, fabBottom, this@UIRam, drawer, preferencesBuilder)


        val ProfileAdapter = ArrayAdapter.createFromResource(this@UIRam,
                R.array.ram_profiles, android.R.layout.simple_spinner_item)
        ProfileAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        SpinnerRAM0.adapter = ProfileAdapter

        val RuntimeBooster = ArrayAdapter.createFromResource(this@UIRam,
                R.array.runtime_memory_improvements_items, android.R.layout.simple_spinner_item)
        RuntimeBooster.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRAM1.adapter = RuntimeBooster

        CardRAM1.setOnClickListener { spinnerRAM1.performClick() }

        SpinnerRAM0.setSelection(preferencesBuilder.getInt("RAM0", 0))
        CardRAM0.setOnClickListener { _ -> SpinnerRAM0.performClick() }

        val tmp0 = intArrayOf(SpinnerRAM0.selectedItemPosition)
        SpinnerRAM0.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (tmp0[0] != position) {
                    preferencesBuilder.putInt("RAM0", position)

                    Core.set_ram_profile(position)

                    when (position) {
                        FrameworkSurface.PROFILE_DEFAULT -> UI.success(getString(R.string.ram_profile_switch).replace("%p",
                                getString(R.string.ram_runtime_1)))
                        FrameworkSurface.PROFILE_POWER_SAVING -> UI.success(getString(R.string.ram_profile_switch).replace("%p",
                                getString(R.string.ram_runtime_2)))
                        FrameworkSurface.PROFILE_SMOOTH -> UI.success(getString(R.string.ram_profile_switch).replace("%p",
                                getString(R.string.ram_runtime_3)))
                        FrameworkSurface.PROFILE_MULTITASKING -> UI.success(getString(R.string.ram_profile_switch).replace("%p",
                                getString(R.string.ram_runtime_4)))
                    }
                    preferencesBuilder.putInt("RAM0", position)
                    tmp0[0] = position
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        var i0 = ""; var i1 = ""; var i2 = ""; var i3 = ""; var i4 = ""; var i5 = ""

        CoroutineScope(Dispatchers.Main).launch {
            val buildprop = run("cat " + FrameworkSurface.buildprop_path).getStdout().trim { it <= ' ' }

             i0 = HardwareCore.Companion.BUILD.heapsize
             i1 = HardwareCore.Companion.BUILD.getMaxHeapsize()
             i2 = HardwareCore.Companion.BUILD.getMinHeapsize()
             i3 = HardwareCore.Companion.BUILD.getTargetUtilization(buildprop)
             i4 = HardwareCore.Companion.BUILD.getGrowthLimit(buildprop)
             i5 = HardwareCore.Companion.BUILD.getFlags(buildprop)

            if (preferencesBuilder.getBoolean("FSRAM", true)) {
                preferencesBuilder.putBoolean("FSRAM", false)
                preferencesBuilder.putString("RamBackup0", i0)
                preferencesBuilder.putString("RamBackup1", i1)
                preferencesBuilder.putString("RamBackup2", i2)
                preferencesBuilder.putString("RamBackup3", i3)
                preferencesBuilder.putString("RamBackup4", i4)
                preferencesBuilder.putString("RamBackup5", i5)
            }

            CoroutineScope(Dispatchers.Main).launch {
                render1RAM.text = preferencesBuilder.getString("RAMRender0", i0)
                render2RAM.text = preferencesBuilder.getString("RAMRender1", i1)
                render3RAM.text = preferencesBuilder.getString("RAMRender2", i2)
                render4RAM.text = preferencesBuilder.getString("RAMRender3", i3)
                render5RAM.text = preferencesBuilder.getString("RAMRender4", i4)
                render6RAM.text = preferencesBuilder.getString("RAMRender5", i5)
            }
        }


        spinnerRAM1.setSelection(preferencesBuilder.getInt("RAM1", 0))
        val selectedValue = intArrayOf(spinnerRAM1.selectedItemPosition)
        spinnerRAM1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (selectedValue[0] != position) {
                    var t = position
                    when (position) {
                        0 -> {
                            UI.success(getString(R.string.ram_runtime_switch).replace("%p",
                                    getString(R.string.ram_runtime_1)))
                            t = 0

                            render1RAM.text = i0
                            render2RAM.text = i1
                            render3RAM.text = i2
                            render4RAM.text = i3
                            render5RAM.text = i4
                            render6RAM.text = i5
                            CoroutineScope(Dispatchers.Main).launch { preferencesBuilder.putInt("RAMProfile", 0) }
                        }
                        1 -> {
                            UI.success(getString(R.string.ram_runtime_switch).replace("%p",
                                    getString(R.string.ram_runtime_2)))
                            t = 1
                            render1RAM.text = "128 MB"
                            render2RAM.text = "2 MB"
                            render3RAM.text = "512 KB"
                            render4RAM.text = "0.75"
                            render5RAM.text = "48 MB"
                            render6RAM.text = "o=v,m=y"

                            CoroutineScope(Dispatchers.Main).launch {
                                preferencesBuilder.putInt("RAMProfile", 1)

                                Core.set_heapsize("128m")
                                Core.set_maxfree("2m")
                                Core.set_minfree("512k")
                                Core.set_heaptargetutilization("0.75")
                                Core.set_heapgrowthlimit("48m")
                                Core.set_flags("o=v,m=v")
                            }
                        }
                        2 -> if (ram > 0.600) {
                            UI.success(getString(R.string.ram_runtime_switch).replace("%p",
                                    getString(R.string.ram_runtime_3)))
                            t = 2

                            render1RAM.text = "256 MB"
                            render2RAM.text = "4 MB"
                            render3RAM.text = "1 MB"
                            render4RAM.text = "0.75"
                            render5RAM.text = "64 MB"
                            render6RAM.text = "o=v,m=y,v=a"

                            CoroutineScope(Dispatchers.Main).launch {
                                preferencesBuilder.putInt("RAMProfile", 2)

                                Core.set_heapsize("256m")
                                Core.set_maxfree("4m")
                                Core.set_minfree("1m")
                                Core.set_heaptargetutilization("0.75")
                                Core.set_heapgrowthlimit("64m")
                                Core.set_flags("o=v,m=y,v=a")
                            }
                        } else {
                            UI.error(getString(R.string.ram_not_enough))
                        }
                        3 -> if (ram > 1.30) {
                            UI.success(getString(R.string.ram_runtime_switch).replace("%p",
                                    getString(R.string.ram_runtime_4)))

                            render1RAM.text = "512 MB"
                            render2RAM.text = "8 MB"
                            render3RAM.text = "2 MB"
                            render4RAM.text = "0.80"
                            render5RAM.text = "128 MB"
                            render6RAM.text = "v=n o=a"

                            CoroutineScope(Dispatchers.Main).launch {
                                preferencesBuilder.putInt("RAMProfile", 3)

                                Core.set_heapsize("512m")
                                Core.set_maxfree("8m")
                                Core.set_minfree("2m")
                                Core.set_heaptargetutilization("0.80")
                                Core.set_heapgrowthlimit("128m")
                                Core.set_flags("v=n,o=a")
                            }
                        } else {
                            UI.error(getString(R.string.ram_not_enough))
                        }
                        4 -> if (ram > 3.00) {
                            UI.success(getString(R.string.ram_runtime_switch).replace("%p",
                                    getString(R.string.ram_runtime_5)))

                            render1RAM.text = "1024 MB"
                            render2RAM.text = "32 MB"
                            render3RAM.text = "8 MB"
                            render4RAM.text = "0.80"
                            render5RAM.text = "128 MB"
                            render6RAM.text = "v=n o=a"

                            CoroutineScope(Dispatchers.Main).launch {
                                preferencesBuilder.putInt("RAMProfile", 4)

                                Core.set_heapsize("1024m")
                                Core.set_maxfree("32m")
                                Core.set_minfree("8m")
                                Core.set_heaptargetutilization("0.80")
                                Core.set_heapgrowthlimit("128m")
                                Core.set_flags("v=n,o=a")
                            }
                        } else {
                            UI.error(getString(R.string.ram_not_enough))
                        }
                    }

                    selectedValue[0] = t
                    preferencesBuilder.putInt("RAM1", t)
                    spinnerRAM1.setSelection(t)
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {}

        }

        CardRAM2.setOnClickListener { _ -> SwitchRAM2.performClick() }
        CardRAM3_1.setOnClickListener { _ -> Switch1RAM3.performClick() }
        CardRAM3_2.setOnClickListener { _ -> Switch2RAM3.performClick() }
        CardRAM6.setOnClickListener { _ -> SwitchRAM6.performClick() }
        CardRAM7.setOnClickListener { _ -> SwitchRAM7.performClick() }

        lmk_state.text = preferencesBuilder.getString("RAMRender", getString(R.string.ram_lmk_1))

        val d: String? = packageManager.getInstallerPackageName(packageName)
        if (d != null) {
            if (CryptoFactory.sha256(d) == "D79B77BC4C48DE2746DE9F43CFB9209C4EA8D27D38B5AD9260FF3F8EA06D4252") {
                Toasty.error(baseContext, getString(R.string.app_toast))
                shutdownApp()
                return
            }
        }

        CardRAM4.setOnClickListener { _ ->
            MaterialDialog.Builder(this@UIRam)
                    .title(R.string.ram_profile_dialog_title)
                    .items(R.array.lmk_items)
                    .itemsCallback { dialog, view, which, text ->
                        when (which) {
                            FrameworkSurface.DEFAULT -> {
                                lmk_state.text = getString(R.string.ram_lmk_1)
                                preferencesBuilder.putString("RAMRender", lmk_state.text.toString())
                                UI.success(getString(R.string.ram_lmk_switch))
                                Core.lmk(FrameworkSurface.DEFAULT, preferencesBuilder)
                            }
                            FrameworkSurface.VERY_LIGHT -> {
                                lmk_state.text = getString(R.string.ram_lmk_2).split("(")[0].trim()
                                preferencesBuilder.putString("RAMRender", lmk_state.text.toString())
                                UI.success(getString(R.string.ram_lmk_switch))
                                Core.lmk(FrameworkSurface.VERY_LIGHT, preferencesBuilder)
                            }
                            FrameworkSurface.LIGHT -> {
                                lmk_state.text = getString(R.string.ram_lmk_3).split("(")[0].trim()
                                preferencesBuilder.putString("RAMRender", lmk_state.text.toString())
                                UI.success(getString(R.string.ram_lmk_switch))
                                Core.lmk(FrameworkSurface.LIGHT, preferencesBuilder)
                            }
                            FrameworkSurface.NORMAL -> if (ram > 0.700) {
                                lmk_state.text = getString(R.string.ram_lmk_4).split("(")[0].trim()
                                preferencesBuilder.putString("RAMRender", lmk_state.text.toString())
                                UI.success(getString(R.string.ram_lmk_switch))
                                Core.lmk(FrameworkSurface.NORMAL, preferencesBuilder)
                            } else {
                                UI.error(getString(R.string.ram_not_enough))
                            }
                            FrameworkSurface.AGGRESSIVE -> if (ram > 1.20) {
                                lmk_state.text = getString(R.string.ram_lmk_5).split("(")[0].trim()
                                preferencesBuilder.putString("RAMRender", lmk_state.text.toString())
                                UI.success(getString(R.string.ram_lmk_switch))
                                Core.lmk(FrameworkSurface.AGGRESSIVE, preferencesBuilder)
                            } else {
                                UI.error(getString(R.string.ram_not_enough))
                            }
                            FrameworkSurface.VERY_AGGRESSIVE -> if (ram > 2.90) {
                                lmk_state.text = getString(R.string.ram_lmk_6).split("(")[0].trim()
                                preferencesBuilder.putString("RAMRender", lmk_state.text.toString())
                                UI.success(getString(R.string.ram_lmk_switch))
                                Core.lmk(FrameworkSurface.VERY_AGGRESSIVE, preferencesBuilder)
                            } else {
                                UI.error(getString(R.string.ram_not_enough))
                            }
                            FrameworkSurface.INSANE -> if (ram > 3.90) {
                                lmk_state.text = getString(R.string.ram_lmk_7).split("(")[0].trim()
                                preferencesBuilder.putString("RAMRender", lmk_state.text.toString())
                                UI.success(getString(R.string.ram_lmk_switch))
                                Core.lmk(FrameworkSurface.INSANE, preferencesBuilder)
                            } else {
                                UI.error(getString(R.string.ram_not_enough))
                            }
                        }
                    }
                    .show()
        }

        SwitchRAM2.setOnClickListener { _ ->
            if (SwitchRAM2.isChecked) {
                preferencesBuilder.putBoolean("RAM2", true)
                Core.set_memory_release(true)
                UI.on()
            } else {
                preferencesBuilder.putBoolean("RAM2", true)
                Core.set_memory_release(false)
                preferencesBuilder.putBoolean("RAM2", false)
                UI.off()
            }
        }

        Switch1RAM3.setOnClickListener { _ ->
            if (Switch1RAM3.isChecked) {
                Core.multitasking_patches(true)
                Core.tweak_touch_controls(true)
                preferencesBuilder.putBoolean("RAM3_S1", true)
                UI.on()
            } else {
                Core.multitasking_patches(false)
                preferencesBuilder.putBoolean("RAM3_S1", false)
                Core.tweak_touch_controls(false)
                UI.off()
            }
        }

        Switch2RAM3.setOnClickListener { _ ->
            if (Switch2RAM3.isChecked) {
                Core.boost_max_events(true)
                Core.set_ui_smooth(true)
                preferencesBuilder.putBoolean("RAM3_S2", true)
                UI.on()
            } else {
                Core.boost_max_events(true)
                Core.set_ui_smooth(false)
                preferencesBuilder.putBoolean("RAM3_S2", false)
                UI.off()
            }
        }

        SwitchRAM6.setOnClickListener { _ ->
            if (SwitchRAM6.isChecked) {
                preferencesBuilder.putBoolean("RAM6", true)
                Core.set_zram(true)
                UI.on()
            } else {
                preferencesBuilder.putBoolean("RAM6", false)
                Core.set_zram(false)
                UI.off()
            }
        }

        SwitchRAM7.setOnClickListener { _ ->
            if (SwitchRAM7.isChecked) {
                preferencesBuilder.putBoolean("RAM7", true)
                Core.home_app_adj(true)
                UI.on()
            } else {
                preferencesBuilder.putBoolean("RAM7", false)
                Core.home_app_adj(false)
                UI.off()
            }
        }


        SpinnerRAM0.setSelection(preferencesBuilder.getInt("RAM0", 0))
        SwitchRAM2.isChecked = preferencesBuilder.getBoolean("RAM2", false)
        Switch1RAM3.isChecked = preferencesBuilder.getBoolean("RAM3_S1", false)
        Switch2RAM3.isChecked = preferencesBuilder.getBoolean("RAM3_S2", false)
        SwitchRAM6.isChecked = preferencesBuilder.getBoolean("RAM6", false)
        SwitchRAM7.isChecked = preferencesBuilder.getBoolean("RAM7", false)

        val accentColor = ThemeStore.accentColor(this)
        val primaryColor = ThemeStore.primaryColor(this)
        val primaryDarkColor = ThemeStore.primaryColorDark(this)

        val collapsingToolbar = findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout)
        collapsingToolbar.title = title
        collapsingToolbar.setStatusBarScrimColor(primaryDarkColor)

        ATH.setActivityToolbarColor(this, toolbar, primaryColor)
        ATH.setBackgroundTint(collapsingToolbar, primaryColor)
        ATH.setBackgroundTint(fabTop!!, accentColor)
        ATH.setBackgroundTint(fabBottom!!, accentColor)
        toolbar.setBackgroundColor(primaryColor)

        ATH.setTint(SpinnerRAM0, accentColor)
        ATH.setTint(spinnerRAM1, accentColor)
        ATH.setTint(SwitchRAM2, accentColor)
        ATH.setTint(Switch1RAM3, accentColor)
        ATH.setTint(Switch2RAM3, accentColor)
        ATH.setTint(SwitchRAM6, accentColor)
        ATH.setTint(SwitchRAM7, accentColor)
        ATH.setTint(RAMBase, primaryColor)
    }

    fun updateWidget() {
        CoroutineScope(Dispatchers.Main).launch {
            val heap = HardwareCore.Companion.BUILD.heapsize

            CoroutineScope(Dispatchers.Main).launch {
                dashboard_ram_content.text = "" +
                        "${getString(R.string.ram_widget_ram)}: $ramString\n" +
                        "${getString(R.string.ram_widget_heap)}: $heap\n" +
                        "${getString(R.string.ram_widget_check)}: ${getString(R.string.action_ok)}"
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
        } catch (k: NullPointerException) {}
    }

    private fun setUpDrawer(toolbar: Toolbar) {
        DrawerBuilder().withActivity(this@UIRam).build()

        val DRAWER_DASHBOARD = PrimaryDrawerItem().withIdentifier(1).withName(R.string.drawer_dashboard).withIcon(R.drawable.dashboard).withOnDrawerItemClickListener { _, _, _ ->
            handleIntent(LaunchStruct.DASHBOARD_ACTIVITY)
            false
        }
        val DRAWER_CPU = PrimaryDrawerItem().withIdentifier(2).withName(R.string.drawer_cpu).withOnDrawerItemClickListener { _, _, _ ->
            handleIntent(LaunchStruct.CPU_ACTIVITY)
            false
        }
        val DRAWER_RAM = PrimaryDrawerItem().withIdentifier(3).withName(R.string.drawer_ram)
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
            LicenseManager.startProActivity(this@UIRam, this@UIRam, drawer)
            false
        }


        DRAWER_BACKUP = PrimaryDrawerItem().withIdentifier(19L).withName(R.string.drawer_backup).withOnDrawerItemClickListener { _, _, _ ->
            startActivity(Intent(this@UIRam, UIBackup::class.java))
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
        BaseActivity.setDrawerHeader(DrawerHeader.findViewById(R.id.Title), DrawerHeader.findViewById(R.id.Content), DrawerHeader.findViewById(R.id.Image), DrawerHeader.findViewById(R.id.RootLayout), this@UIRam, pro)


        if (pro) {
            drawer = DrawerBuilder()
                    .withActivity(this@UIRam)
                    .withToolbar(toolbar)
                    .addDrawerItems(
                            DRAWER_RAM,
                            DRAWER_DASHBOARD,
                            DividerDrawerItem(),
                            DRAWER_CPU,
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
                    .withActivity(this@UIRam)
                    .withToolbar(toolbar)
                    .addDrawerItems(
                            DRAWER_RAM,
                            DRAWER_DASHBOARD,
                            DividerDrawerItem(),
                            DRAWER_CPU,
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

        drawerInitialized = true
    }

    private fun handleIntent(ActivityID: Int) {
        LaunchManager.startActivity(ActivityID, this@UIRam)
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
                val UI = UI(this@UIRam)
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
                startActivity(Intent(this@UIRam, UIAbout::class.java))
                return true
            }
            R.id.menu_dashboard -> {
                startActivity(Intent(this@UIRam, UIDashboard::class.java))
                return true
            }
            R.id.menu_settings -> {
                startActivity(Intent(this@UIRam, UISettings::class.java))
                return true
            }
            R.id.menu_drawer -> {
                drawer.openDrawer()
                return true
            }
            R.id.menu_backup -> {
                startActivity(Intent(this@UIRam, UIBackup::class.java))
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
