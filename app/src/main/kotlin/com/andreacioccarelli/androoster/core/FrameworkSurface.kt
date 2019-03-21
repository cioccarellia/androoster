package com.andreacioccarelli.androoster.core

import android.annotation.SuppressLint

import com.andreacioccarelli.androoster.BuildConfig

/**
 * Created by andrea on 2017/nov.
 * Part of the package com.andreacioccarelli.androoster.core
 */

interface FrameworkSurface {
    companion object {

        /**
         * Paths
         */
        val sysctl_path = "/system/etc/sysctl.conf"
        val buildprop_path = "/system/build.prop"
        @SuppressLint("SdCardPath")
        val data_dir = "/data/data/" + BuildConfig.APPLICATION_ID + '/'.toString()
        val backup_path = data_dir + "backup/"
        val backup_sysctl_path = data_dir + "backup/sysctl.conf"
        val backup_buildprop_path = data_dir + "backup/build.prop"


        /**
         * Namespaces
         */

        val SYSTEM = "system"
        val SECURE = "secure"
        val GLOBAL = "global"

        /**
         * Composition methods
         */

        /** Default method  */
        val gui_composition_method_cpu = "cpu"

        /** Default when debug.sf.hw = 1  */
        val cpu_composition_method = "cpu"
        val gpu_composition_method = "gpu"

        /** Alternative methods  */
        val gui_composition_method_c2n = "c2n"
        val gui_composition_method_mdp = "mdp"

        /**
         * Governors
         */

        val governor_ondemand = "ondemand"
        val governor_performance = "performance"
        val governor_powersave = "powersave"
        val governor_conservative = "conservative"
        val governor_userspace = "userspace"
        val governor_minmax = "minmax"
        val governor_interactive = "interactive"


        /**
         * LMKs
         */
        const val DEFAULT = 0
        const val VERY_LIGHT = 1
        const val LIGHT = 2
        const val NORMAL = 3
        const val AGGRESSIVE = 4
        const val VERY_AGGRESSIVE = 5
        const val INSANE = 6

        val LMK_VERY_LIGHT = "2048,3072,4608,6144,16384,20992"
        val LMK_LIGHT = "3584,7680,15872,20992,32768,44032"
        val LMK_NORMAL = "4096,8192,16384,32768,47360,57344"
        val LMK_AGGRESSIVE = "8192,16384,23552,33280,47872,64512"
        val LMK_VERY_AGGRESSIVE = "10752,16384,32768,38912,56320,67584"
        val LMK_INSANE = "12288,23552,32768,53248,57344,72704"

        val PROFILE_DEFAULT = 0
        val PROFILE_POWER_SAVING = 1
        val PROFILE_SMOOTH = 2
        val PROFILE_MULTITASKING = 3
    }

}
