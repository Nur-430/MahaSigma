package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.receiver.TaskAlarmReceiver

object ReminderHelper {

    private const val TAG = "ReminderHelper"

    fun calculateTriggerTime(dueDate: Long, reminderType: String): Long? {
        val oneHour = 60 * 60 * 1000L
        val oneDay = 24 * oneHour
        return when (reminderType) {
            "1_HOUR_BEFORE" -> dueDate - oneHour
            "2_HOURS_BEFORE" -> dueDate - (2 * oneHour)
            "1_DAY_BEFORE" -> dueDate - oneDay
            "2_DAYS_BEFORE" -> dueDate - (2 * oneDay)
            "AT_DUE_DATE" -> dueDate
            else -> null
        }
    }

    fun scheduleTaskReminder(
        context: Context,
        taskId: Int,
        title: String,
        courseName: String,
        triggerTimeInMillis: Long
    ) {
        val now = System.currentTimeMillis()
        if (triggerTimeInMillis <= now) {
            Log.d(TAG, "Trigger time is already in the past, skipping schedule: $triggerTimeInMillis")
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
            putExtra(TaskAlarmReceiver.EXTRA_TASK_ID, taskId)
            putExtra(TaskAlarmReceiver.EXTRA_TASK_TITLE, title)
            putExtra(TaskAlarmReceiver.EXTRA_COURSE_NAME, courseName)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeInMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeInMillis,
                    pendingIntent
                )
            }
            Log.d(TAG, "Successfully scheduled alarm for task $taskId at $triggerTimeInMillis")
        } catch (e: SecurityException) {
            Log.w(TAG, "Cannot schedule exact alarm due to security permission, using non-exact", e)
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTimeInMillis,
                pendingIntent
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule alarm", e)
        }
    }

    fun cancelTaskReminder(context: Context, taskId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, TaskAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Log.d(TAG, "Cancelled alarm for task $taskId")
    }
}
