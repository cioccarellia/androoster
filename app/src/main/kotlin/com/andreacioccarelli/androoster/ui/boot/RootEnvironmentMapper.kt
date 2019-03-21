package com.andreacioccarelli.androoster.ui.boot

import android.content.Context
import android.content.pm.PackageManager

import com.andreacioccarelli.androoster.R
import com.andreacioccarelli.androoster.interfaces.RootCodes
import com.jrummyapps.android.shell.Shell

class RootEnvironmentMapper {
    companion object {

        private fun getSuperuserCode(ctx: Context): Int {
            try {
                val Output = getOutput("su -v", false).toLowerCase()
                if (Output.contains("not found")) return RootCodes.NOT_INSTALLED
                try {
                    Output.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                } catch (e: ArrayIndexOutOfBoundsException) {
                    return RootCodes.NOT_INSTALLED
                }

                if (Output.contains("connection to ui") || Output.contains("timed out")) {
                    return RootCodes.BROKEN
                } else if (Output.contains("error")) {
                    return RootCodes.NOT_INSTALLED
                }

                if (Output.contains("kingroot")) return RootCodes.KINGROOT_CODE
                if (Output.contains("kingoroot")) return RootCodes.KINGOROOT_CODE
                if (Output.contains("supersu")) return RootCodes.SUPERSU_CODE
                if (Output.contains("magisk") || Output.contains("topjohnwu")) return RootCodes.MAGISK_CODE

                if (isPackageInstalled(RootCodes.iRootPackageName, ctx)) return RootCodes.IROOT_CODE
                if (isPackageInstalled(RootCodes.MySuPackageName, ctx)) return RootCodes.MYSU_CODE
                if (isPackageInstalled(RootCodes.CWMPackageName, ctx)) return RootCodes.CWM_CODE
                if (isPackageInstalled(RootCodes.ChainsDDPackageName, ctx)) return RootCodes.CHAINSDD_CODE
                return if (isPackageInstalled(RootCodes.KingoRootPackageName, ctx)) RootCodes.KINGOROOT_CODE else RootCodes.ROOT_CODE

// No app found
            } catch (e: RuntimeException) {
                return RootCodes.NOT_INSTALLED
            }

        }

        fun getSuperuserApp(withVersion: Boolean, ctx: Context): String {
            when (getSuperuserCode(ctx)) {
                RootCodes.SUPERSU_CODE -> return "SuperSU" + if (withVersion) " $rootVersion" else ""
                RootCodes.KINGROOT_CODE -> return "Kingroot" + if (withVersion) " $rootVersion" else ""
                RootCodes.KINGOROOT_CODE -> return "Kingoroot" + if (withVersion) " $rootVersion" else ""
                RootCodes.MAGISK_CODE -> return "MagiskSU" + if (withVersion) " $rootVersion" else ""
                RootCodes.IROOT_CODE -> return "iRoot" + if (withVersion) " $rootVersion" else ""
                RootCodes.MYSU_CODE -> return "MySu" + if (withVersion) " $rootVersion" else ""
                RootCodes.CWM_CODE -> return "SuperUser" + if (withVersion) " $rootVersion" else ""
                RootCodes.ROOT_CODE -> return ctx.getString(R.string.dashboard_widget_software_unknown_su)
                else -> return ctx.getString(R.string.dashboard_widget_software_unknown_su)
            }
        }

        fun getSuperuserPackage(ctx: Context): String {
            return when (getSuperuserCode(ctx)) {
                RootCodes.SUPERSU_CODE -> "eu.chainfire.supersu"
                RootCodes.KINGROOT_CODE -> "com.kingroot.kinguser"
                RootCodes.KINGOROOT_CODE -> "com.kingoapp.apk"
                RootCodes.MAGISK_CODE -> "com.topjohnwu.magisk"
                else -> ""
            }
        }

        val rootVersion: String
            get() {
                try {
                    return 'v' + getOutput("su -v", false).toLowerCase().split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].replace("v", "").replace("_su", "")
                } catch (e: ArrayIndexOutOfBoundsException) {
                    return "null"
                }

            }

        val busyboxVersion: String
            get() {
                var tmpResult = ""
                try {
                    val stdout = Shell.SH.run("busybox").getStdout()
                    tmpResult = stdout.substring(stdout.indexOf("v"), stdout.indexOf("(") - 1)
                    return tmpResult.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                } catch (BusyBoxNotFound: StringIndexOutOfBoundsException) {
                    return tmpResult.replace("-jrummy", "").replace("-Stericson", "").replace("-osm0sis", "")
                }

            }

        private fun isPackageInstalled(packageName: String, baseContext: Context): Boolean {
            try {
                val PM = baseContext.packageManager
                PM.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
                return true
            } catch (e: PackageManager.NameNotFoundException) {
                return false
            }

        }

        private fun getOutput(Command: String, ar: Boolean): String {
            return if (ar) {
                Shell.SU.run(Command).getStdout().replace("\n", "").replace(" ", "")
            } else {
                Shell.SH.run(Command).getStdout().replace("\n", "").replace(" ", "")
            }
        }
    }
}
