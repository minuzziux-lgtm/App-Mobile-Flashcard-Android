package com.example.myapplication.demo

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import com.example.myapplication.study.StudyReview

internal class DemoLocalPersistence(context: Context, namespace: String = "demo") {
    private val storageName = if (namespace == "demo") "mydeck_local_demo_v2" else {
        val hash = java.security.MessageDigest.getInstance("SHA-256")
            .digest(namespace.toByteArray(Charsets.UTF_8))
            .take(12)
            .joinToString("") { byte -> "%02x".format(byte) }
        "mydeck_user_${hash}_v1"
    }
    private val preferences = context.applicationContext.getSharedPreferences(
        storageName, Context.MODE_PRIVATE
    )

    fun restore(store: LocalMyDeckStore) {
        val data = preferences.getString("snapshot", null) ?: return
        try {
            val root = JSONObject(data)
            for (item in root.optJSONArray("classes").items()) {
                store.classes.add(DemoClass(item.getString("id"), item.getString("name"), item.getString("joinCode")))
            }
            for (item in root.optJSONArray("decks").items()) {
                val originalClassId = item.getString("classId")
                val assigned = item.optJSONArray("assignedClassIds")?.let { it.texts() }
                    ?: if (originalClassId.isBlank()) emptyList() else listOf(originalClassId)
                store.decks.add(DemoDeck(
                    item.getString("id"), originalClassId, item.getString("title"), assigned
                ))
            }
            for (item in root.optJSONArray("cards").items()) {
                val media = item.optJSONObject("mediaLink")?.let {
                    DemoMediaLink(
                        DemoMediaType.valueOf(it.getString("type")),
                        it.getString("url")
                    )
                }
                store.cards.add(DemoCard(
                    id = item.getString("id"),
                    deckId = item.getString("deckId"),
                    question = item.getString("question"),
                    answer = item.optString("answer"),
                    tags = item.optJSONArray("tags").texts(),
                    type = DemoCardType.valueOf(item.optString("type", "BASIC")),
                    description = item.optString("description"),
                    options = item.optJSONArray("options").texts(),
                    correctOptionIndex = if (item.has("correctOptionIndex") && !item.isNull("correctOptionIndex")) {
                        item.getInt("correctOptionIndex")
                    } else null,
                    frontLanguage = item.optString("frontLanguage"),
                    backLanguage = item.optString("backLanguage"),
                    mediaLink = media
                ))
            }
            store.enrolledClassIds.addAll(root.optJSONArray("enrolledClassIds").texts())
            root.optJSONArray("userEnrollments").items().forEach { item ->
                store.userEnrollments.add(DemoEnrollment(item.getString("classId"), item.getString("studentId")))
            }
            store.availableTags.addAll(
                root.optJSONArray("availableTags").texts().ifEmpty {
                    store.cards.flatMap { it.tags }.distinct()
                }
            )
            for (item in root.optJSONArray("reviews").items()) {
                store.reviews.add(StudyReview(
                    id = item.getString("id"),
                    studentId = item.getString("studentId"),
                    deckId = item.getString("deckId"),
                    cardId = item.getString("cardId"),
                    classId = if (item.isNull("classId")) null else item.optString("classId"),
                    correct = if (item.isNull("correct")) null else item.optBoolean("correct"),
                    writtenResponse = if (item.isNull("writtenResponse")) null else item.optString("writtenResponse"),
                    tags = item.optJSONArray("tags").texts(),
                    reviewedAtEpochMillis = item.getLong("reviewedAtEpochMillis"),
                    dueAtEpochMillis = item.getLong("dueAtEpochMillis"),
                    intervalDays = item.optInt("intervalDays", 0),
                    repetitions = item.optInt("repetitions", 0)
                ))
            }
            if (root.has("participants")) {
                root.optJSONArray("participants").items().forEach { participant ->
                    store.participants.add(DemoParticipant(
                        id = participant.getString("id"),
                        classId = participant.getString("classId"),
                        username = participant.getString("username"),
                        isCurrentDemoStudent = participant.optBoolean("isCurrentDemoStudent", false)
                    ))
                }
            } else {
                store.enrolledClassIds.forEach { id ->
                    store.participants.add(DemoParticipant(
                        id = java.util.UUID.randomUUID().toString(),
                        classId = id,
                        username = "@aluno-demo",
                        isCurrentDemoStudent = true
                    ))
                }
            }
        } catch (_: Exception) {
            store.classes.clear()
            store.decks.clear()
            store.cards.clear()
            store.enrolledClassIds.clear()
            store.userEnrollments.clear()
            store.availableTags.clear()
            store.participants.clear()
            store.reviews.clear()
        }
    }

