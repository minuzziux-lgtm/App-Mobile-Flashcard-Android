package com.example.myapplication.data

import com.example.myapplication.demo.DemoCard
import com.example.myapplication.demo.DemoCardType
import com.example.myapplication.demo.DemoMediaLink
import com.example.myapplication.demo.DemoMediaType

/**
 * Mapeia os cinco tipos de cartão sem depender da SDK Firebase.
 * Formato usado pelo futuro repositório remoto. O UID dono e as atribuições
 * do deck são validados separadamente pelas regras do servidor.
 */
object FirestoreCardMapper {
    fun toFields(card: DemoCard): Map<String, Any?> = mapOf(
        "type" to card.type.name,
        "question" to card.question,
        "answer" to card.answer,
        "description" to card.description,
        "tags" to card.tags,
        "options" to card.options,
        "correctOptionIndex" to card.correctOptionIndex,
        "frontLanguage" to card.frontLanguage,
        "backLanguage" to card.backLanguage,
        "mediaType" to (card.mediaLink?.type?.name ?: ""),
        "mediaUrl" to (card.mediaLink?.url ?: "")
    )

    fun fromFields(cardId: String, deckId: String, fields: Map<String, Any?>): DemoCard? {
        if (cardId.isBlank() || deckId.isBlank()) return null
        val typeName = fields["type"] as? String ?: return null
        val type = DemoCardType.entries.firstOrNull { it.name == typeName } ?: return null
        val question = fields["question"] as? String ?: return null
        val answer = fields["answer"] as? String ?: return null
        val tags = (fields["tags"] as? List<*>)?.filterIsInstance<String>() ?: return null
        val options = (fields["options"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
        val index = (fields["correctOptionIndex"] as? Number)?.toInt()
        val mediaType = (fields["mediaType"] as? String).orEmpty()
            .takeIf(String::isNotBlank)?.let { raw ->
                DemoMediaType.entries.firstOrNull { it.name == raw }
                    ?: return null
            }
        val mediaUrl = (fields["mediaUrl"] as? String).orEmpty()
        val media = if (mediaType != null && mediaUrl.isNotBlank()) {
            DemoMediaLink(mediaType, mediaUrl)
        } else null
        return DemoCard(
            id = cardId,
            deckId = deckId,
            question = question,
            answer = answer,
            tags = tags,
            type = type,
            description = (fields["description"] as? String).orEmpty(),
            options = options,
            correctOptionIndex = index,
            frontLanguage = (fields["frontLanguage"] as? String).orEmpty(),
            backLanguage = (fields["backLanguage"] as? String).orEmpty(),
            mediaLink = media
        )
    }
}
