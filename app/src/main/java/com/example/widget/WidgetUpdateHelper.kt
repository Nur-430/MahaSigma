package com.example.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context

object WidgetUpdateHelper {

    fun updateAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context) ?: return

        // 1. Update Today's Schedule Widgets
        val scheduleComponent = ComponentName(context, TodayScheduleWidgetProvider::class.java)
        val scheduleWidgetIds = appWidgetManager.getAppWidgetIds(scheduleComponent)
        for (id in scheduleWidgetIds) {
            TodayScheduleWidgetProvider.updateWidget(context, appWidgetManager, id)
        }

        // 2. Update Upcoming Tasks Widgets
        val tasksComponent = ComponentName(context, UpcomingTasksWidgetProvider::class.java)
        val taskWidgetIds = appWidgetManager.getAppWidgetIds(tasksComponent)
        for (id in taskWidgetIds) {
            UpcomingTasksWidgetProvider.updateWidget(context, appWidgetManager, id)
        }

        // 3. Update Quick Notes Widgets
        val notesComponent = ComponentName(context, QuickNotesWidgetProvider::class.java)
        val notesWidgetIds = appWidgetManager.getAppWidgetIds(notesComponent)
        for (id in notesWidgetIds) {
            QuickNotesWidgetProvider.updateWidget(context, appWidgetManager, id)
        }
    }
}
