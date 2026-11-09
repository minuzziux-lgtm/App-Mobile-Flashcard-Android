package com.example.myapplication.data

import android.content.Context
import com.example.myapplication.demo.DemoCard
import com.example.myapplication.demo.DemoCardDraft
import com.example.myapplication.demo.DemoCardType
import com.example.myapplication.demo.validateCardDraft
import com.example.myapplication.study.StudyReview
import java.util.UUID
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.tasks.Tasks
import java.security.SecureRandom
import java.util.Locale

data class MyDeckClass(val id: String, val name: String, val joinCode: String)
data class MyDeckEnrollment(val classId: String, val studentId: String, val username: String)
data class MyDeckDeck(
    val id: String,
    val classId: String,
    val title: String,
    val assignedClassIds: List<String> = if (classId.isBlank()) emptyList() else listOf(classId)
)
data class MyDeckCard(val id: String, val question: String, val answer: String, val tags: List<String>)

sealed interface DataResult<out T> {
    data class Success<T>(val value: T) : DataResult<T>
    data class Failure(val message: String) : DataResult<Nothing>
}

/**
 * Firestore data access only. The Firebase rules enforce the actual permissions.
 * Teacher accounts must have role=teacher approved by a trusted administrator.
 */
class MyDeckDataRepository private constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    companion object {
        private const val CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        private val random = SecureRandom()

        fun create(context: Context): MyDeckDataRepository? {
            val app = FirebaseApp.initializeApp(context.applicationContext) ?: return null
            return MyDeckDataRepository(FirebaseFirestore.getInstance(app), FirebaseAuth.getInstance(app))
        }

