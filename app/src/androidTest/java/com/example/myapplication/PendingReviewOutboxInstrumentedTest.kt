package com.example.myapplication

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.myapplication.data.PendingReviewOutbox
import com.example.myapplication.study.StudyReview
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class PendingReviewOutboxInstrumentedTest {
    @Test
    fun pendingReviewsSurviveRecreationAndRemainAccountScoped() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val firstUid = UUID.randomUUID().toString()
        val otherUid = UUID.randomUUID().toString()
        val id = UUID.randomUUID().toString()
        val review = StudyReview(
            id = id, studentId = firstUid, deckId = "deck",
            cardId = "card", classId = "class", correct = false,
            writtenResponse = null, tags = listOf("teste"),
            reviewedAtEpochMillis = 1000L,
            dueAtEpochMillis = 1100L
        )
        PendingReviewOutbox(context, firstUid).enqueue(review)
        assertEquals(
            listOf(review),
            PendingReviewOutbox(context, firstUid).pending(firstUid)
        )
        assertTrue(PendingReviewOutbox(context, otherUid).pending(otherUid).isEmpty())

        // Regravar o mesmo evento substitui seu payload, sem duplicar registros.
        PendingReviewOutbox(context, firstUid).enqueue(review.copy(correct = true))
        assertEquals(
            listOf(review.copy(correct = true)),
            PendingReviewOutbox(context, firstUid).pending(firstUid)
        )
        PendingReviewOutbox(context, firstUid).remove(id)
        assertTrue(PendingReviewOutbox(context, firstUid).pending(firstUid).isEmpty())
    }
}
