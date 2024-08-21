@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.andreacioccarelli.androoster.ui.backup

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.andreacioccarelli.androoster.core.RootFile
import com.andreacioccarelli.androoster.core.TerminalCore
import com.jrummyapps.android.shell.Shell

/**
 * Created by andrea on 2018/apr.
 * Part of the package com.andreacioccarelli.androoster.tools
 */

class BackupManager (val context: Context) {

    val backupDirectoryName = "backups"
    val autoPrefix = "auto-"
    val backupPrefix = "backup-"

    val syntaxPrefixBackup = "Androoster system backup"
    val syntaxPrefixAuto = "Androoster default system configuration backup"

    @SuppressLint("SdCardPath")
    val basePath = "/data/data/${context.packageName}/$backupDirectoryName"

    var backupDirectory: RootFile

    val doesAutoBackupExist: Boolean
        get() {
            return Shell.SU.run("ls $basePath").stdout.toString().contains(autoPrefix)
        }

    val backupsNumber: Int
        get() {
            return backupDirectory.listFiles().size
        }

    fun getBackupsList(): ArrayList<RootFile> {
        val backupsBundleList = ArrayList<RootFile>()

        for (file in backupDirectory.listFiles()) {
            backupsBundleList.add(RootFile(file))
        }

        for (f in backupsBundleList) {
            Log.d("RootFile", "[${f.name}]")
        }

        return backupsBundleList
    }

    init {
        CoroutineScope(Dispatchers.Main).launch {
            TerminalCore.mount()
            Shell.SU.run("mkdir $basePath")
        }
        backupDirectory = RootFile(basePath)
    }


    fun addBackup(isFirst: Boolean) {
        val prefix: String
        val backupFileName: String

        if (isFirst) {
            backupFileName = "${backupDirectory.path}$autoPrefix${DateGenerator.getNowStringDate()}"
            prefix = syntaxPrefixBackup
        } else {
            backupFileName = "${backupDirectory.path}$backupPrefix${DateGenerator.getNowStringDate()}"
            prefix = syntaxPrefixAuto
        }

        CoroutineScope(Dispatchers.Main).launch {
            Shell.SU.run("touch $backupFileName")
            val configBackupFile = RootFile(backupFileName)

            configBackupFile.createFile()
            configBackupFile.write("### $prefix $backupFileName\n" + Shell.SU.run("cat /system/build.prop").getStdout())
        }
    }

    fun restoreBackup(file: RootFile) {
        CoroutineScope(Dispatchers.Main).launch {
            TerminalCore.mount()
            RootFile("/system/build.prop").write(file.content)
        }
    }

    fun removeBackup(file: RootFile) {
        CoroutineScope(Dispatchers.Main).launch {
            TerminalCore.mount()
            file.delete()

            if (file.name.contains(autoPrefix)) {
                RootFile("/etc/sysctl.conf").write("")
            }
        }
    }

    fun removeUserBackups() {
        CoroutineScope(Dispatchers.Main).launch {
            TerminalCore.run("rm -rf ${backupDirectory.path}/$backupPrefix*")
        }
    }

}