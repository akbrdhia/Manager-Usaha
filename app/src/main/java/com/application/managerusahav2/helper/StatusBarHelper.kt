package com.application.managerusahav2.helper

import android.app.Activity
import androidx.annotation.ColorInt
import androidx.core.view.WindowInsetsControllerCompat

object StatusBarHelper {

    // Simpan default state (misal: warna & mode light/dark)
    private var defaultLight: Boolean? = null
    private var defaultColor: Int? = null

    fun setStatusBar(activity: Activity, isLight: Boolean, @ColorInt color: Int? = null) {
        val window = activity.window
        val controller = WindowInsetsControllerCompat(window, window.decorView)

        // Kalau belum pernah disimpan, simpan dulu default value
        if (defaultLight == null) {
            defaultLight = controller.isAppearanceLightStatusBars
            defaultColor = window.statusBarColor
        }

        // Atur sesuai parameter
        controller.isAppearanceLightStatusBars = isLight
        color?.let { window.statusBarColor = it }
    }

    // Reset balik ke default
    fun resetStatusBar(activity: Activity) {
        val window = activity.window
        val controller = WindowInsetsControllerCompat(window, window.decorView)

        defaultLight?.let { controller.isAppearanceLightStatusBars = it }
        defaultColor?.let { window.statusBarColor = it }
    }
}
