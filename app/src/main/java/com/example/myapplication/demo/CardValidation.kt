package com.example.myapplication.demo

import java.net.URI

fun validateCardDraft(draft: DemoCardDraft): String? {
    if (draft.question.isBlank()) return "Informe a pergunta ou o texto do cartão."
    if (draft.question.length > 2000) return "A pergunta deve ter até 2.000 caracteres."
    if (draft.answer.length > 2000) return "A resposta deve ter até 2.000 caracteres."
    if (draft.description.length > 4000) return "A descrição deve ter até 4.000 caracteres."
    if (draft.tags.size > 20 || draft.tags.any { it.isBlank() || it.length > 40 }) {
        return "Informe até 20 tags com até 40 caracteres cada."
    }
    when (draft.type) {
        DemoCardType.BASIC, DemoCardType.REVERSIBLE -> {
            if (draft.answer.isBlank()) return "Preencha a resposta."
        }
        DemoCardType.CLOZE -> {
            if (draft.answer.isBlank()) return "Preencha a palavra que completa a lacuna."
            if (draft.question.split("___").size != 2) return "Use exatamente uma lacuna com ___."
        }
        DemoCardType.MULTIPLE_CHOICE -> {
            val options = draft.options.map { it.trim() }
            if (options.size !in 2..4 || options.any { it.isEmpty() }) {
                return "Preencha entre duas e quatro alternativas."
            }
            if (options.distinctBy(String::lowercase).size != options.size) {
                return "As alternativas devem ser diferentes."
            }
            val correct = draft.correctOptionIndex
            if (correct == null || correct !in options.indices) return "Selecione uma alternativa correta."
            if (draft.answer.trim() != options[correct]) return "O gabarito não corresponde à alternativa."
        }
        DemoCardType.OPEN_ENDED -> {
            if (draft.answer.isNotBlank()) return "Cartões de resposta livre não possuem gabarito."
        }
    }
    if (draft.mediaLink != null && !isValidExternalLink(draft.mediaLink.url)) {
        return "Informe um link HTTPS válido."
    }
    return null
}

fun isValidExternalLink(url: String): Boolean {
    val normalized = url.trim()
    if (normalized.length > 2000 || normalized.isBlank()) return false
    return try {
        val uri = URI(normalized)
        uri.scheme.equals("https", ignoreCase = true) &&
            !uri.host.isNullOrBlank() &&
            uri.userInfo == null &&
            uri.port in -1..65535
    } catch (_: IllegalArgumentException) {
        false
    }
}
