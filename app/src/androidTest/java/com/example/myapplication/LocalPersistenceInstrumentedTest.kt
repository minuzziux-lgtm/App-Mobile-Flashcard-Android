package com.example.myapplication

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.myapplication.demo.JoinClassResult
import com.example.myapplication.demo.LocalMyDeckStore
import com.example.myapplication.study.StudyReview
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Teste de integração real com SharedPreferences: requer emulador/dispositivo Android.
 * O app deve ser instalado em um ambiente exclusivo de testes, pois o snapshot
 * demonstrativo é apagado no início e no fim dos testes.
 */
@RunWith(AndroidJUnit4::class)
class LocalPersistenceInstrumentedTest {
    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    private fun resetDemo() {
        context.getSharedPreferences("mydeck_local_demo_v2", Context.MODE_PRIVATE)
            .edit().clear().commit()
        Unit
    }

    @Before fun before() = resetDemo()
    @After fun after() = resetDemo()

    @Test fun reloadKeepsClassesDecksCardsAndEnrollmentPerStudent() {
        val store = LocalMyDeckStore(context)
        val classroom = store.createClass("Turma teste")
        val deck = store.createDeck(classroom.id, "Revisão")
        val card = store.addCard(deck.id, "Pergunta", "Resposta", listOf("tema"))
        assertTrue(store.joinByCode(classroom.joinCode, "student-a") is JoinClassResult.Joined)

        val restored = LocalMyDeckStore(context)
        assertEquals(classroom.id, restored.classes.single().id)
        assertEquals(deck.id, restored.decks.single().id)
        assertEquals(card.id, restored.cards.single().id)
        assertEquals(listOf(classroom.id), restored.enrolledForStudent("student-a"))
        assertTrue(restored.enrolledForStudent("student-b").isEmpty())
        assertEquals(listOf(card.id), restored.pendingCards(1_000L, "student-a").map { it.id })
        assertTrue(restored.pendingCards(1_000L, "student-b").isEmpty())
    }

    @Test fun reloadPreservesIndependentReviewSchedules() {
        val store = LocalMyDeckStore(context)
        val classroom = store.createClass("Turma")
        val deck = store.createDeck(classroom.id, "Deck")
        val card = store.addCard(deck.id, "Q", "A", emptyList())
        store.joinByCode(classroom.joinCode, "student-a")
        store.joinByCode(classroom.joinCode, "student-b")
        store.recordReview(
            StudyReview(
                id = "session-a:card-a",
                studentId = "student-a",
                deckId = deck.id,
                cardId = card.id,
                classId = classroom.id,
                correct = true,
                writtenResponse = null,
                tags = emptyList(),
                reviewedAtEpochMillis = 1_000L,
                dueAtEpochMillis = 1_000L
            )
        )

        val restored = LocalMyDeckStore(context)
        assertEquals(1, restored.reviews.size)
        assertFalse(restored.reviews.any { it.studentId == "student-b" })
        assertTrue(restored.pendingCards(2_000L, "student-a").isEmpty())
        assertEquals(listOf(card.id), restored.pendingCards(2_000L, "student-b").map { it.id })
    }

    @Test fun recordingSameReviewIdDoesNotDuplicateAfterRestart() {
        val store = LocalMyDeckStore(context)
        val classroom = store.createClass("A")
        val deck = store.createDeck(classroom.id, "B")
        val card = store.addCard(deck.id, "Q", "A", emptyList())
        val review = StudyReview(
            id = "one-session:one-card",
            studentId = "student-a",
            deckId = deck.id,
            cardId = card.id,
            classId = classroom.id,
            correct = false,
            writtenResponse = null,
            tags = emptyList(),
            reviewedAtEpochMillis = 3_000L,
            dueAtEpochMillis = 3_000L
        )
        store.recordReview(review)
        val restored = LocalMyDeckStore(context)
        restored.recordReview(review.copy(correct = true))
        val again = LocalMyDeckStore(context)
        assertEquals(1, again.reviews.size)
        assertEquals(true, again.reviews.single().correct)
    }
}
