package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class TaskAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "mahasigma_task_reminders"
        const val CHANNEL_NAME = "Pengingat Tugas Mahasiswa"
        const val EXTRA_TASK_ID = "EXTRA_TASK_ID"
        const val EXTRA_TASK_TITLE = "EXTRA_TASK_TITLE"
        const val EXTRA_COURSE_NAME = "EXTRA_COURSE_NAME"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getIntExtra(EXTRA_TASK_ID, 0)
        val title = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Pengingat Deadline Tugas"
        val courseName = intent.getStringExtra(EXTRA_COURSE_NAME) ?: "MahaSigma"

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create Channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi pengingat tenggat waktu tugas perkuliahan"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Tap action -> Open MainActivity
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("⏰ Deadline Segera: $title")
            .setContentText("Mata Kuliah: $courseName. Jangan lupa dikumpulkan!")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Tugas '$title' untuk mata kuliah $courseName mendekati tenggat waktu pengumpulan. Segera selesaikan dan submit!")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(taskId, notification)
    }
}
