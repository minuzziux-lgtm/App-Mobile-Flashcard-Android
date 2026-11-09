package com.example.myapplication.study

/** Evento de estudo local. Avaliação nula representa resposta aberta não corrigida. */
data class StudyReview(
    val id: String,
    val studentId: String,
    val deckId: String,
    val cardId: String,
    val classId: String?,
    val correct: Boolean?,
    val writtenResponse: String?,
    val tags: List<String>,
    val reviewedAtEpochMillis: Long,
    val dueAtEpochMillis: Long,
    val intervalDays: Int = 0,
    val repetitions: Int = 0
) {
    init {
        require(id.isNotBlank() && studentId.isNotBlank() && deckId.isNotBlank() && cardId.isNotBlank())
        require(reviewedAtEpochMillis >= 0 && dueAtEpochMillis >= 0)
        require(intervalDays >= 0 && repetitions >= 0)
    }
}
