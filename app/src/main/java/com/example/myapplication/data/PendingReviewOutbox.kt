package com.example.myapplication.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import com.example.myapplication.study.StudyReview
import java.security.MessageDigest

/**
 * Fila privada de revisões por UID. Os dados são mantidos no aparelho até
 * confirmação de gravação no Firestore; nunca reatribui revisões a outra conta.
 * O mesmo ID de documento permite repetir a gravação sem duplicar eventos.
 */
class PendingReviewOutbox(context: Context, uid: String) {
    init { require(uid.isNotBlank()) }

    private val key = MessageDigest.getInstance("SHA-256")
        .digest(uid.toByteArray(Charsets.UTF_8))
        .take(12).joinToString("") { "%02x".format(it) }
    private val prefs = context.applicationContext.getSharedPreferences(
        "mydeck_review_outbox_${key}_v1", Context.MODE_PRIVATE
    )

    @Synchronized
    fun enqueue(review: StudyReview) {
        require(review.studentId.isNotBlank())
        val snapshot = JSONObject(prefs.getString("queue", "{}") ?: "{}")
        val data = JSONObject()
        FirestoreReviewMapper.toFields(review).forEach { (field, value) ->
            data.put(field, when (value) {
                null -> JSONObject.NULL
                is List<*> -> JSONArray(value)
                else -> value
            })
        }
        snapshot.put(review.id, data)
        prefs.edit().putString("queue", snapshot.toString()).apply()
    }

    @Synchronized
    fun remove(id: String) {
        val snapshot = JSONObject(prefs.getString("queue", "{}") ?: "{}")
        snapshot.remove(id)
        prefs.edit().putString("queue", snapshot.toString()).apply()
    }

    @Synchronized
    fun pending(studentId: String): List<StudyReview> {
        val snapshot = JSONObject(prefs.getString("queue", "{}") ?: "{}")
        val results = mutableListOf<StudyReview>()
        val keys = snapshot.keys()
        while (keys.hasNext()) {
            val id = keys.next()
            val json = snapshot.optJSONObject(id) ?: continue
            val fields = mutableMapOf<String, Any?>()
            val fieldNames = json.keys()
            while (fieldNames.hasNext()) {
                val field = fieldNames.next()
                fields[field] = when (val value = json.opt(field)) {
                    JSONObject.NULL -> null
                    is JSONArray -> (0 until value.length()).map { value.optString(it) }
                    else -> value
                }
            }
            FirestoreReviewMapper.fromFields(id, fields)?.let {
                if (it.studentId == studentId) results += it
            }
        }
        return results
    }
}
