package com.andreacioccarelli.androoster.tools

import android.content.Context
import android.graphics.drawable.GradientDrawable

import com.kabouzeid.appthemehelper.ThemeStore

object GradientGenerator {
    fun get(ctx: Context): GradientDrawable {
        val primaryColor = ThemeStore.primaryColor(ctx)
        val accentColor = ThemeStore.accentColor(ctx)
        val gd = GradientDrawable(GradientDrawable.Orientation.TL_BR,
                intArrayOf(accentColor, primaryColor))
        gd.cornerRadius = 0f
        return gd
    }


    fun get(ctx: Context, orientation: GradientDrawable.Orientation = GradientDrawable.Orientation.TL_BR,
            cornerRadius: Float = 0f): GradientDrawable {
        val primaryColor = ThemeStore.primaryColor(ctx)
        val accentColor = ThemeStore.accentColor(ctx)
        val gd = GradientDrawable(orientation,
                intArrayOf(accentColor, primaryColor))
        gd.cornerRadius = cornerRadius
        return gd
    }


    fun get(startColor: Int, endColor: Int,
            orientation: GradientDrawable.Orientation = GradientDrawable.Orientation.TL_BR,
            cornerRadius: Float = 0f): GradientDrawable {
        val gd = GradientDrawable(orientation, intArrayOf(startColor, endColor))
        gd.cornerRadius = cornerRadius
        return gd
    }
}
