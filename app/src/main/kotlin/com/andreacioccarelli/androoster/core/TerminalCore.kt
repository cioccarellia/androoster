package com.andreacioccarelli.androoster.core

import com.jrummyapps.android.shell.CommandResult
import com.jrummyapps.android.shell.Shell

/**
 * Created by andrea on 2017/nov.
 * Part of the package com.andreacioccarelli.androoster.core
 */

object TerminalCore {
    fun run(c: String): CommandResult {
        mount()
        // Crashlytics.log(0, "TerminalCore run()", c)
        return Shell.SU.run(c)
    }

    fun crun(c: String): CommandResult {
        // Crashlytics.log(0, "TerminalCore crun()", c)
        return Shell.SU.run(c)
    }

    fun crun(vararg c: String): CommandResult {
        return Shell.SU.run(*c)
    }

    fun arun(c: String) {
        CoroutineScope(Dispatchers.Main).launch { Shell.SU.run(c) }
    }

    fun arun(vararg c: String) {
        CoroutineScope(Dispatchers.Main).launch {
            Shell.SU.run(*c)
        }
    }

    fun run(vararg c: String): CommandResult {
        var log = ""
        for (str in c) {
            log += "$c\n"
        }
        // Crashlytics.log(0, "TerminalCore run()", log)
        mount()
        return Shell.SU.run(*c)
    }

    fun mount(): CommandResult {
        // Crashlytics.log(0, "TerminalCore", "Mount RW")
        return Shell.SU.run("mount -o remount,rw,remount,rw,remount /system",
                "mount -o remount,rw,remount,rw,remount /sys",
                "mount -o remount,rw,remount,rw,remount /data")
    }

    fun mountro(): CommandResult {
        // Crashlytics.log(0, "TerminalCore", "Mount RO")
        return Shell.SU.run("mount -o remount,ro,remount,ro,remount /system",
                "mount -o remount,ro,remount,ro,remount /data")
    }


    fun mount(fs: String): CommandResult {
        // Crashlytics.log(0, "TerminalCore", "Mount RW $fs")
        return Shell.SU.run("mount -o remount,rw,remount,rw,remount $fs")
    }

    fun mountro(fs: String): CommandResult {
        // Crashlytics.log(0, "TerminalCore", "Mount RO $fs")
        return Shell.SU.run("mount -o remount,ro,remount,ro,remount $fs")
    }

    internal object SETTINGS {
        fun put(namespace: String, key: String, value: String) {
            // Crashlytics.log(0, "TerminalCore", "Settings put $namespace $key $value")
            CoroutineScope(Dispatchers.Main).launch { Shell.SU.run("settings put " + namespace + ' '.toString() + key + ' '.toString() + value) }
        }

        fun delete(namespace: String, key: String) {
            // Crashlytics.log(0, "TerminalCore", "Settings delete $namespace $key")
            CoroutineScope(Dispatchers.Main).launch { Shell.SU.run("settings delete $namespace") }
        }

        fun list(namespace: String): String {
            return Shell.SU.run("settings list $namespace").getStdout().trim()
        }

        operator fun get(namespace: String, key: String): String {
            val result = Shell.SU.run("settings get " + namespace + ' '.toString() + key).getStdout().trim { it <= ' ' }
            // Crashlytics.log(0, "TerminalCore", " Settings get $namespace $key: $result")
            return result
        }
    }


}