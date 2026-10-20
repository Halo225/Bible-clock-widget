package com.bibleclock

import android.content.Context
import org.json.JSONObject
import java.util.Calendar

data class Verse(val ref: String, val text: String)

/** Maps clock time h:mm to chapter:verse (John 3:16 at 3:16). Falls back to verse:chapter, then a time-themed verse on the hour. */
object VerseEngine {
    private var data: JSONObject? = null

    fun forTime(ctx: Context, cal: Calendar = Calendar.getInstance()): Verse {
        val d = data ?: JSONObject(ctx.assets.open("verses.json").bufferedReader().use { it.readText() }).also { data = it }
        val h = cal.get(Calendar.HOUR).let { if (it == 0) 12 else it }
        val options = d.getJSONArray("$h:${cal.get(Calendar.MINUTE)}")
        val pick = options.getJSONArray(cal.get(Calendar.DAY_OF_YEAR) % options.length()) // varies day to day
        return Verse(pick.getString(0), pick.getString(1))
    }
}
