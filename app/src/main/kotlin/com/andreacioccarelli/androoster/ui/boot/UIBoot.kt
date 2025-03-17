package com.andreacioccarelli.androoster.ui.boot

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Vibrator
import android.support.v4.content.ContextCompat
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.afollestad.assent.Assent
import com.afollestad.materialdialogs.MaterialDialog
import com.andreacioccarelli.androoster.R
import com.andreacioccarelli.androoster.dataset.KeyStore
import com.andreacioccarelli.androoster.dataset.XmlKeys
import com.andreacioccarelli.androoster.tools.*
import com.andreacioccarelli.androoster.ui.base.BaseActivity
import com.andreacioccarelli.androoster.ui.dashboard.UIDashboard
import com.andreacioccarelli.androoster.ui.settings.SettingStore
import com.andreacioccarelli.androoster.ui.wizard.UIWizard
import com.jrummyapps.android.shell.Shell
import com.kabouzeid.appthemehelper.ATH
import com.kabouzeid.appthemehelper.ThemeStore
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.schedule

class UIBoot : BaseActivity(), LaunchStruct {

    private var TESTING_RELEASE: Boolean = false
    private var COMPATIBILITY_MODE: Boolean = true
    private var root: Boolean = false
    private var busyBoxInstalled: Boolean = false
    private var fs: Boolean = false
    private var isSedInstalled: Boolean = false
    private var environmentChecksPassed = false
    internal lateinit var UI: UI
    private lateinit var passwordInput: EditText
    internal lateinit var notificationFingerprint: TextView
    private lateinit var fingerprintBase: ImageView
    private lateinit var passwordIcon: ImageView
    private lateinit var passwordBase: ImageView
    internal lateinit var FingerprintIcon: ImageView
    internal lateinit var loginDialog: MaterialDialog
    internal var scanNumber = 0
    internal var unsuccessfulScansNumber = 0
    private var just_bought: Boolean = false
    private var locked: Boolean = false
    internal var lockType: Int = 0

    private val signedIntent: Intent
        get() {
            return when (preferencesBuilder.getPreferenceString(SettingStore.GENERAL.START_PAGE, "0")) {
                "0" -> Intent(this@UIBoot, UIDashboard::class.java)
                "1" -> {
                    val activityID = preferencesBuilder.getInt("last_opened", 0)
                    val prefs = PreferencesBuilder(baseContext, PreferencesBuilder.defaultFilename)
                    val hashBuilder = PreferencesBuilder(baseContext, PreferencesBuilder.Hashes, CryptoFactory.md5(KeyStore.hashedDecryptionKey))
                    val k = if (prefs.getBoolean("pro", false)) {
                        hashBuilder.getString("encryptedKey", "0") == CryptoFactory.sha256(CryptoFactory.sha1(prefs.getString("baseKey", "1")))
                    } else false
                    if (LaunchManager.canLaunch(activityID, k)) {
                        Intent(this@UIBoot, LaunchManager.getTargetClass(activityID))
                    } else {
                        Intent(this@UIBoot, UIDashboard::class.java)
                    }
                }
                else -> Intent(this@UIBoot, UIDashboard::class.java)
            }
        }

