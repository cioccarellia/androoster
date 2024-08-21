package com.andreacioccarelli.androoster.ui.upgrade

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.view.ViewGroup
import com.afollestad.materialdialogs.MaterialDialog
import com.andreacioccarelli.androoster.R
import com.andreacioccarelli.androoster.tools.GradientGenerator
import com.andreacioccarelli.androoster.tools.PreferencesBuilder
import com.andreacioccarelli.androoster.tools.UI
import com.andreacioccarelli.androoster.ui.base.BaseActivity
import com.kabouzeid.appthemehelper.ATH
import com.kabouzeid.appthemehelper.ThemeStore
import kotlinx.android.synthetic.main.upgrade_activity_v2.*
import kotlinx.android.synthetic.main.upgrade_content_v2.*
import org.jetbrains.anko.image
import org.jetbrains.anko.textColor
import org.jetbrains.anko.vibrator

class UIUpgrade : BaseActivity() {

    var pro = false
    lateinit var UI: UI
    private var hasPurchased = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.upgrade_activity_v2)
        setSupportActionBar(toolbar)
        animateContent(content as ViewGroup)

        pro = PreferencesBuilder(this, PreferencesBuilder.defaultFilename).getBoolean("pro", false)

        UI = UI(baseContext)

        setupToolbar()
        setupTheme()
        setupViews()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)


        toolbar.setBackgroundColor(ThemeStore.primaryColor(this))
        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }

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

        } else {
            icon.image = ContextCompat.getDrawable(baseContext, R.drawable.launcher_light)
            //Performances issue: Image loaded twice
        }

        upgradeButton.background = GradientGenerator.get(baseContext, GradientDrawable.Orientation.TL_BR, 16f)
    }

    @Suppress("WhenWithOnlyElse")
    private fun setupViews() {
        upgradeButton.setOnClickListener {
            vibrator.vibrate(100)
            PreferencesBuilder(this, PreferencesBuilder.defaultFilename).putBoolean("pro", true)

            MaterialDialog.Builder(this)
                    .title("Androoster Pro Enabled")
                    .content("Thank you! Androoster Pro has been activated. Just restart the app and enjoy all the professional features.")
                    .positiveText("OK")
                    .onPositive { dialog, which ->
                        dialog.dismiss()
                        finishAffinity()
                    }
                    .autoDismiss(false)
                    .show()
        }
    }

    override fun onBackPressed() {
        if (!hasPurchased) {
            super.onBackPressed()
        } else {
            UI.unconditionalSuccess(getString(R.string.upgrade_press_next))
        }
    }
}
