@file:Suppress("unused")

package com.andreacioccarelli.androoster.ui.backup

import android.content.Context
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.andreacioccarelli.androoster.R
import com.andreacioccarelli.androoster.core.FrameworkSurface
import com.andreacioccarelli.androoster.dataset.XmlKeys
import com.andreacioccarelli.androoster.tools.PreferencesBuilder
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.util.*
import kotlin.concurrent.schedule


/**
 * Created by andrea on 2018/apr.
 * Part of the package com.andreacioccarelli.androoster.tools
 */


class BackupPreferencesPatcher(val patcher: PreferencesBuilder, val dialog: MaterialDialog? = null, val ctx: Context) {

    var workWithDialog = true

    fun patchPreferences(rawContent: String) {
        if (dialog == null) workWithDialog = false
        var reader: BufferedReader

        doAsync {
            reader = BufferedReader(
                    InputStreamReader(ByteArrayInputStream(rawContent.toByteArray(Charsets.UTF_8)), "UTF-8"))

            var i = 1
            while (reader.readLine() != null) {
                if (workWithDialog) {
                    uiThread {
                        dialog?.setContent("${ctx.getString(R.string.backup_dialog_importing)} #$i")
                    }
                    Thread.sleep(2)
                }
                val str = reader.readLine() ?: break
                findMatches(str.trim())
                i++
            }

            reader.close()

            if (workWithDialog) {
                uiThread {
                    dialog!!.progressBar.visibility = View.GONE
                    dialog.setTitle(R.string.backup_dialog_done_title)
                    dialog.setContent(R.string.backup_dialog_done_content)
                    Timer().schedule(3000){
                        dialog.dismiss()
                    }
                }

            }
        }
    }

