package com.example.a13hhh

import java.io.Serializable

data class Reminder(
    var reminderId: Int = 0,
    var eventId: Int = 0,
    var reminderTime: String = "",
    var reminderText: String = "",
    var isActive: Boolean = true,
    var eventTitle: String = "Без события",
    var eventDate: String = "",
    var eventTime: String = ""
) : Serializable {

    fun getFormattedReminderTime(): String {
        return try {
            val minutes = reminderTime.toInt()
            when (minutes) {
                0 -> "В момент события"
                5 -> "За 5 минут"
                15 -> "За 15 минут"
                30 -> "За 30 минут"
                60 -> "За 1 час"
                1440 -> "За 1 день"
                else -> {
                    if (minutes >= 60) {
                        val hours = minutes / 60
                        val remainingMinutes = minutes % 60
                        if (remainingMinutes > 0) {
                            "За $hours ч $remainingMinutes мин"
                        } else {
                            "За $hours ч"
                        }
                    } else {
                        "За $minutes минут"
                    }
                }
            }
        } catch (e: Exception) {
            "За $reminderTime минут"
        }
    }

    fun getFullEventDescription(): String {
        return if (eventTitle.isNotEmpty() && eventTime.isNotEmpty()) {
            "$eventTitle в $eventTime"
        } else if (eventTitle.isNotEmpty()) {
            eventTitle
        } else {
            "Событие не найдено"
        }
    }
}