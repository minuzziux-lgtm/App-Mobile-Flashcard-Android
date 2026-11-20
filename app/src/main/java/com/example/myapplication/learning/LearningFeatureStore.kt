package com.example.myapplication.learning

import android.content.Context
import com.example.myapplication.demo.LocalMyDeckStore
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Recursos acadêmicos. Na prévia, usa armazenamento isolado no dispositivo.
 * Em sessão autenticada, as escritas ocorrem no Firestore e são autorizadas por regras.
 */
class LearningFeatureStore(context: Context, private val accountId: String? = null) {
    val lessons = mutableStateListOf<MyDeckLesson>()
    val resources = mutableStateListOf<LearningResource>()
    val feedbacks = mutableStateListOf<LessonFeedback>()
    private val prefs = context.applicationContext.getSharedPreferences("mydeck_learning_preview_v1", Context.MODE_PRIVATE)
    private val firebase = if (accountId == null) null else FirebaseApp.initializeApp(context.applicationContext)
    private val db = if (firebase == null) null else FirebaseFirestore.getInstance(firebase)
    var error by mutableStateOf<String?>(null)
        private set
    val isCloud: Boolean get() = accountId != null

    init {
        if (!isCloud) restore()
    }

    /** Dados ilustrativos criados apenas nas prévias locais, uma única vez. */
    fun seedDemoContent(local: LocalMyDeckStore) {
        if (isCloud || prefs.getBoolean("sample_initialized", false)) return
        if (lessons.isNotEmpty() || resources.isNotEmpty() || feedbacks.isNotEmpty()) return
        val portuguese = local.classes.firstOrNull { it.name.contains("Portugues", ignoreCase = true) ||
            it.name.contains("Portug", ignoreCase = true) } ?: return
        val mathematics = local.classes.firstOrNull { it.name.contains("Matemática") }
        val grammar = local.decks.firstOrNull { it.title.contains("Gramática") }
        val fractions = local.decks.firstOrNull { it.title.contains("Frações") }
        val completed = MyDeckLesson(
            id = "demo-aula-gramatica", classId = portuguese.id,
            title = "Aula 01 — Concordância verbal",
            description = "Sujeito, verbo e concordância em frases do cotidiano.",
            deckId = grammar?.id.orEmpty(),
            status = LessonStatus.COMPLETED,
            completedAt = System.currentTimeMillis() - 2 * 86_400_000L
        )
        lessons.add(completed)
        lessons.add(MyDeckLesson(
            id = "demo-aula-interpretacao", classId = portuguese.id,
            title = "Aula 02 — Interpretação de texto",
            description = "Leitura de um texto curto e discussão de ideias principais.",
            status = LessonStatus.COMPLETED,
            completedAt = System.currentTimeMillis() - 86_400_000L
        ))
        if (mathematics != null) {
            lessons.add(MyDeckLesson(
                id = "demo-aula-fracoes", classId = mathematics.id,
                title = "Aula 03 — Operações com frações",
                description = "Planejamento para a próxima aula.",
                deckId = fractions?.id.orEmpty(),
                cards = listOf(
                    LessonDraftCard("demo-rascunho", fractions?.id.orEmpty(),
                        "Quanto é 1/2 + 1/2?", "1")
                )
            ))
        }
        resources.add(LearningResource(
            id = "demo-material-gramatica", title = "Concordância verbal — leitura",
            url = "https://pt.wikipedia.org/wiki/Concord%C3%A2ncia_verbal",
            type = LearningResourceType.LINK, classId = portuguese.id,
            deckId = grammar?.id.orEmpty(),
            tags = listOf("gramática"), lessonId = completed.id
        ))
        feedbacks.add(LessonFeedback(
            id = "demo-feedback-1", classId = portuguese.id, lessonId = completed.id,
            studentId = "@aluno-demo", understanding = FeedbackUnderstanding.PARTIAL,
            comment = "A explicação foi boa, mas preciso de mais exemplos."
        ))
        feedbacks.add(LessonFeedback(
            id = "demo-feedback-2", classId = portuguese.id, lessonId = completed.id,
            studentId = "@aluna-maria", understanding = FeedbackUnderstanding.DIFFICULT,
            comment = "Gostaria de rever o conteúdo do slide final."
        ))
        prefs.edit().putBoolean("sample_initialized", true).apply()
        persist()
    }

