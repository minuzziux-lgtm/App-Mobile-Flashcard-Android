package com.example.myapplication.demo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class CardValidationTest {
    @Test fun validBasicCard() {
        assertNull(validateCardDraft(DemoCardDraft(DemoCardType.BASIC, "Pergunta", "Resposta")))
    }

    @Test fun rejectsMissingAnswer() {
        assertNotNull(validateCardDraft(DemoCardDraft(DemoCardType.BASIC, "Pergunta")))
    }

    @Test fun validatesSingleCloze() {
        assertNull(validateCardDraft(DemoCardDraft(DemoCardType.CLOZE, "I ___ home.", "am")))
        assertNotNull(validateCardDraft(DemoCardDraft(DemoCardType.CLOZE, "I ___ ___ home.", "am")))
    }

    @Test fun validatesMultipleChoice() {
        val valid = DemoCardDraft(DemoCardType.MULTIPLE_CHOICE, "2 + 2?", "4",
            options = listOf("3", "4", "5"), correctOptionIndex = 1)
        assertNull(validateCardDraft(valid))
        assertNotNull(validateCardDraft(valid.copy(correctOptionIndex = 0)))
        assertNotNull(validateCardDraft(valid.copy(options = listOf("4", "4"))))
    }

    @Test fun validatesOpenEnded() {
        assertNull(validateCardDraft(DemoCardDraft(DemoCardType.OPEN_ENDED, "Explique sua escolha.")))
        assertNotNull(validateCardDraft(DemoCardDraft(DemoCardType.OPEN_ENDED, "Por quê?", "Gabarito")))
    }

    @Test fun acceptsHttpsOnly() {
        assertEquals(true, isValidExternalLink("https://example.com/material.pdf"))
        assertEquals(false, isValidExternalLink("javascript:alert(1)"))
        assertEquals(false, isValidExternalLink("http://example.com"))
    }
}
