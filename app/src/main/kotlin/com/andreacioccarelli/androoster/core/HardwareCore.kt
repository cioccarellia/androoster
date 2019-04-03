package com.andreacioccarelli.androoster.core

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.FeatureInfo
import android.os.Build
import android.provider.Settings
import java.io.File
import java.io.FileFilter
import java.io.IOException
import java.io.RandomAccessFile
import java.text.DecimalFormat
import java.util.regex.Pattern

/**
 * Created by andrea on 2017/nov.
 * Part of the package com.andreacioccarelli.androoster.core
 */

class HardwareCore {
    companion object {
        val cores: Int
            get() {
                if (Build.VERSION.SDK_INT >= 17) {
                    return Runtime.getRuntime().availableProcessors()
                } else {
                    class CpuFilter : FileFilter {
                        override fun accept(pathname: File): Boolean {
                            return Pattern.matches("cpu[0-9]+", pathname.name)
                        }
                    }

                    return try {
                        val dir = File("/sys/devices/system/cpu/")
                        val files = dir.listFiles(CpuFilter())
                        files.size
                    } catch (e: Exception) {
                        1
                    }

                }
            }

        val ram: String
            get() {
                val reader: RandomAccessFile
                val load: String
                val twoDecimalForm = DecimalFormat("#.##")
                val totRam: Double
                var lastValue = ""
                try {
                    reader = RandomAccessFile("/proc/meminfo", "r")
                    load = reader.readLine()

                    val p = Pattern.compile("(\\d+)")
                    val m = p.matcher(load)
                    var value = ""
                    while (m.find()) {
                        value = m.group(1)
                    }
                    reader.close()

                    totRam = java.lang.Double.parseDouble(value)

                    val mb = totRam / 1024.0
                    val gb = totRam / 1048576.0
                    val tb = totRam / 1073741824.0

                    lastValue = when {
                        tb > 1 -> twoDecimalForm.format(tb) + " TB"
                        gb > 1 -> twoDecimalForm.format(gb) + " GB"
                        mb > 1 -> twoDecimalForm.format(mb) + " MB"
                        else -> twoDecimalForm.format(totRam) + " KB"
                    }

                } catch (ex: IOException) {
                    ex.printStackTrace()
                }

                return lastValue
            }

        val ramInGb: Float
            get() {
                val reader: RandomAccessFile
                val load: String
                val totRam: Double
                val lastValue = 0F
                try {
                    reader = RandomAccessFile("/proc/meminfo", "r")
                    load = reader.readLine()

                    val p = Pattern.compile("(\\d+)")
                    val m = p.matcher(load)
                    var value = ""
                    while (m.find()) {
                        value = m.group(1)
                    }
                    reader.close()

                    totRam = java.lang.Double.parseDouble(value)

                    return (totRam / 1048576.0).toFloat()


                } catch (ex: IOException) {
                    ex.printStackTrace()
                }

                return lastValue
            }


        @Suppress("DEPRECATION")
        val arch: String
            get() {
                return if (Build.CPU_ABI == "arm64-v8a") {
                    "arm64"
                } else if (Build.CPU_ABI == "x86_64") {
                    "x86_64"
                } else if (Build.CPU_ABI == "mips64") {
                    "mips64"
                } else if (Build.CPU_ABI.startsWith("x86") || Build.CPU_ABI2.startsWith("x86")) {
                    "x86"
                } else if (Build.CPU_ABI.startsWith("mips")) {
                    "mips"
                } else if (Build.CPU_ABI.startsWith("armeabi-v5") || Build.CPU_ABI.startsWith("armeabi-v6")) {
                    "armv5"
                } else {
                    "arm"
                }
            }


        @SuppressLint("PrivateApi")
        fun getBatteryCapacity(ctx: Context): String {
            var mPowerProfile_: Any? = null
            val POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile"

            try {
                mPowerProfile_ = Class.forName(POWER_PROFILE_CLASS)
                        .getConstructor(Context::class.java).newInstance(ctx)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return try {
                val batteryCapacity = Class
                        .forName(POWER_PROFILE_CLASS)
                        .getMethod("getAveragePower", java.lang.String::class.java)
                        .invoke(mPowerProfile_, "battery.capacity") as Double
                batteryCapacity.toString() + " MAh"
            } catch (e: Exception) {
                e.printStackTrace()
                "Unknown"
            }
        }

        object BUILD {

            val heapsize: String
                get() {
                    return CoreBase.scanbuild("dalvik.vm.heapsize")
                }

            fun getMaxHeapsize(): String {
                return CoreBase.scanbuild("dalvik.vm.heapmaxfree")
            }

            fun getMinHeapsize(): String {
                return CoreBase.scanbuild("dalvik.vm.heapminfree")
            }

            fun getTargetUtilization(build: String): String {
                return CoreBase.scanbuild("dalvik.vm.heaptargetutilization", build)
            }

            fun getFlags(build: String): String {
                return CoreBase.scanbuild("dalvik.vm.dexopt-flags", build)
            }

            fun getGrowthLimit(build: String): String {
                return CoreBase.scanbuild("dalvik.vm.heapgrowthlimit", build)
            }

        }

        @SuppressLint("HardwareIds")
        fun getAndroidId(baseContext: Context): String {
            return Settings.Secure.getString(baseContext.contentResolver, Settings.Secure.ANDROID_ID)
        }


        fun getGLVersion(context: Context): Int {
            val packageManager = context.packageManager
            val featureInfos = packageManager.systemAvailableFeatures
            if (featureInfos != null && featureInfos.size > 0) {
                for (featureInfo in featureInfos) {
                    // Null feature KEY means this feature is the open gl es version feature.
                    if (featureInfo.name == null) {
                        return if (featureInfo.reqGlEsVersion != FeatureInfo.GL_ES_VERSION_UNDEFINED) {
                            featureInfo.reqGlEsVersion and -0x10000 shr 16
                        } else {
                            1 // Lack of property means OpenGL ES version 1
                        }
                    }
                }
            }
            return 1
        }
    }
}
