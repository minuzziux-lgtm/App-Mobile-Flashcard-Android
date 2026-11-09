package com.example.myapplication.study

import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class ReviewPeriodTest {
    @Test fun filtersYearAndMonthSeparately() {
        val tz = TimeZone.getTimeZone("UTC")
        fun review(month: Int): StudyReview {
            val c = Calendar.getInstance(tz)
            c.clear()
            c.set(2026, month, 1)
            return StudyReview("r$month","@x","d","c$month",null,true,null,emptyList(),c.timeInMillis,c.timeInMillis)
        }
        val data = listOf(review(0), review(6))
        assertEquals(2, filterReviewsByPeriod(data, 2026, null, tz).size)
        assertEquals(1, filterReviewsByPeriod(data, 2026, 6, tz).size)
        assertTrue(filterReviewsByPeriod(data, 2025, null, tz).isEmpty())
    }
}
