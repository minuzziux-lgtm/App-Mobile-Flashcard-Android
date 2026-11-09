package com.example.myapplication.data

import com.example.myapplication.demo.DemoCardDraft
import com.example.myapplication.demo.DemoCardType
import com.example.myapplication.demo.LocalMyDeckStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalMyDeckContentRepositoryTest {
    @Test fun sharesOneDeckWithMultipleClassesButNotUnassignedClasses() {
        val store = LocalMyDeckStore()
        val repo = LocalMyDeckContentRepository(store)
        val a = store.createClass("A")
        val b = store.createClass("B")
        val deck = store.createDeck("", "Compartilhado")
        store.addCard(deck.id, DemoCardDraft(DemoCardType.BASIC, "Pergunta", "Resposta"))

        repo.assignDeckToClasses(deck.id, listOf(a.id, b.id)) {
            assertTrue(it is DataResult.Success)
        }
        repo.decksForClass(a.id) {
            assertEquals(listOf(deck.id), (it as DataResult.Success).value.map { d -> d.id })
        }
        repo.decksForClass(b.id) {
            assertEquals(listOf(deck.id), (it as DataResult.Success).value.map { d -> d.id })
        }
        repo.cardsForDeck(deck.id) {
            assertEquals(1, (it as DataResult.Success).value.size)
        }
        repo.assignDeckToClasses(deck.id, emptyList()) { assertTrue(it is DataResult.Success) }
        repo.decksForClass(a.id) {
            assertTrue((it as DataResult.Success).value.isEmpty())
        }
        assertEquals(1, store.cards.size)
    }

    @Test fun separatesStudentsAndRejectsInvalidCode() {
        val store = LocalMyDeckStore()
        val repo = LocalMyDeckContentRepository(store)
        val classroom = store.createClass("Teste")
        repo.joinClass(classroom.joinCode, "student-one") {
            assertTrue(it is DataResult.Success)
        }
        repo.studentClasses("student-one") {
            assertEquals(1, (it as DataResult.Success).value.size)
        }
        repo.studentClasses("student-two") {
            assertTrue((it as DataResult.Success).value.isEmpty())
        }
        repo.joinClass("INVALID", "student-two") {
            assertTrue(it is DataResult.Failure)
        }
    }
}
