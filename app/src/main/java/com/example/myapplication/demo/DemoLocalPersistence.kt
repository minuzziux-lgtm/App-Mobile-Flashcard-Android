package com.example.myapplication.demo

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

internal class DemoLocalPersistence(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        "mydeck_local_demo_v2", Context.MODE_PRIVATE
    )

    fun restore(store: LocalMyDeckStore) {
        val data = preferences.getString("snapshot", null) ?: return
        try {
            val root = JSONObject(data)
            for (item in root.optJSONArray("classes").items()) {
                store.classes.add(DemoClass(item.getString("id"), item.getString("name"), item.getString("joinCode")))
            }
            for (item in root.optJSONArray("decks").items()) {
                store.decks.add(DemoDeck(item.getString("id"), item.getString("classId"), item.getString("title")))
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
        } catch (_: Exception) {
            store.classes.clear()
            store.decks.clear()
            store.cards.clear()
            store.enrolledClassIds.clear()
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
                put(JSONObject().put("id", value.id).put("classId", value.classId).put("title", value.title))
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
