package com.example.myapplication.study

import java.util.Calendar
import java.util.TimeZone

fun filterReviewsByPeriod(
    reviews: List<StudyReview>,
    year: Int?,
    month: Int?,
    timezone: TimeZone = TimeZone.getDefault()
): List<StudyReview> = reviews.filter { review ->
    val calendar = Calendar.getInstance(timezone)
    calendar.timeInMillis = review.reviewedAtEpochMillis
    (year == null || calendar.get(Calendar.YEAR) == year) &&
        (month == null || calendar.get(Calendar.MONTH) == month)
}
