package com.example.myapplication.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.myapplication.demo.DemoCard
import com.example.myapplication.demo.DemoCardDraft
import com.example.myapplication.demo.DemoClass
import com.example.myapplication.demo.DemoDeck
import com.example.myapplication.demo.DemoEnrollment
import com.example.myapplication.demo.DemoParticipant
import com.example.myapplication.demo.LocalMyDeckStore
import com.example.myapplication.demo.toDraft
import com.example.myapplication.study.StudyReview

/**
 * Ponte entre a UI Compose existente e as operações autorizadas no Firestore.
 *
 * Exclusiva para sessão autenticada. O armazenamento local de prévia não é
 * carregado nem automaticamente enviado à nuvem. As regras Firestore continuam
 * sendo a autoridade sobre perfil, matrícula, deck e acesso a cartões.
 */
class CloudMyDeckState(
    private val remote: MyDeckDataRepository,
    val store: LocalMyDeckStore,
    private val uid: String,
    val teacher: Boolean,
    context: Context
) {
    private val outbox = PendingReviewOutbox(context, uid)
    var loading by mutableStateOf(false)
        private set
    var notice by mutableStateOf<String?>(null)
        private set

    private var generation = 0

    private fun error(result: DataResult.Failure) {
        notice = result.message
    }

    private fun <T> write(result: DataResult<T>, success: (T) -> Unit) {
        when (result) {
            is DataResult.Success -> {
                notice = "Alteração salva no Firebase."
                success(result.value)
            }
            is DataResult.Failure -> error(result)
        }
    }

    fun refresh() {
        generation += 1
        val request = generation
        loading = true
        notice = null
        if (teacher) refreshTeacher(request) else refreshStudent(request)
        if (!teacher) flushPendingReviews()
    }

    private fun refreshTeacher(request: Int) {
        remote.listTeacherClasses { result ->
            if (request != generation) return@listTeacherClasses
            when (result) {
                is DataResult.Success -> {
                    store.classes.clear()
                    store.classes.addAll(result.value.map { DemoClass(it.id, it.name, it.joinCode) })
                    store.participants.clear()
                    result.value.forEach { classroom ->
                        remote.listClassParticipants(classroom.id) { response ->
                            if (request != generation) return@listClassParticipants
                            when (response) {
                                is DataResult.Success -> {
                                    store.participants.removeAll { it.classId == classroom.id }
                                    store.participants.addAll(response.value.map {
                                        DemoParticipant(it.studentId, it.classId, "@" + it.username)
                                    })
                                }
                                is DataResult.Failure -> error(response)
                            }
                        }
                    }
                }
                is DataResult.Failure -> error(result)
            }
        }
        remote.listTeacherDecks { result ->
            if (request != generation) return@listTeacherDecks
            when (result) {
                is DataResult.Success -> {
                    store.decks.clear()
                    store.decks.addAll(result.value.map {
                        DemoDeck(it.id, it.classId, it.title, it.assignedClassIds)
                    })
                    loadCards(request, result.value.map { it.id })
                }
                is DataResult.Failure -> error(result)
            }
            loading = false
        }
        remote.listTeacherReviews { result ->
            if (request != generation) return@listTeacherReviews
            when (result) {
                is DataResult.Success -> {
                    store.reviews.clear()
                    store.reviews.addAll(result.value)
                }
                is DataResult.Failure -> error(result)
            }
        }
    }

    private fun refreshStudent(request: Int) {
        remote.listStudentClasses { result ->
            if (request != generation) return@listStudentClasses
            when (result) {
                is DataResult.Failure -> {
                    error(result)
                    loading = false
                }
                is DataResult.Success -> {
                    val classes = result.value
                    store.classes.clear()
                    store.classes.addAll(classes.map { DemoClass(it.id, it.name, it.joinCode) })
                    store.userEnrollments.clear()
                    store.userEnrollments.addAll(classes.map { DemoEnrollment(it.id, uid) })
                    if (classes.isEmpty()) {
                        store.decks.clear()
                        store.cards.clear()
                        loading = false
                    } else {
                        val collected = linkedMapOf<String, DemoDeck>()
                        var remaining = classes.size
                        classes.forEach { classroom ->
                            remote.listDecks(classroom.id) { response ->
                                if (request != generation) return@listDecks
                                when (response) {
                                    is DataResult.Success -> response.value.forEach { deck ->
                                        val previous = collected[deck.id]
                                        val all = (previous?.assignedClassIds.orEmpty() +
                                            deck.assignedClassIds + classroom.id).distinct()
                                        collected[deck.id] = DemoDeck(deck.id, deck.classId, deck.title, all)
                                    }
                                    is DataResult.Failure -> error(response)
                                }
                                remaining--
                                if (remaining == 0) {
                                    store.decks.clear()
                                    store.decks.addAll(collected.values)
                                    loadCards(request, collected.keys.toList())
                                    loading = false
                                }
                            }
                        }
                    }
                }
            }
        }
        remote.listStudentReviews { result ->
            if (request != generation) return@listStudentReviews
            when (result) {
                is DataResult.Success -> {
                    store.reviews.clear()
                    store.reviews.addAll(result.value)
                    outbox.pending(uid).forEach { item ->
                        val index = store.reviews.indexOfFirst { it.id == item.id }
                        if (index >= 0) store.reviews[index] = item else store.reviews.add(item)
                    }
                }
                is DataResult.Failure -> error(result)
            }
        }
    }

    private fun loadCards(request: Int, ids: List<String>) {
        store.cards.clear()
        store.availableTags.clear()
        ids.forEach { deckId ->
            remote.listDetailedCards(deckId) { result ->
                if (request != generation) return@listDetailedCards
                when (result) {
                    is DataResult.Success -> {
                        store.cards.removeAll { it.deckId == deckId }
                        store.cards.addAll(result.value)
                        val tags = store.cards.flatMap { it.tags }.distinct()
                        store.availableTags.clear()
                        store.availableTags.addAll(tags)
                    }
                    is DataResult.Failure -> error(result)
                }
            }
        }
    }

    fun createClass(name: String, onCreated: (String) -> Unit = {}) {
        remote.createClass(name) { result ->
            write(result) { created ->
                store.classes.add(DemoClass(created.id, created.name, created.joinCode))
                onCreated(created.id)
            }
        }
    }

    fun renameClass(classId: String, name: String) {
        remote.renameClass(classId, name) { result ->
            write(result) {
                val index = store.classes.indexOfFirst { it.id == classId }
                if (index >= 0) store.classes[index] = store.classes[index].copy(name = name.trim())
                else refresh()
            }
        }
    }

    fun createDeck(classId: String, title: String, onCreated: (String) -> Unit = {}) {
        remote.createDeck(classId, title) { result ->
            write(result) { created ->
                store.decks.add(DemoDeck(created.id, created.classId, created.title, created.assignedClassIds))
                onCreated(created.id)
            }
        }
    }

    fun renameDeck(deckId: String, title: String) {
        remote.renameDeck(deckId, title) { result ->
            write(result) {
                val index = store.decks.indexOfFirst { it.id == deckId }
                if (index >= 0) store.decks[index] = store.decks[index].copy(title = title.trim())
                else refresh()
            }
        }
    }

    fun assignDeck(deckId: String, classIds: List<String>) {
        remote.assignDeckToClasses(deckId, classIds) { result ->
            write(result) {
                val index = store.decks.indexOfFirst { it.id == deckId }
                if (index >= 0) {
                    store.decks[index] = store.decks[index].copy(assignedClassIds = classIds.distinct())
                } else refresh()
            }
        }
    }

    fun addCard(deckId: String, draft: DemoCardDraft) {
        remote.addDetailedCard(deckId, draft) { result ->
            write(result) { store.cards.add(it); refreshTags() }
        }
    }

    fun updateCard(cardId: String, draft: DemoCardDraft) {
        val original = store.cards.firstOrNull { it.id == cardId } ?: return
        remote.updateDetailedCard(original.deckId, cardId, draft) { result ->
            write(result) {
                if (store.cards.any { it.id == cardId }) store.updateCard(cardId, draft)
                else refresh()
            }
        }
    }

    fun duplicateCard(cardId: String) {
        val original = store.cards.firstOrNull { it.id == cardId } ?: return
        remote.addDetailedCard(original.deckId, original.toDraft()) { result ->
            write(result) { store.cards.add(it); refreshTags() }
        }
    }

    fun deleteCard(cardId: String) {
        val original = store.cards.firstOrNull { it.id == cardId } ?: return
        remote.deleteCard(original.deckId, cardId) { result ->
            write(result) {
                store.cards.removeAll { it.id == cardId }
                refreshTags()
            }
        }
    }

    private fun refreshTags() {
        val tags = store.cards.flatMap { it.tags }.distinct()
        store.availableTags.clear()
        store.availableTags.addAll(tags)
    }

    fun removeStudent(participantId: String) {
        val participant = store.participants.firstOrNull { it.id == participantId } ?: return
        remote.removeStudentFromClass(participant.classId, participant.id) { result ->
            write(result) { store.participants.remove(participant) }
        }
    }

    fun joinClass(code: String, onFinished: (Boolean, String) -> Unit) {
        remote.joinClass(code) { result ->
            when (result) {
                is DataResult.Success -> {
                    notice = "Matrícula confirmada no Firebase."
                    refresh()
                    onFinished(true, "Matrícula confirmada. Seus decks serão carregados.")
                }
                is DataResult.Failure -> {
                    error(result)
                    onFinished(false, result.message)
                }
            }
        }
    }

    fun recordReview(review: StudyReview) {
        if (review.studentId != uid) {
            notice = "Não é possível gravar a revisão de outra conta."
            return
        }
        // Primeiro registrar na fila durável; a revisão não é perdida se a rede falhar.
        store.recordReview(review)
        val scheduled = store.reviews.first { it.id == review.id }
        outbox.enqueue(scheduled)
        sendPendingReview(scheduled)
    }

    private fun sendPendingReview(review: StudyReview) {
        remote.saveDetailedReview(review) { result ->
            when (result) {
                is DataResult.Success -> {
                    outbox.remove(review.id)
                    notice = "Revisão sincronizada."
                }
                is DataResult.Failure -> {
                    notice = "Revisão aguardando sincronização: ${result.message}"
                }
            }
        }
    }

    fun flushPendingReviews() {
        if (teacher) return
        outbox.pending(uid).forEach(::sendPendingReview)
    }
}
