package com.example.myapplication.data

import com.example.myapplication.demo.LocalMyDeckStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MigrationPreflightTest {
    @Test fun teacherMigratesLibraryWithoutCopyingStudentReviews() {
        val store = LocalMyDeckStore()
        val classroom = store.createClass("Matemática")
        val deck = store.createDeck(classroom.id, "Frações")
        store.addCard(deck.id, "Pergunta", "Resposta", emptyList())
        val result = inspectLocalMigration(store, "teacher-uid", MigrationRole.TEACHER)
        assertTrue(result.canStart)
        assertEquals(1, result.classes)
        assertEquals(1, result.decks)
        assertEquals(1, result.cards)
        assertEquals(0, result.ownReviews)
    }

    @Test fun studentCannotClaimTeacherLibraryAsTheirOwn() {
        val store = LocalMyDeckStore()
        val classroom = store.createClass("Turma local")
        store.createDeck(classroom.id, "Conteúdo")
        val result = inspectLocalMigration(store, "student-uid", MigrationRole.STUDENT)
        assertTrue(result.canStart)
        assertEquals(0, result.classes)
        assertEquals(0, result.decks)
        assertTrue(result.notices.any { "biblioteca" in it.lowercase() })
    }

    @Test fun refusesOrphanedCardsAndAssignments() {
        val store = LocalMyDeckStore()
        val classroom = store.createClass("Turma")
        val deck = store.createDeck(classroom.id, "Deck")
        store.addCard(deck.id, "Pergunta", "Resposta", emptyList())
        store.classes.clear()
        val result = inspectLocalMigration(store, "teacher", MigrationRole.TEACHER)
        assertFalse(result.canStart)
        assertTrue(result.problems.isNotEmpty())
    }
}
