package com.example.myapplication.ui.student

import androidx.compose.foundation.background
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.example.myapplication.demo.LocalMyDeckStore
import com.example.myapplication.learning.*
import com.example.myapplication.ui.design.MyDeckDesign
import com.example.myapplication.ui.design.MyDeckButton
import com.example.myapplication.ui.design.MyDeckButtonVariant
import java.util.UUID

@Composable
fun StudentLearningScreen(
    local: LocalMyDeckStore,
    learning: LearningFeatureStore,
    studentId: String,
    onBack: () -> Unit
) {
    val classes = local.classes.filter { it.id in local.enrolledForStudent(studentId) }
    var selectedClass by rememberSaveable { mutableStateOf("") }
    var selectedLesson by rememberSaveable { mutableStateOf("") }
    var rating by rememberSaveable { mutableStateOf(FeedbackUnderstanding.PARTIAL.name) }
    var comment by rememberSaveable { mutableStateOf("") }
    var generalComment by rememberSaveable { mutableStateOf("") }
    val classroom = classes.firstOrNull { it.id == selectedClass } ?: classes.firstOrNull()
    val allowedDeckIds = local.decks.filter { deck ->
        deck.assignedClassIds.any { classId -> classes.any { it.id == classId } }
    }.map { it.id }.toSet()
    val visibleResources = learning.resources.filter { item ->
        classes.any { it.id == item.classId } &&
            (item.deckId.isBlank() || item.deckId in allowedDeckIds)
    }
    val suggestions = studySuggestions(
        local.reviews.filter { it.studentId == studentId },
        visibleResources, allowedDeckIds
    )
    val uriHandler = LocalUriHandler.current

    StudentSection(
        title = "Aulas.",
        description = "Acompanhe as aulas, encontre materiais e compartilhe suas dificuldades."
    ) {
        MyDeckButton("Voltar", MyDeckButtonVariant.LIGHT, onClick = onBack)
        if (learning.isCloud) {
            Spacer(Modifier.height(10.dp))
            MyDeckButton("Atualizar aulas e materiais", MyDeckButtonVariant.LIGHT,
                onClick = { learning.refresh(classes.map { it.id }, teacher = false) })
        }
        Spacer(Modifier.height(34.dp))
        HistoryHeader("REVISÕES RECOMENDADAS")
        Spacer(Modifier.height(14.dp))
        if (suggestions.isEmpty()) {
            HistoryText("Nenhum assunto com dificuldade identificada a partir de cinco avaliações.")
        }
        suggestions.forEach { suggestion ->
            Spacer(Modifier.height(16.dp))
            HistoryText("${suggestion.tag} · ${suggestion.correct} de ${suggestion.graded} acertos")
            suggestion.resources.forEach { material ->
                StudentSectionRow(material.title, "Abrir material de apoio") {
                    uriHandler.openUri(material.url)
                }
            }
            if (suggestion.resources.isEmpty()) {
                HistoryText("Ainda não há material indicado pelo professor para este assunto.")
            }
        }
        Spacer(Modifier.height(56.dp))
        HistoryHeader("MINHAS TURMAS")
        Spacer(Modifier.height(16.dp))
        if (classes.isEmpty()) HistoryText("Entre em uma turma para consultar as aulas.")
        classes.forEach { item ->
            StudentSectionRow(item.name,
                if (classroom?.id == item.id) "Selecionada" else "Ver aulas") {
                selectedClass = item.id; selectedLesson = ""
            }
        }
        if (classroom == null) return@StudentSection
        Spacer(Modifier.height(34.dp))
        HistoryHeader("AULAS CONCLUÍDAS")
        Spacer(Modifier.height(12.dp))
        val lessons = learning.lessons.filter {
            it.classId == classroom.id && it.status == LessonStatus.COMPLETED
        }
        if (lessons.isEmpty()) HistoryText("Ainda não há aulas concluídas nesta turma.")
        lessons.forEach { lesson ->
            StudentSectionRow(lesson.title,
                if (selectedLesson == lesson.id) "Selecionada" else "Ver conteúdo e avaliar") {
                selectedLesson = if (selectedLesson == lesson.id) "" else lesson.id
            }
        }
        val selected = lessons.firstOrNull { it.id == selectedLesson }
        if (selected != null) {
            Spacer(Modifier.height(28.dp))
            HistoryHeader(selected.title.uppercase())
            Spacer(Modifier.height(14.dp))
            if (selected.description.isNotBlank()) HistoryText(selected.description)
            val lessonResources = visibleResources.filter {
                it.classId == classroom.id &&
                    (it.lessonId == selected.id || (it.lessonId.isBlank() &&
                        (selected.deckId.isBlank() || it.deckId == selected.deckId)))
            }
            Spacer(Modifier.height(24.dp))
            HistoryHeader("MATERIAIS DA AULA")
            Spacer(Modifier.height(12.dp))
            if (lessonResources.isEmpty()) HistoryText("Nenhum material complementar disponível.")
            lessonResources.forEach { material ->
                StudentSectionRow(material.title, "${material.type.name} · Abrir link") {
                    uriHandler.openUri(material.url)
                }
            }
            Spacer(Modifier.height(42.dp))
            HistoryHeader("SUA AVALIAÇÃO DA AULA")
            Spacer(Modifier.height(12.dp))
            val previous = learning.feedbacks.any {
                it.classId == classroom.id && it.lessonId == selected.id && it.studentId == studentId
            }
            if (previous) HistoryText("Seu feedback já foi enviado.")
            else {
                HistoryText("Como foi seu entendimento?")
                FeedbackUnderstanding.entries.forEach { option ->
                    StudentSectionRow(when (option) {
                        FeedbackUnderstanding.CLEAR -> "Entendi bem"
                        FeedbackUnderstanding.PARTIAL -> "Entendi parcialmente"
                        FeedbackUnderstanding.DIFFICULT -> "Tive dificuldade"
                    }, if (rating == option.name) "Selecionado" else "Escolher") {
                        rating = option.name
                    }
                }
                Spacer(Modifier.height(16.dp))
                HistoryText("Sugestão ou dificuldade (opcional)")
                Spacer(Modifier.height(12.dp))
                BasicTextField(
                    value = comment, onValueChange = { comment = it.take(1200) },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = MyDeckDesign.font, color = MyDeckDesign.ink
                    ),
                    cursorBrush = SolidColor(MyDeckDesign.ink),
                    modifier = Modifier.fillMaxWidth()
                        .background(MyDeckDesign.fieldBackground)
                        .drawBehind {
                            drawLine(
                                MyDeckDesign.inputLine, Offset(0f, size.height),
                                Offset(size.width, size.height), strokeWidth = 1.dp.toPx()
                            )
                        }.padding(16.dp),
                    decorationBox = { field ->
                        if (comment.isBlank()) HistoryText("O que poderia melhorar?")
                        field()
                    }
                )
                Spacer(Modifier.height(18.dp))
                MyDeckButton("Enviar feedback da aula", MyDeckButtonVariant.DARK, onClick = {
                    learning.submitFeedback(LessonFeedback(
                        UUID.randomUUID().toString(), classroom.id, selected.id, studentId,
                        FeedbackUnderstanding.valueOf(rating), comment.trim()))
                    comment = ""
                })
            }
        }
        Spacer(Modifier.height(52.dp))
        HistoryHeader("SUGESTÃO GERAL")
        Spacer(Modifier.height(14.dp))
        HistoryText("Compartilhe uma sugestão sobre a experiência na turma.")
        Spacer(Modifier.height(12.dp))
        BasicTextField(
            value = generalComment, onValueChange = { generalComment = it.take(1200) },
            textStyle = androidx.compose.ui.text.TextStyle(
                fontFamily = MyDeckDesign.font, color = MyDeckDesign.ink),
            cursorBrush = SolidColor(MyDeckDesign.ink),
            modifier = Modifier.fillMaxWidth()
                .background(MyDeckDesign.fieldBackground)
                        .drawBehind {
                            drawLine(
                                MyDeckDesign.inputLine, Offset(0f, size.height),
                                Offset(size.width, size.height), strokeWidth = 1.dp.toPx()
                            )
                        }.padding(16.dp),
            decorationBox = { field ->
                if (generalComment.isBlank()) HistoryText("Escreva sua sugestão")
                field()
            }
        )
        Spacer(Modifier.height(16.dp))
        MyDeckButton("Enviar sugestão geral", MyDeckButtonVariant.LIGHT,
            onClick = if (generalComment.isNotBlank()) ({
                learning.submitFeedback(LessonFeedback(
                    UUID.randomUUID().toString(), classroom.id, "", studentId,
                    FeedbackUnderstanding.PARTIAL, generalComment.trim()))
                generalComment = ""
            }) else null
        )
        learning.error?.let {
            Spacer(Modifier.height(16.dp))
            HistoryText("Aviso: $it")
        }
        Spacer(Modifier.height(64.dp))
    }
}
