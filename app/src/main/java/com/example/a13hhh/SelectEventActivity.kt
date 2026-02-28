package com.example.a13hhh

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class SelectEventActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var tvNoEvents: TextView
    private lateinit var btnCreateEvent: Button
    private lateinit var recycler: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_event)

        dbHelper = DatabaseHelper(this)

        recycler = findViewById<RecyclerView>(R.id.recyclerFutureEvents)
        tvNoEvents = findViewById<TextView>(R.id.tvNoFutureEvents)
        btnCreateEvent = findViewById<Button>(R.id.btnCreateEvent)

        recycler.layoutManager = LinearLayoutManager(this)

        findViewById<View>(R.id.btnBack).setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        loadFutureEvents()
    }

    override fun onResume() {
        super.onResume()
        loadFutureEvents()
    }

    private fun loadFutureEvents() {
        val futureEvents = dbHelper.getFutureEvents()

        if (futureEvents.isEmpty()) {
            tvNoEvents.visibility = View.VISIBLE
            tvNoEvents.text = "Будущих событий нет"
            recycler.visibility = View.GONE
        } else {
            tvNoEvents.visibility = View.GONE
            recycler.visibility = View.VISIBLE

            val eventsAdapter = FutureEventsAdapter(futureEvents) { event ->
                val intent = Intent(this, CreateReminderActivity::class.java)
                intent.putExtra("EVENT_ID", event.id)
                startActivity(intent)
                finish()
            }

            recycler.adapter = eventsAdapter
        }

        // Кнопка создания события ВСЕГДА видна
        btnCreateEvent.visibility = Button.VISIBLE
        btnCreateEvent.setOnClickListener {
            val intent = Intent(this, CreateEventActivity::class.java)
            intent.putExtra("SELECTED_DATE", getCurrentDate())
            startActivityForResult(intent, CREATE_EVENT_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CREATE_EVENT_REQUEST && resultCode == Activity.RESULT_OK) {
            loadFutureEvents()
        }
    }

    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, day)
    }

    companion object {
        private const val CREATE_EVENT_REQUEST = 100
    }
}

class FutureEventsAdapter(
    private val events: List<Event>,
    private val onItemClick: (Event) -> Unit
) : RecyclerView.Adapter<FutureEventsAdapter.EventViewHolder>() {

    class EventViewHolder(
        view: View,
        private val onItemClick: (Event) -> Unit,
        private val events: List<Event>
    ) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.eventTitle)
        val date: TextView = view.findViewById(R.id.eventDate)
        val time: TextView = view.findViewById(R.id.eventTime)
        val topBar: View = view.findViewById(R.id.topColorBar)
        val cardView: androidx.cardview.widget.CardView = view.findViewById(R.id.cardView)

        init {
            view.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onItemClick(events[adapterPosition])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event_select, parent, false)
        return EventViewHolder(view, onItemClick, events)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        holder.title.text = event.title

        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            val date = inputFormat.parse(event.date)
            holder.date.text = outputFormat.format(date ?: event.date)
        } catch (e: Exception) {
            holder.date.text = event.date
        }

        holder.time.text = event.time
        holder.topBar.setBackgroundColor(event.color)

        val gradient = ColorUtils.createVerySubtleGradient(event.color)
        holder.cardView.background = gradient
    }

    override fun getItemCount() = events.size
}