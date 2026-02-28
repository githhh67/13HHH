package com.example.a13hhh

import java.io.Serializable

data class Event(
    var id: Int = 0,
    var title: String,
    var time: String,
    var description: String,
    var date: String,
    var color: Int = 0
) : Serializable {

    companion object {
        private val rainbowColors = listOf(
            0xFF1744,
            0xFFFF9100,
            0xFFFFEA00,
            0xFF00E676,
            0xFF2979FF,
            0xFF304FFE,
            0xFFD500F9,
            0xFF6200EE
        ).map { it.toInt() }

        fun generateColorForDate(date: String): Int {
            val hash = date.hashCode()
            val index = Math.abs(hash) % rainbowColors.size
            return rainbowColors[index]
        }

        private var idCounter = 0
        fun createId(): Int {
            return ++idCounter
        }
    }
}