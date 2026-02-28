package com.example.a13hhh

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.*

class MainActivity1 : AppCompatActivity() {

    private lateinit var events: MutableList<Event>
    private lateinit var eventsAdapter: EventAdapter
    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var tvNoEvents: TextView
    private lateinit var eventsHeader: TextView
    private lateinit var tvMonthYear: TextView
    private lateinit var btnPrevMonth: ImageButton
    private lateinit var btnNextMonth: ImageButton
    private lateinit var btnAddEvent: Button
    private lateinit var calendarRecyclerView: RecyclerView

    private lateinit var dbHelper: DatabaseHelper

    private var currentSelectedDate = getCurrentDate()
    private val datesWithEvents = HashSet<String>()

    private var currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var currentYear = Calendar.getInstance().get(Calendar.YEAR)

    private val editEventLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            data?.let {
                if (it.hasExtra("NEW_EVENT")) {
                    val newEvent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        it.getSerializableExtra("NEW_EVENT", Event::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        it.getSerializableExtra("NEW_EVENT") as? Event
                    }
                    newEvent?.let { event ->
                        refreshDataFromDatabase()
                    }
                } else if (it.hasExtra("UPDATED_EVENT")) {
                    val updatedEvent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        it.getSerializableExtra("UPDATED_EVENT", Event::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        it.getSerializableExtra("UPDATED_EVENT") as? Event
                    }

                    if (updatedEvent != null) {
                        dbHelper.updateEvent(updatedEvent)
                        refreshDataFromDatabase()
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
                        refreshDataFromDatabase()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_main1)

        dbHelper = DatabaseHelper(this)

        val eventsRecycler = findViewById<RecyclerView>(R.id.recyclerCalendarEvents)
        tvNoEvents = findViewById(R.id.tvNoEvents)
        eventsHeader = findViewById(R.id.eventsHeader)
        tvMonthYear = findViewById(R.id.tvMonthYear)
        btnPrevMonth = findViewById(R.id.btnPrevMonth)
        btnNextMonth = findViewById(R.id.btnNextMonth)
        btnAddEvent = findViewById(R.id.btnAddEvent)
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView)

        eventsRecycler.layoutManager = LinearLayoutManager(this)
        calendarRecyclerView.layoutManager = GridLayoutManager(this, 7)

        events = dbHelper.getAllEvents().toMutableList()

        eventsAdapter = EventAdapter(emptyList()) { position ->
            val filteredEvents = getEventsForDate(currentSelectedDate)
            if (position < filteredEvents.size) {
                val event = filteredEvents[position]
                val intent = Intent(this, EditEventActivity::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.putExtra("EVENT", event)
                } else {
                    @Suppress("DEPRECATION")
                    intent.putExtra("EVENT", event)
                }
                editEventLauncher.launch(intent)
            }
        }

        eventsRecycler.adapter = eventsAdapter

        calendarAdapter = CalendarAdapter(emptyList()) { day ->
            if (day.isCurrentMonth && day.day > 0) {
                currentSelectedDate = day.getDateString()
                updateEventsForSelectedDate()
                updateCalendarDays()
            }
        }

        calendarRecyclerView.adapter = calendarAdapter

        refreshDataFromDatabase()
        updateMonthYearDisplay()
        updateEventsForSelectedDate()

        btnPrevMonth.setOnClickListener {
            currentMonth--
            if (currentMonth < 1) {
                currentMonth = 12
                currentYear--
            }
            updateCalendarDays()
            updateMonthYearDisplay()
        }

        btnNextMonth.setOnClickListener {
            currentMonth++
            if (currentMonth > 12) {
                currentMonth = 1
                currentYear++
            }
            updateCalendarDays()
            updateMonthYearDisplay()
        }

        btnAddEvent.setOnClickListener {
            val intent = Intent(this, CreateEventActivity::class.java)
            intent.putExtra("SELECTED_DATE", currentSelectedDate)
            editEventLauncher.launch(intent)
        }

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.nav_calendar

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_calendar -> true
                R.id.nav_events -> {
                    startActivity(Intent(this, MainActivity2::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    true
                }
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
        refreshDataFromDatabase()
    }

    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, day)
    }

