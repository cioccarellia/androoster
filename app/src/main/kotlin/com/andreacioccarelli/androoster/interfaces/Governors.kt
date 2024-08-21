package com.andreacioccarelli.androoster.interfaces

import android.annotation.SuppressLint

@SuppressLint("SdCardPath")
interface Governors {
    companion object {
        const val USERSPACE = "userspace"
        const val PERFORMANCES = "performances"
        const val INTERACTIVE = "interactive"
        const val POWERSAVE = "powersave"
        const val MIN_MAX = "minmax"
        const val CONSERVATIVE = "conservative"
        const val ONDEMAND = "ondemand"
    }

}
