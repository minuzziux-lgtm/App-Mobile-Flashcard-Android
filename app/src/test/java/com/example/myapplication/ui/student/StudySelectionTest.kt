package com.example.myapplication.ui.student

import com.example.myapplication.demo.DemoCard
import com.example.myapplication.demo.DemoClass
import com.example.myapplication.demo.DemoDeck
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StudySelectionTest {
    private val classA = DemoClass("class-a", "A", "AAAA2222")
    private val classB = DemoClass("class-b", "B", "BBBB3333")
    private val hidden = DemoDeck("hidden", "class-b", "Não matriculado")
    private val shared = DemoDeck("shared", "", "Compartilhado", listOf("class-b", "class-a"))
    private val cards = listOf(
        DemoCard("not-allowed", "hidden", "Q", "A", emptyList()),
        DemoCard("one", "shared", "Q1", "A1", emptyList()),
        DemoCard("two", "shared", "Q2", "A2", emptyList())
    )

    @Test fun skipsDecksOutsideEnrolledClasses() {
        val selected = findStudySelection(listOf(classA), listOf(hidden, shared), cards, emptySet(), false)
        assertEquals("shared", selected?.deckId)
        assertEquals("class-a", selected?.classId)
        assertEquals(listOf("one", "two"), selected?.cards?.map { it.id })
    }

    @Test fun reviewsOnlyDueCardsAndRespectsClassMembership() {
        val selected = findStudySelection(
            listOf(classA), listOf(hidden, shared), cards, setOf("not-allowed", "two"), true
        )
        assertEquals(listOf("two"), selected?.cards?.map { it.id })
    }

    @Test fun reviewIsRecordedOnlyAgainstAnAssignedClass() {
        assertEquals(
            "class-a",
            resolveStudyClassId("class-b", shared, listOf(classA))
        )
        assertEquals(
            "class-b",
            resolveStudyClassId("class-b", shared, listOf(classA, classB))
        )
        assertNull(resolveStudyClassId("class-b", hidden, listOf(classA)))
    }

    @Test fun noAvailableCardsDoesNotStartEmptySession() {
        assertNull(findStudySelection(listOf(classA), listOf(shared), cards, emptySet(), true))
        assertNull(findStudySelection(emptyList(), listOf(shared), cards, setOf("one"), false))
        assertNull(findStudySelection(listOf(classB), emptyList(), cards, setOf("one"), false))
    }
}
