package com.example.myapplication.demo

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
