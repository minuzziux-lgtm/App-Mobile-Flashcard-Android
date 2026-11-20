package com.example.myapplication.ui.student

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import com.example.myapplication.ui.design.MyDeckDesign
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
            modifier = Modifier.horizontalScroll(rememberScrollState()).semantics {
                contentDescription = "Frequência de estudo dos últimos 84 dias. " +
                    "${days.sumOf { counts[it] ?: 0 }} revisões registradas nesse período."
            },
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            days.chunked(7).forEach { week ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    week.forEach { day ->
                        val count = counts[day] ?: 0
                        val color = when {
                            count == 0 -> if (MyDeckDesign.darkMode) Color(0xFF303030) else Color(0xFFECECE9)
                            count <= 2 -> if (MyDeckDesign.darkMode) Color(0xFF575755) else Color(0xFFC6C6C1)
                            count <= 5 -> if (MyDeckDesign.darkMode) Color(0xFF939391) else Color(0xFF898984)
                            else -> if (MyDeckDesign.darkMode) Color(0xFFE8E8E5) else Color(0xFF272727)
                        }
                        Box(Modifier.size(13.dp).background(color))
                    }
                }
            }
        }
    }
}