    private fun lessonsRef(classId: String) = db!!.collection("classes").document(classId).collection("lessons")
    private fun resourcesRef(classId: String) = db!!.collection("classes").document(classId).collection("materials")
    private fun feedbackRef(classId: String) = db!!.collection("classes").document(classId).collection("lessonFeedback")

    fun refresh(classIds: List<String>, teacher: Boolean) {
        if (!isCloud || db == null) return
        val allowed = classIds.toSet()
        lessons.removeAll { it.classId !in allowed }
        resources.removeAll { it.classId !in allowed }
        feedbacks.removeAll { it.classId !in allowed }
        classIds.forEach { classId ->
            val lessonQuery = if (teacher) lessonsRef(classId) else lessonsRef(classId).whereEqualTo("status", "COMPLETED")
            lessonQuery.get().addOnSuccessListener { result ->
                lessons.removeAll { it.classId == classId }
                lessons.addAll(result.documents.mapNotNull { doc ->
                    runCatching {
                        val drafts = (doc.get("draftCards") as? List<*>)?.mapNotNull { obj ->
                            val m = obj as? Map<*, *> ?: return@mapNotNull null
                            LessonDraftCard(
                                m["id"] as? String ?: return@mapNotNull null,
                                m["deckId"] as? String ?: "",
                                m["question"] as? String ?: "",
                                m["answer"] as? String ?: "",
                                m["published"] as? Boolean ?: false
                            )
                        }.orEmpty()
                        MyDeckLesson(doc.id, classId, doc.getString("title").orEmpty(),
                            doc.getString("description").orEmpty(), doc.getString("deckId").orEmpty(),
                            LessonStatus.valueOf(doc.getString("status") ?: "PLANNED"),
                            drafts, doc.getLong("completedAt"))
                    }.getOrNull()
                })
            }.addOnFailureListener { error = it.localizedMessage }
            resourcesRef(classId).get().addOnSuccessListener { result ->
                resources.removeAll { it.classId == classId }
                resources.addAll(result.documents.mapNotNull { doc ->
                    runCatching {
                        LearningResource(doc.id, doc.getString("title").orEmpty(),
                            doc.getString("url").orEmpty(),
                            LearningResourceType.valueOf(doc.getString("type") ?: "LINK"),
                            classId, doc.getString("deckId").orEmpty(),
                            (doc.get("tags") as? List<*>)?.filterIsInstance<String>().orEmpty(),
                            doc.getString("cardId").orEmpty(), doc.getString("lessonId").orEmpty())
                    }.getOrNull()
                })
            }.addOnFailureListener { error = it.localizedMessage }
            val query = feedbackRef(classId).let { base ->
                if (teacher) base else base.whereEqualTo("studentId", accountId)
            }
            query.get().addOnSuccessListener { result ->
                feedbacks.removeAll { it.classId == classId }
                feedbacks.addAll(result.documents.mapNotNull { doc ->
                    runCatching {
                        LessonFeedback(doc.id, classId, doc.getString("lessonId").orEmpty(),
                            doc.getString("studentId").orEmpty(),
                            FeedbackUnderstanding.valueOf(doc.getString("understanding") ?: "PARTIAL"),
                            doc.getString("comment").orEmpty(), doc.getLong("createdAt") ?: 0L)
                    }.getOrNull()
                })
            }.addOnFailureListener { error = it.localizedMessage }
        }
    }

    fun saveLesson(item: MyDeckLesson) {
        require(item.title.isNotBlank())
        if (isCloud) {
            if (db == null) { error = "Firebase indisponível"; return }
            lessonsRef(item.classId).document(item.id).set(mapOf(
                "title" to item.title.trim(), "description" to item.description,
                "deckId" to item.deckId, "status" to item.status.name,
                "completedAt" to item.completedAt,
                "draftCards" to item.cards.map {
                    mapOf("id" to it.id, "deckId" to it.deckId,
                        "question" to it.question, "answer" to it.answer, "published" to it.published)
                }
            )).addOnSuccessListener { upsertLesson(item) }
                .addOnFailureListener { error = it.localizedMessage }
        } else { upsertLesson(item); persist() }
    }

