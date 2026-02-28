package com.example.a13hhh

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class CreateEventActivity : AppCompatActivity() {

    private val calendar = Calendar.getInstance()
    private var selectedHour = 12
    private var selectedMinute = 0

    private lateinit var btnSelectHour: Button
    private lateinit var btnSelectMinute: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_event)

        val etTitle = findViewById<EditText>(R.id.etTitle)
        val btnSelectDate = findViewById<Button>(R.id.btnSelectDate)
        val tvSelectedDate = findViewById<TextView>(R.id.tvSelectedDate)
        btnSelectHour = findViewById<Button>(R.id.btnSelectHour)
        btnSelectMinute = findViewById<Button>(R.id.btnSelectMinute)
        val etDescription = findViewById<EditText>(R.id.etDescription)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        val selectedDate = intent.getStringExtra("SELECTED_DATE")
        if (selectedDate != null) {
            try {
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = format.parse(selectedDate)
                calendar.time = date ?: Calendar.getInstance().time
                tvSelectedDate.text = formatDateForDisplay(selectedDate)
            } catch (e: Exception) {
                e.printStackTrace()
                tvSelectedDate.text = formatDateForDisplay(getCurrentDate())
            }
        } else {
            tvSelectedDate.text = formatDateForDisplay(getCurrentDate())
        }

        val currentCalendar = Calendar.getInstance()
        selectedHour = currentCalendar.get(Calendar.HOUR_OF_DAY)
        selectedMinute = currentCalendar.get(Calendar.MINUTE)
        updateTimeDisplay()

        btnSelectDate.setOnClickListener {
            showDatePickerDialog(tvSelectedDate)
        }

        btnSelectHour.setOnClickListener {
            showHourPickerDialog()
        }

        btnSelectMinute.setOnClickListener {
            showMinutePickerDialog()
        }

        btnBack.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        btnSave.setOnClickListener {
            if (validateInput(etTitle)) {
                val eventDate = formatDateForStorage()
                val newEvent = Event(
                    id = Event.createId(),
                    title = etTitle.text.toString(),
                    date = eventDate,
                    time = String.format("%02d:%02d", selectedHour, selectedMinute),
                    description = etDescription.text.toString(),
                    color = Event.generateColorForDate(eventDate)
                )

                val dbHelper = DatabaseHelper(this)
                dbHelper.addEvent(newEvent)

                val resultIntent = Intent()
                resultIntent.putExtra("NEW_EVENT", newEvent)
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }
    }

    private fun showDatePickerDialog(tvSelectedDate: TextView) {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(Calendar.YEAR, selectedYear)
                calendar.set(Calendar.MONTH, selectedMonth)
                calendar.set(Calendar.DAY_OF_MONTH, selectedDay)
                tvSelectedDate.text = formatDateForDisplay(
                    String.format(
                        Locale.getDefault(),
                        "%04d-%02d-%02d",
                        selectedYear,
                        selectedMonth + 1,
                        selectedDay
                    )
                )
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun showHourPickerDialog() {
        val hours = (1..12).toList()
        val hourItems = hours.map { String.format("%02d", it) }.toTypedArray()

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Выберите час")
            .setItems(hourItems) { _, which ->
                selectedHour = hours[which]
                updateTimeDisplay()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showMinutePickerDialog() {
        val minutes = (0..59).toList()
        val minuteItems = minutes.map { String.format("%02d", it) }.toTypedArray()

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Выберите минуты")
            .setItems(minuteItems) { _, which ->
                selectedMinute = minutes[which]
                updateTimeDisplay()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun updateTimeDisplay() {
        btnSelectHour.text = String.format("%02d", selectedHour)
        btnSelectMinute.text = String.format("%02d", selectedMinute)
    }

    private fun formatDateForDisplay(dateStr: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateStr)
            outputFormat.format(date ?: return dateStr)
        } catch (e: Exception) {
            dateStr
        }
    }

    private fun formatDateForStorage(): String {
        return String.format(
            Locale.getDefault(),
            "%04d-%02d-%02d",
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, day)
    }

    private fun validateInput(etTitle: EditText): Boolean {
        if (etTitle.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Введите название события", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    override fun onBackPressed() {
        setResult(RESULT_CANCELED)
        super.onBackPressed()
    }
}