        fun normalizeCode(value: String): String = value.trim().uppercase(Locale.ROOT)
        fun validJoinCode(value: String): Boolean =
            normalizeCode(value).matches(Regex("^[A-Z2-9]{8}$"))
        private fun newCode(): String = (1..8).map { CODE_CHARS[random.nextInt(CODE_CHARS.length)] }.joinToString("")
    }

    private fun currentUid(): String? = auth.currentUser?.uid

    private fun classroom(snapshot: DocumentSnapshot): MyDeckClass? {
        val name = snapshot.getString("name") ?: return null
        return MyDeckClass(snapshot.id, name, snapshot.getString("joinCode").orEmpty())
    }

    private fun deck(snapshot: DocumentSnapshot, activeClassId: String = ""): MyDeckDeck? {
        if (!snapshot.exists()) return null
        val assigned = (snapshot.get("assignedClassIds") as? List<*>)
            ?.filterIsInstance<String>().orEmpty()
        return MyDeckDeck(
            snapshot.id,
            activeClassId.ifBlank { assigned.firstOrNull().orEmpty() },
            snapshot.getString("title").orEmpty(),
            assigned
        )
    }

    private fun card(snapshot: DocumentSnapshot): MyDeckCard? {
        val question = snapshot.getString("question") ?: return null
        val answer = snapshot.getString("answer") ?: return null
        val tags = (snapshot.get("tags") as? List<*>)?.filterIsInstance<String>().orEmpty()
        return MyDeckCard(snapshot.id, question, answer, tags)
    }

    fun createClass(name: String, onResult: (DataResult<MyDeckClass>) -> Unit) {
        val uid = currentUid()
        if (uid == null) return onResult(DataResult.Failure("Faça login como professor."))
        if (name.isBlank()) return onResult(DataResult.Failure("Informe o nome da turma."))

        val classRef = db.collection("classes").document()
        val code = newCode()
        val codeRef = db.collection("classCodes").document(code)
        db.runTransaction { tx ->
            if (tx.get(codeRef).exists()) error("Código já utilizado. Tente novamente.")
            tx.set(classRef, mapOf(
                "name" to name.trim(), "ownerUid" to uid, "joinCode" to code,
                "createdAt" to FieldValue.serverTimestamp()
            ))
            tx.set(codeRef, mapOf(
                "classId" to classRef.id, "ownerUid" to uid,
                "createdAt" to FieldValue.serverTimestamp()
            ))
            MyDeckClass(classRef.id, name.trim(), code)
        }.addOnSuccessListener { onResult(DataResult.Success(it)) }
            .addOnFailureListener { onResult(DataResult.Failure("Não foi possível criar a turma: ${it.localizedMessage}")) }
    }

    fun joinClass(code: String, onResult: (DataResult<String>) -> Unit) {
        val uid = currentUid()
        if (uid == null) return onResult(DataResult.Failure("Faça login como aluno."))
        val normalized = normalizeCode(code)
        if (!validJoinCode(normalized)) {
            return onResult(DataResult.Failure("Informe um código válido de 8 caracteres."))
        }

        // O username da matrícula vem do perfil salvo no servidor, nunca da UI.
        db.collection("users").document(uid).get()
            .addOnSuccessListener { profile ->
                val username = profile.getString("username")
                if (username.isNullOrBlank() || profile.getString("role") != "student") {
                    onResult(DataResult.Failure("Apenas alunos com cadastro aprovado podem entrar em turmas."))
                    return@addOnSuccessListener
                }
                db.collection("classCodes").document(normalized).get()
                    .addOnSuccessListener { codeDoc ->
                        val classId = codeDoc.getString("classId")
                        if (classId.isNullOrBlank()) {
                            onResult(DataResult.Failure("Código de turma não encontrado."))
                            return@addOnSuccessListener
                        }
                        val id = "${classId}_${uid}"
                        db.collection("enrollments").document(id)
                            .set(mapOf(
                                "studentId" to uid,
                                "classId" to classId,
                                "username" to username,
                                "joinCode" to normalized,
                                "joinedAt" to FieldValue.serverTimestamp()
                            ))
                            .addOnSuccessListener { onResult(DataResult.Success(classId)) }
                            .addOnFailureListener {
                                onResult(DataResult.Failure("Não foi possível entrar na turma: ${it.localizedMessage}"))
                            }
                    }
                    .addOnFailureListener {
                        onResult(DataResult.Failure("Falha ao consultar o código: ${it.localizedMessage}"))
                    }
            }
            .addOnFailureListener {
                onResult(DataResult.Failure("Não foi possível carregar o perfil do aluno."))
            }
    }

    fun renameClass(
        classId: String, name: String,
        onResult: (DataResult<Unit>) -> Unit
    ) {
        if (currentUid() == null || name.isBlank()) {
            return onResult(DataResult.Failure("Informe uma turma e um nome válido."))
        }
        db.collection("classes").document(classId)
            .update("name", name.trim())
            .addOnSuccessListener { onResult(DataResult.Success(Unit)) }
            .addOnFailureListener {
                onResult(DataResult.Failure("Não foi possível renomear a turma: ${it.localizedMessage}"))
            }
    }

    fun listTeacherClasses(onResult: (DataResult<List<MyDeckClass>>) -> Unit) {
        val uid = currentUid()
        if (uid == null) return onResult(DataResult.Failure("Faça login como professor."))
        db.collection("classes").whereEqualTo("ownerUid", uid).get()
            .addOnSuccessListener { snapshot -> onResult(DataResult.Success(snapshot.documents.mapNotNull(::classroom))) }
            .addOnFailureListener { onResult(DataResult.Failure("Não foi possível consultar as turmas: ${it.localizedMessage}")) }
    }

    fun listStudentClasses(onResult: (DataResult<List<MyDeckClass>>) -> Unit) {
        val uid = currentUid()
        if (uid == null) return onResult(DataResult.Failure("Faça login como aluno."))
        db.collection("enrollments").whereEqualTo("studentId", uid).get()
            .addOnSuccessListener { enrollments ->
                val ids = enrollments.documents.mapNotNull { it.getString("classId") }.distinct()
                if (ids.isEmpty()) {
                    onResult(DataResult.Success(emptyList()))
                    return@addOnSuccessListener
                }
                val tasks = ids.map { db.collection("classes").document(it).get() }
                Tasks.whenAllSuccess<DocumentSnapshot>(tasks)
                    .addOnSuccessListener { docs -> onResult(DataResult.Success(docs.mapNotNull(::classroom))) }
                    .addOnFailureListener { onResult(DataResult.Failure("Não foi possível carregar turmas: ${it.localizedMessage}")) }
            }.addOnFailureListener { onResult(DataResult.Failure("Não foi possível consultar participações: ${it.localizedMessage}")) }
    }

    fun removeStudentFromClass(
        classId: String, studentId: String,
        onResult: (DataResult<Unit>) -> Unit
    ) {
        if (currentUid() == null) {
            return onResult(DataResult.Failure("Faça login como professor."))
        }
        db.collection("enrollments").document("${classId}_${studentId}").delete()
            .addOnSuccessListener { onResult(DataResult.Success(Unit)) }
            .addOnFailureListener {
                onResult(DataResult.Failure("Não foi possível remover o aluno: ${it.localizedMessage}"))
            }
    }

    fun listClassParticipants(
        classId: String,
        onResult: (DataResult<List<MyDeckEnrollment>>) -> Unit
    ) {
        if (currentUid() == null) {
            return onResult(DataResult.Failure("Faça login como professor."))
        }
        db.collection("enrollments").whereEqualTo("classId", classId).get()
            .addOnSuccessListener { snapshot ->
                val entries = snapshot.documents.mapNotNull { item ->
                    val studentId = item.getString("studentId") ?: return@mapNotNull null
                    val username = item.getString("username") ?: return@mapNotNull null
                    MyDeckEnrollment(classId, studentId, username)
                }
                onResult(DataResult.Success(entries))
            }
            .addOnFailureListener {
                onResult(DataResult.Failure("Não foi possível listar os alunos desta turma."))
            }
    }

    fun createDeck(classId: String, title: String, onResult: (DataResult<MyDeckDeck>) -> Unit) {
        val uid = currentUid()
        if (uid == null) return onResult(DataResult.Failure("Faça login como professor."))
        if (title.isBlank()) return onResult(DataResult.Failure("Informe o nome do deck."))
        val deckRef = db.collection("decks").document()
        val assigned = listOfNotNull(classId.takeIf(String::isNotBlank))
        val batch = db.batch()
        batch.set(deckRef, mapOf(
            "ownerUid" to uid,
            "title" to title.trim(),
            "assignedClassIds" to assigned,
            "createdAt" to FieldValue.serverTimestamp()
        ))
        if (classId.isNotBlank()) {
            val assignment = db.collection("classDeckAssignments")
                .document("${classId}_${deckRef.id}")
            batch.set(assignment, mapOf(
                "ownerUid" to uid, "classId" to classId, "deckId" to deckRef.id
            ))
        }
        batch.commit()
            .addOnSuccessListener {
                onResult(DataResult.Success(MyDeckDeck(deckRef.id, classId, title.trim(), assigned)))
            }
            .addOnFailureListener {
                onResult(DataResult.Failure("Não foi possível criar o deck: ${it.localizedMessage}"))
            }
    }

    fun assignDeckToClasses(
        deckId: String,
        classIds: List<String>,
        onResult: (DataResult<Unit>) -> Unit
    ) {
        val uid = currentUid()
        if (uid == null) return onResult(DataResult.Failure("Faça login como professor."))
        val ids = classIds.distinct()
        if (ids.size > 40 || ids.any(String::isBlank)) {
            return onResult(DataResult.Failure("Selecione até 40 turmas válidas."))
        }
        val ref = db.collection("decks").document(deckId)
        ref.get()
            .addOnSuccessListener { document ->
                if (document.getString("ownerUid") != uid) {
                    onResult(DataResult.Failure("Este deck não pertence à sua conta."))
                    return@addOnSuccessListener
                }
                val previous = (document.get("assignedClassIds") as? List<*>)
                    ?.filterIsInstance<String>().orEmpty()
                val batch = db.batch()
                batch.update(ref, "assignedClassIds", ids)
                (ids - previous.toSet()).forEach { classId ->
                    batch.set(db.collection("classDeckAssignments").document("${classId}_${deckId}"), mapOf(
                        "ownerUid" to uid, "classId" to classId, "deckId" to deckId
                    ))
                }
                (previous - ids.toSet()).forEach { classId ->
                    batch.delete(db.collection("classDeckAssignments").document("${classId}_${deckId}"))
                }
                batch.commit()
                    .addOnSuccessListener { onResult(DataResult.Success(Unit)) }
                    .addOnFailureListener {
                        onResult(DataResult.Failure("Não foi possível atribuir o deck: ${it.localizedMessage}"))
                    }
            }.addOnFailureListener {
                onResult(DataResult.Failure("Não foi possível consultar a atribuição do deck."))
            }
    }

    fun renameDeck(
        deckId: String, title: String,
        onResult: (DataResult<Unit>) -> Unit
    ) {
        if (currentUid() == null || title.isBlank()) {
            return onResult(DataResult.Failure("Informe um nome válido para o deck."))
        }
        db.collection("decks").document(deckId)
            .update("title", title.trim())
            .addOnSuccessListener { onResult(DataResult.Success(Unit)) }
            .addOnFailureListener {
                onResult(DataResult.Failure("Não foi possível renomear o deck: ${it.localizedMessage}"))
            }
    }

    fun listTeacherDecks(onResult: (DataResult<List<MyDeckDeck>>) -> Unit) {
        val uid = currentUid()
        if (uid == null) return onResult(DataResult.Failure("Faça login como professor."))
        db.collection("decks").whereEqualTo("ownerUid", uid).get()
            .addOnSuccessListener { snapshot ->
                onResult(DataResult.Success(snapshot.documents.mapNotNull { deck(it) }))
            }.addOnFailureListener {
                onResult(DataResult.Failure("Não foi possível listar sua biblioteca."))
            }
    }

    /** Cada turma lista as atribuições; o aluno obtém um grant validado pelas regras. */
    fun listDecks(classId: String, onResult: (DataResult<List<MyDeckDeck>>) -> Unit) {
        val uid = currentUid()
        if (uid == null) return onResult(DataResult.Failure("Faça login para consultar decks."))
        db.collection("classDeckAssignments").whereEqualTo("classId", classId).get()
            .addOnSuccessListener { assigned ->
                val ids = assigned.documents.mapNotNull { it.getString("deckId") }.distinct()
                if (ids.isEmpty()) {
                    onResult(DataResult.Success(emptyList()))
                    return@addOnSuccessListener
                }
                db.collection("users").document(uid).get()
                    .addOnSuccessListener { profile ->
                        val isStudent = profile.getString("role") == "student"
                        val prepared = if (isStudent) {
                            ids.map { deckId ->
                                db.collection("deckAccess").document("${deckId}_${uid}")
                                    .set(mapOf("studentId" to uid, "classId" to classId, "deckId" to deckId))
                            }
                        } else emptyList()
                        Tasks.whenAll(prepared)
                            .addOnSuccessListener {
                                val reads = ids.map { db.collection("decks").document(it).get() }
                                Tasks.whenAllSuccess<DocumentSnapshot>(reads)
                                    .addOnSuccessListener { docs ->
                                        onResult(DataResult.Success(docs.mapNotNull { deck(it, classId) }))
                                    }
                                    .addOnFailureListener {
                                        onResult(DataResult.Failure("Não foi possível abrir os decks atribuídos."))
                                    }
                            }
                            .addOnFailureListener {
                                onResult(DataResult.Failure("Seu acesso aos decks não pôde ser validado."))
                            }
                    }.addOnFailureListener {
                        onResult(DataResult.Failure("Não foi possível verificar seu perfil."))
                    }
            }.addOnFailureListener {
                onResult(DataResult.Failure("Não foi possível consultar as atribuições."))
            }
    }

    fun addCard(
        deckId: String, question: String, answer: String, tags: List<String>,
        onResult: (DataResult<MyDeckCard>) -> Unit
    ) {
        val draft = DemoCardDraft(DemoCardType.BASIC, question, answer, tags = tags)
        addDetailedCard(deckId, draft) { result ->
            when (result) {
                is DataResult.Success -> onResult(DataResult.Success(MyDeckCard(
                    result.value.id, result.value.question, result.value.answer, result.value.tags
                )))
                is DataResult.Failure -> onResult(result)
            }
        }
    }

    /** A biblioteca Firestore grava todos os tipos de cartão no mesmo deck canônico. */
    fun addDetailedCard(
        deckId: String,
        draft: DemoCardDraft,
        onResult: (DataResult<DemoCard>) -> Unit
    ) {
        if (currentUid() == null) return onResult(DataResult.Failure("Faça login como professor."))
        val validationError = validateCardDraft(draft)
        if (validationError != null) return onResult(DataResult.Failure(validationError))
        val ref = db.collection("decks").document(deckId).collection("cards").document()
        val card = DemoCard(
            id = ref.id, deckId = deckId, question = draft.question.trim(),
            answer = draft.answer.trim(), tags = draft.tags.distinct(),
            type = draft.type, description = draft.description.trim(),
            options = draft.options.toList(), correctOptionIndex = draft.correctOptionIndex,
            frontLanguage = draft.frontLanguage.trim(), backLanguage = draft.backLanguage.trim(),
            mediaLink = draft.mediaLink
        )
        ref.set(FirestoreCardMapper.toFields(card) + ("createdAt" to FieldValue.serverTimestamp()))
            .addOnSuccessListener { onResult(DataResult.Success(card)) }
            .addOnFailureListener {
                onResult(DataResult.Failure("Não foi possível salvar o flashcard: ${it.localizedMessage}"))
            }
    }

    fun listDetailedCards(deckId: String, onResult: (DataResult<List<DemoCard>>) -> Unit) {
        db.collection("decks").document(deckId).collection("cards").get()
            .addOnSuccessListener { snapshot ->
                val cards = snapshot.documents.mapNotNull { document ->
                    FirestoreCardMapper.fromFields(document.id, deckId, document.data.orEmpty())
                }
                onResult(DataResult.Success(cards))
            }.addOnFailureListener {
                onResult(DataResult.Failure("Não foi possível consultar os flashcards."))
            }
    }

    fun listCards(deckId: String, onResult: (DataResult<List<MyDeckCard>>) -> Unit) {
        listDetailedCards(deckId) { result ->
            when (result) {
                is DataResult.Success -> onResult(DataResult.Success(result.value.map {
                    MyDeckCard(it.id, it.question, it.answer, it.tags)
                }))
                is DataResult.Failure -> onResult(result)
            }
        }
    }

    fun updateDetailedCard(
        deckId: String,
        cardId: String,
        draft: DemoCardDraft,
        onResult: (DataResult<Unit>) -> Unit
    ) {
        if (currentUid() == null) return onResult(DataResult.Failure("Faça login como professor."))
        val invalid = validateCardDraft(draft)
        if (invalid != null) return onResult(DataResult.Failure(invalid))
        val card = DemoCard(
            id = cardId, deckId = deckId, question = draft.question.trim(),
            answer = draft.answer.trim(), tags = draft.tags.distinct(),
            type = draft.type, description = draft.description.trim(),
            options = draft.options.toList(), correctOptionIndex = draft.correctOptionIndex,
            frontLanguage = draft.frontLanguage.trim(), backLanguage = draft.backLanguage.trim(),
            mediaLink = draft.mediaLink
        )
        db.collection("decks").document(deckId).collection("cards").document(cardId)
            .update(FirestoreCardMapper.toFields(card))
            .addOnSuccessListener { onResult(DataResult.Success(Unit)) }
            .addOnFailureListener {
                onResult(DataResult.Failure("Não foi possível atualizar o cartão."))
            }
    }

    fun deleteCard(
        deckId: String,
        cardId: String,
        onResult: (DataResult<Unit>) -> Unit
    ) {
        if (currentUid() == null) return onResult(DataResult.Failure("Faça login como professor."))
        db.collection("decks").document(deckId).collection("cards").document(cardId).delete()
            .addOnSuccessListener { onResult(DataResult.Success(Unit)) }
            .addOnFailureListener { onResult(DataResult.Failure("Não foi possível excluir o cartão.")) }
    }

    /** API legada: novas sessões devem chamar saveDetailedReview já agendada. */
    fun saveReview(
        deckId: String, cardId: String, correct: Boolean,
        onResult: (DataResult<String>) -> Unit
    ) {
        val uid = currentUid()
        if (uid == null) return onResult(DataResult.Failure("Faça login como aluno."))
        val now = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()
        saveDetailedReview(
            StudyReview(
                id = id, studentId = uid, deckId = deckId, cardId = cardId,
                classId = null, correct = correct, writtenResponse = null,
                tags = emptyList(), reviewedAtEpochMillis = now, dueAtEpochMillis = now
            )
        ) { result ->
            when (result) {
                is DataResult.Success -> onResult(DataResult.Success(id))
                is DataResult.Failure -> onResult(result)
            }
        }
    }

    /** Documento por ID do evento: atualiza a avaliação sem duplicar revisão. */
    fun saveDetailedReview(
        review: StudyReview,
        onResult: (DataResult<Unit>) -> Unit
    ) {
        val uid = currentUid()
        if (uid == null || review.studentId != uid) {
            return onResult(DataResult.Failure("Você não pode registrar a revisão de outra conta."))
        }
        db.collection("decks").document(review.deckId).get()
            .addOnSuccessListener { deck ->
                val ownerUid = deck.getString("ownerUid")
                if (ownerUid.isNullOrBlank()) {
                    onResult(DataResult.Failure("Deck não encontrado ou acesso não autorizado."))
                    return@addOnSuccessListener
                }
                val values = FirestoreReviewMapper.toFields(review) +
                    ("ownerUid" to ownerUid)
                db.collection("reviews").document(review.id).set(values)
                    .addOnSuccessListener { onResult(DataResult.Success(Unit)) }
                    .addOnFailureListener {
                        onResult(DataResult.Failure("Não foi possível sincronizar a revisão: ${it.localizedMessage}"))
                    }
            }.addOnFailureListener {
                onResult(DataResult.Failure("Não foi possível validar a revisão."))
            }
    }

    fun listStudentReviews(onResult: (DataResult<List<StudyReview>>) -> Unit) {
        val uid = currentUid()
        if (uid == null) return onResult(DataResult.Failure("Faça login como aluno."))
        db.collection("reviews").whereEqualTo("studentId", uid).get()
            .addOnSuccessListener { snapshot ->
                onResult(DataResult.Success(snapshot.documents.mapNotNull { item ->
                    FirestoreReviewMapper.fromFields(item.id, item.data.orEmpty())
                }))
            }
            .addOnFailureListener { onResult(DataResult.Failure("Não foi possível carregar seu histórico.")) }
    }

    fun listTeacherReviews(onResult: (DataResult<List<StudyReview>>) -> Unit) {
        val uid = currentUid()
        if (uid == null) return onResult(DataResult.Failure("Faça login como professor."))
        db.collection("reviews").whereEqualTo("ownerUid", uid).get()
            .addOnSuccessListener { snapshot ->
                onResult(DataResult.Success(snapshot.documents.mapNotNull { item ->
                    FirestoreReviewMapper.fromFields(item.id, item.data.orEmpty())
                }))
            }
            .addOnFailureListener { onResult(DataResult.Failure("Não foi possível carregar o desempenho.")) }
    }
}
