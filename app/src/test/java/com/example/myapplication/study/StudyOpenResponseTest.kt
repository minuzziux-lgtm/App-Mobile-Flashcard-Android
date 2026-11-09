package com.example.myapplication.study

import org.junit.Assert.*
import org.junit.Test

class StudyOpenResponseTest {
    @Test fun writtenResponseMustBeSubmittedBeforeNext() {
        val session = startStudySession("deck", listOf("free"), requiredWrittenCardIds = setOf("free"))
        assertEquals(session, session.revealAnswer().nextCard())
        val submitted = session.submitOpenResponse("Minha reflexão").revealAnswer()
        assertEquals("Minha reflexão", submitted.writtenResponses["free"])
        assertEquals(StudySessionStatus.COMPLETED, submitted.nextCard().status)
        assertEquals(0, submitted.correctCount)
    }
    @Test fun emptyResponseIsIgnored() {
        val session = startStudySession("deck", listOf("free"), requiredWrittenCardIds = setOf("free"))
        assertTrue(session.submitOpenResponse("   ").writtenResponses.isEmpty())
    }
}
