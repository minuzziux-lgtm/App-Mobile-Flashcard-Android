package com.example.myapplication.data

import com.example.myapplication.demo.LocalMyDeckStore

enum class MigrationRole { TEACHER, STUDENT }

/**
 * Prévia somente leitura. Nenhuma alteração remota é feita automaticamente.
 * IDs existentes são preservados para permitir verificação de duplicidade.
 */
data class MigrationPreflight(
    val classes: Int,
    val decks: Int,
    val cards: Int,
    val ownReviews: Int,
    val problems: List<String>,
    val notices: List<String>
) {
    val canStart: Boolean get() = problems.isEmpty()
}

fun inspectLocalMigration(
    store: LocalMyDeckStore,
    uid: String,
    role: MigrationRole
): MigrationPreflight {
    require(uid.isNotBlank())
    val classIds = store.classes.map { it.id }.toSet()
    val deckIds = store.decks.map { it.id }.toSet()
    val cardIds = store.cards.map { it.id }.toSet()
    val problems = mutableListOf<String>()
    val notices = mutableListOf<String>()

    if (store.classes.size != classIds.size ||
        store.decks.size != deckIds.size ||
        store.cards.size != cardIds.size
    ) problems += "Existem IDs duplicados na biblioteca local."

    store.decks.forEach { deck ->
        val invalidClasses = deck.assignedClassIds.filterNot { it in classIds }
        if (invalidClasses.isNotEmpty()) {
            problems += "O deck ${deck.id} possui atribuições de turma desconhecidas."
        }
    }
    store.cards.filterNot { it.deckId in deckIds }.forEach {
        problems += "Flashcard órfão: ${it.id}."
    }

    val ownReviews = store.reviews.filter { it.studentId == uid }
    val otherReviews = store.reviews.filter { it.studentId != uid }
    if (otherReviews.isNotEmpty()) {
        notices += "Revisões de outras contas não serão importadas."
    }
    if (role == MigrationRole.STUDENT && (store.classes.isNotEmpty() ||
            store.decks.isNotEmpty() || store.cards.isNotEmpty())) {
        notices += "A biblioteca de professor não será atribuída à conta de aluno."
    }
    if (role == MigrationRole.TEACHER && ownReviews.isNotEmpty()) {
        notices += "Revisões pertencem ao perfil de aluno; não migrar como dados do professor."
    }

    return MigrationPreflight(
        classes = if (role == MigrationRole.TEACHER) store.classes.size else 0,
        decks = if (role == MigrationRole.TEACHER) store.decks.size else 0,
        cards = if (role == MigrationRole.TEACHER) store.cards.size else 0,
        ownReviews = if (role == MigrationRole.STUDENT) ownReviews.size else 0,
        problems = problems.distinct(),
        notices = notices.distinct()
    )
}
