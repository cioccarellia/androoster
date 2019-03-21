package com.andreacioccarelli.androoster.ui.base

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.LayoutAnimationController
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.andreacioccarelli.androoster.BuildConfig
import com.andreacioccarelli.androoster.R
import com.andreacioccarelli.androoster.core.FrameworkSurface
import com.andreacioccarelli.androoster.core.TerminalCore
import com.andreacioccarelli.androoster.dataset.KeyStore
import com.andreacioccarelli.androoster.tools.CryptoFactory
import com.andreacioccarelli.androoster.tools.GradientGenerator
import com.andreacioccarelli.androoster.tools.PreferencesBuilder
import com.andreacioccarelli.androoster.ui.settings.SettingStore
import com.andreacioccarelli.androoster.ui.settings.SettingsWrapper
import com.crashlytics.android.Crashlytics
import com.jrummyapps.android.shell.CommandResult
import com.kabouzeid.appthemehelper.ATH
import com.kabouzeid.appthemehelper.ThemeStore
import com.kabouzeid.appthemehelper.common.ATHToolbarActivity
import com.kabouzeid.appthemehelper.util.ColorUtil
import com.kabouzeid.appthemehelper.util.MaterialDialogsUtil
import org.jetbrains.anko.doAsync
import java.util.*
import kotlin.concurrent.schedule

/**
 * Created by andrea on 2018/mar.
 * Part of the package com.andreacioccarelli.androoster.ui.base
 */

@SuppressLint("Registered")
open class BaseActivity : ATHToolbarActivity(), FrameworkSurface {

    lateinit var preferencesBuilder: PreferencesBuilder
    var dark: Boolean = false
    var green: Int = 0
    var yellow: Int = 0
    var red: Int = 0
    private lateinit var settingsReader: SettingsWrapper

    public override fun onCreate(savedInstanceState: Bundle?) {
        preferencesBuilder = PreferencesBuilder(baseContext)

        initializeTheme()
        setNavigationBarColorAuto()

        initThemePreferences()
        setupMultitaskingTheme()
        initCrashlytics()

        super.onCreate(savedInstanceState)
        MaterialDialogsUtil.updateMaterialDialogsThemeSingleton(this)
    }

    @SuppressLint("ApplySharedPref")
    private fun initThemePreferences() {
        getSharedPreferences(PreferencesBuilder.ThemeFilename, 0).edit().putInt(SettingStore.THEME.THEME, SettingsWrapper.getInstance(this).generalTheme).commit()

        settingsReader = SettingsWrapper.getInstance(baseContext)
        dark = settingsReader.isDark

        green = ContextCompat.getColor(baseContext, if (dark) R.color.Green_400 else R.color.Green_500)
        yellow = ContextCompat.getColor(baseContext, if (dark) R.color.Orange_400 else R.color.Orange_700)
        red = ContextCompat.getColor(baseContext, if (dark) R.color.Red_400 else R.color.Red_500)
    }

    private fun initCrashlytics() {

        val hashBuilder = PreferencesBuilder(baseContext, PreferencesBuilder.Hashes, CryptoFactory.md5(KeyStore.hashedDecryptionKey))
        val p = if (preferencesBuilder.getBoolean("pro", false)) {
            hashBuilder.getString("encryptedKey", "0") == CryptoFactory.sha256(CryptoFactory.sha1(preferencesBuilder.getString("baseKey", "1")))
        } else false

        doAsync {
            Crashlytics.setBool("is_dark_mode_enabled", dark)
            Crashlytics.setBool("107", p)
        }
    }

    private fun initializeTheme() {
        if (!ThemeStore.isConfigured(this, 1)) {
            ThemeStore.editTheme(this)
                    .activityTheme(R.style.Theme_Androoster_Light)
                    .coloredNavigationBar(preferencesBuilder.getBoolean("coloredNavigationBar", false))
                    .primaryColorRes(R.color.primary)
                    .statusBarColor(ContextCompat.getColor(baseContext, R.color.primary))
                    .accentColorRes(R.color.accent)
                    .commit()
            Crashlytics.setBool("is_first_time", true)
        } else {
            Crashlytics.setBool("is_first_time", false)
        }
    }

