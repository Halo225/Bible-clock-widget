package com.bibleclock

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ClockWidget : AppWidgetProvider() {

    override fun onUpdate(ctx: Context, mgr: AppWidgetManager, ids: IntArray) { render(ctx, mgr, ids); schedule(ctx) }
    override fun onEnabled(ctx: Context) = schedule(ctx)
    override fun onDisabled(ctx: Context) { ctx.getSystemService(AlarmManager::class.java).cancel(tickIntent(ctx)) }

    override fun onReceive(ctx: Context, intent: Intent) {
        super.onReceive(ctx, intent)
        if (intent.action == TICK || intent.action == Intent.ACTION_TIME_CHANGED || intent.action == Intent.ACTION_TIMEZONE_CHANGED ||
            intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val mgr = AppWidgetManager.getInstance(ctx)
            val ids = mgr.getAppWidgetIds(ComponentName(ctx, ClockWidget::class.java))
            if (ids.isNotEmpty()) { render(ctx, mgr, ids); schedule(ctx) }
        }
    }

    companion object {
        const val TICK = "com.bibleclock.TICK"

        private fun tickIntent(ctx: Context) = PendingIntent.getBroadcast(
            ctx, 0, Intent(ctx, ClockWidget::class.java).setAction(TICK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        /** Wake at the start of every minute (non-wakeup, so it costs no battery while the screen is off). */
        fun schedule(ctx: Context) {
            val am = ctx.getSystemService(AlarmManager::class.java)
            val next = (System.currentTimeMillis() / 60_000 + 1) * 60_000
            if (Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms())
                am.setExactAndAllowWhileIdle(AlarmManager.RTC, next, tickIntent(ctx))
            else am.setAndAllowWhileIdle(AlarmManager.RTC, next, tickIntent(ctx))
        }

        fun render(ctx: Context, mgr: AppWidgetManager, ids: IntArray) {
            val cal = Calendar.getInstance()
            val v = VerseEngine.forTime(ctx, cal)
            val open = PendingIntent.getActivity(ctx, 1, Intent(ctx, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
            val rv = RemoteViews(ctx.packageName, R.layout.widget_clock).apply {
                setTextViewText(R.id.time, SimpleDateFormat("h:mm a", Locale.getDefault()).format(cal.time))
                setTextViewText(R.id.date, SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(cal.time))
                setTextViewText(R.id.verse, "“${v.text}”")
                setTextViewText(R.id.ref, "— ${v.ref}")
                setOnClickPendingIntent(R.id.root, open)
            }
            mgr.updateAppWidget(ids, rv)
        }
    }
}
