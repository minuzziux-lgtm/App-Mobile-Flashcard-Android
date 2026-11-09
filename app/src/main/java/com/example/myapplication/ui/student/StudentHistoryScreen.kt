package com.example.myapplication.ui.student

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.demo.DemoDeck
import com.example.myapplication.study.StudyReview
import com.example.myapplication.ui.design.MyDeckDesign
import java.text.DateFormat
import java.util.Date
import java.util.Locale

/** Histórico do estudante demonstrativo, derivado de eventos gravados no aparelho. */
@Composable
fun StudentHistoryScreen(reviews: List<StudyReview>, decks: List<DemoDeck>) {
    var selectedDeckId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedTag by rememberSaveable { mutableStateOf<String?>(null) }
    val sorted = reviews.filter { item ->
        (selectedDeckId == null || selectedDeckId == item.deckId) &&
            (selectedTag == null || selectedTag in item.tags)
    }.sortedByDescending { it.reviewedAtEpochMillis }
    val usedDecks = decks.filter { deck -> reviews.any { it.deckId == deck.id } }
    val usedTags = reviews.flatMap { it.tags }.distinct().sorted()
    val graded = sorted.filter { it.correct != null }
    val correct = graded.count { it.correct == true }
    val accuracy = if (graded.isEmpty()) 0 else correct * 100 / graded.size
    val date = DateFormat.getDateInstance(DateFormat.SHORT, Locale("pt", "BR"))
    StudentSection(
        title = "Dados.",
        description = "Histórico local de estudos e avaliações. Respostas livres não entram na taxa de acertos."
    ) {
        HistoryHeader("FILTROS")
        Spacer(Modifier.height(12.dp))
        StudentSectionRow(
            "Todos os decks",
            if (selectedDeckId == null) "Selecionado" else "Mostrar todos",
            onClick = { selectedDeckId = null }
        )
        usedDecks.forEach { deck ->
            StudentSectionRow(
                deck.title,
                if (selectedDeckId == deck.id) "Selecionado" else "Filtrar por deck",
                onClick = { selectedDeckId = if (selectedDeckId == deck.id) null else deck.id }
            )
        }
        if (usedTags.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            StudentSectionRow(
                "Todas as tags",
                if (selectedTag == null) "Selecionada" else "Mostrar todas",
                onClick = { selectedTag = null }
            )
            usedTags.forEach { tag ->
                StudentSectionRow(
                    "#$tag",
                    if (selectedTag == tag) "Selecionada" else "Filtrar por tag",
                    onClick = { selectedTag = if (selectedTag == tag) null else tag }
                )
            }
        }
        Spacer(Modifier.height(36.dp))
        HistoryHeader("01 / DESEMPENHO")
        Spacer(Modifier.height(16.dp))
        HistoryText("${sorted.size} registros · ${graded.size} avaliados · $accuracy% de acertos")
        Spacer(Modifier.height(35.dp))
        StudentActivityHeatmap(sorted)
        Spacer(Modifier.height(40.dp))
        HistoryHeader("02 / HISTÓRICO")
        Spacer(Modifier.height(16.dp))
        if (sorted.isEmpty()) {
            HistoryText("Nenhuma revisão registrada. Estude um deck para começar.")
        } else {
            sorted.forEach { item ->
                Column {
                    val title = decks.firstOrNull { it.id == item.deckId }?.title ?: "Deck removido"
                    HistoryText("$title · ${date.format(Date(item.reviewedAtEpochMillis))}")
                    Spacer(Modifier.height(5.dp))
                    HistoryText(
                        when (item.correct) {
                            true -> "Acerto registrado"
                            false -> "Erro registrado"
                            null -> "Resposta livre — sem nota"
                        }
                    )
                    Spacer(Modifier.height(19.dp))
                }
            }
        }
    }
}

@Composable
internal fun HistoryHeader(label: String) {
    BasicText(label, style = TextStyle(
        fontFamily = MyDeckDesign.font, fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold, color = MyDeckDesign.ink
    ))
}

@Composable
internal fun HistoryText(label: String) {
    BasicText(label, style = TextStyle(
        fontFamily = MyDeckDesign.font, fontSize = 14.sp,
        lineHeight = 22.sp, color = MyDeckDesign.secondary
    ))
}