    private fun setupMultitaskingTheme() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            val taskDescription: ActivityManager.TaskDescription
            val icon = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
            taskDescription = ActivityManager.TaskDescription(getString(R.string.app_name), icon, ThemeStore.primaryColor(baseContext))
            setTaskDescription(taskDescription)
        }
    }

    fun animateContent(panel: ViewGroup) {
        if (preferencesBuilder.getPreferenceBoolean(SettingStore.ANIMATIONS.ENABLE_ANIMATIONS, true)) {
            val set = AnimationSet(true)

            lateinit var animation: Animation

            when (preferencesBuilder.getPreferenceString(SettingStore.ANIMATIONS.ANIMATION_ORIENTATION, "2")) {
                "0" -> {
                    animation = TranslateAnimation(
                            Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f
                    )
                }
                "1" -> {
                    animation = TranslateAnimation(
                            Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f
                    )
                }
                "2" -> {
                    animation = TranslateAnimation(
                            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                            Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f
                    )
                }
                "3" -> {
                    animation = TranslateAnimation(
                            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                            Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f
                    )
                }

                else -> return
            }
            set.addAnimation(animation)


            when (preferencesBuilder.getPreferenceString(SettingStore.ANIMATIONS.ANIMATION_SPEED, "1")) {
                "0" -> animation.duration = 500
                "1" -> animation.duration = 300
                "2" -> animation.duration = 250
                "3" -> animation.duration = 107
                else -> return
            }


            val controller = LayoutAnimationController(set, 0.25f)
            panel.layoutAnimation = controller
        }
    }

    fun run(c: String): CommandResult {
        return TerminalCore.run(c)
    }

    fun run(c: Array<String>): CommandResult {
        return TerminalCore.run(*c)
    }

    protected fun setDrawUnderStatusbar(drawUnderStatusbar: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    }

    fun setStatusbarColor(color: Int) {
        if (Build.VERSION.SDK_INT >= 21) {
            window.statusBarColor = ColorUtil.darkenColor(color)
            setLightStatusbarAuto(color)
        }
    }

    fun setStatusbarColorAuto() {
        setStatusbarColor(ThemeStore.primaryColor(this))
    }

    private fun setTaskDescriptionColor(@ColorInt color: Int) {
        ATH.setTaskDescriptionColor(this, color)
    }

    fun setTaskDescriptionColorAuto() {
        setTaskDescriptionColor(ThemeStore.primaryColor(this))
    }

    private fun setNavigationBarColor(color: Int) {
        if (ThemeStore.coloredNavigationBar(this)) {
            ATH.setNavigationbarColor(this, color)
        } else {
            ATH.setNavigationbarColor(this, Color.BLACK)
        }
    }

    fun setNavigationBarColorAuto() {
        setNavigationBarColor(ThemeStore.navigationBarColor(this))
    }

    private fun setLightStatusbar(enabled: Boolean) {
        ATH.setLightStatusbar(this, enabled)
    }

    private fun setLightStatusbarAuto(bgColor: Int) {
        setLightStatusbar(ColorUtil.isColorLight(bgColor))
    }

    companion object {
        @SuppressLint("SetTextI18n")
        fun setDrawerHeader(title: TextView, content: TextView, Image: ImageView, rl: RelativeLayout, ctx: Context, pro: Boolean) {
            title.text = ctx.getString(R.string.app_name)
            when {
                pro -> content.text = ctx.getString(R.string.app_name_pro_version)
                else -> content.text = ctx.getString(R.string.app_name_normal_version)
            }
            rl.background = GradientGenerator.get(ctx)
        }
    }


    fun closeApp() {
            finishAffinity()
            Timer().schedule(107){
                System.exit(0)
            }
    }


    fun shutdownApp() {
        finishAffinity()
        System.exit(1)
        android.os.Process.killProcess(android.os.Process.myPid())
        run("am force-stop $packageName")
    }

    val isDebug: Boolean
    get() {
        return BuildConfig.DEBUG || BuildConfig.BUILD_TYPE == "debug"
    }


    @Suppress("unused")
    fun isCompatibility(): Boolean {
        return BuildConfig.COMPATIBILITY_MODE
    }

    @Suppress("unused")
    fun isTesting(): Boolean {
        return BuildConfig.TESTING_RELEASE
    }

    fun isPackageInstalled(packagename: String): Boolean {
        return try {
            baseContext.packageManager.getPackageInfo(packagename, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    @SuppressLint("HardwareIds")
    fun getDeviceSerial(ctx: Context): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED) {
            Build.getSerial()
        } else {
            Build.SERIAL
        }
    }

    fun openStore() {
        val link: Uri = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
        startActivity(Intent(Intent.ACTION_VIEW).setData(link))
    }

    fun openStoreForBusybox() {
        startActivity(Intent(Intent.ACTION_VIEW).setData(
                Uri.parse("https://play.google.com/store/search?q=busybox%20installer")))
    }
}
