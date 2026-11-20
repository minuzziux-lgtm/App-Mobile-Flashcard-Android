package com.example.myapplication.learning

import com.example.myapplication.study.StudyReview
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LearningRecommendationsTest {
    private fun review(id: Int, correct: Boolean?, tag: String = "gramatica"): StudyReview =
        StudyReview(
            id = id.toString(), studentId = "aluno", deckId = "deck",
            cardId = "card$id", classId = "turma",
            correct = correct, writtenResponse = null, tags = listOf(tag),
            reviewedAtEpochMillis = id.toLong(), dueAtEpochMillis = id.toLong()
        )

    @Test fun recommendsOnlyWithEnoughAnswers() {
        assertTrue(studySuggestions((1..4).map { review(it, false) }, emptyList(), setOf("deck")).isEmpty())
        val result = studySuggestions((1..5).map { review(it, false) }, emptyList(), setOf("deck"))
        assertEquals("gramatica", result.single().tag)
        assertEquals(0, result.single().accuracy)
    }

    @Test fun ignoresFreeResponsesAndInaccessibleDecks() {
        val reviews = (1..5).map { review(it, false) } + review(6, null)
        assertEquals(5, studySuggestions(reviews, emptyList(), setOf("deck")).single().graded)
        assertTrue(studySuggestions(reviews, emptyList(), emptySet()).isEmpty())
    }

    @Test fun doesNotFlagSixtyPercentAccuracy() {
        val reviews = (1..3).map { review(it, true) } + (4..5).map { review(it, false) }
        assertTrue(studySuggestions(reviews, emptyList(), setOf("deck")).isEmpty())
    }

    @Test fun selectsMatchingMaterialsOnly() {
        val materials = listOf(
            LearningResource("1", "Revisar gramatica", "https://example.com", LearningResourceType.LINK,
                "turma", "deck", listOf("gramatica")),
            LearningResource("2", "Outro deck", "https://example.com", LearningResourceType.LINK,
                "turma", "restrito", listOf("gramatica"))
        )
        val suggestions = studySuggestions((1..5).map { review(it, false) }, materials, setOf("deck"))
        assertEquals(listOf("1"), suggestions.single().resources.map { it.id })
    }

    @Test fun requiresSecureUrls() {
        assertTrue(validLearningUrl("https://example.org/aula.pdf"))
        assertTrue(!validLearningUrl("javascript:alert(1)"))
        assertTrue(!validLearningUrl("http://example.org"))
    }
}
