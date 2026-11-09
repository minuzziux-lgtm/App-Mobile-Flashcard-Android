package com.example.myapplication.study

import org.junit.Assert.*
import org.junit.Test

class StudyStatisticsTest {
    @Test fun excludesUngradedFromAccuracy() {
        fun e(id: String, rating: Boolean?) = StudyReview(
            id, "@aluno-demo", "d", id, null, rating, null, emptyList(), 0,0
        )
        val s = summarizeReviews(listOf(e("a", true),e("b", false),e("c",null)))
        assertEquals(3, s.total)
        assertEquals(2, s.graded)
        assertEquals(50, s.accuracyPercent)
    }
    @Test fun emptySummaryHasZeroAccuracy() {
        assertEquals(0, summarizeReviews(emptyList()).accuracyPercent)
    }
}
