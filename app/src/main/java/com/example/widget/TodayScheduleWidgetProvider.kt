package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.database.MahaSigmaDatabase
import com.example.util.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TodayScheduleWidgetProvider : AppWidgetProvider() {

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
            val views = RemoteViews(context.packageName, R.layout.widget_today_schedule)

            // Intent to open Schedule screen
            val openScheduleIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("EXTRA_TAB", 1)
            }
            val schedulePendingIntent = PendingIntent.getActivity(
                context,
                101,
                openScheduleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_schedule_root, schedulePendingIntent)
            views.setOnClickPendingIntent(R.id.btn_widget_open_schedule, schedulePendingIntent)

            val currentDayOfWeek = DateUtils.getCurrentDayOfWeek()
            val dayName = DateUtils.getDayName(currentDayOfWeek)
            views.setTextViewText(R.id.tv_widget_schedule_day, dayName)

            // Asynchronously load database records
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = MahaSigmaDatabase.getDatabase(context)
                    val allSchedules = db.mahaSigmaDao().getAllSchedulesSnapshot()
                    val allCourses = db.mahaSigmaDao().getAllCoursesSnapshot().associateBy { it.id }
                    val overrides = db.mahaSigmaDao().getAllOverridesSnapshot()

                    val todaySchedules = allSchedules
                        .filter { it.dayOfWeek == currentDayOfWeek }
                        .sortedBy { it.startTime }

                    withContext(Dispatchers.Main) {
                        if (todaySchedules.isEmpty()) {
                            views.setViewVisibility(R.id.tv_widget_schedule_empty, View.VISIBLE)
                            views.setViewVisibility(R.id.layout_schedule_item_1, View.GONE)
                            views.setViewVisibility(R.id.layout_schedule_item_2, View.GONE)
                        } else {
                            views.setViewVisibility(R.id.tv_widget_schedule_empty, View.GONE)

                            // First schedule item
                            val first = todaySchedules[0]
                            val firstCourse = allCourses[first.courseId]
                            val firstOverride = overrides.firstOrNull { it.scheduleId == first.id }

                            views.setViewVisibility(R.id.layout_schedule_item_1, View.VISIBLE)
                            views.setTextViewText(R.id.tv_schedule_1_title, firstCourse?.name ?: "Mata Kuliah")
                            val time1 = if (firstOverride?.status == "CHANGED_TIME" && !firstOverride.newStartTime.isNullOrBlank()) {
                                "${firstOverride.newStartTime} - ${firstOverride.newEndTime}"
                            } else {
                                "${first.startTime} - ${first.endTime}"
                            }
                            views.setTextViewText(R.id.tv_schedule_1_time, time1)

                            val room1 = if (firstOverride?.status == "CHANGED_ROOM" && !firstOverride.newRoom.isNullOrBlank()) {
                                firstOverride.newRoom
                            } else if (firstOverride?.status == "ONLINE") {
                                "Kuliah Online"
                            } else {
                                first.room
                            }
                            views.setTextViewText(R.id.tv_schedule_1_room, "$room1 • ${firstCourse?.lecturer ?: ""}")

                            if (firstOverride != null) {
                                val statusLabel = when (firstOverride.status) {
                                    "CANCELED" -> "🚫 Diliburkan"
                                    "CHANGED_TIME" -> "⏱ Jam Berubah"
                                    "CHANGED_ROOM" -> "📍 Pindah Ruang"
                                    "ONLINE" -> "💻 Online"
                                    else -> firstOverride.status
                                }
                                views.setTextViewText(R.id.tv_schedule_1_status, statusLabel)
                                views.setViewVisibility(R.id.tv_schedule_1_status, View.VISIBLE)
                            } else {
                                views.setViewVisibility(R.id.tv_schedule_1_status, View.GONE)
                            }

                            // Second schedule item
                            if (todaySchedules.size > 1) {
                                val second = todaySchedules[1]
                                val secondCourse = allCourses[second.courseId]
                                val secondOverride = overrides.firstOrNull { it.scheduleId == second.id }

                                views.setViewVisibility(R.id.layout_schedule_item_2, View.VISIBLE)
                                views.setTextViewText(R.id.tv_schedule_2_title, secondCourse?.name ?: "Mata Kuliah")
                                val time2 = if (secondOverride?.status == "CHANGED_TIME" && !secondOverride.newStartTime.isNullOrBlank()) {
                                    "${secondOverride.newStartTime} - ${secondOverride.newEndTime}"
                                } else {
                                    "${second.startTime} - ${second.endTime}"
                                }
                                views.setTextViewText(R.id.tv_schedule_2_time, time2)
                                val room2 = if (secondOverride?.status == "CHANGED_ROOM" && !secondOverride.newRoom.isNullOrBlank()) {
                                    secondOverride.newRoom
                                } else if (secondOverride?.status == "ONLINE") {
                                    "Kuliah Online"
                                } else {
                                    second.room
                                }
                                views.setTextViewText(R.id.tv_schedule_2_room, "$room2 • ${secondCourse?.lecturer ?: ""}")

                                if (secondOverride != null) {
                                    val statusLabel2 = when (secondOverride.status) {
                                        "CANCELED" -> "🚫 Diliburkan"
                                        "CHANGED_TIME" -> "⏱ Jam Berubah"
                                        "CHANGED_ROOM" -> "📍 Pindah Ruang"
                                        "ONLINE" -> "💻 Online"
                                        else -> secondOverride.status
                                    }
                                    views.setTextViewText(R.id.tv_schedule_2_status, statusLabel2)
                                    views.setViewVisibility(R.id.tv_schedule_2_status, View.VISIBLE)
                                } else {
                                    views.setViewVisibility(R.id.tv_schedule_2_status, View.GONE)
                                }
                            } else {
                                views.setViewVisibility(R.id.layout_schedule_item_2, View.GONE)
                            }
                        }

                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                } catch (e: Exception) {
                    // Update fallback
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }
    }
}
