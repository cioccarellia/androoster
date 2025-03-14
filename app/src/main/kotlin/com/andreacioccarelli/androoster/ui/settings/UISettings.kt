package com.andreacioccarelli.androoster.ui.settings

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.annotation.ColorInt
import android.support.v4.content.ContextCompat
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import android.support.v7.preference.*
import android.support.v7.widget.Toolbar
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.ColorChooserDialog
import com.andreacioccarelli.androoster.R
import com.andreacioccarelli.androoster.dataset.KeyStore
import com.andreacioccarelli.androoster.dataset.XmlKeys
import com.andreacioccarelli.androoster.tools.CryptoFactory
import com.andreacioccarelli.androoster.tools.LaunchStruct
import com.andreacioccarelli.androoster.tools.PreferencesBuilder
import com.andreacioccarelli.androoster.tools.UI
import com.andreacioccarelli.androoster.ui.base.BaseActivity
import com.andreacioccarelli.androoster.ui.upgrade.UIUpgrade
import com.kabouzeid.appthemehelper.ATH
import com.kabouzeid.appthemehelper.ThemeStore
import com.kabouzeid.appthemehelper.common.prefs.supportv7.ATEColorPreference
import com.kabouzeid.appthemehelper.common.prefs.supportv7.ATEListPreference
import com.kabouzeid.appthemehelper.common.prefs.supportv7.ATEPreferenceFragmentCompat
import com.kabouzeid.appthemehelper.util.ColorUtil
import es.dmoral.toasty.Toasty
import java.util.*

