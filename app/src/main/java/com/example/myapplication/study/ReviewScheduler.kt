package com.example.myapplication.study

import kotlin.math.min

private const val DAY_MILLIS = 86_400_000L
private const val RETRY_MILLIS = 10 * 60_000L

/**
 * Agenda local determinística. Acerto: 1, 3, 7 dias e, após, cresce
 * até 365 dias. Erro: repetição após 10 minutos.
 * Resposta aberta não possui nota e não altera a sequência de acertos.
 */
fun scheduleReview(incoming: StudyReview, previous: StudyReview?): StudyReview {
    val now = incoming.reviewedAtEpochMillis
    if (incoming.correct == null) return incoming.copy(
        repetitions = 0, intervalDays = 0, dueAtEpochMillis = now
    )
    if (incoming.correct == false) return incoming.copy(
        repetitions = 0, intervalDays = 0, dueAtEpochMillis = now + RETRY_MILLIS
    )
    val streak = if (previous?.correct == true) previous.repetitions else 0
    val interval = when (streak) {
        0 -> 1
        1 -> 3
        2 -> 7
        else -> min(365, (previous?.intervalDays ?: 7) * 2)
    }
    return incoming.copy(
        repetitions = streak + 1,
        intervalDays = interval,
        dueAtEpochMillis = now + interval * DAY_MILLIS
    )
}

fun isReviewDue(last: StudyReview?, nowEpochMillis: Long): Boolean =
    last == null || last.dueAtEpochMillis <= nowEpochMillis
