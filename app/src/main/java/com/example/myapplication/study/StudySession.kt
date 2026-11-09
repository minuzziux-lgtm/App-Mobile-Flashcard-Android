package com.example.myapplication.study

/** Estado imutável de uma sessão; a ordem de cartões é congelada no início. */
enum class StudySessionStatus { ACTIVE, PAUSED, COMPLETED }

/** Avaliação autorrelatada do estudante; não representa correção automática. */
enum class StudyRecall { CORRECT, INCORRECT }

data class StudySessionState(
    val deckId: String,
    val cardIds: List<String>,
    val currentIndex: Int = 0,
    val isAnswerRevealed: Boolean = false,
    val status: StudySessionStatus = StudySessionStatus.ACTIVE,
    val requiredRatingCardIds: Set<String> = emptySet(),
    val ratings: Map<String, StudyRecall> = emptyMap(),
    val requiredWrittenCardIds: Set<String> = emptySet(),
    val writtenResponses: Map<String, String> = emptyMap()
) {
    init {
        require(deckId.isNotBlank()) { "O deck deve ser identificado." }
        require(cardIds.isNotEmpty()) { "O deck precisa ter cartões para iniciar uma sessão." }
        require(cardIds.all(String::isNotBlank) && cardIds.distinct().size == cardIds.size) {
            "A sessão deve conter identificadores únicos e válidos."
        }
        require(currentIndex in cardIds.indices) { "Índice do cartão fora da sessão." }
        require(requiredRatingCardIds.all { it in cardIds }) {
            "Não é possível avaliar cartões fora da sessão."
        }
        require(ratings.keys.all { it in requiredRatingCardIds }) {
            "As avaliações precisam pertencer aos cartões avaliáveis."
        }
        require(requiredWrittenCardIds.all { it in cardIds } && writtenResponses.keys.all { it in requiredWrittenCardIds }) {
            "As respostas escritas devem pertencer aos cartões abertos da sessão."
        }
    }

    val totalCards: Int get() = cardIds.size
    val currentCardId: String? get() = if (status == StudySessionStatus.COMPLETED) null else cardIds[currentIndex]
    val viewedCards: Int get() = if (status == StudySessionStatus.COMPLETED) totalCards
        else currentIndex + if (isAnswerRevealed) 1 else 0
    val progress: Float get() = viewedCards.toFloat() / totalCards
    val correctCount: Int get() = ratings.values.count { it == StudyRecall.CORRECT }
    val incorrectCount: Int get() = ratings.values.count { it == StudyRecall.INCORRECT }

    fun submitOpenResponse(response: String): StudySessionState {
        val id = currentCardId ?: return this
        val clean = response.trim()
        if (status != StudySessionStatus.ACTIVE || isAnswerRevealed ||
            id !in requiredWrittenCardIds || clean.isEmpty() || clean.length > 2000) return this
        return copy(writtenResponses = writtenResponses + (id to clean))
    }

    fun rateCurrentCard(recall: StudyRecall): StudySessionState {
        val id = currentCardId ?: return this
        if (status != StudySessionStatus.ACTIVE || !isAnswerRevealed || id !in requiredRatingCardIds) return this
        return copy(ratings = ratings + (id to recall))
    }

    fun revealAnswer(): StudySessionState {
        val id = currentCardId ?: return this
        if (status != StudySessionStatus.ACTIVE) return this
        // Respostas abertas exigem que o texto seja enviado antes de revelar/concluir.
        if (id in requiredWrittenCardIds && id !in writtenResponses) return this
        return copy(isAnswerRevealed = true)
    }

    fun nextCard(): StudySessionState {
        if (status != StudySessionStatus.ACTIVE || !isAnswerRevealed) return this
        if (cardIds[currentIndex] in requiredRatingCardIds && cardIds[currentIndex] !in ratings) return this
        if (cardIds[currentIndex] in requiredWrittenCardIds && cardIds[currentIndex] !in writtenResponses) return this
        return if (currentIndex == cardIds.lastIndex) {
            copy(status = StudySessionStatus.COMPLETED)
        } else {
            copy(currentIndex = currentIndex + 1, isAnswerRevealed = false)
        }
    }

    fun pause(): StudySessionState =
        if (status == StudySessionStatus.ACTIVE) copy(status = StudySessionStatus.PAUSED) else this

    fun resume(): StudySessionState =
        if (status == StudySessionStatus.PAUSED) copy(status = StudySessionStatus.ACTIVE) else this
}

fun startStudySession(
    deckId: String,
    cardIds: List<String>,
    requiredRatingCardIds: Set<String> = emptySet(),
    requiredWrittenCardIds: Set<String> = emptySet()
): StudySessionState = StudySessionState(
    deckId = deckId,
    cardIds = cardIds.toList(),
    requiredRatingCardIds = requiredRatingCardIds.toSet(),
    requiredWrittenCardIds = requiredWrittenCardIds.toSet()
)
