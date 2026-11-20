package com.example.myapplication.learning

import com.example.myapplication.study.StudyReview

/** A aula existe independentemente de cartões. */
enum class LessonStatus { PLANNED, COMPLETED }
enum class LearningResourceType { PDF, VIDEO, LINK }

data class LearningResource(
    val id: String,
    val title: String,
    val url: String,
    val type: LearningResourceType,
    val classId: String,
    val deckId: String = "",
    val tags: List<String> = emptyList(),
    val cardId: String = "",
    val lessonId: String = ""
)

data class LessonDraftCard(
    val id: String,
    val deckId: String,
    val question: String,
    val answer: String,
    val published: Boolean = false
)

data class MyDeckLesson(
    val id: String,
    val classId: String,
    val title: String,
    val description: String = "",
    val deckId: String = "",
    val status: LessonStatus = LessonStatus.PLANNED,
    val cards: List<LessonDraftCard> = emptyList(),
    val completedAt: Long? = null
)

enum class FeedbackUnderstanding { CLEAR, PARTIAL, DIFFICULT }

data class LessonFeedback(
    val id: String,
    val classId: String,
    val lessonId: String = "",
    val studentId: String,
    val understanding: FeedbackUnderstanding,
    val comment: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

/** Respostas livres não entram na taxa. Exige amostra mínima para evitar falso alerta. */
data class StudySuggestion(
    val tag: String,
    val correct: Int,
    val graded: Int,
    val resources: List<LearningResource>
) {
    val accuracy: Int get() = if (graded == 0) 0 else correct * 100 / graded
}

fun studySuggestions(
    reviews: List<StudyReview>,
    resources: List<LearningResource>,
    accessibleDeckIds: Set<String>,
    minimumGraded: Int = 5,
    maximumAccuracy: Int = 60
): List<StudySuggestion> {
    val grades = reviews.filter { it.deckId in accessibleDeckIds && it.correct != null }
        .flatMap { review -> review.tags.distinct().map { tag -> tag to (review.correct == true) } }
        .groupBy({ it.first }, { it.second })
    return grades.mapNotNull { (tag, answers) ->
        val correct = answers.count { it }
        val accuracy = if (answers.isEmpty()) 100 else correct * 100 / answers.size
        if (answers.size < minimumGraded || accuracy >= maximumAccuracy) null
        else StudySuggestion(
            tag, correct, answers.size,
            resources.filter { tag in it.tags && (it.deckId.isBlank() || it.deckId in accessibleDeckIds) }
        )
    }.sortedWith(compareBy<StudySuggestion> { it.accuracy }.thenByDescending { it.graded })
}

fun validLearningUrl(value: String): Boolean {
    val uri = runCatching { java.net.URI(value.trim()) }.getOrNull() ?: return false
    return uri.scheme?.lowercase() == "https" && !uri.host.isNullOrBlank()
}
