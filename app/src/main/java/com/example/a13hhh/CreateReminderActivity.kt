package com.example.a13hhh

import android.app.TimePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

class CreateReminderActivity : AppCompatActivity() {

    private var eventId: Int = 0
    private lateinit var event: Event
    private lateinit var dbHelper: DatabaseHelper

    private var selectedHours = 0
    private var selectedMinutes = 30 // По умолчанию 30 минут

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_reminder)

        eventId = intent.getIntExtra("EVENT_ID", 0)
        dbHelper = DatabaseHelper(this)

        val eventFromDb = dbHelper.getEventById(eventId)
        if (eventFromDb == null) {
            Toast.makeText(this, "Событие не найдено", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        event = eventFromDb

        val tvEventTitle = findViewById<TextView>(R.id.tvEventTitle)
        val tvEventDateTime = findViewById<TextView>(R.id.tvEventDateTime)
        val etReminderText = findViewById<EditText>(R.id.etReminderText)
        val btnSelectTime = findViewById<Button>(R.id.btnSelectTime)
        val tvSelectedTime = findViewById<TextView>(R.id.tvSelectedTime)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        tvEventTitle.text = event.title

        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            val date = inputFormat.parse(event.date)
            val formattedDate = outputFormat.format(date ?: event.date)
            tvEventDateTime.text = "$formattedDate в ${event.time}"
        } catch (e: Exception) {
            tvEventDateTime.text = "${event.date} в ${event.time}"
        }

        updateTimeDisplay(tvSelectedTime)

        btnSelectTime.setOnClickListener {
            showTimePickerDialog(tvSelectedTime)
        }

        btnBack.setOnClickListener {
            finish()
        }

        btnSave.setOnClickListener {
            if (validateInput(etReminderText)) {
                val reminderText = etReminderText.text.toString().trim()
                val reminderTimeInMinutes = selectedHours * 60 + selectedMinutes

                val reminder = Reminder(
                    eventId = eventId,
                    reminderTime = reminderTimeInMinutes.toString(),
                    reminderText = reminderText,
                    isActive = true,
                    eventTitle = event.title,
                    eventDate = event.date,
                    eventTime = event.time
                )

                dbHelper.addReminder(reminder)

                Toast.makeText(this, "Напоминание создано", Toast.LENGTH_SHORT).show()

                // Возвращаемся в MainActivity3
                val intent = Intent(this, MainActivity3::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }
        }
    }

    private fun showTimePickerDialog(tvSelectedTime: TextView) {
        val timePickerDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                selectedHours = hourOfDay
                selectedMinutes = minute
                updateTimeDisplay(tvSelectedTime)
            },
            selectedHours,
            selectedMinutes,
            true
        )
        timePickerDialog.show()
    }

    private fun updateTimeDisplay(tvSelectedTime: TextView) {
        val reminderTimeInMinutes = selectedHours * 60 + selectedMinutes

        when (reminderTimeInMinutes) {
            0 -> tvSelectedTime.text = "В момент события"
            5 -> tvSelectedTime.text = "За 5 минут"
            15 -> tvSelectedTime.text = "За 15 минут"
            30 -> tvSelectedTime.text = "За 30 минут"
            60 -> tvSelectedTime.text = "За 1 час"
            1440 -> tvSelectedTime.text = "За 1 день"
            else -> {
                if (selectedHours > 0) {
                    if (selectedMinutes > 0) {
                        tvSelectedTime.text = "За $selectedHours ч $selectedMinutes мин"
                    } else {
                        tvSelectedTime.text = "За $selectedHours ч"
                    }
                } else {
                    tvSelectedTime.text = "За $selectedMinutes минут"
                }
            }
        }
    }

    private fun validateInput(etReminderText: EditText): Boolean {
        val text = etReminderText.text.toString().trim()

        if (text.isEmpty()) {
            Toast.makeText(this, "Введите текст напоминания", Toast.LENGTH_SHORT).show()
            return false
        }

        if (text.length > 200) {
            Toast.makeText(this, "Текст не должен превышать 200 символов", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
}