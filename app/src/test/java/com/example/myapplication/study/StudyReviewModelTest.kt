package com.example.myapplication.study

import org.junit.Assert.*
import org.junit.Test

class StudyReviewModelTest {
    @Test fun createsReviewForCorrectAnswer() {
        val item = StudyReview(
            "s:card", "@aluno-demo", "deck", "card", "class",
            true, null, listOf("gramática"), 1000L, 1000L
        )
        assertTrue(item.correct == true)
        assertEquals("card", item.cardId)
    }

    @Test fun rejectsNegativeTimestamp() {
        assertTrue(runCatching {
            StudyReview("s:c", "@student", "d", "c", null, null, "texto", emptyList(), -1, 0)
        }.isFailure)
    }
}
