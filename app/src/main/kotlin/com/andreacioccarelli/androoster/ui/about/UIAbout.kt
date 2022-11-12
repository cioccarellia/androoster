@file:Suppress("PrivatePropertyName")

package com.andreacioccarelli.androoster.ui.about

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.customtabs.CustomTabsIntent
import android.support.v7.widget.CardView
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.andreacioccarelli.androoster.BuildConfig
import com.andreacioccarelli.androoster.R
import com.andreacioccarelli.androoster.dataset.KeyStore
import com.andreacioccarelli.androoster.interfaces.ClickListener
import com.andreacioccarelli.androoster.tools.CryptoFactory
import com.andreacioccarelli.androoster.tools.PreferencesBuilder
import com.andreacioccarelli.androoster.tools.UI
import com.andreacioccarelli.androoster.ui.about.UIAbout.LIBRARIES.CODE_URL
import com.andreacioccarelli.androoster.ui.base.BaseActivity
import com.kabouzeid.appthemehelper.ThemeStore
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.card_about_app.*
import kotlinx.android.synthetic.main.card_actions.*
import kotlinx.android.synthetic.main.card_author.*
import kotlinx.android.synthetic.main.card_special_thanks.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.vibrator
import java.util.*

@Suppress("ConstantConditionIf")
class UIAbout : BaseActivity(), View.OnClickListener {

    internal var pro: Boolean = false

    private val LICENSE_APACHE2 = "Apache License 2.0"
    private val LICENSE_GNU = "GNU general Public License"
    private val LICENSE_CCBY3 = "CC-By 3.0 License"

    private var libList: MutableList<Library> = ArrayList()
    private var translatorsList: MutableList<Translator> = ArrayList()
    private lateinit var UI: UI
    private val email = Intent()

    object SPECIAL_THANKS {
        internal var AIDAN_FOLLESTAD_TWITTER = "https://twitter.com/afollestad"
        internal var AIDAN_FOLLESTAD_GITHUB = "https://github.com/afollestad"

        internal var KARIM_ABOU_GOOGLE_PLUS = "https://plus.google.com/u/0/+KarimAbouZeid23697"
        internal var KARIM_ABOU_GITHUB = "https://github.com/kabouzeid/"
    }
    
    object LIBRARIES {
        internal const val CODE_URL = "https://github.com/cioccarellia/androoster"
        internal const val MATERIAL_DIALOGS = "https://github.com/afollestad/material-dialogs"
        internal const val ASSENT = "https://github.com/afollestad/assent"
        internal const val MATERIAL_DRAWER = "https://github.com/mikepenz/MaterialDrawer"
        internal const val TOASTY = "https://github.com/GrenderG/Toasty"
        internal const val PLAIN_PIE_VIEW = "https://github.com/zurche/plain-pie"
        internal const val ANDROID_SHELL = "https://github.com/jrummyapps/android-shell/"
        internal const val THEME_ENGINE = "https://github.com/kabouzeid/app-theme-helper"
        internal const val ANDORID_DEVICE_NAMES = "https://github.com/jaredrummler/AndroidDeviceNames"
        internal const val APP_INTRO = "https://github.com/apl-devs/AppIntro"
        internal const val CHROME_CUSTOM_TABS = "https://developer.chrome.com/multidevice/android/customtabs"
        internal const val MATERIAL_DESIGN_ICONS = "https://material.io/icons/"
        internal const val DIGITUS = "https://libraries.io/github/afollestad/digitus"
    }

    object AUTHORS {
        internal const val AIDAN_FOLLESTAD = "Aidan Follestad"
        internal const val MIKE_PENZ = "Mike Penz"
        internal const val PAOLO_ROTOLO = "Paolo Rotolo"
        internal const val GRENDERG = "GrenderG"
        internal const val ZURCHE = "zurche"
        internal const val JRUMMY = "Jrummy Apps Inc."
        internal const val KABOUZEID = "Kabouzeid"
        internal const val GOOGLE = "Google Inc."
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pro = PreferencesBuilder(this, PreferencesBuilder.defaultFilename).getBoolean("pro", false)
        setContentView(R.layout.activity_about)
        animateContent(content as ViewGroup)
        setDrawUnderStatusbar(true)
        UI = UI(baseContext)

        setStatusbarColorAuto()
        setNavigationBarColorAuto()
        setTaskDescriptionColorAuto()

        setUpViews()
        initializeLibraries()
        initializeTranslators()

