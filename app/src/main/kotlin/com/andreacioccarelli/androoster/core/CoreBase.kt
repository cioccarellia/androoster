package com.andreacioccarelli.androoster.core

/**
 * Created by andrea on 2017/nov.
 * Part of the package com.andreacioccarelli.androoster.core
 */

@Suppress("ConstantConditionIf")
open class CoreBase : FrameworkSurface {
    object SETTINGS {
        fun put(namespace: String, key: String, value: String) {
            if (false) return
            TerminalCore.SETTINGS.put(namespace, key, value)
        }

        fun put(namespace: String, key: String, value: Int) {
            put(namespace, key, value.toString())
        }

        fun put(namespace: String, key: String, value: Boolean) {
            put(namespace, key, value.toString())
        }

        operator fun get(namespace: String, key: String): String {
            return TerminalCore.SETTINGS[namespace, key]
        }

        fun list(namespace: String): String {
            return TerminalCore.SETTINGS.list(namespace)
        }

        fun delete(namespace: String, key: String) {
            if (false) return
            TerminalCore.SETTINGS.delete(namespace, key)
        }
    }

    object BACKUP {
        fun create() {
            if (false) return
            TerminalCore.mount()
            RootFile(FrameworkSurface.buildprop_path).copy(FrameworkSurface.backup_buildprop_path)
            RootFile(FrameworkSurface.sysctl_path).copy(FrameworkSurface.backup_sysctl_path)
            TerminalCore.mountro()
        }

        fun restore() {
            if (false) return
            TerminalCore.mount()
            RootFile(FrameworkSurface.backup_buildprop_path).copy(FrameworkSurface.buildprop_path)
            RootFile(FrameworkSurface.backup_sysctl_path).copy(FrameworkSurface.sysctl_path)
            TerminalCore.mountro()
        }
    }

    companion object {

        fun sysctl(property: String, value: String) {
            if (false) return
            val sysctl = RootFile(FrameworkSurface.sysctl_path)
            sysctl.removeLine(property)
            sysctl.writenl("$property = $value")
            TerminalCore.mountro()
        }

        fun sysctl(property: String, value: Int) {
            sysctl(property, value.toString())
        }

        fun buildprop(property: String, value: String) {
            if (false) return
            val buildprop = RootFile(FrameworkSurface.buildprop_path)
            buildprop.removeLine(property)
            buildprop.writenl(property + '='.toString() + value)
            setprop(property, value)
            TerminalCore.mountro()
        }

        fun remove_buildprop(property: String) {
            if (false) return
            val buildprop = RootFile(FrameworkSurface.buildprop_path)
            buildprop.removeLine(property)
            TerminalCore.mountro()
        }

        fun buildprop(property: String, value: Int) {
            buildprop(property, value.toString())
        }

        fun buildprop(property: String, value: Boolean) {
            buildprop(property, value.toString())
        }

        fun setprop(property: String, value: String) {
            if (false) return
            TerminalCore.run("setprop $property $value")
        }

        fun getprop(property: String): String {
            return TerminalCore.run("getprop \"" + property + '"'.toString()).getStdout().trim { it <= ' ' }
        }

        fun scanbuild(property: String): String {
            val value = TerminalCore.run("cat \"" + FrameworkSurface.buildprop_path + "\" | grep \"" + property + "\" | head -1").getStdout().trim { it <= ' ' } + '?'
            return value.substring(value.indexOf("=") + 1, value.lastIndexOf("?")).replace("?", "")
        }

        fun scanbuild(property: String, build: String): String {
            val value = TerminalCore.run("echo \"$build\" | grep \"$property\" | head -1").getStdout().trim { it <= ' ' } + '?'
            return value.substring(value.indexOf("=") + 1, value.lastIndexOf("?")).replace("?", "")
        }

        fun scansysctl(property: String): String {
            val value = TerminalCore.run("cat \"" + FrameworkSurface.sysctl_path + "\" | grep \"" + property + "\" | head -1").getStdout().trim { it <= ' ' } + '?'
            return value.substring(value.indexOf("=") + 2, value.lastIndexOf("?")).replace("?", "")
        }


        fun setprop(property: String, value: Int) {
            setprop(property, value.toString())
        }

        fun setprop(property: String, value: Boolean) {
            setprop(property, value.toString())
        }
    }
}