    fun createLesson(classId: String, title: String, description: String, deckId: String): MyDeckLesson {
        val lesson = MyDeckLesson(UUID.randomUUID().toString(), classId, title.trim(), description.trim(), deckId)
        saveLesson(lesson)
        return lesson
    }

    fun addDraft(lesson: MyDeckLesson, deckId: String, question: String, answer: String) {
        require(deckId.isNotBlank() && question.isNotBlank() && answer.isNotBlank())
        saveLesson(lesson.copy(cards = lesson.cards + LessonDraftCard(UUID.randomUUID().toString(),
            deckId, question.trim(), answer.trim())))
    }

    /**
     * Publicação idempotente: cada rascunho usa seu próprio ID como ID do
     * cartão no deck. Na nuvem, cartões e conclusão são uma operação atômica.
     */
    fun completeLesson(
        lesson: MyDeckLesson,
        publishLocally: (deckId: String, question: String, answer: String) -> Unit,
        onCloudDone: () -> Unit = {}
    ) {
        if (lesson.status == LessonStatus.COMPLETED) return
        val completed = lesson.copy(
            status = LessonStatus.COMPLETED,
            completedAt = System.currentTimeMillis(),
            cards = lesson.cards.map { it.copy(published = true) }
        )
        if (isCloud) {
            if (db == null) { error = "Firebase indisponível"; return }
            val batch = db.batch()
            lesson.cards.filterNot { it.published }.forEach { card ->
                val ref = db.collection("decks").document(card.deckId).collection("cards").document(card.id)
                batch.set(ref, mapOf(
                    "type" to "BASIC", "question" to card.question, "answer" to card.answer,
                    "description" to "", "tags" to emptyList<String>(),
                    "options" to emptyList<String>(), "correctOptionIndex" to null,
                    "frontLanguage" to "", "backLanguage" to "", "mediaType" to "",
                    "mediaUrl" to "", "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                ))
            }
            batch.set(lessonsRef(lesson.classId).document(lesson.id), mapOf(
                "title" to completed.title, "description" to completed.description,
                "deckId" to completed.deckId, "status" to completed.status.name,
                "completedAt" to completed.completedAt,
                "draftCards" to completed.cards.map {
                    mapOf("id" to it.id, "deckId" to it.deckId,
                        "question" to it.question, "answer" to it.answer, "published" to true)
                }
            ))
            batch.commit().addOnSuccessListener {
                upsertLesson(completed)
                onCloudDone()
            }.addOnFailureListener { error = it.localizedMessage }
        } else {
            lesson.cards.filterNot { it.published }.forEach {
                publishLocally(it.deckId, it.question, it.answer)
            }
            upsertLesson(completed)
            persist()
        }
    }

    fun saveMaterial(item: LearningResource) {
        require(item.title.isNotBlank() && validLearningUrl(item.url))
        if (isCloud) {
            if (db == null) { error = "Firebase indisponível"; return }
            resourcesRef(item.classId).document(item.id).set(mapOf(
                "title" to item.title.trim(), "url" to item.url.trim(),
                "type" to item.type.name, "deckId" to item.deckId,
                "cardId" to item.cardId, "lessonId" to item.lessonId,
                "tags" to item.tags
            )).addOnSuccessListener { upsertMaterial(item) }
                .addOnFailureListener { error = it.localizedMessage }
        } else { upsertMaterial(item); persist() }
    }

    fun submitFeedback(item: LessonFeedback) {
        require(item.comment.length <= 1200)
        if (isCloud) {
            if (db == null || accountId != item.studentId) { error = "Conta inválida"; return }
            feedbackRef(item.classId).document(item.id).set(mapOf(
                "studentId" to item.studentId, "lessonId" to item.lessonId,
                "understanding" to item.understanding.name,
                "comment" to item.comment, "createdAt" to item.createdAt
            )).addOnSuccessListener { upsertFeedback(item) }
                .addOnFailureListener { error = it.localizedMessage }
        } else { upsertFeedback(item); persist() }
    }

    private fun upsertLesson(v: MyDeckLesson) {
        lessons.removeAll { it.id == v.id && it.classId == v.classId }; lessons.add(v)
    }
    private fun upsertMaterial(v: LearningResource) {
        resources.removeAll { it.id == v.id && it.classId == v.classId }; resources.add(v)
    }
    private fun upsertFeedback(v: LessonFeedback) {
        feedbacks.removeAll { it.id == v.id && it.classId == v.classId }; feedbacks.add(v)
    }

    private fun persist() {
        val root = JSONObject()
        root.put("lessons", JSONArray(lessons.map { v ->
            JSONObject().put("id", v.id).put("classId", v.classId).put("title", v.title)
                .put("description", v.description).put("deckId", v.deckId).put("status", v.status.name)
                .put("completedAt", v.completedAt ?: JSONObject.NULL)
                .put("cards", JSONArray(v.cards.map { c ->
                    JSONObject().put("id", c.id).put("deckId", c.deckId).put("question", c.question)
                        .put("answer", c.answer).put("published", c.published)
                }))
        }))
        root.put("materials", JSONArray(resources.map { v ->
            JSONObject().put("id", v.id).put("classId", v.classId).put("title", v.title)
                .put("url", v.url).put("type", v.type.name).put("deckId", v.deckId)
                .put("cardId", v.cardId).put("lessonId", v.lessonId).put("tags", JSONArray(v.tags))
        }))
        root.put("feedback", JSONArray(feedbacks.map { v ->
            JSONObject().put("id", v.id).put("classId", v.classId).put("lessonId", v.lessonId)
                .put("studentId", v.studentId).put("understanding", v.understanding.name)
                .put("comment", v.comment).put("createdAt", v.createdAt)
        }))
        prefs.edit().putString("snapshot", root.toString()).apply()
    }

    private fun restore() {
        runCatching {
            val root = JSONObject(prefs.getString("snapshot", "{}") ?: "{}")
            val l = root.optJSONArray("lessons") ?: JSONArray()
            for (i in 0 until l.length()) {
                val d = l.getJSONObject(i); val ca = d.optJSONArray("cards") ?: JSONArray()
                val cards = (0 until ca.length()).map { j ->
                    val c = ca.getJSONObject(j)
                    LessonDraftCard(c.getString("id"), c.optString("deckId"), c.optString("question"),
                        c.optString("answer"), c.optBoolean("published"))
                }
                lessons.add(MyDeckLesson(d.getString("id"), d.getString("classId"), d.getString("title"),
                    d.optString("description"), d.optString("deckId"),
                    LessonStatus.valueOf(d.optString("status", "PLANNED")), cards,
                    if (d.isNull("completedAt")) null else d.optLong("completedAt")))
            }
            val m = root.optJSONArray("materials") ?: JSONArray()
            for (i in 0 until m.length()) {
                val d = m.getJSONObject(i); val tags = d.optJSONArray("tags") ?: JSONArray()
                resources.add(LearningResource(d.getString("id"), d.getString("title"), d.getString("url"),
                    LearningResourceType.valueOf(d.getString("type")), d.getString("classId"),
                    d.optString("deckId"), (0 until tags.length()).map { tags.getString(it) },
                    d.optString("cardId"), d.optString("lessonId")))
            }
            val f = root.optJSONArray("feedback") ?: JSONArray()
            for (i in 0 until f.length()) {
                val d = f.getJSONObject(i)
                feedbacks.add(LessonFeedback(d.getString("id"), d.getString("classId"),
                    d.optString("lessonId"), d.getString("studentId"),
                    FeedbackUnderstanding.valueOf(d.getString("understanding")),
                    d.optString("comment"), d.optLong("createdAt")))
            }
        }.onFailure { error = it.localizedMessage }
    }
}
