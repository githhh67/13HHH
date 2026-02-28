package com.example.a13hhh

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class CalendarAdapter(
    private val days: List<CalendarDay>,
    private val onDayClick: (CalendarDay) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    class CalendarViewHolder(view: View, private val onDayClick: (CalendarDay) -> Unit) : RecyclerView.ViewHolder(view) {
        private val tvDay: TextView = view.findViewById(R.id.tvDay)
        private val dayContainer: View = view.findViewById(R.id.dayContainer)
        private val eventDot: View = view.findViewById(R.id.eventDot)
        private var currentDay: CalendarDay? = null

        init {
            dayContainer.setOnClickListener {
                currentDay?.let { day -> onDayClick(day) }
            }
        }

        fun bind(day: CalendarDay) {
            currentDay = day

            if (day.isCurrentMonth && day.day > 0) {
                tvDay.text = day.day.toString()
                tvDay.alpha = 1f
                dayContainer.isEnabled = true
            } else {
                tvDay.text = ""
                tvDay.alpha = 0.3f
                dayContainer.isEnabled = false
            }

            dayContainer.isSelected = day.isSelected

            dayContainer.isActivated = day.isToday

            if (day.hasEvent) {
                eventDot.visibility = View.VISIBLE
            } else {
                eventDot.visibility = View.GONE
            }

            if (day.isSelected) {
                tvDay.setTextColor(Color.WHITE)
            } else if (day.isToday) {
                tvDay.setTextColor(ContextCompat.getColor(itemView.context, R.color.primary_indigo))
            } else {
                tvDay.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_primary))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_day, parent, false)
        return CalendarViewHolder(view, onDayClick)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        holder.bind(days[position])
    }

    override fun getItemCount() = days.size
}