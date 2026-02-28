package com.example.a13hhh

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity2 : AppCompatActivity() {

    private lateinit var allEvents: MutableList<Event>
    private lateinit var adapter: GroupedEventAdapter
    private lateinit var tvNoEvents: TextView
    private lateinit var dbHelper: DatabaseHelper

    private val editEventLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            data?.let {
                if (it.hasExtra("UPDATED_EVENT")) {
                    val updatedEvent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        it.getSerializableExtra("UPDATED_EVENT", Event::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        it.getSerializableExtra("UPDATED_EVENT") as? Event
                    }

                    if (updatedEvent != null) {
                        dbHelper.updateEvent(updatedEvent)
                        refreshEventsFromDatabase()
                    }
                } else if (it.hasExtra("DELETE_EVENT")) {
                    val eventToDelete = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        it.getSerializableExtra("DELETE_EVENT", Event::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        it.getSerializableExtra("DELETE_EVENT") as? Event
                    }

                    eventToDelete?.let { event ->
                        dbHelper.deleteEvent(event.id)
                        refreshEventsFromDatabase()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_main2)

        dbHelper = DatabaseHelper(this)

        val recycler = findViewById<RecyclerView>(R.id.recyclerAllEvents)
        val searchBar = findViewById<EditText>(R.id.searchBar)
        tvNoEvents = findViewById(R.id.tvNoEvents)

        recycler.layoutManager = LinearLayoutManager(this)

        allEvents = dbHelper.getAllEvents().toMutableList()

        sortAndGroupEvents()

        adapter = GroupedEventAdapter(allEvents.groupBy { it.date }.toSortedMap()) { event ->
            val intent = Intent(this, EditEventActivity::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.putExtra("EVENT", event)
            } else {
                @Suppress("DEPRECATION")
                intent.putExtra("EVENT", event)
            }
            editEventLauncher.launch(intent)
        }

        recycler.adapter = adapter

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterEvents(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        checkIfNoEvents()

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.nav_events

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_calendar -> {
                    startActivity(Intent(this, MainActivity1::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    true
                }
                R.id.nav_events -> true
                R.id.nav_reminders -> {
                    startActivity(Intent(this, MainActivity3::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshEventsFromDatabase()
    }

    private fun refreshEventsFromDatabase() {
        allEvents = dbHelper.getAllEvents().toMutableList()
        sortAndGroupEvents()
        adapter = GroupedEventAdapter(allEvents.groupBy { it.date }.toSortedMap()) { event ->
            val intent = Intent(this, EditEventActivity::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.putExtra("EVENT", event)
            } else {
                @Suppress("DEPRECATION")
                intent.putExtra("EVENT", event)
            }
            editEventLauncher.launch(intent)
        }
        findViewById<RecyclerView>(R.id.recyclerAllEvents).adapter = adapter
        checkIfNoEvents()
    }

    private fun sortAndGroupEvents() {
        allEvents.sortWith(compareBy<Event> { event ->
            try {
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                format.parse(event.date).time
            } catch (e: ParseException) {
                0L
            }
        }.thenBy { event ->
            try {
                val timeParts = event.time.split(":")
                if (timeParts.size == 2) {
                    timeParts[0].toInt() * 60 + timeParts[1].toInt()
                } else {
                    0
                }
            } catch (e: Exception) {
                0
            }
        })
    }

    private fun filterEvents(query: String) {
        val filteredEvents = if (query.isEmpty()) {
            allEvents
        } else {
            val lowerCaseQuery = query.lowercase(Locale.getDefault())
            allEvents.filter { event ->
                event.title.lowercase(Locale.getDefault()).contains(lowerCaseQuery) ||
                        event.description.lowercase(Locale.getDefault()).contains(lowerCaseQuery) ||
                        event.date.contains(query) ||
                        event.time.contains(query)
            }
        }

        val groupedEvents = filteredEvents.groupBy { it.date }.toSortedMap()

        adapter = GroupedEventAdapter(groupedEvents) { event ->
            val intent = Intent(this, EditEventActivity::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.putExtra("EVENT", event)
            } else {
                @Suppress("DEPRECATION")
                intent.putExtra("EVENT", event)
            }
            editEventLauncher.launch(intent)
        }

        findViewById<RecyclerView>(R.id.recyclerAllEvents).adapter = adapter
        checkIfNoEvents(filteredEvents.isEmpty() && query.isNotEmpty())
    }

    private fun checkIfNoEvents(noSearchResults: Boolean = false) {
        val searchBar = findViewById<EditText>(R.id.searchBar)
        val query = searchBar.text.toString()

        if (noSearchResults && query.isNotEmpty()) {
            tvNoEvents.visibility = TextView.VISIBLE
            tvNoEvents.text = "События не найдены"
        } else if (allEvents.isEmpty()) {
            tvNoEvents.visibility = TextView.VISIBLE
            tvNoEvents.text = "Событий нет"
        } else {
            tvNoEvents.visibility = TextView.GONE
        }
    }
}

class GroupedEventAdapter(
    private val groupedEvents: Map<String, List<Event>>,
    private val onItemClick: (Event) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_EVENT = 1
    }

    private val items = mutableListOf<Any>()

    init {
        groupedEvents.forEach { (date, events) ->
            items.add(date)
            items.addAll(events)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position] is String) TYPE_HEADER else TYPE_EVENT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_date_header, parent, false)
            DateHeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_event, parent, false)
            EventViewHolder(view, onItemClick)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DateHeaderViewHolder -> {
                val date = items[position] as String
                holder.bind(date)
            }
            is EventViewHolder -> {
                val event = items[position] as Event
                holder.bind(event)
            }
        }
    }

    override fun getItemCount() = items.size

    class DateHeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvDate: TextView = view.findViewById(R.id.tvDateHeader)

        fun bind(date: String) {
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                val dateObj = inputFormat.parse(date)
                tvDate.text = outputFormat.format(dateObj ?: date)
            } catch (e: Exception) {
                tvDate.text = date
            }
        }
    }

    class EventViewHolder(
        view: View,
        private val onItemClick: (Event) -> Unit
    ) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.eventTitle)
        private val time: TextView = view.findViewById(R.id.eventTime)
        private val description: TextView = view.findViewById(R.id.eventDescription)
        private val topBar: View = view.findViewById(R.id.topColorBar)
        private val cardView: androidx.cardview.widget.CardView = view.findViewById(R.id.cardView)

        init {
            view.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    val event = (itemView.tag as? Event)
                    event?.let { onItemClick(it) }
                }
            }
        }

        fun bind(event: Event) {
            itemView.tag = event
            title.text = event.title
            time.text = event.time
            description.text = event.description

            topBar.setBackgroundColor(event.color)

            val gradient = ColorUtils.createVerySubtleGradient(event.color)
            cardView.background = gradient
        }
    }
}