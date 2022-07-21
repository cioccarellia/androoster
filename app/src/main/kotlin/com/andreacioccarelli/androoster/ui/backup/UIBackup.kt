package com.andreacioccarelli.androoster.ui.backup

import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v7.widget.*
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.andreacioccarelli.androoster.R
import com.andreacioccarelli.androoster.core.RootFile
import com.andreacioccarelli.androoster.interfaces.ClickListener
import com.andreacioccarelli.androoster.tools.LaunchStruct
import com.andreacioccarelli.androoster.ui.about.RecyclerViewTouchListener
import com.andreacioccarelli.androoster.tools.PreferencesBuilder
import com.andreacioccarelli.androoster.tools.UI
import com.andreacioccarelli.androoster.ui.base.BaseActivity
import com.andreacioccarelli.androoster.ui.dashboard.RecentWidget
import com.github.zawadz88.materialpopupmenu.popupMenu
import com.kabouzeid.appthemehelper.ATH
import com.kabouzeid.appthemehelper.ThemeStore
import es.dmoral.toasty.Toasty

import kotlinx.android.synthetic.main.activity_backup.*
import kotlinx.android.synthetic.main.backup.*
import org.jetbrains.anko.vibrator
import java.util.*
import kotlin.concurrent.schedule

class UIBackup : BaseActivity() {

