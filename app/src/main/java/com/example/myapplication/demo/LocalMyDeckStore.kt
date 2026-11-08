package com.example.myapplication.demo

import androidx.compose.runtime.mutableStateListOf
import java.security.SecureRandom
import java.util.Locale
import java.util.UUID

data class DemoClass(val id: String, val name: String, val joinCode: String)
data class DemoDeck(val id: String, val classId: String, val title: String)

enum class DemoCardType(val label: String) {
    BASIC("Pergunta e resposta"),
    MULTIPLE_CHOICE("Múltipla escolha"),
    CLOZE("Completar lacuna"),
    REVERSIBLE("Idiomas / reversível"),
    OPEN_ENDED("Resposta livre")
}

enum class DemoMediaType(val label: String) {
    IMAGE("Imagem"), VIDEO("Vídeo"), DOCUMENT("Documento (PDF)")
}

data class DemoMediaLink(val type: DemoMediaType, val url: String)

data class DemoCardDraft(
    val type: DemoCardType,
    val question: String,
    val answer: String = "",
    val description: String = "",
    val tags: List<String> = emptyList(),
    val options: List<String> = emptyList(),
    val correctOptionIndex: Int? = null,
    val frontLanguage: String = "",
    val backLanguage: String = "",
    val mediaLink: DemoMediaLink? = null
)

data class DemoCard(
    val id: String,
    val deckId: String,
    val question: String,
    val answer: String,
    val tags: List<String>,
    val type: DemoCardType = DemoCardType.BASIC,
    val description: String = "",
    val options: List<String> = emptyList(),
    val correctOptionIndex: Int? = null,
    val frontLanguage: String = "",
    val backLanguage: String = "",
    val mediaLink: DemoMediaLink? = null
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
