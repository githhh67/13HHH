package com.example.a13hhh

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class EditEventActivity : AppCompatActivity() {

    private lateinit var event: Event
    private val calendar = Calendar.getInstance()
    private var selectedHour = 12
    private var selectedMinute = 0

    private lateinit var btnSelectHour: Button
    private lateinit var btnSelectMinute: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_event)

        event = intent.getSerializableExtra("EVENT") as Event

        val etTitle = findViewById<EditText>(R.id.etTitle)
        val btnSelectDate = findViewById<Button>(R.id.btnSelectDate)
        val tvSelectedDate = findViewById<TextView>(R.id.tvSelectedDate)
        btnSelectHour = findViewById<Button>(R.id.btnSelectHour)
        btnSelectMinute = findViewById<Button>(R.id.btnSelectMinute)
        val etDescription = findViewById<EditText>(R.id.etDescription)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnDelete = findViewById<Button>(R.id.btnDelete)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        parseEventDateTime()

        etTitle.setText(event.title)
        etDescription.setText(event.description)
        tvSelectedDate.text = formatDateForDisplay(event.date)

        val timeParts = event.time.split(":")
        if (timeParts.size == 2) {
            selectedHour = timeParts[0].toIntOrNull() ?: 12
            selectedMinute = timeParts[1].toIntOrNull() ?: 0
            updateTimeDisplay()
        }

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
            finish()
        }

        btnSave.setOnClickListener {
            if (validateInput()) {
                val newDate = formatDateForStorage()

                if (event.date != newDate) {
                    event.color = Event.generateColorForDate(newDate)
                }

                event.title = etTitle.text.toString()
                event.date = newDate
                event.time = String.format("%02d:%02d", selectedHour, selectedMinute)
                event.description = etDescription.text.toString()

                val resultIntent = Intent()
                resultIntent.putExtra("UPDATED_EVENT", event)
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }

        btnDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun parseEventDateTime() {
        try {
            val dateParts = event.date.split("-")
            if (dateParts.size == 3) {
                calendar.set(Calendar.YEAR, dateParts[0].toInt())
                calendar.set(Calendar.MONTH, dateParts[1].toInt() - 1)
                calendar.set(Calendar.DAY_OF_MONTH, dateParts[2].toInt())
            }

            val timeParts = event.time.split(":")
            if (timeParts.size == 2) {
                selectedHour = timeParts[0].toInt()
                selectedMinute = timeParts[1].toInt()
            }
        } catch (e: Exception) {
            e.printStackTrace()
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

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Удаление события")
            .setMessage("Вы уверены, что хотите удалить это событие?")
            .setPositiveButton("Удалить") { _, _ ->
                val resultIntent = Intent()
                resultIntent.putExtra("DELETE_EVENT", event)
                setResult(RESULT_OK, resultIntent)
                finish()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun validateInput(): Boolean {
        val etTitle = findViewById<EditText>(R.id.etTitle)

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