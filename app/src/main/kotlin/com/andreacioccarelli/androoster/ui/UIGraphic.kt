package com.andreacioccarelli.androoster.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v7.widget.AppCompatSeekBar
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.andreacioccarelli.androoster.tools.LicenseManager

import com.andreacioccarelli.androoster.core.Core
import com.andreacioccarelli.androoster.dataset.XmlKeys
import com.andreacioccarelli.androoster.ui.dashboard.RecentWidget
import com.andreacioccarelli.androoster.ui.settings.SettingsReflector
import com.andreacioccarelli.androoster.tools.FabManager
import com.andreacioccarelli.androoster.R
import com.andreacioccarelli.androoster.core.HardwareCore
import com.andreacioccarelli.androoster.core.TerminalCore
import com.andreacioccarelli.androoster.dataset.KeyStore
import com.andreacioccarelli.androoster.ui.settings.SettingStore
import com.andreacioccarelli.androoster.tools.LaunchStruct
import com.andreacioccarelli.androoster.interfaces.Governors
import com.andreacioccarelli.androoster.tools.*
import com.andreacioccarelli.androoster.ui.about.UIAbout
import com.andreacioccarelli.androoster.ui.backup.UIBackup
import com.andreacioccarelli.androoster.ui.base.BaseActivity
import com.andreacioccarelli.androoster.ui.dashboard.UIDashboard
import com.andreacioccarelli.androoster.ui.settings.UISettings
import com.kabouzeid.appthemehelper.ATH
import com.kabouzeid.appthemehelper.ThemeStore
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import kotlinx.android.synthetic.main.graph.*
import kotlinx.android.synthetic.main.graph_content.*
import org.jetbrains.anko.vibrator
import java.util.*
import kotlin.concurrent.schedule

class UIGraphic : BaseActivity(), NavigationView.OnNavigationItemSelectedListener, Governors, LaunchStruct {

    internal var pro: Boolean = false
    internal var drawerInitialized = false
    internal var doubleBackToExitPressedOnce = false
    lateinit var UI: UI
    lateinit var DRAWER_SETTINGS: PrimaryDrawerItem
    lateinit var DRAWER_BACKUP: PrimaryDrawerItem
    lateinit var drawer: Drawer
    var menu: Menu? = null

    var animScaleWindow = ""
    var animScaleTransition = ""
    var animScaleAnimator = ""

    object AnimationStates {
        val STATE_0x0 = "0.0"
        val STATE_0x5 = "0.5"
        val STATE_1x0 = "1.0"
        val STATE_1x5 = "1.5"
        val STATE_2x0 = "2.0"
        val STATE_2x5 = "2.5"
        val STATE_3x0 = "3.0"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.graph)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        RecentWidget.collect(this@UIGraphic, LaunchStruct.GRAPHICS_ACTIVITY)

        UI = UI(this@UIGraphic)
        pro = PreferencesBuilder(this, PreferencesBuilder.defaultFilename).getBoolean("pro", false)

        CoroutineScope(Dispatchers.Main).launch {
            animScaleWindow = TerminalCore.SETTINGS.get("global", "window_animation_scale")
            animScaleTransition = TerminalCore.SETTINGS.get("global", "transition_animation_scale")
            animScaleAnimator = TerminalCore.SETTINGS.get("global", "animator_duration_scale")
        }

        val accentColor = ThemeStore.accentColor(this@UIGraphic)
        val primaryColor = ThemeStore.primaryColor(this@UIGraphic)
        val primaryDarkColor = ThemeStore.primaryColorDark(this@UIGraphic)

        preferencesBuilder = PreferencesBuilder(this@UIGraphic, PreferencesBuilder.defaultFilename)

        preferencesBuilder.putInt(XmlKeys.LAST_OPENED, LaunchStruct.GRAPHICS_ACTIVITY)
        setUpDrawer(toolbar)
        FabManager.setup(fabTop, fabBottom, this@UIGraphic, drawer, preferencesBuilder)

        CardGraph1.setOnClickListener { _ -> SwitchGraph1.performClick() }
        CardGraph2.setOnClickListener { _ -> SwitchGraph2.performClick() }
        CardGraph3.setOnClickListener { _ -> SwitchGraph3.performClick() }
        CardGraph4.setOnClickListener { _ -> SwitchGraph4.performClick() }
        CardGraph5.setOnClickListener { _ -> SwitchGraph5.performClick() }
        cardAnimations.setOnClickListener { _ -> buttonAnimations.performClick() }


