package com.example.myapplication.data

import com.example.myapplication.study.StudyReview
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FirestoreReviewMapperTest {
    @Test fun preservesStudyScheduleForSelfRatedAndWrittenCards() {
        val samples = listOf(
            StudyReview("session:card1", "student", "deck", "card1", "class", true, null, listOf("tag"), 1700000000000L, 1700086400000L, 1, 1),
            StudyReview("session:card2", "student", "deck", "card2", "class", false, null, emptyList(), 1700000000000L, 1700000010000L, 0, 0),
            StudyReview("session:card3", "student", "deck", "card3", null, null, "Resposta aberta", emptyList(), 1700000000000L, 1700000000000L)
        )
        for (review in samples) {
            val stored = FirestoreReviewMapper.toFields(review)
            assertEquals(review, FirestoreReviewMapper.fromFields(review.id, stored))
            assertEquals(review.id, review.id)
        }
    }

    @Test fun rejectsMalformedDatesAndMissingIdentity() {
        val review = StudyReview("id", "student", "deck", "card", null, true, null, emptyList(), 10L, 20L)
        assertNull(FirestoreReviewMapper.fromFields("id", FirestoreReviewMapper.toFields(review) - "studentId"))
        assertNull(FirestoreReviewMapper.fromFields("id", FirestoreReviewMapper.toFields(review) + ("dueAtEpochMillis" to -1L)))
    }
}