    fun save(store: LocalMyDeckStore) {
        val snapshot = JSONObject()
        snapshot.put("classes", JSONArray().apply {
            store.classes.forEach { value ->
                put(JSONObject().put("id", value.id).put("name", value.name).put("joinCode", value.joinCode))
            }
        })
        snapshot.put("decks", JSONArray().apply {
            store.decks.forEach { value ->
                put(JSONObject().put("id", value.id).put("classId", value.classId)
                    .put("title", value.title).put("assignedClassIds", JSONArray(value.assignedClassIds)))
            }
        })
        snapshot.put("cards", JSONArray().apply {
            store.cards.forEach { value ->
                put(JSONObject().put("id", value.id).put("deckId", value.deckId)
                    .put("question", value.question).put("answer", value.answer)
                    .put("tags", JSONArray(value.tags)).put("type", value.type.name)
                    .put("description", value.description)
                    .put("options", JSONArray(value.options))
                    .put("correctOptionIndex", value.correctOptionIndex ?: JSONObject.NULL)
                    .put("frontLanguage", value.frontLanguage).put("backLanguage", value.backLanguage)
                    .put("mediaLink", value.mediaLink?.let { media ->
                        JSONObject().put("type", media.type.name).put("url", media.url)
                    } ?: JSONObject.NULL))
            }
        })
        snapshot.put("enrolledClassIds", JSONArray(store.enrolledClassIds))
        snapshot.put("userEnrollments", JSONArray().apply {
            store.userEnrollments.forEach { value ->
                put(JSONObject().put("classId", value.classId).put("studentId", value.studentId))
            }
        })
        snapshot.put("availableTags", JSONArray(store.availableTags))
        snapshot.put("participants", JSONArray().apply {
            store.participants.forEach { value ->
                put(JSONObject().put("id", value.id).put("classId", value.classId)
                    .put("username", value.username)
                    .put("isCurrentDemoStudent", value.isCurrentDemoStudent))
            }
        })
        snapshot.put("reviews", JSONArray().apply {
            store.reviews.forEach { item ->
                put(JSONObject()
                    .put("id", item.id).put("studentId", item.studentId)
                    .put("deckId", item.deckId).put("cardId", item.cardId)
                    .put("classId", item.classId ?: JSONObject.NULL)
                    .put("correct", item.correct ?: JSONObject.NULL)
                    .put("writtenResponse", item.writtenResponse ?: JSONObject.NULL)
                    .put("tags", JSONArray(item.tags))
                    .put("reviewedAtEpochMillis", item.reviewedAtEpochMillis)
                    .put("dueAtEpochMillis", item.dueAtEpochMillis)
                    .put("intervalDays", item.intervalDays)
                    .put("repetitions", item.repetitions))
            }
        })
        preferences.edit().putString("snapshot", snapshot.toString()).apply()
    }

    private fun JSONArray?.items(): List<JSONObject> {
        if (this == null) return emptyList()
        return (0 until length()).mapNotNull { optJSONObject(it) }
    }

    private fun JSONArray?.texts(): List<String> {
        if (this == null) return emptyList()
        return (0 until length()).mapNotNull { index -> optString(index).takeIf(String::isNotBlank) }
    }
}
