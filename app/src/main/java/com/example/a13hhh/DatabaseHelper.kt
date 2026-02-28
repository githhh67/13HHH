package com.example.a13hhh

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.*

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "events_database.db"
        private const val DATABASE_VERSION = 3
        private const val TABLE_EVENTS = "events"
        private const val TABLE_REMINDERS = "reminders"

        private const val COLUMN_ID = "id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_TIME = "time"
        private const val COLUMN_DESCRIPTION = "description"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_COLOR = "color"

        private const val COLUMN_REMINDER_ID = "reminder_id"
        private const val COLUMN_EVENT_ID = "event_id"
        private const val COLUMN_REMINDER_TIME = "reminder_time"
        private const val COLUMN_REMINDER_TEXT = "reminder_text"
        private const val COLUMN_IS_ACTIVE = "is_active"
        private const val COLUMN_CREATED_AT = "created_at"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createEventsTableQuery = """
            CREATE TABLE $TABLE_EVENTS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_TIME TEXT NOT NULL,
                $COLUMN_DESCRIPTION TEXT,
                $COLUMN_DATE TEXT NOT NULL,
                $COLUMN_COLOR INTEGER DEFAULT 0
            )
        """.trimIndent()
        db.execSQL(createEventsTableQuery)

        val createRemindersTableQuery = """
            CREATE TABLE $TABLE_REMINDERS (
                $COLUMN_REMINDER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_EVENT_ID INTEGER NOT NULL,
                $COLUMN_REMINDER_TIME TEXT NOT NULL,
                $COLUMN_REMINDER_TEXT TEXT NOT NULL,
                $COLUMN_IS_ACTIVE INTEGER DEFAULT 1,
                $COLUMN_CREATED_AT TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY ($COLUMN_EVENT_ID) REFERENCES $TABLE_EVENTS($COLUMN_ID) ON DELETE CASCADE
            )
        """.trimIndent()
        db.execSQL(createRemindersTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            val createRemindersTableQuery = """
                CREATE TABLE IF NOT EXISTS $TABLE_REMINDERS (
                    $COLUMN_REMINDER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_EVENT_ID INTEGER NOT NULL,
                    $COLUMN_REMINDER_TIME TEXT NOT NULL,
                    $COLUMN_REMINDER_TEXT TEXT NOT NULL,
                    $COLUMN_IS_ACTIVE INTEGER DEFAULT 1,
                    $COLUMN_CREATED_AT TEXT DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY ($COLUMN_EVENT_ID) REFERENCES $TABLE_EVENTS($COLUMN_ID) ON DELETE CASCADE
                )
            """.trimIndent()
            db.execSQL(createRemindersTableQuery)
        }
    }

    fun addEvent(event: Event): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, event.title)
            put(COLUMN_TIME, event.time)
            put(COLUMN_DESCRIPTION, event.description)
            put(COLUMN_DATE, event.date)
            put(COLUMN_COLOR, event.color)
        }
        val id = db.insert(TABLE_EVENTS, null, values)
        event.id = id.toInt()
        db.close()
        return id
    }

    fun getAllEvents(): List<Event> {
        val events = mutableListOf<Event>()
        val selectQuery = "SELECT * FROM $TABLE_EVENTS ORDER BY $COLUMN_DATE, $COLUMN_TIME"
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val event = Event(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                    time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME)),
                    description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                    date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                    color = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COLOR))
                )
                events.add(event)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return events
    }

    fun getEventsByDate(date: String): List<Event> {
        val events = mutableListOf<Event>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_EVENTS,
            null,
            "$COLUMN_DATE = ?",
            arrayOf(date),
            null,
            null,
            "$COLUMN_TIME ASC"
        )

        if (cursor.moveToFirst()) {
            do {
                val event = Event(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                    time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME)),
                    description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                    date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                    color = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COLOR))
                )
                events.add(event)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return events
    }

    fun updateEvent(event: Event): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, event.title)
            put(COLUMN_TIME, event.time)
            put(COLUMN_DESCRIPTION, event.description)
            put(COLUMN_DATE, event.date)
            put(COLUMN_COLOR, event.color)
        }
        val result = db.update(
            TABLE_EVENTS,
            values,
            "$COLUMN_ID = ?",
            arrayOf(event.id.toString())
        )
        db.close()
        return result
    }

    fun deleteEvent(id: Int): Int {
        val db = this.writableDatabase
        val result = db.delete(TABLE_EVENTS, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
        return result
    }

    fun getDatesWithEvents(): Set<String> {
        val dates = mutableSetOf<String>()
        val query = "SELECT DISTINCT $COLUMN_DATE FROM $TABLE_EVENTS"
        val db = this.readableDatabase
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                dates.add(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return dates
    }

    fun addReminder(reminder: Reminder): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_EVENT_ID, reminder.eventId)
            put(COLUMN_REMINDER_TIME, reminder.reminderTime)
            put(COLUMN_REMINDER_TEXT, reminder.reminderText)
            put(COLUMN_IS_ACTIVE, if (reminder.isActive) 1 else 0)
        }
        val id = db.insert(TABLE_REMINDERS, null, values)
        db.close()
        return id
    }

    fun getAllReminders(): List<Reminder> {
        val reminders = mutableListOf<Reminder>()
        val selectQuery = """
            SELECT r.*, e.title as event_title, e.date as event_date, e.time as event_time 
            FROM $TABLE_REMINDERS r
            LEFT JOIN $TABLE_EVENTS e ON r.$COLUMN_EVENT_ID = e.$COLUMN_ID
            ORDER BY r.$COLUMN_CREATED_AT DESC
        """.trimIndent()

        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val eventTitleColumn = cursor.getColumnIndex("event_title")
                val eventDateColumn = cursor.getColumnIndex("event_date")
                val eventTimeColumn = cursor.getColumnIndex("event_time")

                val reminder = Reminder(
                    reminderId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REMINDER_ID)),
                    eventId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EVENT_ID)),
                    reminderTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REMINDER_TIME)),
                    reminderText = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REMINDER_TEXT)),
                    isActive = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_ACTIVE)) == 1,
                    eventTitle = if (eventTitleColumn != -1) cursor.getString(eventTitleColumn) ?: "Без события" else "Без события",
                    eventDate = if (eventDateColumn != -1) cursor.getString(eventDateColumn) ?: "" else "",
                    eventTime = if (eventTimeColumn != -1) cursor.getString(eventTimeColumn) ?: "" else ""
                )
                reminders.add(reminder)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return reminders
    }

    fun updateReminder(reminder: Reminder): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_EVENT_ID, reminder.eventId)
            put(COLUMN_REMINDER_TIME, reminder.reminderTime)
            put(COLUMN_REMINDER_TEXT, reminder.reminderText)
            put(COLUMN_IS_ACTIVE, if (reminder.isActive) 1 else 0)
        }
        val result = db.update(
            TABLE_REMINDERS,
            values,
            "$COLUMN_REMINDER_ID = ?",
            arrayOf(reminder.reminderId.toString())
        )
        db.close()
        return result
    }

    fun deleteReminder(reminderId: Int): Int {
        val db = this.writableDatabase
        val result = db.delete(TABLE_REMINDERS, "$COLUMN_REMINDER_ID = ?", arrayOf(reminderId.toString()))
        db.close()
        return result
    }

    fun getFutureEvents(): List<Event> {
        val events = mutableListOf<Event>()
        val db = this.readableDatabase

        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(Calendar.getInstance().time)

        val query = """
            SELECT * FROM $TABLE_EVENTS 
            WHERE $COLUMN_DATE >= ? 
            ORDER BY $COLUMN_DATE, $COLUMN_TIME
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(currentDate))

        if (cursor.moveToFirst()) {
            do {
                val event = Event(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                    time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME)),
                    description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                    date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                    color = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COLOR))
                )
                events.add(event)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return events
    }

    fun getEventById(eventId: Int): Event? {
        val db = this.readableDatabase
        val query = """
            SELECT * FROM $TABLE_EVENTS 
            WHERE $COLUMN_ID = ?
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(eventId.toString()))

        var event: Event? = null
        if (cursor.moveToFirst()) {
            event = Event(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME)),
                description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                color = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COLOR))
            )
        }
        cursor.close()
        db.close()
        return event
    }
}