    fun findMatches(line: String) {
        if (line.startsWith("#")) return
        if (!line.contains("=")) return
        if (line.isEmpty()) return

        val subline = try {
            line.split("=")[1]
        } catch (oob: IndexOutOfBoundsException) { return }

        if (line.contains("hw3d.force"))
            if (line.on) patcher.on("GPU1") else patcher.off("GPU1")
        if (line.contains("ro.config.disable.hw_accel"))
            if (line.off) patcher.on("GPU1") else patcher.off("GPU1")

        if (line.contains("media.stagefright.enable-player"))
            if (line.on) patcher.on("GPU3") else patcher.off("GPU3")

        if (line.contains("video.accelerate.hw"))
            if (line.on) patcher.on("GPU2") else patcher.off("GPU2")


        if (line.contains("debug.composition.type"))
            if (subline.contains(FrameworkSurface.gpu_composition_method)) patcher.on("GPU4") else patcher.off("GPU4")

        if (line.contains("ro.media.dec.jpeg.memcap"))
            if (subline.contains("8000000")) patcher.on("GPU5") else patcher.off("GPU5")

        if (line.contains("com.qc.hardware"))
            if (line.on) patcher.on("CPU3") else patcher.off("CPU3")

        if (line.contains("debug.performance.tuning"))
            if (line.on) patcher.on("RAM3_S1") else patcher.off("RAM3_S1")

        if (line.contains("net.core.optmem_max"))
            if (line.contains("20480")) patcher.on("CPU5") else patcher.off("CPU5")

        if (line.contains("persist.sys.use_dithering"))
            if (line.on) patcher.on("CPU4") else patcher.off("CPU4")

        if (line.contains("persist.sys.use_16bpp_alpha"))
            if (line.on) patcher.on("HW2") else patcher.off("HW2")

        if (line.contains("dalvik.vm.heapsize"))
            when (subline) {
                "128m" -> patcher.putInt("RAMProfile", 1)
                "256m" -> patcher.putInt("RAMProfile", 2)
                "512m" -> patcher.putInt("RAMProfile", 3)
                "1024m" -> patcher.putInt("RAMProfile", 4)
                else -> patcher.putInt("RAMProfile", 0)
            }

        if (line.contains("dalvik.vm.lockprof.threshold"))
            when (subline) {
                "800" -> patcher.putInt("RAM0", FrameworkSurface.PROFILE_POWER_SAVING)
                "500" -> patcher.putInt("RAM0", FrameworkSurface.PROFILE_SMOOTH)
                "250" -> patcher.putInt("RAM0", FrameworkSurface.PROFILE_MULTITASKING)
                else -> patcher.putInt("RAM0", FrameworkSurface.PROFILE_DEFAULT)
            }

        if (line.contains("persist.service.zram"))
            if (line.on) patcher.on("RAM6") else patcher.off("RAM6")
        if (line.contains("ro.HOME_APP_ADJ"))
            if (line.on) patcher.on("RAM7") else patcher.off("RAM7")
        if (line.contains("windowsmgr.max_events_per_sec"))
            if ((subline == "190") || subline == "220" || subline == "260") patcher.on("RAM3_S2") else patcher.off("RAM3_S2")
        if (line.contains("vm.min_free_kbytes"))
            if (subline == "16384") patcher.on("RAM2") else  patcher.off("RAM2")
        if (line.contains("persist.sys.purgeable_assets"))
            if (line.on) patcher.on("RAM3_S1") else patcher.off("RAM3_S1")
        if (line.contains("wifi.supplicant_scan_interval"))
            patcher.putString(XmlKeys.WIFI_EDITED_TEXT_CACHE, subline)
        if (line.contains("ro.config.hwfeature_wakeupkey"))
            if (line.on) patcher.on("HW7") else patcher.off("HW7")
        if (line.contains("profiler.launch"))
            if (line.off) patcher.on("Battery3") else patcher.off("Battery3")
        if (line.contains("network.dns1"))
            if (line.contains("8.8.8.8")) patcher.on("NET1") else patcher.off("NET1")
        if (line.contains("network.ipv4.tcp_congestion_control"))
            if (subline == "cubic") patcher.on("NET4") else patcher.off("NET4")
        if (line.contains("persist.dbg.ims_volte_enable"))
            if (line.on) patcher.on("NET7") else patcher.off("NET7")
        if (line.contains("ro.ril.def.agps.mode"))
            if (subline.contains("2")) patcher.on("NET3") else patcher.off("NET3")


        if (line.contains("ro.ril.enable.amr.wideband"))
            if (line.on) patcher.on("NET3") else patcher.off("NET3")

        if (line.contains("profiler.force_disable_ulog"))
            if (line.on) patcher.on("Debug3") else patcher.off("Debug3")

        if (line.contains("ro.ril.power_collapse"))
            if (line.on) patcher.on("Battery3") else patcher.off("Battery3")

        if (line.contains("ro.config.hw_quickpoweron"))
            if (line.on) patcher.on("General1") else patcher.off("General1")

        if (line.contains("ro.config.nocheckin"))
            if (line.on) patcher.on("Debug4") else patcher.off("Debug4")

        if (line.contains("persist.adb.notify"))
            if (line.on) patcher.on("Debug2") else patcher.off("Debug2")

        if (line.contains("pm.sleep_mode"))
            if (line.on) patcher.on("battery6") else patcher.off("battery6")

        if (line.contains("debugtool.anrhistory"))
            if (line.on) patcher.on("Debug5") else patcher.off("Debug5")

        if (line.contains("debug.egl.hw"))
            if (line.on) patcher.on("Debug6") else patcher.off("Debug6")

        if (line.contains("vm.swappiness"))
            if (line.on) patcher.on("Kernel1") else patcher.off("Kernel1")

        if (line.contains("ro.kernel.checkjni"))
            if (line.off) patcher.on("Kernel3") else patcher.off("Kernel3")

        if (line.contains("kernel.panic_on_oops"))
            if (line.on) patcher.on("Kernel6") else patcher.off("Kernel6")

        if (line.contains("kernel.shmall"))
            if (line.contains("16777216")) patcher.on("Kernel7") else patcher.off("Kernel7")
        if (line.contains("debug.sf.nobootanimation"))
            if (line.on) patcher.on("General0") else patcher.off("General0")
        if (line.contains("ro.allow.mock.location"))
            if (line.on) patcher.on("GPS2") else patcher.off("GPS2")
        if (line.contains("ro.telephony.call_ring.delay"))
            if (line.off) patcher.on("General5") else patcher.off("General5")
        if (line.contains("ro.lge.proximity.delay"))
            if (subline.contains("25")) patcher.on("General4") else patcher.off("General4")
        if (line.contains("ro.product.max_num_touch"))
            patcher.putString("General3", (subline.toInt() + 2).toString())


        if (line.contains("windowsmgr.support_rotation_180"))
            if (line.on) patcher.on("HW4") else patcher.off("HW4")
        if (line.contains("video.accelerate.hw"))
            if (line.on) patcher.on("HW1") else patcher.off("HW1")

    }

}

private fun PreferencesBuilder.on(key: String) {
    this.putBoolean(key, true)
}

private fun PreferencesBuilder.off(key: String) {
    this.putBoolean(key, false)
}
private fun PreferencesBuilder.set(key: String, value: String) {
    this.putString(key, value)
}
private fun PreferencesBuilder.set(key: String, value: Int) {
    this.putString(key, value.toString())
}

private val String.on: Boolean
    get() {
        return try {
            try {
                this.toInt() == 1
            } catch (nfe: java.lang.NumberFormatException) {
                try {
                    this.split("=")[1] == "true"
                } catch (oob: ArrayIndexOutOfBoundsException) {
                    false
                }
            }
        } catch (e: Exception) {
            false
        }
    }

private val String.off: Boolean
    get() {
        return !this.on
    }
