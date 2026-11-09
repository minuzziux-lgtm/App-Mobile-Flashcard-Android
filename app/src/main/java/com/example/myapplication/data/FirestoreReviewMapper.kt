package com.example.myapplication.data

import com.example.myapplication.study.StudyReview

/** Eventos idempotentes de revisão. O ID da sessão+cartão é a chave do documento. */
object FirestoreReviewMapper {
    fun toFields(review: StudyReview): Map<String, Any?> = mapOf(
        "studentId" to review.studentId,
        "deckId" to review.deckId,
        "cardId" to review.cardId,
        "classId" to review.classId,
        "correct" to review.correct,
        "writtenResponse" to review.writtenResponse,
        "tags" to review.tags,
        "reviewedAtEpochMillis" to review.reviewedAtEpochMillis,
        "dueAtEpochMillis" to review.dueAtEpochMillis,
        "intervalDays" to review.intervalDays,
        "repetitions" to review.repetitions
    )

    fun fromFields(id: String, value: Map<String, Any?>): StudyReview? {
        val studentId = value["studentId"] as? String ?: return null
        val deckId = value["deckId"] as? String ?: return null
        val cardId = value["cardId"] as? String ?: return null
        val reviewedAt = (value["reviewedAtEpochMillis"] as? Number)?.toLong() ?: return null
        val dueAt = (value["dueAtEpochMillis"] as? Number)?.toLong() ?: return null
        val interval = (value["intervalDays"] as? Number)?.toInt() ?: return null
        val repetitions = (value["repetitions"] as? Number)?.toInt() ?: return null
        val tags = (value["tags"] as? List<*>)?.filterIsInstance<String>() ?: return null
        val correct = value["correct"]?.let { it as? Boolean ?: return null }
        val written = value["writtenResponse"]?.let { it as? String ?: return null }
        return runCatching {
            StudyReview(
                id = id,
                studentId = studentId,
                deckId = deckId,
                cardId = cardId,
                classId = value["classId"] as? String,
                correct = correct,
                writtenResponse = written,
                tags = tags,
                reviewedAtEpochMillis = reviewedAt,
                dueAtEpochMillis = dueAt,
                intervalDays = interval,
                repetitions = repetitions
            )
        }.getOrNull()
    }
}
