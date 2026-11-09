package com.example.myapplication.data

import com.example.myapplication.demo.DemoCard
import com.example.myapplication.demo.DemoCardDraft
import com.example.myapplication.demo.DemoClass
import com.example.myapplication.demo.DemoDeck
import com.example.myapplication.demo.JoinClassResult
import com.example.myapplication.demo.LocalMyDeckStore
import com.example.myapplication.study.StudyReview

/**
 * Adaptador de demonstração/testes. Não deve ser usado para dados reais entre contas:
 * a instância LocalMyDeckStore é compartilhada no modo de prévia.
 */
class LocalMyDeckContentRepository(private val store: LocalMyDeckStore) : MyDeckContentRepository {
    private inline fun <T> execute(
        onResult: (DataResult<T>) -> Unit,
        action: () -> T
    ) {
        try {
            onResult(DataResult.Success(action()))
        } catch (error: IllegalArgumentException) {
            onResult(DataResult.Failure(error.message ?: "Dados inválidos."))
        } catch (error: IllegalStateException) {
            onResult(DataResult.Failure(error.message ?: "Operação indisponível."))
        }
    }

    override fun teacherClasses(onResult: (DataResult<List<DemoClass>>) -> Unit) =
        execute(onResult) { store.classes.toList() }

    override fun studentClasses(studentId: String, onResult: (DataResult<List<DemoClass>>) -> Unit) =
        execute(onResult) {
            val ids = store.enrolledForStudent(studentId).toSet()
            store.classes.filter { it.id in ids }
        }

    override fun decksForClass(classId: String, onResult: (DataResult<List<DemoDeck>>) -> Unit) =
        execute(onResult) { store.decks.filter { classId in it.assignedClassIds } }

    override fun cardsForDeck(deckId: String, onResult: (DataResult<List<DemoCard>>) -> Unit) =
        execute(onResult) { store.cards.filter { it.deckId == deckId } }

    override fun reviewsForStudent(studentId: String, onResult: (DataResult<List<StudyReview>>) -> Unit) =
        execute(onResult) { store.reviews.filter { it.studentId == studentId } }

    override fun createClass(name: String, onResult: (DataResult<DemoClass>) -> Unit) =
        execute(onResult) { store.createClass(name) }

    override fun joinClass(code: String, studentId: String, onResult: (DataResult<DemoClass>) -> Unit) =
        execute(onResult) {
            when (val joined = store.joinByCode(code, studentId)) {
                is JoinClassResult.Joined -> joined.classroom
                JoinClassResult.AlreadyJoined -> {
                    val normalized = code.trim().uppercase(java.util.Locale.ROOT)
                    store.classes.first { it.joinCode == normalized }
                }
                JoinClassResult.NotFound -> throw IllegalArgumentException("Código de turma não encontrado.")
            }
        }

    override fun createDeck(
        title: String,
        classId: String,
        onResult: (DataResult<DemoDeck>) -> Unit
    ) = execute(onResult) { store.createDeck(classId, title) }

    override fun assignDeckToClasses(
        deckId: String,
        classIds: List<String>,
        onResult: (DataResult<Unit>) -> Unit
    ) = execute(onResult) { store.assignDeckToClasses(deckId, classIds) }

    override fun addCard(
        deckId: String,
        card: DemoCardDraft,
        onResult: (DataResult<DemoCard>) -> Unit
    ) = execute(onResult) { store.addCard(deckId, card) }

    override fun recordReview(
        review: StudyReview,
        onResult: (DataResult<Unit>) -> Unit
    ) = execute(onResult) { store.recordReview(review) }
}
