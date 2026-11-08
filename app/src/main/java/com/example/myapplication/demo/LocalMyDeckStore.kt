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
    val availableTags = mutableStateListOf<String>()

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
        val deckIds = decks.filter {
            it.classId == classId && it.assignedClassIds.none { assigned -> assigned != classId }
        }.map { it.id }.toSet()
        cards.removeAll { it.deckId in deckIds }
        for (index in decks.indices.reversed()) {
            val deck = decks[index]
            if (deck.id in deckIds) decks.removeAt(index)
            else if (deck.classId == classId || classId in deck.assignedClassIds) {
                decks[index] = deck.copy(
                    classId = if (deck.classId == classId) "" else deck.classId,
                    assignedClassIds = deck.assignedClassIds.filterNot { it == classId }
                )
            }
        }
        enrolledClassIds.remove(classId)
        classes.removeAll { it.id == classId }
        persist()
    }

    fun createDeck(classId: String, title: String): DemoDeck {
        require(title.isNotBlank() && (classId.isBlank() || classes.any { it.id == classId }))
        val deck = DemoDeck(UUID.randomUUID().toString(), classId, title.trim())
        decks.add(deck)
        persist()
        return deck
    }

    fun renameDeck(deckId: String, title: String) {
        require(title.isNotBlank())
        val index = decks.indexOfFirst { it.id == deckId }
        require(index >= 0)
        decks[index] = decks[index].copy(title = title.trim())
        persist()
    }

    fun assignDeckToClasses(deckId: String, classIds: List<String>) {
        val index = decks.indexOfFirst { it.id == deckId }
        require(index >= 0)
        val ids = classIds.distinct()
        require(ids.all { id -> classes.any { it.id == id } })
        decks[index] = decks[index].copy(assignedClassIds = ids)
        persist()
    }

    fun deleteDeck(deckId: String) {
        require(decks.any { it.id == deckId })
        cards.removeAll { it.deckId == deckId }
        decks.removeAll { it.id == deckId }
        persist()
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
        registerTags(card.tags)
        persist()
        return card
    }

    fun updateCard(cardId: String, draft: DemoCardDraft) {
        val index = cards.indexOfFirst { it.id == cardId }
        require(index >= 0)
        val error = validateCardDraft(draft)
        require(error == null) { error ?: "Cartão inválido." }
        cards[index] = cards[index].copy(
            question = draft.question.trim(),
            answer = draft.answer.trim(),
            tags = draft.tags.map(String::trim).filter(String::isNotBlank).distinct(),
            type = draft.type,
            description = draft.description.trim(),
            options = draft.options.map(String::trim),
            correctOptionIndex = draft.correctOptionIndex,
            frontLanguage = draft.frontLanguage.trim(),
            backLanguage = draft.backLanguage.trim(),
            mediaLink = draft.mediaLink?.copy(url = draft.mediaLink.url.trim())
        )
        registerTags(draft.tags)
        persist()
    }

    fun duplicateCard(cardId: String): DemoCard {
        val original = cards.first { it.id == cardId }
        val copy = original.copy(id = UUID.randomUUID().toString())
        cards.add(copy)
        persist()
        return copy
    }

    fun deleteCard(cardId: String) {
        require(cards.any { it.id == cardId })
        cards.removeAll { it.id == cardId }
        persist()
    }

    private fun registerTags(items: List<String>) {
        items.map(String::trim).filter(String::isNotBlank).forEach { name ->
            if (availableTags.none { it.equals(name, ignoreCase = true) }) availableTags.add(name)
        }
    }

    fun addTag(name: String) {
        val clean = name.trim()
        require(clean.isNotEmpty() && clean.length <= 40)
        registerTags(listOf(clean))
        persist()
    }

    fun renameTag(old: String, replacement: String) {
        val clean = replacement.trim()
        require(clean.isNotEmpty() && clean.length <= 40)
        val index = availableTags.indexOfFirst { it.equals(old, ignoreCase = true) }
        require(index >= 0)
        require(availableTags.none { it.equals(clean, ignoreCase = true) && !it.equals(old, ignoreCase = true) })
        availableTags[index] = clean
        for (i in cards.indices) {
            val card = cards[i]
            if (card.tags.any { it.equals(old, ignoreCase = true) }) {
                cards[i] = card.copy(tags = card.tags.map { if (it.equals(old, ignoreCase = true)) clean else it })
            }
        }
        persist()
    }

    fun deleteTag(name: String) {
        require(availableTags.any { it.equals(name, ignoreCase = true) })
        availableTags.removeAll { it.equals(name, ignoreCase = true) }
        for (i in cards.indices) {
            val card = cards[i]
            cards[i] = card.copy(tags = card.tags.filterNot { it.equals(name, ignoreCase = true) })
        }
        persist()
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
