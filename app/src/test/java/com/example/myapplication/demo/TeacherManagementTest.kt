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

    @Test fun editDuplicateAndDeleteDeckCards() {
        val store = LocalMyDeckStore()
        val classroom = store.createClass("Turma")
        val deck = store.createDeck(classroom.id, "Original")
        store.renameDeck(deck.id, "Novo")
        assertEquals("Novo", store.decks.single().title)
        val card = store.addCard(deck.id, "Pergunta", "Resposta", emptyList())
        store.updateCard(card.id, DemoCardDraft(DemoCardType.BASIC, "Atualizada", "Sim"))
        assertEquals("Atualizada", store.cards.single().question)
        val duplicated = store.duplicateCard(card.id)
        assertEquals(2, store.cards.size)
        store.deleteCard(duplicated.id)
        assertEquals(1, store.cards.size)
        store.deleteDeck(deck.id)
        assertTrue(store.decks.isEmpty())
        assertTrue(store.cards.isEmpty())
    }

    @Test fun generalDeckAssignmentIsControlledByClass() {
        val store = LocalMyDeckStore()
        val first = store.createClass("Primeira")
        val second = store.createClass("Segunda")
        val general = store.createDeck("", "Vocabulário")
        assertTrue(general.assignedClassIds.isEmpty())
        store.assignDeckToClasses(general.id, listOf(first.id))
        assertEquals(listOf(first.id), store.decks.single().assignedClassIds)
        store.deleteClass(first.id)
        assertTrue(store.decks.single().assignedClassIds.isEmpty())
        store.assignDeckToClasses(general.id, listOf(second.id))
        assertEquals(listOf(second.id), store.decks.single().assignedClassIds)
    }

    @Test fun demoStudentAppearsAndCanBeRemoved() {
        val store = LocalMyDeckStore()
        val classroom = store.createClass("Turma")
        assertTrue(store.joinByCode(classroom.joinCode) is JoinClassResult.Joined)
        assertEquals(1, store.participants.size)
        assertEquals("@aluno-demo", store.participants.single().username)
        val fake = store.addDemoParticipant(classroom.id, "@maria")
        assertEquals(2, store.participants.size)
        store.removeDemoParticipant(fake.id)
        assertEquals(1, store.participants.size)
        store.removeDemoParticipant(store.participants.single().id)
        assertTrue(store.enrolledClassIds.isEmpty())
        assertTrue(store.participants.isEmpty())
    }
}
