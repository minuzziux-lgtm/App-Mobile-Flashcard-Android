package com.example.myapplication.data

import com.example.myapplication.demo.DemoCard
import com.example.myapplication.demo.DemoCardType
import com.example.myapplication.demo.DemoMediaLink
import com.example.myapplication.demo.DemoMediaType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FirestoreCardMapperTest {
    @Test fun preservesAllFiveCardTypesAndMetadata() {
        for (type in DemoCardType.entries) {
            val card = DemoCard(
                id = "card-$type",
                deckId = "deck",
                question = "Pergunta",
                answer = if (type == DemoCardType.OPEN_ENDED) "" else "Resposta",
                tags = listOf("tag-a", "tag-b"),
                type = type,
                description = "Descrição",
                options = if (type == DemoCardType.MULTIPLE_CHOICE) listOf("A", "B") else emptyList(),
                correctOptionIndex = if (type == DemoCardType.MULTIPLE_CHOICE) 1 else null,
                frontLanguage = if (type == DemoCardType.REVERSIBLE) "Português" else "",
                backLanguage = if (type == DemoCardType.REVERSIBLE) "Inglês" else "",
                mediaLink = DemoMediaLink(DemoMediaType.DOCUMENT, "https://example.com/doc.pdf")
            )
            assertEquals(card, FirestoreCardMapper.fromFields(card.id, card.deckId, FirestoreCardMapper.toFields(card)))
        }
    }

    @Test fun decodesNumericIndexFromFirestoreLong() {
        val card = DemoCard(
            id = "1", deckId = "deck", question = "Q", answer = "B",
            tags = emptyList(), type = DemoCardType.MULTIPLE_CHOICE,
            options = listOf("A", "B"), correctOptionIndex = 1
        )
        val fields = FirestoreCardMapper.toFields(card) + ("correctOptionIndex" to 1L)
        assertEquals(1, FirestoreCardMapper.fromFields("1", "deck", fields)?.correctOptionIndex)
    }

    @Test fun rejectsUnknownTypeAndMissingQuestion() {
        val card = DemoCard("1", "d", "Q", "A", emptyList())
        assertNull(FirestoreCardMapper.fromFields("1", "d", FirestoreCardMapper.toFields(card) + ("type" to "UNKNOWN")))
        assertNull(FirestoreCardMapper.fromFields("1", "d", FirestoreCardMapper.toFields(card) - "question"))
    }
}
