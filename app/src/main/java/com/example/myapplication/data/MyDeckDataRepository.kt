package com.example.myapplication.data

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.tasks.Tasks
import java.security.SecureRandom
import java.util.Locale

data class MyDeckClass(val id: String, val name: String, val joinCode: String)
data class MyDeckDeck(val id: String, val classId: String, val title: String)
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
        private fun newCode(): String = (1..8).map { CODE_CHARS[random.nextInt(CODE_CHARS.length)] }.joinToString("")
    }

    private fun currentUid(): String? = auth.currentUser?.uid

    private fun classroom(snapshot: DocumentSnapshot): MyDeckClass? {
        val name = snapshot.getString("name") ?: return null
        return MyDeckClass(snapshot.id, name, snapshot.getString("joinCode").orEmpty())
    }

    private fun deck(snapshot: DocumentSnapshot): MyDeckDeck? {
        val classId = snapshot.getString("classId") ?: return null
        return MyDeckDeck(snapshot.id, classId, snapshot.getString("title").orEmpty())
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
        if (normalized.length != 8) return onResult(DataResult.Failure("Informe o código de 8 caracteres."))

        db.collection("classCodes").document(normalized).get()
            .addOnSuccessListener { codeDoc ->
                val classId = codeDoc.getString("classId")
                if (classId == null) {
                    onResult(DataResult.Failure("Código de turma não encontrado."))
                    return@addOnSuccessListener
                }
                val enrollmentId = "${classId}_${uid}"
                db.collection("enrollments").document(enrollmentId)
                    .set(mapOf(
                        "studentId" to uid, "classId" to classId,
                        "joinCode" to normalized, "joinedAt" to FieldValue.serverTimestamp()
                    ))
                    .addOnSuccessListener { onResult(DataResult.Success(classId)) }
                    .addOnFailureListener { onResult(DataResult.Failure("Não foi possível entrar na turma: ${it.localizedMessage}")) }
            }
            .addOnFailureListener { onResult(DataResult.Failure("Falha ao consultar o código: ${it.localizedMessage}")) }
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

    fun createDeck(classId: String, title: String, onResult: (DataResult<MyDeckDeck>) -> Unit) {
        val uid = currentUid()
        if (uid == null) return onResult(DataResult.Failure("Faça login como professor."))
        if (classId.isBlank() || title.isBlank()) return onResult(DataResult.Failure("Informe a turma e o nome do deck."))
        val ref = db.collection("decks").document()
        ref.set(mapOf(
            "classId" to classId, "ownerUid" to uid, "title" to title.trim(),
            "createdAt" to FieldValue.serverTimestamp()
        )).addOnSuccessListener { onResult(DataResult.Success(MyDeckDeck(ref.id, classId, title.trim()))) }
            .addOnFailureListener { onResult(DataResult.Failure("Não foi possível criar o deck: ${it.localizedMessage}")) }
    }

    fun listDecks(classId: String, onResult: (DataResult<List<MyDeckDeck>>) -> Unit) {
        db.collection("decks").whereEqualTo("classId", classId).get()
            .addOnSuccessListener { snapshot -> onResult(DataResult.Success(snapshot.documents.mapNotNull(::deck))) }
            .addOnFailureListener { onResult(DataResult.Failure("Não foi possível consultar os decks: ${it.localizedMessage}")) }
    }

    fun addCard(
        deckId: String, question: String, answer: String, tags: List<String>,
        onResult: (DataResult<MyDeckCard>) -> Unit
    ) {
        if (currentUid() == null) return onResult(DataResult.Failure("Faça login como professor."))
        if (question.isBlank() || answer.isBlank()) return onResult(DataResult.Failure("Informe pergunta e resposta."))
        val cleanedTags = tags.map(String::trim).filter(String::isNotEmpty).distinct()
        val ref = db.collection("decks").document(deckId).collection("cards").document()
        ref.set(mapOf(
            "question" to question.trim(), "answer" to answer.trim(),
            "tags" to cleanedTags, "createdAt" to FieldValue.serverTimestamp()
        )).addOnSuccessListener {
            onResult(DataResult.Success(MyDeckCard(ref.id, question.trim(), answer.trim(), cleanedTags)))
        }.addOnFailureListener { onResult(DataResult.Failure("Não foi possível adicionar o card: ${it.localizedMessage}")) }
    }

    fun listCards(deckId: String, onResult: (DataResult<List<MyDeckCard>>) -> Unit) {
        db.collection("decks").document(deckId).collection("cards").get()
            .addOnSuccessListener { snapshot -> onResult(DataResult.Success(snapshot.documents.mapNotNull(::card))) }
            .addOnFailureListener { onResult(DataResult.Failure("Não foi possível consultar os cards: ${it.localizedMessage}")) }
    }

    fun saveReview(deckId: String, cardId: String, correct: Boolean, onResult: (DataResult<String>) -> Unit) {
        val uid = currentUid()
        if (uid == null) return onResult(DataResult.Failure("Faça login como aluno."))
        val ref = db.collection("reviews").document()
        ref.set(mapOf(
            "studentId" to uid, "deckId" to deckId, "cardId" to cardId,
            "correct" to correct, "reviewedAt" to FieldValue.serverTimestamp()
        )).addOnSuccessListener { onResult(DataResult.Success(ref.id)) }
            .addOnFailureListener { onResult(DataResult.Failure("Não foi possível registrar a revisão: ${it.localizedMessage}")) }
    }
}
