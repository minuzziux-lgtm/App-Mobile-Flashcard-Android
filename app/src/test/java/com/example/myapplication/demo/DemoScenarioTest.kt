package com.example.myapplication.demo

import org.junit.Assert.*
import org.junit.Test

class DemoScenarioTest {
    @Test fun seedsFiveTypesOnlyInEmptyLibrary() {
        val store = LocalMyDeckStore()
        assertTrue(store.seedDemoScenario())
        assertEquals(1, store.classes.size)
        assertEquals(1, store.decks.size)
        assertEquals(5, store.cards.size)
        assertEquals(DemoCardType.entries.toSet(), store.cards.map { it.type }.toSet())
        assertEquals(1, store.enrolledClassIds.size)
        assertFalse(store.seedDemoScenario())
        assertEquals(5, store.cards.size)
    }
}
