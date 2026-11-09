package com.example.myapplication.study

import java.util.Calendar
import java.util.TimeZone

/** Dia civil local usado pelos gráficos; respeita mudanças de horário de verão. */
fun localDayStart(epochMillis: Long, timezone: TimeZone = TimeZone.getDefault()): Long {
    val c = Calendar.getInstance(timezone)
    c.timeInMillis = epochMillis
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    return c.timeInMillis
}

fun activityByDay(reviews: List<StudyReview>, timezone: TimeZone = TimeZone.getDefault()): Map<Long, Int> =
    reviews.groupingBy { localDayStart(it.reviewedAtEpochMillis, timezone) }.eachCount()
