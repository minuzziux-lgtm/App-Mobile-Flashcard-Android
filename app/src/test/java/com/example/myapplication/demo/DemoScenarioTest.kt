package com.example.myapplication.demo

import org.junit.Assert.*
import org.junit.Test

class DemoScenarioTest {
    @Test fun seedsFiveTypesOnlyInEmptyLibrary() {
        val store = LocalMyDeckStore()
        assertTrue(store.seedDemoScenario())
        assertEquals(3, store.classes.size)
        assertEquals(4, store.decks.size)
        assertEquals(13, store.cards.size)
        assertEquals(DemoCardType.entries.toSet(), store.cards.map { it.type }.toSet())
        assertEquals(3, store.enrolledClassIds.size)
        assertTrue(store.reviews.size >= 30)
        assertFalse(store.seedDemoScenario())
        assertEquals(5, store.cards.size)
    }
}
