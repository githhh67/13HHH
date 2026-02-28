package com.example.a13hhh

import java.io.Serializable
import java.util.*

data class CalendarDay(
    val day: Int,
    val month: Int,
    val year: Int,
    val isCurrentMonth: Boolean = true,
    val hasEvent: Boolean = false,
    val isToday: Boolean = false,
    val isSelected: Boolean = false
) : Serializable {

    fun getDateString(): String {
        return String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, day)
    }

    companion object {
        fun fromDateString(dateStr: String): CalendarDay {
            val parts = dateStr.split("-")
            return if (parts.size == 3) {
                CalendarDay(
                    day = parts[2].toInt(),
                    month = parts[1].toInt(),
                    year = parts[0].toInt()
                )
            } else {
                CalendarDay(1, 1, 2026)
            }
        }
    }
}