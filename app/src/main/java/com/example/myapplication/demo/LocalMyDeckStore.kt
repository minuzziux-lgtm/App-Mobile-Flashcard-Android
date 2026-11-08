package com.example.myapplication.demo

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import java.security.SecureRandom
import java.util.Locale
import java.util.UUID

class LocalMyDeckStore(context: Context? = null) {
    private val persistence = context?.let(::DemoLocalPersistence)
    val classes = mutableStateListOf<DemoClass>()
    val decks = mutableStateListOf<DemoDeck>()
    val cards = mutableStateListOf<DemoCard>()
    val enrolledClassIds = mutableStateListOf<String>()

    init {
        persistence?.restore(this)
    }

    private fun persist() = persistence?.save(this)

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
        persist()
        return classroom
    }

    fun renameClass(classId: String, name: String) {
        require(name.isNotBlank())
        val index = classes.indexOfFirst { it.id == classId }
        require(index >= 0)
        classes[index] = classes[index].copy(name = name.trim())
        persist()
    }

    fun deleteClass(classId: String) {
        require(classes.any { it.id == classId })
        val deckIds = decks.filter { it.classId == classId }.map { it.id }.toSet()
        cards.removeAll { it.deckId in deckIds }
        decks.removeAll { it.id in deckIds }
        enrolledClassIds.remove(classId)
        classes.removeAll { it.id == classId }
        persist()
    }

    fun createDeck(classId: String, title: String): DemoDeck {
        require(title.isNotBlank() && classes.any { it.id == classId })
        val deck = DemoDeck(UUID.randomUUID().toString(), classId, title.trim())
        decks.add(deck)
        persist()
        return deck
    }

    fun addCard(deckId: String, question: String, answer: String, tags: List<String>): DemoCard =
        addCard(deckId, DemoCardDraft(DemoCardType.BASIC, question, answer, tags = tags))

    fun addCard(deckId: String, draft: DemoCardDraft): DemoCard {
        require(decks.any { it.id == deckId })
        val validationError = validateCardDraft(draft)
        require(validationError == null) { validationError ?: "Cartão inválido." }
        val card = DemoCard(
            id = UUID.randomUUID().toString(),
            deckId = deckId,
            question = draft.question.trim(),
            answer = draft.answer.trim(),
            tags = draft.tags.map(String::trim).filter(String::isNotEmpty).distinct(),
            type = draft.type,
            description = draft.description.trim(),
            options = draft.options.map(String::trim),
            correctOptionIndex = draft.correctOptionIndex,
            frontLanguage = draft.frontLanguage.trim(),
            backLanguage = draft.backLanguage.trim(),
            mediaLink = draft.mediaLink?.copy(url = draft.mediaLink.url.trim())
        )
        cards.add(card)
        persist()
        return card
    }

    fun joinByCode(rawCode: String): JoinClassResult {
        val code = rawCode.trim().uppercase(Locale.ROOT)
        val classroom = classes.firstOrNull { it.joinCode == code } ?: return JoinClassResult.NotFound
        if (classroom.id in enrolledClassIds) return JoinClassResult.AlreadyJoined
        enrolledClassIds.add(classroom.id)
        persist()
        return JoinClassResult.Joined(classroom)
    }
}
