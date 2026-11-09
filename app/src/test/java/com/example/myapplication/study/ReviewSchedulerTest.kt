package com.example.myapplication.study

import org.junit.Assert.*
import org.junit.Test

class ReviewSchedulerTest {
    private fun event(at: Long, correct: Boolean?) =
        StudyReview("id", "@aluno-demo", "deck", "card", null, correct,
            null, emptyList(), at, at)

    @Test fun correctStreakUsesIncreasingIntervals() {
        val first = scheduleReview(event(0, true), null)
        assertEquals(1, first.intervalDays)
        val second = scheduleReview(event(86_400_000, true), first)
        assertEquals(3, second.intervalDays)
        val third = scheduleReview(event(4*86_400_000L,true), second)
        assertEquals(7, third.intervalDays)
    }
    @Test fun errorsRetryInTenMinutes() {
        val wrong = scheduleReview(event(1000, false), null)
        assertEquals(601000L, wrong.dueAtEpochMillis)
        assertEquals(0, wrong.repetitions)
        assertFalse(isReviewDue(wrong, 600999))
        assertTrue(isReviewDue(wrong, 601000))
    }
    @Test fun newCardIsDueAndUngradedDoesNotBecomeCorrect() {
        assertTrue(isReviewDue(null, 10))
        val open = scheduleReview(event(3000, null), null)
        assertNull(open.correct)
        assertEquals(0, open.intervalDays)
    }
}
