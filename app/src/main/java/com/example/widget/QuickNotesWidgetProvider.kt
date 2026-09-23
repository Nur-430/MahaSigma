package com.example.widget
import com.nurokhim.mahasigma.R

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.database.MahaSigmaDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuickNotesWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_quick_notes)

            // Intent to open Notes screen
            val openNotesIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("EXTRA_TAB", 3)
            }
            val notesPendingIntent = PendingIntent.getActivity(
                context,
                103,
                openNotesIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_notes_root, notesPendingIntent)
            views.setOnClickPendingIntent(R.id.layout_note_recent, notesPendingIntent)

            // Intent for Write New Note
            val newNoteIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("EXTRA_TAB", 3)
                putExtra("EXTRA_ACTION", "NEW_NOTE")
            }
            val newNotePendingIntent = PendingIntent.getActivity(
                context,
                104,
                newNoteIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_widget_new_note, newNotePendingIntent)

            // Intent for Camera Note
            val cameraNoteIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("EXTRA_TAB", 3)
                putExtra("EXTRA_ACTION", "CAMERA_NOTE")
            }
            val cameraNotePendingIntent = PendingIntent.getActivity(
                context,
                105,
                cameraNoteIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_widget_camera_note, cameraNotePendingIntent)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = MahaSigmaDatabase.getDatabase(context)
                    val notes = db.mahaSigmaDao().getAllNotesSnapshot()
                    val allCourses = db.mahaSigmaDao().getAllCoursesSnapshot().associateBy { it.id }

                    withContext(Dispatchers.Main) {
                        views.setTextViewText(R.id.tv_widget_notes_count, "${notes.size} Catatan")

                        if (notes.isNotEmpty()) {
                            val latestNote = notes.maxByOrNull { it.updatedAt } ?: notes[0]
                            val course = allCourses[latestNote.courseId]
                            views.setTextViewText(R.id.tv_widget_recent_note_course, course?.name ?: "Catatan Umum")
                            views.setTextViewText(R.id.tv_widget_recent_note_title, latestNote.title.ifBlank { "Tanpa Judul" })
                            val images = latestNote.getAllImages()
                            val preview = latestNote.content.ifBlank {
                                if (images.isNotEmpty()) "📷 Mengandung ${images.size} foto materi"
                                else "Tidak ada teks tambahan"
                            }
                            views.setTextViewText(R.id.tv_widget_recent_note_content, preview)
                        } else {
                            views.setTextViewText(R.id.tv_widget_recent_note_course, "Catatan Materi")
                            views.setTextViewText(R.id.tv_widget_recent_note_title, "Belum Ada Catatan")
                            views.setTextViewText(
                                R.id.tv_widget_recent_note_content,
                                "Ketuk tombol di bawah untuk menulis catatan atau mengambil foto papan tulis."
                            )
                        }

                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                } catch (e: Exception) {
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }
    }
}
