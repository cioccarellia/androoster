package com.andreacioccarelli.androoster.ui.settings

import android.support.annotation.ColorRes

import com.andreacioccarelli.androoster.R

class BuiltInThemes {
    object OBSIDIAN {
        val KEY = "OBSIDIAN"
        var dark = true
        @ColorRes
        var primaryColor = R.color.BlueGrey_900
        @ColorRes
        var accentColor = R.color.Red_700
    }

    object CLEAN_GRASS {
        val KEY = "CLEAN_GRASS"
        var dark = true
        @ColorRes
        var primaryColor = R.color.BlueGrey_700
        @ColorRes
        var accentColor = R.color.Green_A400
    }

    object GOLD {
        val KEY = "GOLD"
        var dark = true
        @ColorRes
        var primaryColor = R.color.BlueGrey_900
        @ColorRes
        var accentColor = R.color.Amber_700
    }

    object GALAXY {
        val KEY = "GALAXY"
        var dark = true
        @ColorRes
        var primaryColor = R.color.Indigo_500
        @ColorRes
        var accentColor = R.color.Pink_500
    }

    object OXYGEN {
        val KEY = "OXYGEN"
        var dark = true
        @ColorRes
        var primaryColor = R.color.Cyan_600
        @ColorRes
        var accentColor = R.color.Blue_A400
    }

    object METALLIC {
        val KEY = "METALLIC"
        var dark = true
        @ColorRes
        var primaryColor = R.color.BlueGrey_900
        @ColorRes
        var accentColor = R.color.BlueGrey_500
    }

    object ORANGED {
        val KEY = "ORANGED"
        var dark = false
        @ColorRes
        var primaryColor = R.color.Amber_800
        @ColorRes
        var accentColor = R.color.DeepOrange_500
    }

    object SEA {
        val KEY = "SEA"
        var dark = false
        @ColorRes
        var primaryColor = R.color.Teal_900
        @ColorRes
        var accentColor = R.color.Blue_A700
    }

    object SHROOB {
        val KEY = "SHROOB"
        var dark = false
        @ColorRes
        var primaryColor = R.color.Grey_900
        @ColorRes
        var accentColor = R.color.Purple_400
    }

    object RIVER {
        val KEY = "RIVER"
        var dark = false
        @ColorRes
        var primaryColor = R.color.BlueGrey_800
        @ColorRes
        var accentColor = R.color.Cyan_500
    }

    object CASA_DE_PAPEL {
        val KEY = "CASA_DE_PAPEL"
        var dark = false
        @ColorRes
        var primaryColor = R.color.Red_500
        @ColorRes
        var accentColor = R.color.Red_A700
    }
}