    private var backupFilesList: MutableList<BackupFile> = ArrayList()
    private var didBackup = false
    private lateinit var backupManager: BackupManager
    private lateinit var dialog: MaterialDialog
    private lateinit var restoreDialog: MaterialDialog
    private lateinit var UI: UI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backup)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.close_activity)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        RecentWidget.collect(this@UIBackup, LaunchStruct.BACKUP_ACTIVITY)
        UI = UI(baseContext)
        backupManager = BackupManager(baseContext)
        preferencesBuilder = PreferencesBuilder(baseContext)

        dialog = MaterialDialog.Builder(this@UIBackup)
                .build()
        restoreDialog = MaterialDialog.Builder(this@UIBackup)
                .build()

        initRecyclerView()

        fab.setOnClickListener {
            if (didBackup) {
                Toasty.warning(baseContext, getString(R.string.backup_already_executed)).show()
                return@setOnClickListener
            }

            dialog = MaterialDialog.Builder(this@UIBackup)
                    .title(R.string.backup_dialog_title)
                    .content(R.string.backup_dialog_content)
                    .progress(true,100, false)
                    .progressIndeterminateStyle(false)
                    .cancelable(false)
                    .show()

            backupManager.addBackup(false)

            Handler().postDelayed({
                refreshBackups()
                refreshRecyclerViewList()
            }, 400)
            didBackup = true
        }


        val accentColor = ThemeStore.accentColor(this@UIBackup)
        val primaryColor = ThemeStore.primaryColor(this@UIBackup)
        val primaryDarkColor = ThemeStore.primaryColorDark(this@UIBackup)

        toolbar_layout.title = title
        toolbar_layout.setStatusBarScrimColor(primaryDarkColor)

        ATH.setStatusbarColor(this@UIBackup, primaryDarkColor)
        ATH.setActivityToolbarColor(this@UIBackup, toolbar, primaryColor)
        ATH.setBackgroundTint(toolbar_layout, primaryColor)
        ATH.setBackgroundTint(fab, accentColor)
        ATH.setTint(progressBar, accentColor)
        toolbar.setBackgroundColor(primaryColor)
    }


    fun handleClick(view: View, position: Int) {
        vibrator.vibrate(150)
        val popupMenu = popupMenu {
            dropdownGravity = Gravity.END
            section {
                item {
                    vibrator.vibrate(150)
                    label = getString(R.string.backup_menu_restore)
                    icon = R.drawable.menu_restore
                    callback = {
                        vibrator.vibrate(150)
                        restoreDialog = MaterialDialog.Builder(this@UIBackup)
                                .title(R.string.backup_dialog_title)
                                .content(R.string.backup_dialog_init)
                                .cancelable(false)
                                .progress(true, 100, false)
                                .progressIndeterminateStyle(false)
                                .show()
                        restoreDialog.currentProgress

                        backupManager.restoreBackup(backupFilesList[position].file)
                        val rawContent = backupFilesList[position].file.content

                        Timer().schedule(1000){
                            BackupPreferencesPatcher(preferencesBuilder, restoreDialog, baseContext).patchPreferences(rawContent)
                        }
                    }
                }
                if (position != 0) {
                    item {
                        label = getString(R.string.backup_menu_delete)
                        iconDrawable = ContextCompat.getDrawable(this@UIBackup, R.drawable.menu_delete)
                        callback = {
                            vibrator.vibrate(150)
                            progressBar.visibility = View.VISIBLE
                            backupRecyclerView.visibility = View.GONE

                            backupManager.removeBackup(backupFilesList[position].file)
                            Handler().postDelayed({
                                refreshBackups()
                                refreshRecyclerViewList()
                                progressBar.visibility = View.GONE
                                backupRecyclerView.visibility = View.VISIBLE
                            }, 1000)
                        }
                    }
                }
            }
        }

        popupMenu.show(this@UIBackup, view)
    }

    fun handleLongClick(view: View, position: Int) {
        if (position != 0) {
            vibrator.vibrate(150)
            val popupMenu = popupMenu {
                dropdownGravity = Gravity.END
                section {
                    item {
                        vibrator.vibrate(150)
                        label = getString(R.string.backup_menu_delete_all)
                        iconDrawable = ContextCompat.getDrawable(this@UIBackup, R.drawable.menu_delete)
                        callback = {
                            vibrator.vibrate(150)
                            backupManager.removeUserBackups()
                            progressBar.visibility = View.VISIBLE

                            Handler().postDelayed({
                                refreshBackups()
                                refreshRecyclerViewList()
                                progressBar.visibility = View.GONE
                            }, 1000)
                        }
                    }
                }
            }

            popupMenu.show(this@UIBackup, view)
        }
    }

    fun refreshBackups() {
        backupFilesList.clear()
        val list = backupManager.getBackupsList()

        if (list.size == 0) {
            UI.warning(getString(R.string.pref_show_backup_empty))
            backupFilesList.clear()
            progressBar.visibility = View.GONE
            return
        }

        for ((id, file) in list.withIndex()) {
            val title = if (id == 0 && backupManager.doesAutoBackupExist) {
                getString(R.string.backup_default)
            } else {
                "${getString(R.string.backup_prefix)} #${id + 1}"
            }

            try {
                backupFilesList.add(id, BackupFile(
                        title,
                        DateGenerator.toHumanDate(file.name.split("-")[1]),
                        file))
            } catch (oob: IndexOutOfBoundsException) {
                //Crashlytics.logException(oob)

                UI.unconditionalError(getString(R.string.backup_error_listing))
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun initRecyclerView() {
        refreshBackups()
        progressBar.visibility = View.GONE
        val layoutManager = LinearLayoutManager(this@UIBackup)

        backupRecyclerView.layoutManager = layoutManager
        backupRecyclerView.setHasFixedSize(true)
        backupRecyclerView.itemAnimator = DefaultItemAnimator()
        backupRecyclerView.addOnItemTouchListener(RecyclerViewTouchListener(applicationContext, backupRecyclerView, object : ClickListener {
            override fun onClick(view: View, position: Int) {
                handleClick(view, position)
            }

            override fun onLongClick(view: View, position: Int) {
                handleLongClick(view, position)
            }
        }))

        refreshRecyclerViewList()
    }

    fun refreshRecyclerViewList() {
        val adapter = BackupAdapter(backupFilesList)
        backupRecyclerView.adapter = adapter
        dialog.dismiss()
        animateContent(content as ViewGroup)
    }

    inner class BackupFile internal constructor(internal var title: String, internal var content: String, internal var file: RootFile)

    internal inner class BackupAdapter(val backupList: MutableList<BackupFile>) : RecyclerView.Adapter<BackupAdapter.BackupViewHolder>() {

        override fun getItemCount(): Int {
            return backupList.size
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): BackupViewHolder {
            val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.backup_item, viewGroup, false)
            return BackupViewHolder(v)
        }

        override fun onBindViewHolder(BackupViewHolder: BackupViewHolder, i: Int) {
            BackupViewHolder.backupTitle.text = backupList[i].title
            BackupViewHolder.backupContent.text = backupList[i].content
        }

        internal inner class BackupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val backupCard: CardView = itemView.findViewById(R.id.backupCard)
            val backupTitle: TextView = itemView.findViewById(R.id.backupTitle)
            val backupContent: TextView = itemView.findViewById(R.id.backupDate)
        }
    }
}
