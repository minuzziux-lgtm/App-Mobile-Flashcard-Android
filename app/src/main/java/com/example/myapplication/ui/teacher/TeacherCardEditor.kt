package com.example.myapplication.ui.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.demo.DemoCard
import com.example.myapplication.demo.DemoCardDraft
import com.example.myapplication.demo.DemoCardType
import com.example.myapplication.demo.DemoMediaLink
import com.example.myapplication.demo.DemoMediaType
import com.example.myapplication.demo.isValidExternalLink
import com.example.myapplication.demo.validateCardDraft
import com.example.myapplication.ui.design.MyDeckButton
import com.example.myapplication.ui.design.MyDeckButtonVariant
import com.example.myapplication.ui.design.MyDeckDesign

@Composable
internal fun TeacherCardEditor(
    initialCard: DemoCard? = null,
    availableTags: List<String> = emptyList(),
    onSave: (DemoCardDraft) -> Unit
) {
    var typeName by rememberSaveable { mutableStateOf(initialCard?.type?.name ?: DemoCardType.BASIC.name) }
    val type = DemoCardType.valueOf(typeName)
    var question by rememberSaveable { mutableStateOf(initialCard?.question.orEmpty()) }
    var answer by rememberSaveable { mutableStateOf(initialCard?.answer.orEmpty()) }
    var description by rememberSaveable { mutableStateOf(initialCard?.description.orEmpty()) }
    var tags by rememberSaveable { mutableStateOf(initialCard?.tags?.joinToString(", ").orEmpty()) }
    var option1 by rememberSaveable { mutableStateOf(initialCard?.options?.getOrNull(0).orEmpty()) }
    var option2 by rememberSaveable { mutableStateOf(initialCard?.options?.getOrNull(1).orEmpty()) }
    var option3 by rememberSaveable { mutableStateOf(initialCard?.options?.getOrNull(2).orEmpty()) }
    var option4 by rememberSaveable { mutableStateOf(initialCard?.options?.getOrNull(3).orEmpty()) }
    var correctOption by rememberSaveable { mutableStateOf(initialCard?.correctOptionIndex ?: 0) }
    var frontLanguage by rememberSaveable { mutableStateOf(initialCard?.frontLanguage.orEmpty()) }
    var backLanguage by rememberSaveable { mutableStateOf(initialCard?.backLanguage.orEmpty()) }
    var mediaTypeName by rememberSaveable { mutableStateOf(initialCard?.mediaLink?.type?.name.orEmpty()) }
    var mediaUrl by rememberSaveable { mutableStateOf(initialCard?.mediaLink?.url.orEmpty()) }
    var feedback by rememberSaveable { mutableStateOf<String?>(null) }
    val options = listOf(option1, option2, option3, option4).map(String::trim)
    val selectedMediaType = DemoMediaType.entries.firstOrNull { it.name == mediaTypeName }

    TeacherLabel("TIPO DE CARTÃO")
    Spacer(Modifier.height(12.dp))
    DemoCardType.entries.forEach { available ->
        TeacherSelectionRow(available.label, available == type) {
            typeName = available.name
            feedback = null
        }
    }
    Spacer(Modifier.height(25.dp))
    TeacherInput(
        when (type) {
            DemoCardType.CLOZE -> "Frase com lacuna (___)"
            DemoCardType.REVERSIBLE -> "Lado A (palavra ou expressão)"
            DemoCardType.OPEN_ENDED -> "Pergunta ou tema de reflexão"
            else -> "Pergunta"
        },
        when (type) {
            DemoCardType.CLOZE -> "She ___ to school every day."
            DemoCardType.REVERSIBLE -> "Casa"
            else -> "Digite a pergunta"
        },
        question
    ) { question = it; feedback = null }

    if (type == DemoCardType.MULTIPLE_CHOICE) {
        Spacer(Modifier.height(20.dp))
        TeacherBody("Preencha pelo menos duas alternativas e selecione a correta.")
        Spacer(Modifier.height(15.dp))
        listOf(option1, option2, option3, option4).forEachIndexed { index, option ->
            TeacherInput("Alternativa " + (index + 1), "Texto da alternativa", option) { value ->
                when (index) {
                    0 -> option1 = value
                    1 -> option2 = value
                    2 -> option3 = value
                    else -> option4 = value
                }
                feedback = null
            }
            Spacer(Modifier.height(12.dp))
        }
        Spacer(Modifier.height(10.dp))
        TeacherLabel("RESPOSTA CORRETA")
        Spacer(Modifier.height(12.dp))
        options.forEachIndexed { index, option ->
            if (option.isNotBlank()) {
                TeacherSelectionRow("Alternativa " + (index + 1), index == correctOption) {
                    correctOption = index
                }
            }
        }
    } else if (type != DemoCardType.OPEN_ENDED) {
        Spacer(Modifier.height(20.dp))
        TeacherInput(
            if (type == DemoCardType.REVERSIBLE) "Lado B (tradução)" else "Resposta",
            if (type == DemoCardType.CLOZE) "Palavra que completa a lacuna" else "Digite a resposta",
            answer
        ) { answer = it; feedback = null }
    }

    if (type == DemoCardType.REVERSIBLE) {
        Spacer(Modifier.height(20.dp))
        TeacherInput("Idioma do lado A (opcional)", "Português", frontLanguage) { frontLanguage = it }
        Spacer(Modifier.height(18.dp))
        TeacherInput("Idioma do lado B (opcional)", "Inglês", backLanguage) { backLanguage = it }
    }
    Spacer(Modifier.height(20.dp))
    MultiLineInput("Descrição / instruções (opcional)", description) { description = it }
    Spacer(Modifier.height(20.dp))
    TeacherInput("Tags (opcional)", "Ex.: idioma, gramática", tags) { tags = it }
    Spacer(Modifier.height(26.dp))
    TeacherLabel("LINK DE MATERIAL (OPCIONAL)")
    Spacer(Modifier.height(10.dp))
    TeacherBody("Use um link HTTPS público. Não é feito upload nem armazenamento de arquivos.")
    Spacer(Modifier.height(12.dp))
    TeacherSelectionRow("Sem anexo", selectedMediaType == null) {
        mediaTypeName = ""
        feedback = null
    }
    DemoMediaType.entries.forEach { item ->
        TeacherSelectionRow(item.label, selectedMediaType == item) {
            mediaTypeName = item.name
            feedback = null
        }
    }
    if (selectedMediaType != null) {
        Spacer(Modifier.height(12.dp))
        TeacherInput("Endereço HTTPS", "https://...", mediaUrl) { mediaUrl = it; feedback = null }
    }
    Spacer(Modifier.height(22.dp))
    MyDeckButton(
        if (initialCard == null) "Adicionar cartão" else "Salvar cartão", MyDeckButtonVariant.DARK,
        onClick = {
            val filled = options.filter(String::isNotEmpty)
            val invalidMedia = selectedMediaType != null && !isValidExternalLink(mediaUrl)
            feedback = when {
                question.isBlank() -> "Preencha a pergunta ou o lado A."
                type == DemoCardType.CLOZE && "___" !in question -> "Marque a lacuna com três sublinhados: ___."
                type == DemoCardType.MULTIPLE_CHOICE &&
                    (options[0].isEmpty() || options[1].isEmpty() ||
                        filled.size < 2 || correctOption !in options.indices ||
                        options[correctOption].isBlank()) -> "Preencha duas alternativas e selecione uma correta."
                type != DemoCardType.OPEN_ENDED && type != DemoCardType.MULTIPLE_CHOICE &&
                    answer.isBlank() -> "Preencha a resposta."
                invalidMedia -> "Informe um endereço HTTPS válido para o material."
                else -> null
            }
            if (feedback == null) {
                val mappedOptions = if (type == DemoCardType.MULTIPLE_CHOICE) filled else emptyList()
                val effectiveIndex = if (type == DemoCardType.MULTIPLE_CHOICE)
                    options.take(correctOption).count { it.isNotBlank() } else null
                val draft = DemoCardDraft(
                    type = type,
                    question = question.trim(),
                    answer = if (type == DemoCardType.MULTIPLE_CHOICE) options[correctOption] else answer.trim(),
                    description = description.trim(),
                    tags = tags.split(',').map(String::trim).filter(String::isNotEmpty).distinct(),
                    options = mappedOptions,
                    correctOptionIndex = effectiveIndex,
                    frontLanguage = if (type == DemoCardType.REVERSIBLE) frontLanguage.trim() else "",
                    backLanguage = if (type == DemoCardType.REVERSIBLE) backLanguage.trim() else "",
                    mediaLink = selectedMediaType?.let { DemoMediaLink(it, mediaUrl.trim()) }
                )
                feedback = validateCardDraft(draft)
                if (feedback == null) {
                    onSave(draft)
                    question = ""; answer = ""; description = ""; tags = ""
                option1 = ""; option2 = ""; option3 = ""; option4 = ""
                correctOption = 0; frontLanguage = ""; backLanguage = ""
                mediaTypeName = ""; mediaUrl = ""
                    feedback = if (initialCard == null) "Cartão adicionado." else "Cartão atualizado."
                }
            }
        }
    )
    if (feedback != null) {
        Spacer(Modifier.height(12.dp))
        TeacherBody(feedback.orEmpty())
    }
}

@Composable
private fun MultiLineInput(label: String, value: String, onChange: (String) -> Unit) {
    Column {
        BasicText(label, style = teacherStyle(13, FontWeight.Medium, MyDeckDesign.ink))
        Spacer(Modifier.height(10.dp))
        BasicTextField(
            value = value,
            onValueChange = onChange,
            singleLine = false,
            textStyle = teacherStyle(15, FontWeight.Normal, MyDeckDesign.ink),
            cursorBrush = SolidColor(MyDeckDesign.ink),
            modifier = Modifier.fillMaxWidth().heightIn(min = 90.dp),
            decorationBox = { field ->
                Column {
                    Box(Modifier.fillMaxWidth().heightIn(min = 87.dp), contentAlignment = Alignment.TopStart) {
                        if (value.isEmpty()) {
                            BasicText(
                                "Contexto, explicação ou instruções",
                                style = teacherStyle(14, FontWeight.Normal, Color.Gray)
                            )
                        }
                        field()
                    }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(MyDeckDesign.line))
                }
            }
        )
    }
}
