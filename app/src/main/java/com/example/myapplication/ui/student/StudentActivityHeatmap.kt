package com.example.myapplication.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.study.StudyReview
import com.example.myapplication.study.activityByDay
import com.example.myapplication.study.localDayStart
import java.util.Calendar

/** Mapa de 12 semanas; cada quadrado representa um dia, inclusive dias vazios. */
@Composable
fun StudentActivityHeatmap(reviews: List<StudyReview>, now: Long = System.currentTimeMillis()) {
    val counts = activityByDay(reviews)
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = localDayStart(now)
    calendar.add(Calendar.DAY_OF_YEAR, -83)
    val days = (0 until 84).map {
        val day = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        day
    }
    Column {
        HistoryHeader("ATIVIDADE — ÚLTIMOS 84 DIAS")
        Spacer(Modifier.height(15.dp))
        Row(
            modifier = Modifier.semantics {
                contentDescription = "Frequência de estudo dos últimos 84 dias. " +
                    "${days.sumOf { counts[it] ?: 0 }} revisões registradas nesse período."
            },
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            days.chunked(7).forEach { week ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    week.forEach { day ->
                        val count = counts[day] ?: 0
                        val color = when {
                            count == 0 -> Color(0xFFE7E7E5)
                            count <= 2 -> Color(0xFFB6B6B3)
                            count <= 5 -> Color(0xFF777773)
                            else -> Color(0xFF242424)
                        }
                        Box(Modifier.size(12.dp).background(color))
                    }
                }
            }
        }
    }
}
