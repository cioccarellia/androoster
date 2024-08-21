package com.andreacioccarelli.androoster.ui.backup

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by andrea on 2018/apr.
 * Part of the package com.andreacioccarelli.androoster.tools
 */

object DateGenerator {
    private const val datePattern = "dd_MM_yyyy_HH:mm:ss" // 07_01_2018_12:10:56
    private const val displayPattern = "EEEE d, MMMM yyyy (h:m a)" // 07_01_2018_12:10:56

    private fun fixMonth(brokenDate: Int): String {
        var month = (brokenDate + 1).toString()

        if (month.length == 1) {
            month = "0$month"
        }

        return month
    }

    fun getNowStringDate(): String {
        val calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault())

        return "${calendar.get(Calendar.DAY_OF_MONTH)}_" +
                "${fixMonth(calendar.get(Calendar.MONTH))}_" +
                "${calendar.get(Calendar.YEAR)}_" +
                "${calendar.get(Calendar.HOUR_OF_DAY)}:" +
                "${calendar.get(Calendar.MINUTE)}:" +
                "${calendar.get(Calendar.SECOND)}"
    }

    fun parseString(str: String): Date {
        val formatter = SimpleDateFormat(datePattern, Locale.getDefault())
        return formatter.parse(str)
    }

    fun toHumanDate(str: String): String {
        val dateFormatter = SimpleDateFormat(displayPattern, Locale.getDefault())
        return dateFormatter.format(parseString(str))
    }
}