class UISettings : BaseActivity(), ColorChooserDialog.ColorCallback, LaunchStruct {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preferences)

        PreferencesBuilder(this@UISettings, PreferencesBuilder.SettingsFilename).putInt("last_opened", LaunchStruct.DASHBOARD_ACTIVITY)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        setStatusbarColorAuto()
        setNavigationBarColorAuto()

        pro = PreferencesBuilder(this, PreferencesBuilder.defaultFilename).getBoolean("pro", false)

        toolbar.setBackgroundColor(ThemeStore.primaryColor(this))
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.content_frame, SettingsFragment()).commit()
        } else {
            val frag = supportFragmentManager.findFragmentById(R.id.content_frame) as SettingsFragment
            frag.invalidateSettings()
        }
    }

    override fun onColorSelection(dialog: ColorChooserDialog, @ColorInt selectedColor: Int) {
        when (dialog.title) {
            R.string.primary_color -> com.kabouzeid.appthemehelper.ThemeStore.editTheme(this)
                    .primaryColor(selectedColor)
                    .statusBarColor(selectedColor)
                    .navigationBarColor(selectedColor)
                    .commit()
            R.string.accent_color -> com.kabouzeid.appthemehelper.ThemeStore.editTheme(this)
                    .accentColor(selectedColor)
                    .commit()
        }
        preferencesBuilder.putBoolean("usingCustomTheme", true)

        recreate()
    }

    override fun onColorChooserDismissed(dialog: ColorChooserDialog) {}


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.upgrade, menu)
        menu.getItem(0).isVisible = !pro
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when (id) {
            R.id.menu_upgrade -> {
                startActivity(Intent(this@UISettings, UIUpgrade::class.java))
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }


    class SettingsFragment : ATEPreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

        private lateinit var setupDialog: MaterialDialog
        internal lateinit var signerProgress: ProgressBar
        private var working = false
        private var casaDiCarta = false

        private lateinit var AvailableMethodsSpinner: android.support.v7.widget.AppCompatSpinner
        internal lateinit var auth_setup_password: android.support.design.widget.TextInputEditText
        internal lateinit var auth_setup_confirm: android.support.design.widget.TextInputEditText
        internal lateinit var auth_setup_hint: android.support.design.widget.TextInputEditText
        internal lateinit var auth_setup_password_layout: android.support.design.widget.TextInputLayout
        internal lateinit var auth_setup_confirm_layout: android.support.design.widget.TextInputLayout
        internal lateinit var auth_setup_hint_layout: android.support.design.widget.TextInputLayout


        override fun onCreatePreferences(bundle: Bundle?, s: String?) {
            addPreferencesFromResource(R.xml.pref_general)
            addPreferencesFromResource(R.xml.pref_animations)
            addPreferencesFromResource(R.xml.pref_theme)
            addPreferencesFromResource(R.xml.pref_menu)
            addPreferencesFromResource(R.xml.pref_security)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            listView.setPadding(0, 0, 0, 0)

            pro = PreferencesBuilder(context, PreferencesBuilder.defaultFilename).getBoolean("pro", false)

            invalidateSettings()
            SettingsWrapper.getInstance(activity!!).registerOnSharedPreferenceChangedListener(this)
        }

        override fun onDestroyView() {
            super.onDestroyView()
            SettingsWrapper.getInstance(activity!!).unregisterOnSharedPreferenceChangedListener(this)
        }

        fun invalidateSettings() {
            val UI = UI(context!!)

            val preferencesBuilder = PreferencesBuilder(context, PreferencesBuilder.defaultFilename)
            val defaultStartPage = findPreference(SettingStore.GENERAL.START_PAGE)
            setSummary(defaultStartPage)
            defaultStartPage.setOnPreferenceChangeListener { _, o ->
                setSummary(defaultStartPage, o)
                true
            }

            val generalTheme = findPreference(SettingStore.THEME.THEME) as ATEListPreference
            setSummary(generalTheme)
            generalTheme.setOnPreferenceChangeListener { _, o ->
                setSummary(generalTheme, o)
                com.kabouzeid.appthemehelper.ThemeStore.editTheme(activity!!)
                        .activityTheme(SettingsWrapper.getThemeResFromPrefValue(o.toString()))
                        .commit()

                preferencesBuilder.putBoolean(XmlKeys.DARK_THEME_APPLIED, o.toString().contains("dark"))
                activity!!.recreate()
                true
            }


            val primaryColorPref = findPreference(SettingStore.THEME.PRIMARY_COLOR) as ATEColorPreference
            val primaryColor = com.kabouzeid.appthemehelper.ThemeStore.primaryColor(activity!!)
            primaryColorPref.setColor(primaryColor, ColorUtil.darkenColor(primaryColor))
            primaryColorPref.setOnPreferenceClickListener { _ ->
                ColorChooserDialog.Builder(activity!!, R.string.primary_color)
                        .accentMode(false)
                        .allowUserColorInput(true)
                        .allowUserColorInputAlpha(false)
                        .preselect(primaryColor)
                        .show(fragmentManager)
                true
            }

            val accentColorPref = findPreference(SettingStore.THEME.ACCENT_COLOR) as ATEColorPreference
            val accentColor = com.kabouzeid.appthemehelper.ThemeStore.accentColor(activity!!)
            accentColorPref.setColor(accentColor, ColorUtil.darkenColor(accentColor))
            accentColorPref.setOnPreferenceClickListener { _ ->
                ColorChooserDialog.Builder(activity!!, R.string.accent_color)
                        .accentMode(true)
                        .allowUserColorInput(false)
                        .allowUserColorInputAlpha(false)
                        .preselect(accentColor)
                        .show(fragmentManager)
                true
            }

            val buttonPosition = findPreference(SettingStore.GENERAL.OPEN_DRAWER_FAB_POSITION)
            setSummary(buttonPosition)
            buttonPosition.setOnPreferenceChangeListener { _, o ->
                setSummary(buttonPosition, o)
                true
            }

            val colorNavBar = findPreference(SettingStore.THEME.TINT_NAVIGATION_BAR) as TwoStatePreference
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                colorNavBar.isVisible = false
            } else {
                colorNavBar.isChecked = com.kabouzeid.appthemehelper.ThemeStore.coloredNavigationBar(activity!!)
                colorNavBar.setOnPreferenceChangeListener { _, newValue ->
                    ThemeStore.editTheme(activity!!)
                            .coloredNavigationBar(newValue as Boolean)
                            .navigationBarColor(com.kabouzeid.appthemehelper.ThemeStore.primaryColor(context!!))
                            .commit()
                    activity!!.recreate()

                    preferencesBuilder.putBoolean("coloredNavigationBar", newValue)
                    true
                }
            }

            val builtinTheme = findPreference(SettingStore.THEME.BUILT_IN_THEME)
            if (pro) {
                builtinTheme.isEnabled = true
                if (preferencesBuilder.getBoolean("usingCustomTheme", true)) {
                    builtinTheme.summary = "Custom"
                }
            } else {
                builtinTheme.isEnabled = false
                builtinTheme.summary = getString(R.string.pref_pro_required)
            }

            builtinTheme.setOnPreferenceChangeListener { _, newValue ->
                val themeAccentColor: Int
                val themePrimaryColor: Int

                when (newValue.toString()) {
                    BuiltInThemes.OBSIDIAN.KEY -> {
                        themeAccentColor = ContextCompat.getColor(context!!, BuiltInThemes.OBSIDIAN.accentColor)
                        themePrimaryColor = ContextCompat.getColor(context!!, BuiltInThemes.OBSIDIAN.primaryColor)
                        applyColors(themePrimaryColor, themeAccentColor)
                    }

                    BuiltInThemes.CLEAN_GRASS.KEY -> {
                        themeAccentColor = ContextCompat.getColor(context!!, BuiltInThemes.CLEAN_GRASS.accentColor)
                        themePrimaryColor = ContextCompat.getColor(context!!, BuiltInThemes.CLEAN_GRASS.primaryColor)
                        applyColors(themePrimaryColor, themeAccentColor)
                    }

                    BuiltInThemes.GOLD.KEY -> {
                        themeAccentColor = ContextCompat.getColor(context!!, BuiltInThemes.GOLD.accentColor)
                        themePrimaryColor = ContextCompat.getColor(context!!, BuiltInThemes.GOLD.primaryColor)
                        applyColors(themePrimaryColor, themeAccentColor)
                    }

                    BuiltInThemes.GALAXY.KEY -> {
                        themeAccentColor = ContextCompat.getColor(context!!, BuiltInThemes.GALAXY.accentColor)
                        themePrimaryColor = ContextCompat.getColor(context!!, BuiltInThemes.GALAXY.primaryColor)
                        applyColors(themePrimaryColor, themeAccentColor)
                    }

                    BuiltInThemes.OXYGEN.KEY -> {
                        themeAccentColor = ContextCompat.getColor(context!!, BuiltInThemes.OXYGEN.accentColor)
                        themePrimaryColor = ContextCompat.getColor(context!!, BuiltInThemes.OXYGEN.primaryColor)
                        applyColors(themePrimaryColor, themeAccentColor)
                    }

                    BuiltInThemes.METALLIC.KEY -> {
                        themeAccentColor = ContextCompat.getColor(context!!, BuiltInThemes.METALLIC.accentColor)
                        themePrimaryColor = ContextCompat.getColor(context!!, BuiltInThemes.METALLIC.primaryColor)
                        applyColors(themePrimaryColor, themeAccentColor)
                    }

                    BuiltInThemes.ORANGED.KEY -> {
                        themeAccentColor = ContextCompat.getColor(context!!, BuiltInThemes.ORANGED.accentColor)
                        themePrimaryColor = ContextCompat.getColor(context!!, BuiltInThemes.ORANGED.primaryColor)
                        applyColors(themePrimaryColor, themeAccentColor)
                    }

                    BuiltInThemes.SEA.KEY -> {
                        themeAccentColor = ContextCompat.getColor(context!!, BuiltInThemes.SEA.accentColor)
                        themePrimaryColor = ContextCompat.getColor(context!!, BuiltInThemes.SEA.primaryColor)
                        applyColors(themePrimaryColor, themeAccentColor)
                    }

                    BuiltInThemes.SHROOB.KEY -> {
                        themeAccentColor = ContextCompat.getColor(context!!, BuiltInThemes.SHROOB.accentColor)
                        themePrimaryColor = ContextCompat.getColor(context!!, BuiltInThemes.SHROOB.primaryColor)
                        applyColors(themePrimaryColor, themeAccentColor)
                    }

                    BuiltInThemes.RIVER.KEY -> {
                        themeAccentColor = ContextCompat.getColor(context!!, BuiltInThemes.RIVER.accentColor)
                        themePrimaryColor = ContextCompat.getColor(context!!, BuiltInThemes.RIVER.primaryColor)
                        applyColors(themePrimaryColor, themeAccentColor)
                    }

                    BuiltInThemes.CASA_DE_PAPEL.KEY -> {
                        themeAccentColor = ContextCompat.getColor(context!!, BuiltInThemes.CASA_DE_PAPEL.accentColor)
                        themePrimaryColor = ContextCompat.getColor(context!!, BuiltInThemes.CASA_DE_PAPEL.primaryColor)
                        applyColors(themePrimaryColor, themeAccentColor)
                        casaDiCarta = true
                    }
                }

                preferencesBuilder.putBoolean("casa_di_carta", casaDiCarta)
                preferencesBuilder.putBoolean("usingCustomTheme", false)
                true
            }


            val stickySettings = findPreference(SettingStore.GENERAL.STICKY_SETTINGS) as TwoStatePreference
            if (PreferencesBuilder(context, PreferencesBuilder.defaultFilename).getBoolean("pro", false))
                stickySettings.isEnabled = true
            else {
                stickySettings.isEnabled = false
                stickySettings.summary = getString(R.string.pref_pro_required)
            }

            val showHint = findPreference(SettingStore.LOGIN.SHOW_PASSWORD_HINT) as TwoStatePreference
            val changeHint = findPreference(SettingStore.LOGIN.CHANGE_HINT)

            showHint.isVisible = preferencesBuilder.getString("hint", "").trim().isNotEmpty() || !pro

            val lockApp = findPreference(SettingStore.LOGIN.LOCK_ENABLED) as TwoStatePreference


            lockApp.setOnPreferenceChangeListener { _, newValue ->
                if (java.lang.Boolean.parseBoolean(newValue.toString())) {
                    setupDialog = MaterialDialog.Builder(context!!)
                            .customView(R.layout.auth_setup, true)
                            .cancelable(false)
                            .autoDismiss(false)
                            .title(R.string.settings_lock_title)
                            .iconRes(R.mipmap.ic_launcher)
                            .positiveText(R.string.dialog_set)
                            .negativeText(android.R.string.cancel)
                            .onPositive { dialog, _ ->
                                if (!working) {
                                    working = true
                                    signerProgress.isIndeterminate = true
                                    signerProgress.visibility = View.VISIBLE
                                    AvailableMethodsSpinner.isClickable = false
                                    auth_setup_password_layout.isClickable = false
                                    auth_setup_confirm_layout.isClickable = false
                                    auth_setup_hint_layout.isClickable = false

                                    dialog.setTitle(R.string.settings_checking_data)
                                    Handler().postDelayed({
                                        var signedForm: Boolean
                                        val isPIN = AvailableMethodsSpinner.selectedItemId == AUTH_PIN.toLong()
                                        val lockType = AvailableMethodsSpinner.selectedItemId.toInt()

                                        val auth_password = auth_setup_password.text.toString()
                                        val auth_confirm = auth_setup_confirm.text.toString()
                                        val hint = auth_setup_hint.text.toString()

                                        when {
                                            auth_password.trim().isEmpty() -> {
                                                signedForm = false
                                                clearErrors()
                                                auth_setup_password_layout.error = if (isPIN)
                                                    getString(R.string.dialog_missing_pin_confirm)
                                                else
                                                    getString(R.string.dialog_missing_password_confirm)

                                            }
                                            auth_confirm.trim().isEmpty() -> {
                                                signedForm = false
                                                clearErrors()
                                                auth_setup_confirm_layout.error = if (isPIN)
                                                    getString(R.string.dialog_missing_pin)
                                                else
                                                    getString(R.string.dialog_missing_password)
                                            }
                                            else -> signedForm = true
                                        }

                                        if (signedForm) {
                                            if (auth_password != auth_confirm) {
                                                signedForm = false
                                                clearErrors()
                                                if (auth_password.length >= auth_confirm.length) {
                                                    auth_setup_password_layout.error = if (isPIN)
                                                        getString(R.string.dialog_mismatch_pin)
                                                    else
                                                        getString(R.string.dialog_mismatch_password)
                                                } else {
                                                    auth_setup_confirm_layout.error = if (isPIN)
                                                        getString(R.string.dialog_mismatch_pin)
                                                    else
                                                        getString(R.string.dialog_mismatch_password)
                                                }

                                            } else
                                                signedForm = true
                                        }

                                        if (signedForm) {
                                            if (auth_password.trim().length <= 3) {
                                                signedForm = false
                                                clearErrors()
                                                auth_setup_password_layout.error = if (isPIN)
                                                    getString(R.string.dialog_short_pin)
                                                else
                                                    getString(R.string.dialog_short_password)
                                            } else
                                                signedForm = true
                                        }

                                        if (signedForm) {
                                            if (hint.length >= 40) {
                                                signedForm = false
                                                clearErrors()
                                                auth_setup_hint_layout.error = getString(R.string.dialog_long_hint)
                                            } else
                                                signedForm = true
                                        }

                                        if (signedForm) {
                                            if (hint.lowercase().contains(auth_password.lowercase())) {
                                                signedForm = false
                                                clearErrors()
                                                auth_setup_hint_layout.error = if (isPIN)
                                                    getString(R.string.dialog_contain_pin)
                                                else
                                                    getString(R.string.dialog_contain_password)
                                            } else
                                                signedForm = true
                                        }

                                        if (signedForm) {
                                            if (PasswordChecker.isWeak(auth_password)) {
                                                signedForm = false
                                                clearErrors()
                                                auth_setup_password_layout.error = if (isPIN)
                                                    getString(R.string.dialog_weak_pin)
                                                else
                                                    getString(R.string.dialog_weak_password)
                                            } else
                                                signedForm = true
                                        }

                                        if (signedForm) {
                                            clearErrors()
                                            Handler().postDelayed({
                                                dialog.setTitle(getString(R.string.settings_updating_security))
                                                preferencesBuilder.putBoolean("locked", true)
                                                preferencesBuilder.putInt("lockType", lockType)
                                                preferencesBuilder.putString("login_password_dialog", auth_password)
                                                preferencesBuilder.putString("hint", hint)
                                                if (hint.trim().isEmpty()) {
                                                    showHint.isVisible = false
                                                    changeHint.isVisible = true
                                                } else {
                                                    showHint.isVisible = true
                                                    changeHint.isVisible = true
                                                }
                                                Handler().postDelayed({
                                                    dialog.setTitle(R.string.settings_encrypt_security)
                                                    Handler().postDelayed({
                                                        dialog.dismiss()
                                                        working = false
                                                        lockApp.summary = getString(R.string.dialog_enabled_content)
                                                        preferencesBuilder.putBoolean("pendingLock", false)

                                                    }, 100)
                                                }, 200)
                                            }, 400)
                                        } else {
                                            signerProgress.visibility = View.GONE
                                            working = false
                                            dialog.setTitle(R.string.settings_mismatch)
                                            AvailableMethodsSpinner.isClickable = true
                                            auth_setup_password_layout.isClickable = true
                                            auth_setup_confirm_layout.isClickable = true
                                            auth_setup_hint_layout.isClickable = true
                                            Handler().postDelayed({
                                                dialog.setTitle(R.string.settings_lock_title)
                                                clearErrors()
                                            }, 3000)
                                        }
                                    }, 50)
                                }
                            }
                            .onNegative { dialog, _ ->
                                if (!working) {
                                    dialog.dismiss()
                                    lockApp.isChecked = false
                                    preferencesBuilder.putBoolean("pendingLock", false)
                                }
                            }
                            .build()


                    preferencesBuilder.putBoolean("pendingLock", true)
                    working = false
                    val accentColor12 = com.kabouzeid.appthemehelper.ThemeStore.accentColor(context!!)

                    signerProgress = setupDialog.customView!!.findViewById(R.id.signerProgress)
                    AvailableMethodsSpinner = setupDialog.customView!!.findViewById(R.id.AvailableMethods)
                    auth_setup_password = setupDialog.customView!!.findViewById(R.id.auth_setup_password)
                    auth_setup_confirm = setupDialog.customView!!.findViewById(R.id.auth_setup_confirm)
                    auth_setup_hint = setupDialog.customView!!.findViewById(R.id.auth_setup_hint)

                    val casa = listOf("Tokyo", "Rio", "Berlin", "Moscou", "Denver", "Helsinki", "Oslo", "Nairobi")
                    auth_setup_hint.hint = if (preferencesBuilder.getBoolean("casa_di_carta", false))
                        casa[Random().nextInt(casa.size)]
                    else
                        getString(R.string.dialog_hint)

                    auth_setup_password_layout = setupDialog.customView!!.findViewById(R.id.auth_setup_password_layout)
                    auth_setup_confirm_layout = setupDialog.customView!!.findViewById(R.id.auth_setup_confirm_layout)
                    auth_setup_hint_layout = setupDialog.customView!!.findViewById(R.id.auth_setup_hint_layout)

                    ATH.setTint(auth_setup_password, accentColor12)
                    ATH.setTint(auth_setup_confirm, accentColor12)
                    ATH.setTint(auth_setup_hint, accentColor12)
                    ATH.setTint(signerProgress, accentColor12)
                    ATH.setTint(AvailableMethodsSpinner, accentColor12)

                    ATH.setTint(auth_setup_password_layout, accentColor12)
                    ATH.setTint(auth_setup_confirm_layout, accentColor12)
                    ATH.setTint(auth_setup_hint, accentColor12)

                    auth_setup_password_layout.setHintTextAppearance(com.kabouzeid.appthemehelper.ThemeStore.activityTheme(context!!))
                    auth_setup_confirm_layout.setHintTextAppearance(com.kabouzeid.appthemehelper.ThemeStore.activityTheme(context!!))
                    auth_setup_hint_layout.setHintTextAppearance(com.kabouzeid.appthemehelper.ThemeStore.activityTheme(context!!))

                    val AvailableMethodsAdapter = ArrayAdapter.createFromResource(context!!,
                            R.array.auth_method, android.R.layout.simple_spinner_item)
                    AvailableMethodsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    AvailableMethodsSpinner.adapter = AvailableMethodsAdapter
                    AvailableMethodsSpinner.setSelection(AUTH_PASSWORD)

                    AvailableMethodsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                            auth_setup_password.transformationMethod = PasswordTransformationMethod.getInstance()
                            auth_setup_confirm.transformationMethod = PasswordTransformationMethod.getInstance()

                            auth_setup_password.setText("")
                            auth_setup_confirm.setText("")
                            auth_setup_hint.setText("")

                            when (position) {
                                AUTH_PIN -> {
                                    auth_setup_password.inputType = InputType.TYPE_CLASS_NUMBER
                                    auth_setup_confirm.inputType = InputType.TYPE_CLASS_NUMBER
                                    auth_setup_password_layout.hint = getString(R.string.dialog_pin)
                                    auth_setup_confirm_layout.hint = getString(R.string.dialog_confirm_pin_hint)

                                }
                                AUTH_PASSWORD -> {
                                    auth_setup_password.inputType = InputType.TYPE_CLASS_TEXT
                                    auth_setup_confirm.inputType = InputType.TYPE_CLASS_TEXT
                                    auth_setup_password_layout.hint = getString(R.string.dialog_password)
                                    auth_setup_confirm_layout.hint = getString(R.string.dialog_confirm_password_hint)
                                }
                            }
                        }

                        override fun onNothingSelected(parent: AdapterView<*>) {

                        }
                    }

                    auth_setup_password.transformationMethod = PasswordTransformationMethod.getInstance()
                    auth_setup_confirm.transformationMethod = PasswordTransformationMethod.getInstance()

                    auth_setup_password.inputType = InputType.TYPE_CLASS_TEXT
                    auth_setup_confirm.inputType = InputType.TYPE_CLASS_TEXT
                    auth_setup_password_layout.hint = getString(R.string.dialog_password)
                    auth_setup_confirm_layout.hint = getString(R.string.dialog_confirm_password_hint)

                    setupDialog.show()
                } else if (!java.lang.Boolean.parseBoolean(newValue.toString())) {
                    val unlockDialog = MaterialDialog.Builder(context!!)
                            .title(R.string.settings_unlocking_title)
                            .progress(true, 100)
                            .content(R.string.settings_unlocking_content)
                            .cancelable(false)
                            .progressIndeterminateStyle(true)
                            .show()

                    preferencesBuilder.putBoolean("locked", false)
                    preferencesBuilder.putInt("lockType", 0)
                    preferencesBuilder.putString("login_password_dialog", "")
                    preferencesBuilder.putString("hint", "")

                    Handler().postDelayed({
                        unlockDialog.dismiss()
                        UI.success(getString(R.string.dialog_disabled_title))
                        lockApp.summary = getString(R.string.dialog_disabled_content)
                    }, 400)
                }
                true
            }
            if (pro) {
                lockApp.isEnabled = true
            } else {
                lockApp.isEnabled = false
                lockApp.summary = getString(R.string.pref_pro_required)
            }

            if (preferencesBuilder.getBoolean("pendingLock", false)) {
                lockApp.isChecked = false
            }

            if (lockApp.isChecked) {
                lockApp.summary = getString(R.string.dialog_enabled_content)
            } else {
                lockApp.summary = getString(R.string.dialog_disabled_content)
            }


            var wasEmpty = false
            changeHint.title = if (preferencesBuilder.getString("hint", "").trim().isEmpty()) {
                wasEmpty = true
                getString(R.string.settings_hint_add)
            } else getString(R.string.settings_hint_edit)
            changeHint.setOnPreferenceClickListener { _ ->
                MaterialDialog.Builder(context!!)
                        .title(changeHint.title)
                        .inputRangeRes(0, 40, R.color.Red_500)
                        .input(getString(R.string.dialog_hint),
                                preferencesBuilder.getString("hint", "")) { _, input ->

                            val hint = input.toString().trim()
                            preferencesBuilder.putString("hint", hint)
                            if (wasEmpty) {
                                UI.success(getString(R.string.settings_hint_added))
                            } else {
                                UI.success(getString(R.string.settings_hint_changed))
                            }
                            changeHint.title = getString(R.string.settings_hint_edit)
                        }.show()
                true
            }

            val d: String? = context!!.packageManager.getInstallerPackageName(context!!.packageName)
            if (d != null) {
                if (CryptoFactory.sha1(d) == "d756abfb7665a50be304bae79a0f83db8adffd60") {
                    Toasty.error(context!!, getString(R.string.app_toast))
                    throw NullPointerException("null")
                }
            }


            val changePassword = findPreference(SettingStore.LOGIN.CHANGE_PASSWORD)
            changePassword.setOnPreferenceClickListener { _ ->
                setupDialog = MaterialDialog.Builder(context!!)
                        .customView(R.layout.auth_setup, true)
                        .cancelable(false)
                        .autoDismiss(false)
                        .title(R.string.dialog_change_password)
                        .iconRes(R.mipmap.ic_launcher)
                        .positiveText(R.string.dialog_update)
                        .negativeText(android.R.string.cancel)
                        .onPositive { dialog, _ ->
                            if (!working) {
                                working = true
                                signerProgress.isIndeterminate = true
                                signerProgress.visibility = View.VISIBLE
                                AvailableMethodsSpinner.isClickable = false
                                auth_setup_password_layout.isClickable = false
                                auth_setup_confirm_layout.isClickable = false
                                auth_setup_hint_layout.isClickable = false

                                dialog.setTitle(R.string.settings_checking_data)
                                Handler().postDelayed({
                                    var signedForm: Boolean
                                    val isPIN = AvailableMethodsSpinner.selectedItemId == AUTH_PIN.toLong()
                                    val lockType = AvailableMethodsSpinner.selectedItemId.toInt()

                                    val auth_password = auth_setup_password.text.toString()
                                    val auth_confirm = auth_setup_confirm.text.toString()
                                    val hint = auth_setup_hint.text.toString().trim()

                                    when {
                                        auth_password.trim().isEmpty() -> {
                                            signedForm = false
                                            clearErrors()
                                            auth_setup_password_layout.error = if (isPIN)
                                                getString(R.string.dialog_missing_pin_confirm)
                                            else
                                                getString(R.string.dialog_missing_password_confirm)

                                        }
                                        auth_confirm.trim().isEmpty() -> {
                                            signedForm = false
                                            clearErrors()
                                            auth_setup_confirm_layout.error = if (isPIN)
                                                getString(R.string.dialog_missing_pin)
                                            else
                                                getString(R.string.dialog_missing_password)
                                        }
                                        else -> signedForm = true
                                    }

                                    if (signedForm) {
                                        if (auth_password != auth_confirm) {
                                            signedForm = false
                                            clearErrors()
                                            if (auth_password.length >= auth_confirm.length) {
                                                auth_setup_password_layout.error = if (isPIN)
                                                    getString(R.string.dialog_mismatch_pin)
                                                else
                                                    getString(R.string.dialog_mismatch_password)
                                            } else {
                                                auth_setup_confirm_layout.error = if (isPIN)
                                                    getString(R.string.dialog_mismatch_pin)
                                                else
                                                    getString(R.string.dialog_mismatch_password)
                                            }

                                        } else
                                            signedForm = true
                                    }

                                    if (signedForm) {
                                        if (auth_password.trim().length <= 3) {
                                            signedForm = false
                                            clearErrors()
                                            auth_setup_password_layout.error = if (isPIN)
                                                getString(R.string.dialog_short_pin)
                                            else
                                                getString(R.string.dialog_short_password)
                                        } else
                                            signedForm = true
                                    }

                                    if (signedForm) {
                                        if (hint.length >= 40) {
                                            signedForm = false
                                            clearErrors()
                                            auth_setup_hint_layout.error = getString(R.string.dialog_long_hint)
                                        } else
                                            signedForm = true
                                    }

                                    if (signedForm) {
                                        if (hint.lowercase().contains(auth_password.lowercase())) {
                                            signedForm = false
                                            clearErrors()
                                            auth_setup_hint_layout.error = if (isPIN)
                                                getString(R.string.dialog_contain_pin)
                                            else
                                                getString(R.string.dialog_contain_password)
                                        } else
                                            signedForm = true
                                    }

                                    if (signedForm) {
                                        if (PasswordChecker.isWeak(auth_password)) {
                                            signedForm = false
                                            clearErrors()
                                            auth_setup_password_layout.error = if (isPIN)
                                                getString(R.string.dialog_weak_pin)
                                            else
                                                getString(R.string.dialog_weak_password)
                                        } else
                                            signedForm = true
                                    }

                                    if (signedForm) {
                                        clearErrors()
                                        Handler().postDelayed({
                                            dialog.setTitle(getString(R.string.settings_updating_security))
                                            preferencesBuilder.putBoolean("locked", true)
                                            preferencesBuilder.putInt("lockType", lockType)
                                            preferencesBuilder.putString("login_password_dialog", auth_password)
                                            preferencesBuilder.putString("hint", hint)
                                            if (hint.trim { it <= ' ' }.isEmpty()) {
                                                showHint.isVisible = false
                                                changeHint.isVisible = true
                                            } else {
                                                showHint.isVisible = true
                                                changeHint.isVisible = true
                                            }
                                            Handler().postDelayed({
                                                dialog.setTitle(R.string.settings_decrypt_security)
                                                Handler().postDelayed({
                                                    dialog.setTitle(R.string.settings_encrypt_security)
                                                    Handler().postDelayed({
                                                        dialog.dismiss()
                                                        working = false
                                                        UI.success(if (isPIN)
                                                            getString(R.string.dialog_updated_pin)
                                                        else
                                                            getString(R.string.dialog_updated_password))
                                                    }, 50)
                                                }, 100)
                                            }, 200)
                                        }, 100)
                                    } else {
                                        signerProgress.visibility = View.GONE
                                        working = false
                                        dialog.setTitle(R.string.settings_mismatch)
                                        AvailableMethodsSpinner.isClickable = true
                                        auth_setup_password_layout.isClickable = true
                                        auth_setup_confirm_layout.isClickable = true
                                        auth_setup_hint_layout.isClickable = true
                                        Handler().postDelayed({
                                            dialog.setTitle(R.string.settings_lock_title)
                                            clearErrors()
                                        }, 3000)
                                    }
                                }, 500)
                            }
                        }
                        .onNegative { dialog, _ ->
                            if (!working) {
                                dialog.dismiss()
                            }
                        }
                        .build()


                working = false
                val accentColor1 = com.kabouzeid.appthemehelper.ThemeStore.accentColor(context!!)

                signerProgress = setupDialog.customView!!.findViewById(R.id.signerProgress)
                AvailableMethodsSpinner = setupDialog.customView!!.findViewById(R.id.AvailableMethods)
                auth_setup_password = setupDialog.customView!!.findViewById(R.id.auth_setup_password)
                auth_setup_confirm = setupDialog.customView!!.findViewById(R.id.auth_setup_confirm)
                auth_setup_hint = setupDialog.customView!!.findViewById(R.id.auth_setup_hint)

                val casa = listOf("Tokyo", "Rio", "Berlin", "Moscoù", "Denver", "Helsinki", "Oslo", "Nairobi")
                auth_setup_hint.hint = if (preferencesBuilder.getBoolean("casa_di_carta", false))
                    casa[Random().nextInt(casa.size)]
                else
                    getString(R.string.dialog_hint)

                auth_setup_password_layout = setupDialog.customView!!.findViewById(R.id.auth_setup_password_layout)
                auth_setup_confirm_layout = setupDialog.customView!!.findViewById(R.id.auth_setup_confirm_layout)
                auth_setup_hint_layout = setupDialog.customView!!.findViewById(R.id.auth_setup_hint_layout)

                ATH.setTint(auth_setup_password, accentColor1)
                ATH.setTint(auth_setup_confirm, accentColor1)
                ATH.setTint(auth_setup_hint, accentColor1)
                ATH.setTint(signerProgress, accentColor1)

                ATH.setTint(auth_setup_password_layout, accentColor1)
                ATH.setTint(auth_setup_confirm_layout, accentColor1)
                ATH.setTint(auth_setup_hint, accentColor1)
                ATH.setTint(AvailableMethodsSpinner, accentColor1)

                auth_setup_password_layout.setHintTextAppearance(com.kabouzeid.appthemehelper.ThemeStore.activityTheme(context!!))
                auth_setup_confirm_layout.setHintTextAppearance(com.kabouzeid.appthemehelper.ThemeStore.activityTheme(context!!))
                auth_setup_hint_layout.setHintTextAppearance(com.kabouzeid.appthemehelper.ThemeStore.activityTheme(context!!))

                val AvailableMethodsAdapter = ArrayAdapter.createFromResource(context!!,
                        R.array.auth_method, android.R.layout.simple_spinner_item)
                AvailableMethodsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                AvailableMethodsSpinner.adapter = AvailableMethodsAdapter
                AvailableMethodsSpinner.setSelection(preferencesBuilder.getInt("lockType", AUTH_PASSWORD))

                AvailableMethodsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                        auth_setup_password.transformationMethod = PasswordTransformationMethod.getInstance()
                        auth_setup_confirm.transformationMethod = PasswordTransformationMethod.getInstance()

                        auth_setup_password.setText("")
                        auth_setup_confirm.setText("")
                        auth_setup_hint.setText("")


                        when (position) {
                            AUTH_PIN -> {
                                auth_setup_password.inputType = InputType.TYPE_CLASS_NUMBER
                                auth_setup_confirm.inputType = InputType.TYPE_CLASS_NUMBER
                                auth_setup_password_layout.hint = getString(R.string.dialog_pin)
                                auth_setup_confirm_layout.hint = getString(R.string.dialog_confirm_pin_hint)

                            }
                            AUTH_PASSWORD -> {
                                auth_setup_password.inputType = InputType.TYPE_CLASS_TEXT
                                auth_setup_confirm.inputType = InputType.TYPE_CLASS_TEXT
                                auth_setup_password_layout.hint = getString(R.string.dialog_password)
                                auth_setup_confirm_layout.hint = getString(R.string.dialog_confirm_password_hint)
                            }
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }

                auth_setup_password.transformationMethod = PasswordTransformationMethod.getInstance()
                auth_setup_confirm.transformationMethod = PasswordTransformationMethod.getInstance()

                auth_setup_password.inputType = InputType.TYPE_CLASS_TEXT
                auth_setup_confirm.inputType = InputType.TYPE_CLASS_TEXT
                auth_setup_password_layout.hint = getString(R.string.dialog_password)
                auth_setup_confirm_layout.hint = getString(R.string.dialog_confirm_password_hint)

                setupDialog.show()
                true
            }


            val MenuSettings = findPreference(SettingStore.MENU.SETTINGS) as CheckBoxPreference
            MenuSettings.isChecked = true

            val MenuAbout = findPreference(SettingStore.MENU.ABOUT) as CheckBoxPreference
            MenuAbout.setOnPreferenceChangeListener { _, newValue ->
                preferencesBuilder.putPreferenceBoolean(SettingStore.MENU.ABOUT, newValue as Boolean)
                true
            }
            val MenuDashboard = findPreference(SettingStore.MENU.DASHBOARD) as CheckBoxPreference
            MenuDashboard.setOnPreferenceChangeListener { _, newValue ->
                preferencesBuilder.putPreferenceBoolean(SettingStore.MENU.DASHBOARD, newValue as Boolean)
                true
            }
            val MenuDrawer = findPreference(SettingStore.MENU.OPEN_DRAWER) as CheckBoxPreference
            MenuDrawer.setOnPreferenceChangeListener { _, newValue ->
                preferencesBuilder.putPreferenceBoolean(SettingStore.MENU.OPEN_DRAWER, newValue as Boolean)
                true
            }
            val MenuBackup = findPreference(SettingStore.MENU.BACKUP) as CheckBoxPreference
            MenuBackup.setOnPreferenceChangeListener { _, newValue ->
                preferencesBuilder.putPreferenceBoolean(SettingStore.MENU.BACKUP, newValue as Boolean)
                true
            }
            val MenuReboot = findPreference(SettingStore.MENU.REBOOT) as CheckBoxPreference
            MenuReboot.setOnPreferenceChangeListener { _, newValue ->
                preferencesBuilder.putPreferenceBoolean(SettingStore.MENU.REBOOT, newValue as Boolean)
                true
            }

            val useFingerprint = findPreference(SettingStore.LOGIN.ALLOW_FINGERPRINT) as TwoStatePreference
            val fingerprintManagerCompat = FingerprintManagerCompat.from(context!!)

            if (!fingerprintManagerCompat.isHardwareDetected) {
                useFingerprint.isVisible = false
            } else if (!fingerprintManagerCompat.hasEnrolledFingerprints()) {
                useFingerprint.isEnabled = false
                useFingerprint.isVisible = true
                useFingerprint.summary = getString(R.string.dialog_no_fingerprint)
            } else {
                useFingerprint.isVisible = true
            }

            if (preferencesBuilder.getBoolean(XmlKeys.SETTINGS_AUTO_CONFIG, true)) {
                MenuSettings.isChecked = true
                MenuAbout.isChecked = true
                MenuDashboard.isChecked = true
                MenuDrawer.isChecked = true
                preferencesBuilder.putBoolean(XmlKeys.SETTINGS_AUTO_CONFIG, false)
            }
        }

        private fun applyColors(themePrimaryColor: Int, themeAccentColor: Int) {
            com.kabouzeid.appthemehelper.ThemeStore.editTheme(activity!!)
                    .accentColor(themeAccentColor)
                    .navigationBarColor(themePrimaryColor)
                    .statusBarColor(themePrimaryColor)
                    .primaryColor(themePrimaryColor)
                    .commit()
            activity!!.recreate()
        }

        private fun clearErrors() {
            auth_setup_password_layout.isErrorEnabled = false
            auth_setup_confirm_layout.isErrorEnabled = false
            auth_setup_hint_layout.isErrorEnabled = false
        }

        companion object {
            internal const val AUTH_PIN = 0
            internal const val AUTH_PASSWORD = 1

            private fun setSummary(preference: Preference, value: Any) {
                val stringValue = value.toString()

                if (preference is ListPreference) {
                    val index = preference.findIndexOfValue(stringValue)
                    preference.setSummary(
                            if (index >= 0)
                                preference.entries[index]
                            else
                                null)
                } else {
                    preference.summary = stringValue
                }
            }

            private fun setSummary(
                    preference: Preference,
                    stringValue: String = PreferenceManager.getDefaultSharedPreferences(preference.context).getString(preference.key, "")
                            ?: ""
            ) {
                if (preference is ListPreference) {
                    val index = preference.findIndexOfValue(stringValue)
                    preference.setSummary(
                            if (index >= 0)
                                preference.entries[index]
                            else
                                null)
                } else {
                    preference.summary = stringValue
                }
            }
        }

        override fun onSharedPreferenceChanged(
            sharedPreferences: SharedPreferences?,
            key: String?
        ) {

        }
    }

    companion object {
        private var pro = false
    }
}