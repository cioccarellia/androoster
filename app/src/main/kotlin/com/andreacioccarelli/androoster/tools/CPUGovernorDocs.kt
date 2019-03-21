package com.andreacioccarelli.androoster.tools

import android.support.annotation.StringRes
import com.andreacioccarelli.androoster.R

/**
 * Created by andrea on 2018/Jun.
 * Part of the package com.andreacioccarelli.androoster.tools
 */
object CPUGovernorDocs {

    @StringRes
    fun grab(governor: String): Int {
        return when (governor.toLowerCase()) {
            "ondemand" -> R.string.cpu_governor_content_ondemand
            "ondemandx" -> R.string.cpu_governor_content_ondemandx
            "performance" -> R.string.cpu_governor_content_performance
            "powersave" -> R.string.cpu_governor_content_powersave
            "conservative" -> R.string.cpu_governor_content_conservative
            "userspace" -> R.string.cpu_governor_content_userspace
            "minmax" -> R.string.cpu_governor_content_minmax
            "interactive" -> R.string.cpu_governor_content_interactive
            else -> R.string.cpu_governor_content_unknown
        }
    }
}