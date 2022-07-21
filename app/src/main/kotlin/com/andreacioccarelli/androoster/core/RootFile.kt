package com.andreacioccarelli.androoster.core

import android.annotation.SuppressLint
import android.util.Log
import com.andreacioccarelli.androoster.core.TerminalCore.mount
import com.andreacioccarelli.androoster.core.TerminalCore.run
import com.jrummyapps.android.shell.Shell
import java.io.File

/**
 * Created by andrea on 2017/nov.
 * Part of the package com.andreacioccarelli.androoster.core
 */

class RootFile {

    var file: File

    /**
     * Returns file name
     */
    val name: String
        get() = file.name

    /**
     * Returns the filesystem directory where file is
     */
    val filesystem: String
        get() {
            try {
                return '/' + file.absolutePath.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
            } catch (e: ArrayIndexOutOfBoundsException) {
                return "/"
            }

        }

    /**
     * Returns file path
     */
    val path: String
        get() = file.absolutePath + '/'

    /**
     * Returns the size in bytes
     */

    val sizeInBytes: Long
        get() = file.length()

    /**
     * Returns true if file is a file
     */

    val isFile: Boolean
        get() = file.isFile

    /**
     * Returns true if file is a directory
     */

    val isDirectory: Boolean
        get() = file.isDirectory

    /**
     * Returns true if the file is a link
     */

    val isLink: Boolean
        get() = run("readlink " + file.absolutePath).getStdout().trim { it <= ' ' }.length != 0

    /**
     * Returns the file content
     */

    val content: String
        get() = run("cat " + file.absolutePath).getStdout()

    constructor(path: String) {
        this.file = File(path)
    }

    /**
     * Deletes file
     */
    fun delete() {
        mount()
        run("rm -rf " + file.absolutePath)
    }

    /**
     * Returns the filesystem directory where the given path is
     */
    fun getFilesystem(path: String): String {
        try {
            return '/' + path.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
        } catch (e: ArrayIndexOutOfBoundsException) {
            return "/"
        }

    }

    /**
     * Copy file to another path
     */

    fun copy(destination: String) {
        run("mount -o remount,rw,remount,rw,remount " + this.getFilesystem(destination))
        run("cp -rf " + file.absolutePath + ' '.toString() + destination)
    }

    /**
     * Returns true if file contains the String
     */
    operator fun contains(pattern: String): Boolean {
        return !this.isDirectory && run("cat " + file.absolutePath).getStdout().contains(pattern)
    }

    /**
     * Remove all the lines in the file that contains the content
     */

    fun removeLine(content: String) {
        run("sed -i \"/" + content + "/d\" " + file.absolutePath + "")
    }

    /**
     * Adds a line at the end of the file containing the String's content
     */
    fun writenl(content: String) {
        run("echo \"" + content + "\" >> " + file.absolutePath)
    }

    /**
     * Delete the file content and writes over
     */
    fun write(content: String) {
        run("echo \"" + content + "\" > " + file.absolutePath)
    }


    /**
     * Create the file if it doesn't exists
     */
    fun createFile() {
        run("touch ${file.absolutePath}")
    }

    @SuppressLint("LogConditional")
    fun listFiles(): ArrayList<String> {
        Shell.SU.run("cd ${file.path}", "sync")
        var advancedMode = true
        val testOutput = Shell.SU.run("ls -1 /")

        if (testOutput.getStderr().toLowerCase().contains("unknown option")) {
            advancedMode = false
        }

        var rawList = ""
        val fileNames = if (advancedMode) {
            Shell.SU.run("ls -1 " + file.absolutePath).getStdout().split("\n")
        } else {
            Shell.SU.run("ls " + file.absolutePath).getStdout().split("\n")
        }

        val filesList = ArrayList<String>()

        for (fileName in fileNames) {
            filesList.add("${file.path}/$fileName")
            rawList += "${file.path}/$fileName\n"
        }

        /*Crashlytics.log(0, "RootFile", "Advanced mode = $advancedMode\n" +
                "Number of files found: ${filesList.size}\n" +
                "Filelist: $rawList")*/

        /*Log.d( "RootFile", "Advanced mode = $advancedMode\n" +
                "ls output: $testOutput \n" +
                "Number of files found: ${filesList.size}\n" +
                "Filelist: $rawList")*/

        return filesList
    }

}
