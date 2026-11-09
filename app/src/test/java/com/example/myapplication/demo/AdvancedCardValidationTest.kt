package com.example.myapplication.demo

import org.junit.Assert.*
import org.junit.Test

class AdvancedCardValidationTest {
    @Test fun rejectsInvalidChoiceOptions() {
        assertNotNull(validateCardDraft(DemoCardDraft(
            type = DemoCardType.MULTIPLE_CHOICE,
            question = "Q", answer = "A", options = listOf("A", "A"), correctOptionIndex = 0
        )))
        assertNotNull(validateCardDraft(DemoCardDraft(
            type = DemoCardType.MULTIPLE_CHOICE,
            question = "Q", answer = "A", options = listOf("A", "B"), correctOptionIndex = 9
        )))
    }

    @Test fun validatesClozeAndOpenEnded() {
        assertNull(validateCardDraft(DemoCardDraft(DemoCardType.CLOZE, "Céu ___", "azul")))
        assertNotNull(validateCardDraft(DemoCardDraft(DemoCardType.CLOZE, "Sem lacuna", "azul")))
        assertNull(validateCardDraft(DemoCardDraft(DemoCardType.OPEN_ENDED, "Explique algo")))
        assertNotNull(validateCardDraft(DemoCardDraft(DemoCardType.OPEN_ENDED, "Explique algo", "gabarito indevido")))
    }

    @Test fun mediaMustBeHttpsWithoutCredentials() {
        assertFalse(isValidExternalLink("http://example.com/card.pdf"))
        assertFalse(isValidExternalLink("https://alice:pass@example.com/video"))
        assertTrue(isValidExternalLink("https://example.org/cards/lesson.pdf"))
    }

    @Test fun editedCardPreservesStableIdentifier() {
        val store = LocalMyDeckStore()
        val klass = store.createClass("A")
        val deck = store.createDeck(klass.id, "Deck")
        val card = store.addCard(deck.id, DemoCardDraft(DemoCardType.BASIC, "Frente", "Verso"))
        store.updateCard(card.id, DemoCardDraft(DemoCardType.REVERSIBLE,
            "Hello", "Olá", frontLanguage = "English", backLanguage = "Português"))
        assertEquals(card.id, store.cards.single().id)
        assertEquals(DemoCardType.REVERSIBLE, store.cards.single().type)
    }
}
