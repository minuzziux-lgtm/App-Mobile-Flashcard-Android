package com.example.myapplication.demo

import androidx.compose.runtime.mutableStateListOf
import java.security.SecureRandom
import java.util.Locale
import java.util.UUID

data class DemoClass(val id: String, val name: String, val joinCode: String)
data class DemoDeck(val id: String, val classId: String, val title: String)
data class DemoCard(
    val id: String,
    val deckId: String,
    val question: String,
    val answer: String,
    val tags: List<String>
)

sealed interface JoinClassResult {
    data class Joined(val classroom: DemoClass) : JoinClassResult
    data object AlreadyJoined : JoinClassResult
    data object NotFound : JoinClassResult
}

class LocalMyDeckStore {
    val classes = mutableStateListOf<DemoClass>()
    val decks = mutableStateListOf<DemoDeck>()
    val cards = mutableStateListOf<DemoCard>()
    val enrolledClassIds = mutableStateListOf<String>()

    private val random = SecureRandom()
    private val alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

    private fun generateClassCode(): String {
        var code: String
        do {
            code = (1..8).map { alphabet[random.nextInt(alphabet.length)] }.joinToString("")
        } while (classes.any { it.joinCode == code })
        return code
    }

    fun createClass(name: String): DemoClass {
        require(name.isNotBlank())
        val classroom = DemoClass(UUID.randomUUID().toString(), name.trim(), generateClassCode())
        classes.add(classroom)
        return classroom
    }

    fun createDeck(classId: String, title: String): DemoDeck {
        require(title.isNotBlank() && classes.any { it.id == classId })
        val deck = DemoDeck(UUID.randomUUID().toString(), classId, title.trim())
        decks.add(deck)
        return deck
    }

    fun addCard(deckId: String, question: String, answer: String, tags: List<String>): DemoCard {
        require(question.isNotBlank() && answer.isNotBlank() && decks.any { it.id == deckId })
        val card = DemoCard(
            UUID.randomUUID().toString(), deckId, question.trim(), answer.trim(),
            tags.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        )
        cards.add(card)
        return card
    }

    fun joinByCode(rawCode: String): JoinClassResult {
        val code = rawCode.trim().uppercase(Locale.ROOT)
        val classroom = classes.firstOrNull { it.joinCode == code } ?: return JoinClassResult.NotFound
        if (classroom.id in enrolledClassIds) return JoinClassResult.AlreadyJoined
        enrolledClassIds.add(classroom.id)
        return JoinClassResult.Joined(classroom)
    }
}
