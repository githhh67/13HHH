package com.example.a13hhh

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity3 : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var tvNoReminders: TextView
    private lateinit var btnAddReminder: Button

    private val selectEventLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            loadReminders()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main3)

        dbHelper = DatabaseHelper(this)

        val recycler = findViewById<RecyclerView>(R.id.recyclerReminders)
        tvNoReminders = findViewById<TextView>(R.id.tvNoReminders)
        btnAddReminder = findViewById<Button>(R.id.btnAddReminder)

        recycler.layoutManager = LinearLayoutManager(this)

        btnAddReminder.setOnClickListener {
            val intent = Intent(this, SelectEventActivity::class.java)
            selectEventLauncher.launch(intent)
        }

        loadReminders()

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.nav_reminders

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_calendar -> {
                    startActivity(Intent(this, MainActivity1::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    true
                }
                R.id.nav_events -> {
                    startActivity(Intent(this, MainActivity2::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    true
                }
                R.id.nav_reminders -> true
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadReminders()
    }

    private fun loadReminders() {
        try {
            val reminders = dbHelper.getAllReminders()

            val remindersAdapter = SimpleRemindersAdapter(reminders) { reminder ->
                showReminderOptionsDialog(reminder)
            }

            findViewById<RecyclerView>(R.id.recyclerReminders).adapter = remindersAdapter

            if (reminders.isEmpty()) {
                tvNoReminders.visibility = TextView.VISIBLE
                tvNoReminders.text = "Напоминаний нет"
            } else {
                tvNoReminders.visibility = TextView.GONE
            }
            // Кнопка всегда видна
            btnAddReminder.visibility = Button.VISIBLE

        } catch (e: Exception) {
            tvNoReminders.visibility = TextView.VISIBLE
            tvNoReminders.text = "Ошибка загрузки"
            btnAddReminder.visibility = Button.VISIBLE
        }
    }

    private fun showReminderOptionsDialog(reminder: Reminder) {
        val options = arrayOf("Редактировать", "Удалить", "Отмена")

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Напоминание")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> editReminder(reminder)
                    1 -> deleteReminder(reminder)
                }
            }
            .show()
    }

    private fun editReminder(reminder: Reminder) {
        Toast.makeText(this, "Редактирование в разработке", Toast.LENGTH_SHORT).show()
    }

    private fun deleteReminder(reminder: Reminder) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Удаление напоминания")
            .setMessage("Вы уверены, что хотите удалить это напоминание?")
            .setPositiveButton("Удалить") { _, _ ->
                dbHelper.deleteReminder(reminder.reminderId)
                loadReminders()
                Toast.makeText(this, "Напоминание удалено", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
}

class SimpleRemindersAdapter(
    private val reminders: List<Reminder>,
    private val onItemClick: (Reminder) -> Unit
) : RecyclerView.Adapter<SimpleRemindersAdapter.ReminderViewHolder>() {

    class ReminderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.reminderTitle)
        val subtitle: TextView = view.findViewById(R.id.reminderSubtitle)
        val eventInfo: TextView = view.findViewById(R.id.reminderEventInfo)
        val switch: com.google.android.material.switchmaterial.SwitchMaterial = view.findViewById(R.id.reminderSwitch)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reminder_simple, parent, false)
        val holder = ReminderViewHolder(view)

        view.setOnClickListener {
            val position = holder.adapterPosition
            if (position != RecyclerView.NO_POSITION && position < reminders.size) {
                onItemClick(reminders[position])
            }
        }

        return holder
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val reminder = reminders[position]

        holder.title.text = reminder.reminderText
        holder.subtitle.text = reminder.getFormattedReminderTime()
        holder.eventInfo.text = reminder.getFullEventDescription()

        holder.switch.setOnCheckedChangeListener(null)
        holder.switch.isChecked = reminder.isActive
        holder.switch.setOnCheckedChangeListener { _, isChecked ->
            // Можно добавить обновление статуса в БД
        }
    }

    override fun getItemCount() = reminders.size
}