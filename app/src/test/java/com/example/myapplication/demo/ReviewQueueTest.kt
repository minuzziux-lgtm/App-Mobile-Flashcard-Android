package com.example.myapplication.demo

import com.example.myapplication.study.StudyReview
import org.junit.Assert.*
import org.junit.Test

class ReviewQueueTest {
    @Test fun onlyAssignedDueCardsAreShown() {
        val store = LocalMyDeckStore()
        val classroom = store.createClass("1A")
        val assigned = store.createDeck(classroom.id, "Física")
        val hidden = store.createDeck("", "Privado")
        val a = store.addCard(assigned.id, "A", "B", emptyList())
        store.addCard(hidden.id, "C", "D", emptyList())
        assertTrue(store.pendingCards(1000).isEmpty())
        store.joinByCode(classroom.joinCode)
        assertEquals(listOf(a.id), store.pendingCards(1000).map { it.id })
        store.recordReview(StudyReview("session:a", "@aluno-demo", assigned.id, a.id, classroom.id,
            true, null, emptyList(), 1000, 1000))
        assertTrue(store.pendingCards(2000).isEmpty())
        assertEquals(listOf(a.id), store.pendingCards(86_401_000L).map { it.id })
    }
}
