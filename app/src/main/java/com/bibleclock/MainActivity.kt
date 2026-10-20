package com.bibleclock

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : Activity() {
    private lateinit var time: TextView
    private lateinit var verse: TextView
    private lateinit var ref: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pad = (24 * resources.displayMetrics.density).toInt()
        fun label(sp: Float, color: Int, style: Int) = TextView(this).apply {
            textSize = sp; setTextColor(color); setTypeface(Typeface.SERIF, style); setPadding(0, 0, 0, pad / 2)
        }
        time = label(40f, 0xFFFFFFFF.toInt(), Typeface.BOLD)
        verse = label(20f, 0xFFE8ECF5.toInt(), Typeface.ITALIC)
        ref = label(16f, 0xFFF2C14E.toInt(), Typeface.BOLD)
        val add = Button(this).apply {
            text = "Add widget to Home Screen"
            setOnClickListener {
                val mgr = getSystemService(AppWidgetManager::class.java)
                if (mgr.isRequestPinAppWidgetSupported) mgr.requestPinAppWidget(ComponentName(this@MainActivity, ClockWidget::class.java), null, null)
                else Toast.makeText(this@MainActivity, "Long-press the Home Screen → Widgets → Bible Clock", Toast.LENGTH_LONG).show()
            }
        }
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(pad, pad * 2, pad, pad)
            addView(time); addView(verse); addView(ref); addView(add)
        }
        setContentView(ScrollView(this).apply { setBackgroundColor(0xFF0B1020.toInt()); fitsSystemWindows = true; addView(box) })
    }

    override fun onResume() {
        super.onResume()
        val cal = Calendar.getInstance()
        val v = VerseEngine.forTime(this, cal)
        time.text = SimpleDateFormat("h:mm a", Locale.getDefault()).format(cal.time)
        verse.text = "“${v.text}”"; ref.text = "— ${v.ref}"
    }
}
