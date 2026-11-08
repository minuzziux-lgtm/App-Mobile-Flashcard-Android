package com.example.myapplication.demo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TeacherManagementTest {
    @Test fun renameAndDeleteClassRemovesChildren() {
        val store = LocalMyDeckStore()
        val classroom = store.createClass("A")
        store.renameClass(classroom.id, "B")
        assertEquals("B", store.classes.single().name)
        val deck = store.createDeck(classroom.id, "Deck")
        store.addCard(deck.id, "Pergunta", "Resposta", listOf("tag"))
        store.deleteClass(classroom.id)
        assertTrue(store.classes.isEmpty())
        assertTrue(store.decks.isEmpty())
        assertTrue(store.cards.isEmpty())
    }
}
