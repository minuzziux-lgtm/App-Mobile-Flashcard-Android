package com.example.myapplication.study

import org.junit.Assert.*
import org.junit.Test

class ReviewIndicatorsTest {
    @Test fun zeroFortyAndOneHundredPending() {
        assertEquals(0, reviewIndicator(0).filledDots)
        assertEquals(1, reviewIndicator(1).filledDots)
        assertEquals(40, reviewIndicator(40).filledDots)
        assertEquals(40, reviewIndicator(100).filledDots)
        assertEquals(60, reviewIndicator(100).overflow)
    }

    @Test fun wrongAnswerResetsStreakAndNextCorrectStartsAtOneDay() {
        fun at(id:String, at:Long, correct:Boolean) =
            StudyReview(id, "@x", "d", "c", null, correct, null, emptyList(), at, at)
        val first = scheduleReview(at("1", 0, true), null)
        val second = scheduleReview(at("2", 86_400_000, true), first)
        val wrong = scheduleReview(at("3", 100_000_000, false), second)
        val again = scheduleReview(at("4", 100_600_000, true), wrong)
        assertEquals(0, wrong.repetitions)
        assertEquals(1, again.repetitions)
        assertEquals(1, again.intervalDays)
    }

    @Test fun isDueUsesExactBoundary() {
        val r = StudyReview("1","@u","d","c",null,false,null,emptyList(),1,1000)
        assertFalse(isReviewDue(r,999))
        assertTrue(isReviewDue(r,1000))
    }
}
