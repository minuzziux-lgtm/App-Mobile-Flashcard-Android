package com.example.myapplication.study

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StudySessionTest {
    @Test fun startsWithFirstCardAndAnUnchangedSnapshot() {
        val ids = mutableListOf("a", "b")
        val session = startStudySession("deck", ids)
        ids.clear()
        assertEquals(listOf("a", "b"), session.cardIds)
        assertEquals("a", session.currentCardId)
        assertEquals(0f, session.progress, 0.0001f)
    }

    @Test fun requiresRevealBeforeMovingToNextCard() {
        val session = startStudySession("deck", listOf("a", "b"))
        assertEquals(session, session.nextCard())
        val next = session.revealAnswer().nextCard()
        assertEquals("b", next.currentCardId)
        assertFalse(next.isAnswerRevealed)
        assertEquals(1, next.viewedCards)
    }

    @Test fun pausesAndResumesWithoutChangingPosition() {
        val revealed = startStudySession("deck", listOf("a", "b")).revealAnswer()
        val paused = revealed.pause()
        assertEquals(StudySessionStatus.PAUSED, paused.status)
        assertEquals(paused, paused.nextCard())
        assertEquals(paused, paused.revealAnswer())
        val resumed = paused.resume()
        assertEquals(StudySessionStatus.ACTIVE, resumed.status)
        assertTrue(resumed.isAnswerRevealed)
        assertEquals("b", resumed.nextCard().currentCardId)
    }

    @Test fun completingLastCardDoesNotWrapAround() {
        val finished = startStudySession("deck", listOf("a")).revealAnswer().nextCard()
        assertEquals(StudySessionStatus.COMPLETED, finished.status)
        assertEquals(1f, finished.progress, 0.0001f)
        assertNull(finished.currentCardId)
        assertEquals(finished, finished.nextCard())
    }

    @Test fun rejectsInvalidAndDuplicateCards() {
        listOf(emptyList(), listOf(""), listOf("a", "a")).forEach { ids ->
            assertTrue(runCatching { startStudySession("deck", ids) }.isFailure)
        }
        assertTrue(runCatching { startStudySession("", listOf("a")) }.isFailure)
    }
}