    private fun refreshDataFromDatabase() {
        events = dbHelper.getAllEvents().toMutableList()
        updateDatesWithEvents()
        updateCalendarDays()
        updateEventsForSelectedDate()
    }

    private fun updateMonthYearDisplay() {
        val monthNames = arrayOf(
            "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
            "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
        )
        tvMonthYear.text = "${monthNames[currentMonth - 1]} $currentYear"
    }

    private fun updateCalendarDays() {
        val calendarDays = mutableListOf<CalendarDay>()

        val calendar = Calendar.getInstance().apply {
            set(currentYear, currentMonth - 1, 1)
        }

        val firstDayOfMonth = calendar.get(Calendar.DAY_OF_WEEK)
        val firstDayPosition = if (firstDayOfMonth == Calendar.SUNDAY) 6 else firstDayOfMonth - 2

        for (i in 0 until firstDayPosition) {
            calendarDays.add(CalendarDay(0, currentMonth, currentYear, isCurrentMonth = false))
        }

        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        val today = Calendar.getInstance()
        val todayYear = today.get(Calendar.YEAR)
        val todayMonth = today.get(Calendar.MONTH) + 1
        val todayDay = today.get(Calendar.DAY_OF_MONTH)

        for (day in 1..daysInMonth) {
            val dateStr = String.format(Locale.getDefault(), "%04d-%02d-%02d", currentYear, currentMonth, day)
            val hasEvent = datesWithEvents.contains(dateStr)
            val isToday = (currentYear == todayYear && currentMonth == todayMonth && day == todayDay)
            val isSelected = (dateStr == currentSelectedDate)

            calendarDays.add(
                CalendarDay(
                    day = day,
                    month = currentMonth,
                    year = currentYear,
                    isCurrentMonth = true,
                    hasEvent = hasEvent,
                    isToday = isToday,
                    isSelected = isSelected
                )
            )
        }

        val totalCells = 42
        while (calendarDays.size < totalCells) {
            calendarDays.add(CalendarDay(0, currentMonth, currentYear, isCurrentMonth = false))
        }

        calendarAdapter = CalendarAdapter(calendarDays) { day ->
            if (day.isCurrentMonth && day.day > 0) {
                currentSelectedDate = day.getDateString()
                updateEventsForSelectedDate()
                updateCalendarDays()
            }
        }

        calendarRecyclerView.adapter = calendarAdapter
    }

    private fun updateDatesWithEvents() {
        datesWithEvents.clear()
        datesWithEvents.addAll(dbHelper.getDatesWithEvents())
    }

    private fun updateEventsForSelectedDate() {
        val filteredEvents = getEventsForDate(currentSelectedDate)

        updateEventsHeader()

        eventsAdapter = EventAdapter(filteredEvents) { position ->
            val event = filteredEvents[position]
            val intent = Intent(this, EditEventActivity::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.putExtra("EVENT", event)
            } else {
                @Suppress("DEPRECATION")
                intent.putExtra("EVENT", event)
            }
            editEventLauncher.launch(intent)
        }

        findViewById<RecyclerView>(R.id.recyclerCalendarEvents).adapter = eventsAdapter

        if (filteredEvents.isEmpty()) {
            tvNoEvents.visibility = TextView.VISIBLE
            btnAddEvent.visibility = Button.VISIBLE
            tvNoEvents.text = "Событий нет"
        } else {
            tvNoEvents.visibility = TextView.GONE
            btnAddEvent.visibility = Button.GONE
        }
    }

    private fun getEventsForDate(date: String): List<Event> {
        return dbHelper.getEventsByDate(date)
    }

    private fun updateEventsHeader() {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            val date = inputFormat.parse(currentSelectedDate)
            val formattedDate = outputFormat.format(date ?: return)
            eventsHeader.text = "События на $formattedDate"
        } catch (e: Exception) {
            eventsHeader.text = "События на $currentSelectedDate"
        }
    }
}