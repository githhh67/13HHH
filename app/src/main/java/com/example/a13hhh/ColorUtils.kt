package com.example.a13hhh

import android.graphics.drawable.GradientDrawable

object ColorUtils {

    fun createVerySubtleGradient(color: Int): GradientDrawable {
        return GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(
                0xFFFFFFFF.toInt(),
                getColorWithOpacity(color, 0.15f)
            )
        ).apply {
            cornerRadius = 8f
        }
    }

    fun createSubtleGradientWithTinyRadius(color: Int): GradientDrawable {
        return GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(
                0xFFFFFFFF.toInt(),
                getColorWithOpacity(color, 0.15f)
            )
        ).apply {
            cornerRadius = 4f // Меньший радиус
        }
    }

    private fun getColorWithOpacity(color: Int, opacity: Float): Int {
        val alpha = (255 * opacity).toInt()
        val red = color shr 16 and 0xFF
        val green = color shr 8 and 0xFF
        val blue = color and 0xFF

        return (alpha shl 24) or (red shl 16) or (green shl 8) or blue
    }
}