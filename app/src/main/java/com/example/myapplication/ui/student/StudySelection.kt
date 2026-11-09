package com.example.myapplication.ui.student

import com.example.myapplication.demo.DemoCard
import com.example.myapplication.demo.DemoClass
import com.example.myapplication.demo.DemoDeck

/** Escolhe um deck acessível com cartões, sem depender da navegação ou do estado da UI. */
internal data class StudySelection(
    val deckId: String,
    val classId: String,
    val cards: List<DemoCard>
)

internal fun findStudySelection(
    enrolledClasses: List<DemoClass>,
    decks: List<DemoDeck>,
    cards: List<DemoCard>,
    dueCardIds: Set<String>,
    dueOnly: Boolean
): StudySelection? {
    val enrolledIds = enrolledClasses.map { it.id }.toSet()
    return decks.firstNotNullOfOrNull { deck ->
        val classId = deck.assignedClassIds.firstOrNull { it in enrolledIds }
        val eligibleCards = cards.filter {
            it.deckId == deck.id && (!dueOnly || it.id in dueCardIds)
        }
        if (classId != null && eligibleCards.isNotEmpty()) {
            StudySelection(deck.id, classId, eligibleCards)
        } else null
    }
}

/** Evita atribuir uma revisão à turma antiga ao iniciar estudo diretamente pela Home. */
internal fun resolveStudyClassId(
    requestedClassId: String?,
    deck: DemoDeck?,
    enrolledClasses: List<DemoClass>
): String? {
    val enrolledIds = enrolledClasses.map { it.id }.toSet()
    val validClassIds = deck?.assignedClassIds.orEmpty().filter { it in enrolledIds }
    return requestedClassId?.takeIf { it in validClassIds } ?: validClassIds.firstOrNull()
}