    private var checkingPermissions: Boolean = false
    private var arePermissionsAllowed: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.boot)
        preferencesBuilder = PreferencesBuilder(baseContext)

        TESTING_RELEASE = false
        COMPATIBILITY_MODE = true
        just_bought = preferencesBuilder.getBoolean("just_bought", false)
        preferencesBuilder.putBoolean("just_bought", false)

        preferencesBuilder.putBoolean("pro", true)


        UI = UI(this@UIBoot)
        Assent.setActivity(this@UIBoot, this@UIBoot)

        if (!dark) {
            findViewById<ImageView>(R.id.splashIcon).setImageDrawable(ContextCompat.getDrawable(baseContext, R.drawable.launcher_light))
        }
        ATH.setTint(findViewById<ProgressBar>(R.id.progressBar), ThemeStore.accentColor(this@UIBoot))

        if (preferencesBuilder.getBoolean("firstAppStart", true)) {
            Toasty.success(baseContext, "Pro version enabled", Toast.LENGTH_LONG).show()
            startActivity(Intent(this@UIBoot, UIWizard::class.java))
        } else {
            initApp()
            checkEnv()
        }

    }

    private fun initApp() {
        /*
        Integrity checks
        if (CryptoFactory.sha256(packageName) != "dfd0b21a54b8bc70124bd1a3b2cd306628ab6137597903f8ec9d2d780b2236bf") {
            MaterialDialog.Builder(this@UIBoot)
                    .title(R.string.packagename_error_title)
                    .content(R.string.packagename_error_content)
                    .cancelable(false)
                    .canceledOnTouchOutside(false)
                    .positiveText(R.string.packagename_error_shutdown)
                    .positiveColorRes(R.color.Red_500)
                    .onAny { dialog, which -> shutdownApp() }
                    .show()
            progressBar.visibility = View.GONE
            return
        }

        val d: String? = packageManager.getInstallerPackageName(packageName)
        if (d != null) {
            if (CryptoFactory.md5(d) == "bb8ece83317b06ec453d187d61792a56") {
                Toasty.error(baseContext, getString(R.string.app_toast))
                shutdownApp()
                return
            }
        }

         */

        /*
        if (isDebug && !COMPATIBILITY_MODE) {
            MaterialDialog.Builder(this)
                    .title(R.string.debug_error_title)
                    .content(R.string.debug_error_content)
                    .positiveText(R.string.action_shutdown)
                    .cancelable(false)
                    .onPositive { dialog, which ->
                        shutdownApp()
                    }
                    .show()
            progressBar.visibility = View.GONE
            return
        }*/

        if (preferencesBuilder.getBoolean("firstBoot", true) ||
                !preferencesBuilder.getBoolean("first_boot_successful", false)) {
            CoroutineScope(Dispatchers.Main).launch {
                preferencesBuilder.putBoolean("firstBoot", false)
                //val backupManager = BackupManager(baseContext)
                //backupManager.addBackup(true)

                // BackupPreferencesPatcher(preferencesBuilder, null, baseContext).patchPreferences(RootFile("/system/build.prop").content)
            }
        }



        // Crashlytics.setBool("is_debug", isDebug)
        // Crashlytics.setBool("is_testing", false)

        val prefs = PreferencesBuilder(baseContext, PreferencesBuilder.defaultFilename)
        val hashBuilder = PreferencesBuilder(baseContext, PreferencesBuilder.Hashes, CryptoFactory.md5(KeyStore.hashedDecryptionKey))
        val s = if (prefs.getBoolean("pro", false)) {
            hashBuilder.getString("encryptedKey", "0") == CryptoFactory.sha256(CryptoFactory.sha1(prefs.getString("baseKey", "1")))
        } else false
        // Crashlytics.setString("is_pro", "${prefs.getBoolean("pro", false)}, $s")
        // Crashlytics.setBool("is_dark_mode", preferencesBuilder.getBoolean(XmlKeys.DARK_THEME_APPLIED, false))
        // Crashlytics.setBool("has_xposed", isPackageInstalled("de.robv.android.xposed.installer"))

    }


    internal fun showAppLockscreen(lockType: Int) {
        if (lockType == LOCK_PASSWORD) {
            val hashedPassword = preferencesBuilder.getString("login_password_dialog", "Sofia")
            val failedAttempts = intArrayOf(0)

            findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
            loginDialog = MaterialDialog.Builder(this@UIBoot)
                    .title(R.string.settings_unlock_title)
                    .customView(R.layout.login_password_dialog, true)
                    .cancelable(false)
                    .autoDismiss(false)
                    .positiveText(R.string.action_enter)
                    .negativeText(R.string.action_exit)
                    .onNegative { dialog, which -> closeApp() }
                    .onPositive { dialog, which ->

                        val input = passwordInput.text.toString()
                        if (hashedPassword == input) {
                            passwordIcon.setImageResource(R.drawable.icon_success)
                            setPasswordBaseSuccess()
                            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow(passwordInput.windowToken, 0)

                            auth_success_feedback()

                            Handler().postDelayed({
                                loginDialog.dismiss()
                                checkEnv()
                            }, 200)
                        } else {
                            failedAttempts[0]++
                            passwordInput.setText("")
                            passwordIcon.setImageResource(R.drawable.icon_error)
                            setPasswordBaseError()
                            auth_error_feedback()
                            Handler().postDelayed({
                                passwordIcon.setImageResource(R.drawable.icon_password)
                                setPasswordBaseDefault()
                            }, 800)
                            if (failedAttempts[0] == 3 && preferencesBuilder.getPreferenceBoolean(SettingStore.LOGIN.SHOW_PASSWORD_HINT, false)) {
                                val hint = preferencesBuilder.getString("hint", "")
                                if (hint.trim().isNotEmpty()) {
                                    passwordInput.hint = hint
                                }
                            }
                        }
                    }
                    .build()

            passwordInput = loginDialog.customView!!.findViewById(R.id.PasswordInput)
            passwordIcon = loginDialog.customView!!.findViewById(R.id.password_icon)
            passwordBase = loginDialog.customView!!.findViewById(R.id.password_base)
            setPasswordBaseDefault()

            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(passwordInput, InputMethodManager.SHOW_IMPLICIT)

            passwordIcon.setImageResource(R.drawable.icon_password)
            passwordInput.hint = getString(R.string.dialog_password)

            ATH.setTint(passwordInput, ThemeStore.accentColor(this))
            passwordInput.inputType = InputType.TYPE_CLASS_TEXT
            if (preferencesBuilder.getPreferenceBoolean(SettingStore.LOGIN.MASK_PASSWORDS, false)) {
                passwordInput.transformationMethod = PasswordTransformationMethod.getInstance()
            }

            loginDialog.show()

        } else if (lockType == LOCK_PIN) {
            val hashedPassword = preferencesBuilder.getString("login_password_dialog", "Sofia")
            val failedAttempts = intArrayOf(0)

            findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
            loginDialog = MaterialDialog.Builder(this@UIBoot)
                    .title(R.string.settings_unlock_title)
                    .customView(R.layout.login_password_dialog, true)
                    .cancelable(false)
                    .autoDismiss(false)
                    .positiveText(R.string.action_enter)
                    .negativeText(R.string.action_exit)
                    .onNegative { _, _ -> closeApp() }
                    .onPositive { _, _ ->
                        val input = passwordInput.text.toString()
                        if (hashedPassword == input) {
                            passwordIcon.setImageResource(R.drawable.icon_success)
                            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow(passwordInput.windowToken, 0)
                            setPasswordBaseSuccess()
                            auth_success_feedback()

                            Handler().postDelayed({
                                loginDialog.dismiss()
                                checkEnv()
                            }, 200)
                        } else {
                            failedAttempts[0]++
                            passwordInput.setText("")
                            passwordIcon.setImageResource(R.drawable.icon_error)
                            setPasswordBaseError()
                            auth_error_feedback()
                            Handler().postDelayed({
                                passwordIcon.setImageResource(R.drawable.icon_pin)
                                setPasswordBaseDefault()
                            }, 500)
                            if (failedAttempts[0] == 3 && preferencesBuilder.getPreferenceBoolean(SettingStore.LOGIN.SHOW_PASSWORD_HINT, false)) {
                                val hint = preferencesBuilder.getString("hint", "")
                                if (hint.trim { it <= ' ' }.isNotEmpty()) {
                                    passwordInput.hint = hint
                                }
                            }
                        }
                    }
                    .build()


            passwordInput = loginDialog.customView!!.findViewById(R.id.PasswordInput)
            passwordIcon = loginDialog.customView!!.findViewById(R.id.password_icon)
            passwordBase = loginDialog.customView!!.findViewById(R.id.password_base)
            setPasswordBaseDefault()

            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(passwordInput, InputMethodManager.SHOW_IMPLICIT)

            passwordIcon.setImageResource(R.drawable.icon_pin)
            passwordInput.hint = getString(R.string.dialog_pin)

            ATH.setTint(passwordInput, ThemeStore.accentColor(this))
            passwordInput.inputType = InputType.TYPE_CLASS_NUMBER
            if (preferencesBuilder.getPreferenceBoolean(SettingStore.LOGIN.MASK_PASSWORDS, false)) {
                passwordInput.transformationMethod = PasswordTransformationMethod.getInstance()
            }
            loginDialog.show()
        }
    }

    internal fun auth_error_feedback() {
        vibrate(90)
        Timer().schedule(70) { vibrate(80) }
    }

    internal fun auth_success_feedback() {
        vibrate(50)
    }

    private fun bootApp() {
        preferencesBuilder.putBoolean("first_boot_successful", true)
        preferencesBuilder.putInt("boot_count", 1 + preferencesBuilder.getInt("boot_count", 0))
        preferencesBuilder.putBoolean("just_bought", false)

        startActivity(signedIntent)
    }

    internal fun checkEnv() {
        findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE

        /*
        val d: String? = packageManager.getInstallerPackageName(packageName)
        if (d != null) {
            if (CryptoFactory.sha1(d) == "d756abfb7665a50be304bae79a0f83db8adffd60") {
                Toasty.error(baseContext, getString(R.string.app_toast))
                throw NullPointerException("null")
            }
        }*/


        CoroutineScope(Dispatchers.Main).launch {
            root = Shell.SU.available()
            val busyboxOutput = Shell.SH.run("busybox").getStdout()
            busyBoxInstalled = !busyboxOutput.contains("not found")
            fs = preferencesBuilder.getBoolean("firstStart", true)
            preferencesBuilder.putBoolean("root", root)
            preferencesBuilder.putBoolean("busybox", busyBoxInstalled)

            // Crashlytics.setBool("has_root", root)
            // Crashlytics.setBool("has_busybox", bbInstalled)
            // Crashlytics.setString("details_busybox", busyboxOutput)

            COMPATIBILITY_MODE = true

            CoroutineScope(Dispatchers.Main).launch {
                if (COMPATIBILITY_MODE) {
                    if (!root) {
                        // if no root is detected, we just notify with a toast that the app wont work, but boot regardless
                        Toasty.error(this@UIBoot, "Root access not detected! Application running in compatibility mode", Toast.LENGTH_LONG).show()
                        Toasty.warning(this@UIBoot, "Tweaks will not work, no root access detected/granted", Toast.LENGTH_LONG).show()

                        // To read toasts in boot screen
                        delay(500)
                    }
                    bootApp()
                } else {
                    phase1CheckRootStrict()
                }
            }
        }
    }

    private fun phase1CheckRootStrict() {
        if (!root) {
            MaterialDialog.Builder(this@UIBoot)
                    .title(R.string.root_error_title)
                    .content(R.string.root_error_content)
                    .iconRes(R.drawable.error)
                    .positiveText(getString(R.string.action_grant))
                    .negativeText(getString(R.string.action_exit))
                    .negativeColor(ContextCompat.getColor(this@UIBoot, R.color.Red_500))
                    .onPositive { dialog, which ->
                        Handler().postDelayed({
                            if (run("su").isSuccessful) {
                                phase2CheckBusybox()
                                findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE
                            } else {
                                UI.unconditionalError(getString(R.string.root_error_toast))
                                findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE

                                val packageName = RootEnvironmentMapper.getSuperuserPackage(baseContext)
                                if (isPackageInstalled(packageName)) {
                                    try {
                                        startActivity(Intent(packageManager.getLaunchIntentForPackage(packageName)))
                                    } catch (ignored: ActivityNotFoundException) {
                                        UI.unconditionalError(getString(R.string.not_found))
                                    }
                                }
                            }
                        }, 200)
                    }
                    .onNegative { dialog, which -> closeApp() }
                    .cancelable(false)
                    .show()
        } else {
            phase2CheckBusybox()
        }
    }

    private fun phase2CheckBusybox() {
        CoroutineScope(Dispatchers.Main).launch {
            val rootDetails = RootEnvironmentMapper.getSuperuserApp(true, this@UIBoot)
            preferencesBuilder.putString("rootManagerDetails", rootDetails)
            // Crashlytics.setString("details_root", rootDetails)

            preferencesBuilder.putInt(XmlKeys.LAST_VERSION_CODE, 56)
            preferencesBuilder.putBoolean(XmlKeys.LAST_IS_TEST_RELEASE, false)
        }


        CoroutineScope(Dispatchers.Main).launch {
            isSedInstalled = false
            isSedInstalled = busyBoxInstalled || Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1 && !Shell.SU.run("sed").getStdout().lowercase().contains("not found")

            val sedCheck = run("sed -i")

            if (sedCheck.getStdout().lowercase().contains("unknown option") ||
                    sedCheck.getStderr().lowercase().contains("unknown option") ||
                    sedCheck.getStdout().lowercase().contains("not found") ||
                    sedCheck.getStderr().lowercase().contains("not found")) isSedInstalled = false


            if (isSedInstalled) {
                // Crashlytics.log(0, "UIBoot - busybox check", "Busybox not found. Output: $sedCheck")
                environmentChecksPassed = true
                CoroutineScope(Dispatchers.Main).launch { phase3CheckPermissions() }
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
                    MaterialDialog.Builder(this@UIBoot)
                            .title(R.string.busybox_error_title)
                            .content(R.string.busybox_error_content)
                            .positiveText(R.string.action_open_store)
                            .iconRes(R.drawable.error)
                            .negativeColor(ContextCompat.getColor(this@UIBoot, R.color.Red_500))
                            .onPositive { dialog, which ->
                                openStoreForBusybox()
                                Handler().postDelayed({ closeApp() }, 500)
                            }
                            .negativeText(R.string.action_exit)
                            .onNegative { dialog, which -> closeApp() }
                            .cancelable(false)
                            .show()
                }
            }
        }
    }

    private fun phase3CheckPermissions() {
        // stripped, just boot
        bootApp()
        return

        /*
        checkingPermissions = false
        if (permission_write_external) {
            checkingPermissions = false
        } else {
            arePermissionsAllowed = false
            Assent.requestPermissions(AssentCallback {
                if (it.allPermissionsGranted()) {
                    arePermissionsAllowed = true
                    findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE
                    bootApp()
                } else {
                    arePermissionsAllowed = false
                    findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE

                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.parse("package:$packageName")

                    MaterialDialog.Builder(this@UIBoot)
                            .title(R.string.boot_cannot_get_permissions_title)
                            .content(R.string.boot_cannot_get_permissions_content)
                            .positiveText(R.string.boot_cannot_get_permissions_allow)
                            .negativeColor(ContextCompat.getColor(this@UIBoot, R.color.Red_500))
                            .onPositive { dialog, which ->
                                Assent.requestPermissions( AssentCallback{
                                    if (it.allPermissionsGranted()) {
                                        arePermissionsAllowed = true
                                        findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE
                                        bootApp()
                                    } else {
                                        arePermissionsAllowed = false
                                        try {
                                            startActivity(intent)
                                        } catch (e: ActivityNotFoundException) {
                                            startActivity(Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS))
                                        }

                                    }
                                }, 69, Assent.WRITE_EXTERNAL_STORAGE)
                            }
                            .negativeText("IGNORE")
                            .onNegative { dialog, which -> bootApp() }
                            .cancelable(false)
                            .show()
                }
            }, 69, Assent.WRITE_EXTERNAL_STORAGE)
        }*/
    }

    override fun onResume() {
        super.onResume()
        Assent.setActivity(this@UIBoot, this@UIBoot)
    }

    @SuppressLint("MissingPermission")
    private fun vibrate(VibrationTime: Int) {
        try {
            val v = this@UIBoot.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            v.vibrate(VibrationTime.toLong())
        } catch (e: Exception) {}
    }

    override fun onPause() {
        super.onPause()
        if (isFinishing)
            Assent.setActivity(this@UIBoot, null)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Assent.handleResult(permissions, grantResults)
    }

    internal fun scheduleClear() {
        Handler().postDelayed({
            setFingerprintBaseDefault()
            FingerprintIcon.setImageResource(R.drawable.icon_fingerprint)
        }, 1500)
    }

    internal fun setFingerprintBaseError() {
        ATH.setTint(fingerprintBase, red)
    }

    internal fun setFingerprintBaseWarning() {
        ATH.setTint(fingerprintBase, yellow)
    }

    internal fun setFingerprintBaseSuccess() {
        ATH.setTint(fingerprintBase, green)
    }

    private fun setFingerprintBaseDefault() {
        ATH.setTint(fingerprintBase, ThemeStore.primaryColor(this@UIBoot))
    }

    private fun setPasswordBaseError() {
        ATH.setTint(passwordBase, red)
    }

    private fun setPasswordBaseSuccess() {
        ATH.setTint(passwordBase, green)
    }

    private fun setPasswordBaseDefault() {
        ATH.setTint(passwordBase, ThemeStore.primaryColor(this@UIBoot))
    }

    companion object {
        private const val LOCK_PIN = 0
        private const val LOCK_PASSWORD = 1
    }
}
