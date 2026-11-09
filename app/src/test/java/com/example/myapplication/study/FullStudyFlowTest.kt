package com.example.myapplication.study

import org.junit.Assert.*
import org.junit.Test

class FullStudyFlowTest {
    @Test fun completesMixedDeckWithoutSkippingRequiredReviews() {
        val ids = listOf("basic", "choice", "cloze", "reversible", "free")
        var session = startStudySession("deck", ids,
            requiredRatingCardIds = setOf("basic", "choice", "cloze", "reversible"),
            requiredWrittenCardIds = setOf("free")
        )
        for (card in ids.take(4)) {
            assertEquals(card, session.currentCardId)
            session = session.revealAnswer()
            assertEquals(card, session.nextCard().currentCardId)
            session = session.rateCurrentCard(StudyRecall.CORRECT).nextCard()
        }
        assertEquals("free", session.currentCardId)
        assertEquals(session, session.revealAnswer().nextCard())
        session = session.submitOpenResponse("Uma reflexão sobre o assunto").revealAnswer().nextCard()
        assertEquals(StudySessionStatus.COMPLETED, session.status)
        assertEquals(4, session.correctCount)
        assertEquals(0, session.incorrectCount)
        assertEquals(5, session.viewedCards)
        assertEquals(1f, session.progress, 0.0001f)
        assertEquals(1, session.writtenResponses.size)
    }

    @Test fun pausePreservesRecordedRatingAndResumeAllowsCompletion() {
        val start = startStudySession("deck", listOf("a"), setOf("a"))
            .revealAnswer().rateCurrentCard(StudyRecall.INCORRECT)
        val paused = start.pause()
        assertEquals(StudySessionStatus.PAUSED, paused.status)
        assertEquals(1, paused.incorrectCount)
        assertEquals(paused, paused.nextCard())
        val finished = paused.resume().nextCard()
        assertEquals(StudySessionStatus.COMPLETED, finished.status)
        assertEquals(1, finished.incorrectCount)
    }

    @Test fun invalidTransitionsDoNotMutateImmutableState() {
        val start = startStudySession("deck", listOf("a"), setOf("a"))
        assertEquals(start, start.nextCard())
        assertEquals(start, start.rateCurrentCard(StudyRecall.CORRECT))
        val after = start.revealAnswer().rateCurrentCard(StudyRecall.CORRECT).nextCard()
        assertEquals(after, after.resume())
        assertEquals(after, after.revealAnswer())
    }

    @Test fun inputEvaluationIsIndependentFromUi() {
        assertEquals(StudyRecall.CORRECT, gradeMultipleChoice(2, 2, 4))
        assertEquals(StudyRecall.INCORRECT, gradeMultipleChoice(0, 2, 4))
        assertEquals(StudyRecall.CORRECT, gradeCloze(" SÃO  PAULO ", "são paulo"))
        assertEquals(StudyRecall.INCORRECT, gradeCloze("Rio", "São Paulo"))
    }
}
