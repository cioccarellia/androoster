package com.andreacioccarelli.androoster.ui.upgrade

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatButton
import android.support.v7.widget.CardView
import android.support.v7.widget.Toolbar
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.andreacioccarelli.androoster.R
import com.andreacioccarelli.androoster.tools.GradientGenerator
import com.andreacioccarelli.androoster.tools.PreferencesBuilder
import com.andreacioccarelli.androoster.tools.UI
import com.andreacioccarelli.androoster.ui.base.BaseActivity
import com.kabouzeid.appthemehelper.ATH
import com.kabouzeid.appthemehelper.ThemeStore
import es.dmoral.toasty.Toasty

class UIUpgrade : BaseActivity() {

    var pro = false
    lateinit var UI: UI
    private var hasPurchased = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.upgrade_activity_v2)
        setSupportActionBar(findViewById(R.id.toolbar))
        animateContent(findViewById(R.id.content) as ViewGroup)

        pro = PreferencesBuilder(this, PreferencesBuilder.defaultFilename).getBoolean("pro", false)

        UI = UI(baseContext)

        setupToolbar()
        setupTheme()
        setupViews()

        findViewById<CardView>(R.id.upgradeButton).performClick()
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
        ATH.setBackgroundTint(findViewById(R.id.appbar_layout), primaryColor)

        findViewById<Toolbar>(R.id.toolbar).setBackgroundColor(primaryColor)
        findViewById<TextView>(R.id.separator_text).setTextColor(accentColor)

        if (dark) {

        } else {
            findViewById<ImageView>(R.id.icon).setImageResource(R.drawable.launcher_light)
        }

        findViewById<CardView>(R.id.upgradeButton).background = GradientGenerator.get(baseContext, GradientDrawable.Orientation.TL_BR, 16f)
    }

    @Suppress("WhenWithOnlyElse")
    private fun setupViews() {
        findViewById<CardView>(R.id.upgradeButton).setOnClickListener {
            PreferencesBuilder(this, PreferencesBuilder.defaultFilename).putBoolean("pro", true)

            Toasty.success(this, "Pro version activated!", Toast.LENGTH_SHORT).show()

            MaterialDialog.Builder(this)
                    .title("Androoster Pro Enabled")
                    .content("Thank you! Androoster Pro has been activated.\nRestart the app and enjoy all the professional features.")
                    .positiveText("RESTART APP")
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
