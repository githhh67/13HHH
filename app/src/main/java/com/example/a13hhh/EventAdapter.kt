package com.example.a13hhh

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class EventAdapter(
    private val events: List<Event>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(
        view: View,
        private val onItemClick: (Int) -> Unit
    ) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.eventTitle)
        val time: TextView = view.findViewById(R.id.eventTime)
        val description: TextView = view.findViewById(R.id.eventDescription)
        val topBar: View = view.findViewById(R.id.topColorBar)
        val cardView: androidx.cardview.widget.CardView = view.findViewById(R.id.cardView)

        init {
            view.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onItemClick(adapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        holder.title.text = event.title
        holder.time.text = event.time
        holder.description.text = event.description

        holder.topBar.setBackgroundColor(event.color)

        val gradient = ColorUtils.createVerySubtleGradient(event.color)
        holder.cardView.background = gradient

        holder.title.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.text_primary))
        holder.description.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.text_secondary))
    }

    override fun getItemCount() = events.size
}