        doAsync {
            email.action = Intent.ACTION_SENDTO
            email.data = Uri.parse("mailto:")
            email.putExtra(Intent.EXTRA_EMAIL, "andrea.cioccarelli01@gmail.com")
            email.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
        }
    }

    private fun setUpViews() {
        setUpToolbar()
        setUpAppVersion()
        setUpAppDetails()
        setUpOnClickListeners()
        setUpPackageName()
    }

    private fun setUpToolbar() {
        toolbar.setBackgroundColor(ThemeStore.primaryColor(this))
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun setUpAppDetails() {
        release.text = if (BuildConfig.VERSION_NAME.contains("beta"))
            getString(R.string.app_name_beta) else getString(R.string.app_name_official)
    }

    @SuppressLint("SetTextI18n")
    private fun setUpAppVersion() {
        appVersion.text = "${BuildConfig.VERSION_NAME} ${if (pro) getString(R.string.app_name_suffix_pro) else ""}"
    }

    private fun setUpPackageName() {
        appPackageName.text = packageName
    }

    private fun setUpOnClickListeners() {
        layoutLicenses.setOnClickListener(this)
        layoutCode.setOnClickListener(this)
        layoutTranslations.setOnClickListener(this)

        rateOnGooglePlay.setOnClickListener(this)
        appDetails.setOnClickListener(this)

        writeMail.setOnClickListener(this)
        followOnGithub.setOnClickListener(this)
        followOnTwitter.setOnClickListener(this)

        aidanFollestadTwitter.setOnClickListener(this)
        aidanFollestadGitHub.setOnClickListener(this)
        karimGitHub.setOnClickListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(v: View) {
        when (v) {
            layoutLicenses -> showLicenseDialog()
            layoutCode -> openUrl(CODE_URL)
            layoutTranslations -> showTranslatorsDialog()
            rateOnGooglePlay -> openUrl(RATE_ON_GOOGLE_PLAY)
            appDetails -> openAppDetails()
            writeMail -> {
                try {
                    startActivity(email)
                } catch (e: ActivityNotFoundException) {
                    UI.error(getString(R.string.no_mail_app_found))
                }

            }
            followOnGithub -> openUrl(GITHUB)
            followOnTwitter -> openUrl(TWITTER)
            aidanFollestadTwitter -> openUrl(SPECIAL_THANKS.AIDAN_FOLLESTAD_TWITTER)
            aidanFollestadGitHub -> openUrl(SPECIAL_THANKS.AIDAN_FOLLESTAD_GITHUB)
            karimGitHub -> openUrl(SPECIAL_THANKS.KARIM_ABOU_GITHUB)
        }
    }

    private fun openAppDetails() {
        try {
            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            val intent = Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
            startActivity(intent)
        }

    }

    private fun showLicenseDialog() {
        val licenseDialog = MaterialDialog.Builder(this@UIAbout)
                .customView(R.layout.dialog_licenses, false)
                .cancelable(true)
                .autoDismiss(true)
                .title(R.string.about_libraries)
                .positiveText(android.R.string.cancel)
                .build()

        val recyclerView = licenseDialog.customView?.findViewById<RecyclerView>(R.id.licenseRecyclerView)

        val layoutManager = LinearLayoutManager(this@UIAbout)
        recyclerView?.layoutManager = layoutManager
        recyclerView?.setHasFixedSize(true)
        recyclerView?.itemAnimator = DefaultItemAnimator()
        recyclerView?.addOnItemTouchListener(LicensesTouchListener(applicationContext, recyclerView, object : ClickListener {
            override fun onClick(view: View, position: Int) {
                openUrl(libList[position].URL)
                vibrator.vibrate(150)
            }

            override fun onLongClick(view: View, position: Int) {}
        }))

        val adapter = LicensesAdapter(libList)
        recyclerView?.adapter = adapter

        licenseDialog.show()
    }

    private fun showTranslatorsDialog() {
        val translatorsDialog = MaterialDialog.Builder(this@UIAbout)
                .customView(R.layout.dialog_translators, true)
                .cancelable(true)
                .autoDismiss(true)
                .title(R.string.about_translations)
                .build()

        val recyclerView = translatorsDialog.customView?.findViewById<RecyclerView>(R.id.translatorsRecyclerView)

        val layoutManager = LinearLayoutManager(this@UIAbout)
        recyclerView?.layoutManager = layoutManager
        recyclerView?.setHasFixedSize(true)
        recyclerView?.itemAnimator = DefaultItemAnimator()
        val adapter = TranslatorsAdapter(translatorsList)
        recyclerView?.adapter = adapter

        translatorsDialog.show()
    }

    private fun openUrl(url: String) {
        val builder = CustomTabsIntent.Builder()
        builder.setToolbarColor(ThemeStore.primaryColor(this))
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(this@UIAbout, Uri.parse(url))
    }

    inner class Library internal constructor(internal val title: String, internal val author: String, internal val license: String, internal val URL: String)

    inner class Translator internal constructor(internal val title: String)

    private fun initializeLibraries() {
        libList.add(Library("Material Dialogs", AUTHORS.AIDAN_FOLLESTAD, LICENSE_APACHE2, LIBRARIES.MATERIAL_DIALOGS))
        libList.add(Library("Assent", AUTHORS.AIDAN_FOLLESTAD, LICENSE_APACHE2, LIBRARIES.ASSENT))
        libList.add(Library("Digitus", AUTHORS.AIDAN_FOLLESTAD, LICENSE_APACHE2, LIBRARIES.DIGITUS))
        libList.add(Library("Material Drawer", AUTHORS.MIKE_PENZ, LICENSE_APACHE2, LIBRARIES.MATERIAL_DRAWER))
        libList.add(Library("Toasty", AUTHORS.GRENDERG, LICENSE_GNU, LIBRARIES.TOASTY))
        libList.add(Library("PlainPieView", AUTHORS.ZURCHE, LICENSE_APACHE2, LIBRARIES.PLAIN_PIE_VIEW))
        libList.add(Library("Theme Engine", AUTHORS.KABOUZEID, LICENSE_APACHE2, LIBRARIES.THEME_ENGINE))
        libList.add(Library("Root Shell", AUTHORS.JRUMMY, LICENSE_APACHE2, LIBRARIES.ANDROID_SHELL))
        libList.add(Library("Device Names", AUTHORS.JRUMMY, LICENSE_APACHE2, LIBRARIES.ANDORID_DEVICE_NAMES))
        libList.add(Library("AppIntro", AUTHORS.PAOLO_ROTOLO, LICENSE_APACHE2, LIBRARIES.APP_INTRO))
        libList.add(Library("Chrome Custom Tabs", AUTHORS.GOOGLE, LICENSE_CCBY3, LIBRARIES.CHROME_CUSTOM_TABS))
        libList.add(Library("Material Icons", AUTHORS.GOOGLE, LICENSE_APACHE2, LIBRARIES.MATERIAL_DESIGN_ICONS))
    }

    private fun initializeTranslators() {
        translatorsList.add(Translator("Andrea Cioccarelli (en)"))
        translatorsList.add(Translator("佛壁灯 (cn)"))
    }

    internal inner class LicensesAdapter(val libs: MutableList<Library>) : RecyclerView.Adapter<LicensesAdapter.LibraryViewHolder>() {

        override fun getItemCount(): Int {
            return libs.size
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): LibraryViewHolder {
            val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.license_item, viewGroup, false)
            return LibraryViewHolder(v)
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(LibraryViewHolder: LibraryViewHolder, i: Int) {
            LibraryViewHolder.libraryTitle.text = libs[i].title
            LibraryViewHolder.LibraryContent.text = libs[i].author + " | " + libs[i].license
        }

        internal inner class LibraryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val CardView: CardView = itemView.findViewById(R.id.LibraryCard)
            val libraryTitle: TextView = itemView.findViewById(R.id.LibraryTitle)
            val LibraryContent: TextView = itemView.findViewById(R.id.LibraryContent)
        }
    }

    internal inner class TranslatorsAdapter(val translators: MutableList<Translator>) : RecyclerView.Adapter<TranslatorsAdapter.LibraryViewHolder>() {

        override fun getItemCount(): Int {
            return translators.size
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): LibraryViewHolder {
            val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.translator_item, viewGroup, false)
            return LibraryViewHolder(v)
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(LibraryViewHolder: LibraryViewHolder, i: Int) {
            LibraryViewHolder.translatorTitle.text = translators[i].title
        }

        internal inner class LibraryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val translatorsRoot: LinearLayout = itemView.findViewById(R.id.translator_root)
            val translatorTitle: TextView = itemView.findViewById(R.id.translator_title)
        }
    }

    companion object {
        var GITHUB = "https://github.com/cioccarellia"

        var TWITTER = "https://twitter.com/cioccarellia"
        var RATE_ON_GOOGLE_PLAY = "https://play.google.com/store/apps/details?id=com.andreacioccarelli.androoster"
    }
}
