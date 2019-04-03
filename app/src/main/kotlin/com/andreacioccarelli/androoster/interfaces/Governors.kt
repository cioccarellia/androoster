package com.andreacioccarelli.androoster.interfaces

import android.annotation.SuppressLint

@SuppressLint("SdCardPath")
interface Governors {
    companion object {

        val USERSPACE = "userspace"
        val PERFORMANCES = "performances"
        val INTERACTIVE = "interactive"
        val POWERSAVE = "powersave"
        val MIN_MAX = "minmax"
        val CONSERVATIVE = "conservative"
        val ONDEMAND = "ondemand"
    }

}
