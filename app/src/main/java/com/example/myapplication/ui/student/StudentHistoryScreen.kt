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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/** Histórico do estudante demonstrativo, derivado de eventos gravados no aparelho. */
@Composable
fun StudentHistoryScreen(reviews: List<StudyReview>, decks: List<DemoDeck>) {
    var selectedDeckId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedTag by rememberSaveable { mutableStateOf<String?>(null) }
    var showFilters by rememberSaveable { mutableStateOf(false) }
    val sorted = reviews.filter { item ->
        (selectedDeckId == null || selectedDeckId == item.deckId) &&
            (selectedTag == null || selectedTag in item.tags)
    }.sortedByDescending { it.reviewedAtEpochMillis }
    val usedDecks = decks.filter { deck -> reviews.any { it.deckId == deck.id } }
    val usedTags = reviews.flatMap { it.tags }.distinct().sorted()
    val graded = sorted.filter { it.correct != null }
    val correct = graded.count { it.correct == true }
    val accuracy = if (graded.isEmpty()) 0 else correct * 100 / graded.size
    // Agregação por período: não renderizar centenas de eventos individuais.
    val activeDays = sorted.map { review ->
        Calendar.getInstance().apply { timeInMillis = review.reviewedAtEpochMillis }
            .get(Calendar.YEAR).toString() + "-" +
            Calendar.getInstance().apply { timeInMillis = review.reviewedAtEpochMillis }
                .get(Calendar.DAY_OF_YEAR)
    }.distinct().size
    val deckSummaries = sorted.groupBy { it.deckId }
        .map { (deckId, activity) ->
            val evaluated = activity.filter { it.correct != null }
            val hits = evaluated.count { it.correct == true }
            Triple(decks.firstOrNull { it.id == deckId }?.title ?: "Deck", activity.size,
                if (evaluated.isEmpty()) null else hits * 100 / evaluated.size)
        }.sortedByDescending { it.second }
    StudentSection(
        title = "Dados.",
        description = "Histórico local de estudos e avaliações. Respostas livres não entram na taxa de acertos."
    ) {
        StudentSectionRow(
            "Filtrar histórico",
            if (selectedDeckId == null && selectedTag == null) "Todos os assuntos" else "Filtro ativo"
        ) { showFilters = !showFilters }
        if (showFilters) {

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
        }
        Spacer(Modifier.height(36.dp))
        HistoryHeader("01 / DESEMPENHO")
        Spacer(Modifier.height(16.dp))
        HistoryText("${sorted.size} registros · ${graded.size} avaliados · $accuracy% de acertos")
        Spacer(Modifier.height(48.dp))
        StudentActivityHeatmap(sorted)
        Spacer(Modifier.height(56.dp))
        HistoryHeader("02 / RESUMO DOS ESTUDOS")
        Spacer(Modifier.height(18.dp))
        HistoryText("$activeDays dias ativos · ${sorted.size} revisões registradas")
        Spacer(Modifier.height(24.dp))
        HistoryHeader("DESEMPENHO POR DECK")
        Spacer(Modifier.height(12.dp))
        if (deckSummaries.isEmpty()) {
            HistoryText("Nenhuma atividade registrada. Comece um estudo para ver seu progresso.")
        } else {
            deckSummaries.forEach { (title, count, rate) ->
                StudentSectionRow(title,
                    "$count revisões · " + (rate?.let { "$it% de acertos" } ?: "sem avaliações")) {
                    selectedDeckId = decks.firstOrNull { it.title == title }?.id
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
