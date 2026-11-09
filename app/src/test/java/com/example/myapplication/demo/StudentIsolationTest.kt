package com.example.myapplication.demo

import com.example.myapplication.study.StudyReview
import org.junit.Assert.*
import org.junit.Test

class StudentIsolationTest {
    @Test fun enrollmentsAndDueCardsAreScopedByStudent() {
        val store = LocalMyDeckStore()
        val a = store.createClass("Class A")
        val b = store.createClass("Class B")
        val da = store.createDeck(a.id, "A")
        val db = store.createDeck(b.id, "B")
        val ca = store.addCard(da.id, "QA", "AA", emptyList())
        val cb = store.addCard(db.id, "QB", "AB", emptyList())
        assertTrue(store.joinByCode(a.joinCode, "studentA") is JoinClassResult.Joined)
        assertTrue(store.joinByCode(b.joinCode, "studentB") is JoinClassResult.Joined)
        assertEquals(listOf(a.id), store.enrolledForStudent("studentA"))
        assertEquals(listOf(b.id), store.enrolledForStudent("studentB"))
        assertEquals(listOf(ca.id), store.pendingCards(1000, "studentA").map { it.id })
        assertEquals(listOf(cb.id), store.pendingCards(1000, "studentB").map { it.id })
        assertTrue(store.joinByCode(a.joinCode, "studentA") is JoinClassResult.AlreadyJoined)
        assertTrue(store.enrolledForStudent("studentC").isEmpty())
    }

    @Test fun reviewByOneStudentDoesNotSuppressAnother() {
        val store = LocalMyDeckStore()
        val cls = store.createClass("Shared")
        val deck = store.createDeck(cls.id, "Deck")
        val card = store.addCard(deck.id, "Q", "A", emptyList())
        store.joinByCode(cls.joinCode, "a")
        store.joinByCode(cls.joinCode, "b")
        store.recordReview(StudyReview("id1","a",deck.id,card.id,cls.id,true,null,emptyList(),1000,1000))
        assertTrue(store.pendingCards(2000, "a").isEmpty())
        assertEquals(listOf(card.id), store.pendingCards(2000, "b").map { it.id })
    }
}
