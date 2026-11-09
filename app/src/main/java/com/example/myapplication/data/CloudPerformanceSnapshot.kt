package com.example.myapplication.data

import com.example.myapplication.study.StudyReview
import com.example.myapplication.study.StudySummary
import com.example.myapplication.study.TagDifficulty
import com.example.myapplication.study.activityByDay
import com.example.myapplication.study.difficultTags
import com.example.myapplication.study.filterReviewsByPeriod
import com.example.myapplication.study.summarizeReviews

/** Estatísticas calculadas exclusivamente de revisões autorizadas pelo Firestore. */
data class CloudPerformanceSnapshot(
    val summary: StudySummary,
    val totalByStudent: Map<String, StudySummary>,
    val totalByClass: Map<String, StudySummary>,
    val totalByDeck: Map<String, StudySummary>,
    val activityByLocalDay: Map<Long, Int>,
    val difficultTags: List<TagDifficulty>
)

/**
 * Não confere permissões: o backend deve retornar apenas revisões do professor
 * ou do aluno autenticado. Este agregador não deve ser usado como controle de acesso.
 */
fun aggregateCloudPerformance(
    authorizedReviews: List<StudyReview>,
    year: Int? = null,
    month: Int? = null,
    studentId: String? = null,
    classId: String? = null,
    deckId: String? = null,
    tag: String? = null
): CloudPerformanceSnapshot {
    val filtered = filterReviewsByPeriod(authorizedReviews, year, month)
        .filter { studentId == null || it.studentId == studentId }
        .filter { classId == null || it.classId == classId }
        .filter { deckId == null || it.deckId == deckId }
        .filter { tag == null || tag in it.tags }
    return CloudPerformanceSnapshot(
        summary = summarizeReviews(filtered),
        totalByStudent = filtered.groupBy { it.studentId }
            .mapValues { (_, items) -> summarizeReviews(items) },
        totalByClass = filtered.filter { it.classId != null }
            .groupBy { it.classId!! }
            .mapValues { (_, items) -> summarizeReviews(items) },
        totalByDeck = filtered.groupBy { it.deckId }
            .mapValues { (_, items) -> summarizeReviews(items) },
        activityByLocalDay = activityByDay(filtered),
        difficultTags = difficultTags(filtered)
    )
}
