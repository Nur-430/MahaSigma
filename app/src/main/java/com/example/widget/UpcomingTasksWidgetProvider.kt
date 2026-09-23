package com.example.widget

import com.nurokhim.mahasigma.R
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
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

class UpcomingTasksWidgetProvider : AppWidgetProvider() {

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
            val views = RemoteViews(context.packageName, R.layout.widget_upcoming_tasks)

            // Intent to open Tasks screen
            val openTasksIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("EXTRA_TAB", 2)
            }
            val tasksPendingIntent = PendingIntent.getActivity(
                context,
                102,
                openTasksIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_tasks_root, tasksPendingIntent)
            views.setOnClickPendingIntent(R.id.btn_widget_open_tasks, tasksPendingIntent)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = MahaSigmaDatabase.getDatabase(context)
                    val allTasks = db.mahaSigmaDao().getAllTasksSnapshot()
                    val allCourses = db.mahaSigmaDao().getAllCoursesSnapshot().associateBy { it.id }

                    // Filter only active tasks, sorted by nearest deadline first
                    val activeTasks = allTasks
                        .filter { !it.isCompleted }
                        .sortedBy { it.dueDate }

                    withContext(Dispatchers.Main) {
                        views.setTextViewText(R.id.tv_widget_tasks_count, "${activeTasks.size} Tugas Aktif")

                        if (activeTasks.isEmpty()) {
                            views.setViewVisibility(R.id.tv_widget_tasks_empty, View.VISIBLE)
                            views.setViewVisibility(R.id.layout_task_item_1, View.GONE)
                            views.setViewVisibility(R.id.layout_task_item_2, View.GONE)
                        } else {
                            views.setViewVisibility(R.id.tv_widget_tasks_empty, View.GONE)

                            // Item 1: Nearest Deadline
                            val task1 = activeTasks[0]
                            val course1 = allCourses[task1.courseId]
                            views.setViewVisibility(R.id.layout_task_item_1, View.VISIBLE)
                            views.setTextViewText(R.id.tv_task_1_title, task1.title)
                            views.setTextViewText(R.id.tv_task_1_countdown, DateUtils.getRelativeDeadline(task1.dueDate, false))
                            views.setTextViewText(R.id.tv_task_1_priority, task1.priority.uppercase())
                            views.setTextViewText(R.id.tv_task_1_course, course1?.name ?: "Tugas Umum")

                            // Item 2: Second Nearest Deadline
                            if (activeTasks.size > 1) {
                                val task2 = activeTasks[1]
                                val course2 = allCourses[task2.courseId]
                                views.setViewVisibility(R.id.layout_task_item_2, View.VISIBLE)
                                views.setTextViewText(R.id.tv_task_2_title, task2.title)
                                views.setTextViewText(R.id.tv_task_2_countdown, DateUtils.getRelativeDeadline(task2.dueDate, false))
                                views.setTextViewText(R.id.tv_task_2_priority, task2.priority.uppercase())
                                views.setTextViewText(R.id.tv_task_2_course, course2?.name ?: "Tugas Umum")
                            } else {
                                views.setViewVisibility(R.id.layout_task_item_2, View.GONE)
                            }
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
