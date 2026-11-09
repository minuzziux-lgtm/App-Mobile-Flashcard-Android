package com.example.myapplication.study

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StudyRecallTest {
    @Test fun basicCardCannotAdvanceWithoutRating() {
        val session = startStudySession("deck", listOf("basic", "other"), setOf("basic"))
        val revealed = session.revealAnswer()
        assertEquals(revealed, revealed.nextCard())
        assertEquals(revealed, revealed.rateCurrentCard(StudyRecall.CORRECT).pause().rateCurrentCard(StudyRecall.INCORRECT).resume().rateCurrentCard(StudyRecall.CORRECT).copy(ratings = emptyMap()))
        val rated = revealed.rateCurrentCard(StudyRecall.CORRECT)
        assertEquals(StudyRecall.CORRECT, rated.ratings["basic"])
        assertEquals("other", rated.nextCard().currentCardId)
    }

    @Test fun answerCannotBeRatedBeforeRevealOrWhilePaused() {
        val session = startStudySession("deck", listOf("a"), setOf("a"))
        assertEquals(session, session.rateCurrentCard(StudyRecall.INCORRECT))
        val paused = session.revealAnswer().pause()
        assertEquals(paused, paused.rateCurrentCard(StudyRecall.CORRECT))
    }

    @Test fun ratingsAreChangedOnlyForCurrentAndEnabledBasicCards() {
        val session = startStudySession("deck", listOf("a", "b"), setOf("a"))
        val correct = session.revealAnswer().rateCurrentCard(StudyRecall.CORRECT)
        assertEquals(1, correct.correctCount)
        assertEquals(0, correct.incorrectCount)
        val incorrect = correct.rateCurrentCard(StudyRecall.INCORRECT)
        assertEquals(0, incorrect.correctCount)
        assertEquals(1, incorrect.incorrectCount)
        val next = incorrect.nextCard().revealAnswer()
        assertEquals(next, next.rateCurrentCard(StudyRecall.CORRECT))
        assertEquals(1, next.incorrectCount)
    }

    @Test fun rejectsRatingsForCardsOutsideSession() {
        assertTrue(runCatching {
            startStudySession("deck", listOf("a"), setOf("b"))
        }.isFailure)
        assertTrue(runCatching {
            StudySessionState("deck", listOf("a"), requiredRatingCardIds = setOf("a"), ratings = mapOf("b" to StudyRecall.CORRECT))
        }.isFailure)
    }

    @Test fun completionKeepsBasicRating() {
        val complete = startStudySession("deck", listOf("a"), setOf("a"))
            .revealAnswer().rateCurrentCard(StudyRecall.CORRECT).nextCard()
        assertEquals(StudySessionStatus.COMPLETED, complete.status)
        assertTrue(complete.currentCardId == null)
        assertEquals(1, complete.correctCount)
        assertFalse(complete.ratings.isEmpty())
    }
}
