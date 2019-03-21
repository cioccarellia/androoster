package com.andreacioccarelli.androoster.ui.upgrade

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.view.ViewGroup
import com.afollestad.materialdialogs.MaterialDialog
import com.andreacioccarelli.androoster.BuildConfig
import com.andreacioccarelli.androoster.R
import com.andreacioccarelli.androoster.dataset.KeyStore
import com.andreacioccarelli.androoster.interfaces.BillingCodes
import com.andreacioccarelli.androoster.tools.*
import com.andreacioccarelli.androoster.ui.base.BaseActivity
import com.andreacioccarelli.billingprotector.BillingProtector
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import com.kabouzeid.appthemehelper.ATH
import com.kabouzeid.appthemehelper.ThemeStore
import kotlinx.android.synthetic.main.upgrade_activity_v2.*
import kotlinx.android.synthetic.main.upgrade_content_v2.*
import org.jetbrains.anko.image
import org.jetbrains.anko.textColor
import org.jetbrains.anko.vibrator

class UIUpgrade : BaseActivity(), BillingProcessor.IBillingHandler, BillingCodes {

    var pro = false
    private lateinit var billingProcessor: BillingProcessor
    private lateinit var bp: BillingProtector
    lateinit var UI: UI
    private var hasPurchased = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.upgrade_activity_v2)
        setSupportActionBar(toolbar)
        animateContent(content as ViewGroup)

        pro = PreferencesBuilder(this, PreferencesBuilder.defaultFilename).getBoolean("pro", false)

        billingProcessor = BillingProcessor.newBillingProcessor(baseContext, base64DeveloperKey, this)
        billingProcessor.initialize()
        bp = BillingProtector(baseContext)

        UI = UI(baseContext)

        setupToolbar()
        setupTheme()
        setupViews()
        checkSelfState()
    }

    private fun checkSelfState() {
        if (pro && !BuildConfig.COMPATIBILITY_MODE) {
            MaterialDialog.Builder(this)
                    .title(R.string.upgrade_already_pro_title)
                    .content(R.string.upgrade_already_pro_content)
                    .positiveText(R.string.action_exit)
                    .onPositive { dialog, which ->
                        onBackPressed()
                    }
                    .cancelable(false)
                    .show()
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)


        toolbar.setBackgroundColor(ThemeStore.primaryColor(this))
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupTheme() {
        val primaryColor = ThemeStore.primaryColor(this)
        val primaryDarkColor = ThemeStore.primaryColorDark(this)
        val accentColor = ThemeStore.accentColor(this)

        setStatusbarColor(ThemeStore.primaryColor(this))
        ATH.setStatusbarColor(this, primaryDarkColor)
        ATH.setBackgroundTint(appbar_layout, primaryColor)

        toolbar.setBackgroundColor(primaryColor)
        separator_text.textColor = accentColor

        if (dark) {
            upgradeLayout.background = GradientGenerator.get(
                    ContextCompat.getColor(baseContext, R.color.Grey_800),
                    ContextCompat.getColor(baseContext, R.color.Grey_900),
                    GradientDrawable.Orientation.TL_BR, 8f)
        } else {
            icon.image = ContextCompat.getDrawable(baseContext, R.drawable.launcher_light)
            //Performances issue: Image loaded twice

            upgradeLayout.background = GradientGenerator.get(
                    ContextCompat.getColor(baseContext, R.color.Grey_100),
                    ContextCompat.getColor(baseContext, R.color.Grey_125),
                    GradientDrawable.Orientation.TL_BR, 8f)
        }

        upgradeButton.background = GradientGenerator.get(baseContext, GradientDrawable.Orientation.TL_BR, 16f)
    }

    @Suppress("WhenWithOnlyElse")
    private fun setupViews() {
        upgradeButton.setOnClickListener {
            vibrator.vibrate(100)

            when {
                bp.arePirateAppsInstalled() -> {
                    MaterialDialog.Builder(this)
                            .title(R.string.pirate_apps_title)
                            .content(R.string.pirate_apps_content)
                            .positiveText(R.string.action_open_settings)
                            .onPositive { dialog, which ->
                                openApplicationSettings(this)
                            }
                            .show()
                    return@setOnClickListener
                }
                isDebug -> {
                    MaterialDialog.Builder(this)
                            .title(R.string.debug_error_title)
                            .content(R.string.debug_error_content_purchase)
                            .positiveText(R.string.play_store)
                            .onPositive { dialog, which ->
                                openStore()
                            }
                            .show()
                    return@setOnClickListener
                }
                else -> {
                    try {
                        val isOneTimePurchaseSupported = billingProcessor.isOneTimePurchaseSupported
                        if (isOneTimePurchaseSupported) {
                            billingProcessor.purchase(this@UIUpgrade, upgradeSku)
                        } else {
                            UI.unconditionalError(getString(R.string.upgrade_purchase_error))
                        }
                    } catch (e: RuntimeException) {
                        e.printStackTrace()
                        MaterialDialog.Builder(this@UIUpgrade)
                                .title(R.string.upgrade_purchase_manager_title)
                                .content(R.string.upgrade_purchase_manager_content)
                                .positiveText(R.string.action_ok)
                                .show()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        billingProcessor?.release()
        super.onDestroy()
    }

    override fun onBillingInitialized() {
        billingProcessor.loadOwnedPurchasesFromGoogle()

        if (arePirateAppsInstalled(baseContext)) {
            MaterialDialog.Builder(this)
                    .title(R.string.pirate_apps_title)
                    .content(R.string.pirate_apps_content_restore)
                    .positiveText(R.string.action_open_settings)
                    .onPositive { dialog, which ->
                        openApplicationSettings(this)
                    }
                    .show()
            return
        }

        for (product in billingProcessor.listOwnedProducts()) {
            when (product) {
                upgradeSku, oldUpgradeSku -> onProductPurchased(upgradeSku, null)
            }
        }

    }

    override fun onBackPressed() {
        if (!hasPurchased) {
            super.onBackPressed()
        } else {
            UI.unconditionalSuccess(getString(R.string.upgrade_press_next))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data == null) return
        if (!billingProcessor.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
        if (productId == upgradeSku) {
            LicenseManager.finishProUpgrade(baseContext)
            vibrator.vibrate(50)
            UI.unconditionalSuccess(getString(R.string.upgrade_purchase_ok))
            hasPurchased = true

            upgradeTextView.text = getString(R.string.action_start)
            headerTextView.text = getString(R.string.upgrade_purchase_ok)

            val i = this@UIUpgrade.packageManager
                    .getLaunchIntentForPackage(this@UIUpgrade.packageName)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            upgradeButton.setOnClickListener {
                finish()
                startActivity(i)
            }
        }
    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {}

    override fun onPurchaseHistoryRestored() {
        onBillingInitialized()
    }
}