        buttonAnimations.setOnClickListener {
            if (animScaleAnimator.isEmpty() || animScaleTransition.isEmpty() || animScaleAnimator.isEmpty()) {
                UI.warning(getString(R.string.widget_loading))
                return@setOnClickListener
            }
            val dialog = MaterialDialog.Builder(this)
                    .title(R.string.graphic_anim_title)
                    .customView(R.layout.edit_seekbar, true)
                    .cancelListener {
                        UI.success(getString(R.string.reboot_to_take_effect))
                    }
                    .build()

            val seekBar1 = dialog.customView?.findViewById(R.id.seekBar1) as AppCompatSeekBar
            val seekBar2 = dialog.customView?.findViewById(R.id.seekBar2) as AppCompatSeekBar
            val seekBar3 = dialog.customView?.findViewById(R.id.seekBar3) as AppCompatSeekBar

            val textView1 = dialog.customView?.findViewById(R.id.seek1_textview) as TextView
            val textView2 = dialog.customView?.findViewById(R.id.seek2_textview) as TextView
            val textView3 = dialog.customView?.findViewById(R.id.seek3_textview) as TextView

            val dfuText1 = textView1.text.toString()
            val dfuText2 = textView2.text.toString()
            val dfuText3 = textView3.text.toString()

            textView1.text = "$dfuText1 ($animScaleWindow)"
            textView2.text = "$dfuText2 ($animScaleTransition)"
            textView3.text = "$dfuText3 ($animScaleAnimator)"

            seekBar1.max = 6
            seekBar2.max = 6
            seekBar3.max = 6

            when (animScaleWindow) {
                AnimationStates.STATE_0x0 -> seekBar1.progress = 0
                AnimationStates.STATE_0x5 -> seekBar1.progress = 1
                AnimationStates.STATE_1x0 -> seekBar1.progress = 2
                AnimationStates.STATE_1x5 -> seekBar1.progress = 3
                AnimationStates.STATE_2x0 -> seekBar1.progress = 4
                AnimationStates.STATE_2x5 -> seekBar1.progress = 5
                AnimationStates.STATE_3x0 -> seekBar1.progress = 6
                else -> seekBar1.progress = 0
            }

            when (animScaleTransition) {
                AnimationStates.STATE_0x0 -> seekBar2.progress = 0
                AnimationStates.STATE_0x5 -> seekBar2.progress = 1
                AnimationStates.STATE_1x0 -> seekBar2.progress = 2
                AnimationStates.STATE_1x5 -> seekBar2.progress = 3
                AnimationStates.STATE_2x0 -> seekBar2.progress = 4
                AnimationStates.STATE_2x5 -> seekBar2.progress = 5
                AnimationStates.STATE_3x0 -> seekBar2.progress = 6
                else -> seekBar2.progress = 0
            }

            when (animScaleAnimator) {
                AnimationStates.STATE_0x0 -> seekBar3.progress = 0
                AnimationStates.STATE_0x5 -> seekBar3.progress = 1
                AnimationStates.STATE_1x0 -> seekBar3.progress = 2
                AnimationStates.STATE_1x5 -> seekBar3.progress = 3
                AnimationStates.STATE_2x0 -> seekBar3.progress = 4
                AnimationStates.STATE_2x5 -> seekBar3.progress = 5
                AnimationStates.STATE_3x0 -> seekBar3.progress = 6
                else -> seekBar3.progress = 0
            }


            seekBar1.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    when (seekBar!!.progress) {
                        0 -> {
                            animScaleWindow = AnimationStates.STATE_0x0
                            TerminalCore.SETTINGS.put("global", "window_animation_scale", AnimationStates.STATE_0x0)
                        }
                        1 -> {
                            animScaleWindow = AnimationStates.STATE_0x5
                            TerminalCore.SETTINGS.put("global", "window_animation_scale", AnimationStates.STATE_0x5)
                        }
                        2 -> {
                            animScaleWindow = AnimationStates.STATE_1x0
                            TerminalCore.SETTINGS.put("global", "window_animation_scale", AnimationStates.STATE_1x0)
                        }
                        3 -> {
                            animScaleWindow = AnimationStates.STATE_1x5
                            TerminalCore.SETTINGS.put("global", "window_animation_scale", AnimationStates.STATE_1x5)
                        }
                        4 -> {
                            animScaleWindow = AnimationStates.STATE_2x0
                            TerminalCore.SETTINGS.put("global", "window_animation_scale", AnimationStates.STATE_2x0)
                        }
                        5 -> {
                            animScaleWindow = AnimationStates.STATE_2x5
                            TerminalCore.SETTINGS.put("global", "window_animation_scale", AnimationStates.STATE_2x5)
                        }
                        6 -> {
                            animScaleWindow = AnimationStates.STATE_3x0
                            TerminalCore.SETTINGS.put("global", "window_animation_scale", AnimationStates.STATE_3x0)
                        }
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    vibrator.vibrate(30)
                }

                @SuppressLint("SetTextI18n")
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    vibrator.vibrate(30)
                    when (progress) {
                        0 -> textView1.text = "$dfuText1 (${AnimationStates.STATE_0x0})"
                        1 -> textView1.text = "$dfuText1 (${AnimationStates.STATE_0x5})"
                        2 -> textView1.text = "$dfuText1 (${AnimationStates.STATE_1x0})"
                        3 -> textView1.text = "$dfuText1 (${AnimationStates.STATE_1x5})"
                        4 -> textView1.text = "$dfuText1 (${AnimationStates.STATE_2x0})"
                        5 -> textView1.text = "$dfuText1 (${AnimationStates.STATE_2x5})"
                        6 -> textView1.text = "$dfuText1 (${AnimationStates.STATE_3x0})"
                    }}
            })


            seekBar2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    when (seekBar!!.progress) {
                        0 -> {
                            animScaleTransition = AnimationStates.STATE_0x0
                            TerminalCore.SETTINGS.put("global", "transition_animation_scale", AnimationStates.STATE_0x0)
                        }
                        1 -> {
                            animScaleTransition = AnimationStates.STATE_0x5
                            TerminalCore.SETTINGS.put("global", "transition_animation_scale", AnimationStates.STATE_0x5)
                        }
                        2 -> {
                            animScaleTransition = AnimationStates.STATE_1x0
                            TerminalCore.SETTINGS.put("global", "transition_animation_scale", AnimationStates.STATE_1x0)
                        }
                        3 -> {
                            animScaleTransition = AnimationStates.STATE_1x5
                            TerminalCore.SETTINGS.put("global", "transition_animation_scale", AnimationStates.STATE_1x5)
                        }
                        4 -> {
                            animScaleTransition = AnimationStates.STATE_2x0
                            TerminalCore.SETTINGS.put("global", "transition_animation_scale", AnimationStates.STATE_2x0)
                        }
                        5 -> {
                            animScaleTransition = AnimationStates.STATE_2x5
                            TerminalCore.SETTINGS.put("global", "transition_animation_scale", AnimationStates.STATE_2x5)
                        }
                        6 -> {
                            animScaleTransition = AnimationStates.STATE_3x0
                            TerminalCore.SETTINGS.put("global", "transition_animation_scale", AnimationStates.STATE_3x0)
                        }
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    vibrator.vibrate(30)
                }

                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    vibrator.vibrate(30)
                    when (progress) {
                        0 -> textView2.text = "$dfuText2 (${AnimationStates.STATE_0x0})"
                        1 -> textView2.text = "$dfuText2 (${AnimationStates.STATE_0x5})"
                        2 -> textView2.text = "$dfuText2 (${AnimationStates.STATE_1x0})"
                        3 -> textView2.text = "$dfuText2 (${AnimationStates.STATE_1x5})"
                        4 -> textView2.text = "$dfuText2 (${AnimationStates.STATE_2x0})"
                        5 -> textView2.text = "$dfuText2 (${AnimationStates.STATE_2x5})"
                        6 -> textView2.text = "$dfuText2 (${AnimationStates.STATE_3x0})"
                    }}
            })


            seekBar3.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    when (seekBar!!.progress) {
                        0 -> {
                            animScaleAnimator = AnimationStates.STATE_0x0
                            TerminalCore.SETTINGS.put("global", "animator_duration_scale", AnimationStates.STATE_0x0)
                        }
                        1 -> {
                            animScaleAnimator = AnimationStates.STATE_0x5
                            TerminalCore.SETTINGS.put("global", "animator_duration_scale", AnimationStates.STATE_0x5)
                        }
                        2 -> {
                            animScaleAnimator = AnimationStates.STATE_1x0
                            TerminalCore.SETTINGS.put("global", "animator_duration_scale", AnimationStates.STATE_1x0)
                        }
                        3 -> {
                            animScaleAnimator = AnimationStates.STATE_1x5
                            TerminalCore.SETTINGS.put("global", "animator_duration_scale", AnimationStates.STATE_1x5)
                        }
                        4 -> {
                            animScaleAnimator = AnimationStates.STATE_2x0
                            TerminalCore.SETTINGS.put("global", "animator_duration_scale", AnimationStates.STATE_2x0)
                        }
                        5 -> {
                            animScaleAnimator = AnimationStates.STATE_2x5
                            TerminalCore.SETTINGS.put("global", "animator_duration_scale", AnimationStates.STATE_2x5)
                        }
                        6 -> {
                            animScaleAnimator = AnimationStates.STATE_3x0
                            TerminalCore.SETTINGS.put("global", "animator_duration_scale", AnimationStates.STATE_3x0)
                        }
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    vibrator.vibrate(30)
                }

                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    vibrator.vibrate(30)
                    when (progress) {
                        0 -> textView3.text = "$dfuText3 (${AnimationStates.STATE_0x0})"
                        1 -> textView3.text = "$dfuText3 (${AnimationStates.STATE_0x5})"
                        2 -> textView3.text = "$dfuText3 (${AnimationStates.STATE_1x0})"
                        3 -> textView3.text = "$dfuText3 (${AnimationStates.STATE_1x5})"
                        4 -> textView3.text = "$dfuText3 (${AnimationStates.STATE_2x0})"
                        5 -> textView3.text = "$dfuText3 (${AnimationStates.STATE_2x5})"
                        6 -> textView3.text = "$dfuText3 (${AnimationStates.STATE_3x0})"
                    }
                }
            })

            ATH.setTint(seekBar1, accentColor)
            ATH.setTint(seekBar2, accentColor)
            ATH.setTint(seekBar3, accentColor)
            dialog.show()
        }

        SwitchGraph1.setOnClickListener {
            if (SwitchGraph2.isChecked) {
                preferencesBuilder.putBoolean("GPU1", true)
                Core.gpu_boost(true)
            } else {
                preferencesBuilder.putBoolean("GPU1", false)
                Core.gpu_boost(false)
            }
        }

        SwitchGraph2.setOnClickListener { _ ->
            if (SwitchGraph3.isChecked) {
                Core.hardware_acceleration(true)
                preferencesBuilder.putBoolean("GPU2", true)
            } else {
                Core.hardware_acceleration(false)
                preferencesBuilder.putBoolean("GPU2", false)
            }
        }

        SwitchGraph3.setOnClickListener { _ ->
            if (SwitchGraph4.isChecked) {
                Core.set_stagefright(true)
                preferencesBuilder.putBoolean("GPU3", true)
            } else {
                Core.set_stagefright(false)
                preferencesBuilder.putBoolean("GPU3", false)
            }
        }

        SwitchGraph4.setOnClickListener { _ ->
            if (SwitchGraph5.isChecked) {
                Core.drawing_with_gpu(true)
                preferencesBuilder.putBoolean("GPU4", true)
            } else {
                Core.drawing_with_gpu(false)
                preferencesBuilder.putBoolean("GPU4", false)
            }
        }

        SwitchGraph5.setOnClickListener { _ ->
            if (SwitchGraph5.isChecked) {
                Core.tweak_jpeg(true)
                preferencesBuilder.putBoolean("GPU5", true)
            } else {
                Core.tweak_jpeg(false)
                preferencesBuilder.putBoolean("GPU5", false)
            }
        }
        
        SwitchGraph1.isChecked = preferencesBuilder.getBoolean("GPU1", false)
        SwitchGraph2.isChecked = preferencesBuilder.getBoolean("GPU2", false)
        SwitchGraph3.isChecked = preferencesBuilder.getBoolean("GPU3", false)
        SwitchGraph4.isChecked = preferencesBuilder.getBoolean("GPU4", false)
        SwitchGraph5.isChecked = preferencesBuilder.getBoolean("GPU5", false)

        createWidget()
        animateContent(content as ViewGroup)

        toolbar_layout.title = title
        toolbar_layout.setStatusBarScrimColor(primaryDarkColor)

        ATH.setActivityToolbarColor(this, toolbar, primaryColor)
        ATH.setBackgroundTint(toolbar_layout, primaryColor)
        ATH.setBackgroundTint(fabTop, accentColor)
        ATH.setBackgroundTint(fabBottom, accentColor)
        toolbar.setBackgroundColor(primaryColor)

        ATH.setTint(SwitchGraph1, accentColor)
        ATH.setTint(SwitchGraph2, accentColor)
        ATH.setTint(SwitchGraph3, accentColor)
        ATH.setTint(SwitchGraph4, accentColor)
        ATH.setTint(SwitchGraph5, accentColor)
        ATH.setTint(buttonAnimations, accentColor)
        ATH.setTint(graphBase, primaryColor)
    }

    @SuppressLint("SetTextI18n")
    private fun createWidget() {
        CoroutineScope(Dispatchers.Main).launch {
            val glversion = HardwareCore.getGLVersion(this@UIGraphic)

            CoroutineScope(Dispatchers.Main).launch {
                dashboard_graph_content.text =
                        getString(R.string.graphic_widget_opengl) + " $glversion.0\n" +
                        getString(R.string.graphic_widget_resolution) + " ${resources.displayMetrics.heightPixels}${'x'}${resources.displayMetrics.widthPixels}\n" +
                        getString(R.string.graphic_widget_density) + " ${(resources.displayMetrics.density * 160f).toInt()}${getString(R.string.graphic_dpi_suffix)}"
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

        DrawerBuilder().withActivity(this@UIGraphic).build()

        val DRAWER_DASHBOARD = PrimaryDrawerItem().withIdentifier(1).withName(R.string.drawer_dashboard).withOnDrawerItemClickListener { _, _, _ ->
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
        val DRAWER_GRAPHICS = PrimaryDrawerItem().withIdentifier(13).withName(R.string.drawer_graphics)
        val DRAWER_ABOUT = PrimaryDrawerItem().withIdentifier(14).withName(R.string.drawer_about).withOnDrawerItemClickListener { _, _, _ ->
            handleIntent(LaunchStruct.ABOUT_ACTIVITY)
            false
        }
        val DRAWER_BUY_PRO_VERSION = PrimaryDrawerItem().withIdentifier(15).withName(R.string.drawer_pro).withOnDrawerItemClickListener { _, _, _ ->
            LicenseManager.startProActivity(this@UIGraphic, this@UIGraphic, drawer)
            false
        }


        DRAWER_BACKUP = PrimaryDrawerItem().withIdentifier(19L).withName(R.string.drawer_backup).withOnDrawerItemClickListener { _, _, _ ->
            startActivity(Intent(this@UIGraphic, UIBackup::class.java))
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
        BaseActivity.setDrawerHeader(DrawerHeader.findViewById(R.id.Title), DrawerHeader.findViewById(R.id.Content), DrawerHeader.findViewById(R.id.Image), DrawerHeader.findViewById(R.id.RootLayout), this@UIGraphic, pro)


        if (pro) {
            drawer = DrawerBuilder()
                    .withActivity(this@UIGraphic)
                    .withToolbar(toolbar)
                    .addDrawerItems(
                            DRAWER_GRAPHICS,
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
                            DividerDrawerItem(),
                            DRAWER_BACKUP,
                            DRAWER_ABOUT,
                            DRAWER_SETTINGS
                    )
                    .withHeader(DrawerHeader)
                    .build()
        } else {
            drawer = DrawerBuilder()
                    .withActivity(this@UIGraphic)
                    .withToolbar(toolbar)
                    .addDrawerItems(
                            DRAWER_GRAPHICS,
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
                            DividerDrawerItem(),
                            DRAWER_BACKUP,
                            DRAWER_ABOUT,
                            DRAWER_SETTINGS
                    )
                    .withHeader(DrawerHeader)
                    .build()
        }
        drawerInitialized = true
    }

    internal fun handleIntent(ActivityID: Int) {
        LaunchManager.startActivity(ActivityID, this@UIGraphic)
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
                val UI = UI(this@UIGraphic)
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
                startActivity(Intent(this@UIGraphic, UIAbout::class.java))
                return true
            }
            R.id.menu_dashboard -> {
                startActivity(Intent(this@UIGraphic, UIDashboard::class.java))
                return true
            }
            R.id.menu_settings -> {
                startActivity(Intent(this@UIGraphic, UISettings::class.java))
                return true
            }
            R.id.menu_drawer -> {
                drawer.openDrawer()
                return true
            }
            R.id.menu_backup -> {
                startActivity(Intent(this@UIGraphic, UIBackup::class.java))
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
