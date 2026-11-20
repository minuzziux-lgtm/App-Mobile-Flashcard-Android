package com.example.myapplication.ui.student

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.design.MyDeckDesign
import com.example.myapplication.study.StudyReview
import com.example.myapplication.study.activityByDay
import com.example.myapplication.study.localDayStart
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Visualização editorial semanal: um toque destaca uma semana e detalha os dados.
 * A leitura não depende da cor, e o conteúdo pode rolar em telas menores.
 */
@Composable
fun StudentActivityHeatmap(reviews: List<StudyReview>, now: Long = System.currentTimeMillis()) {
    val daily = activityByDay(reviews)
    val calendar = Calendar.getInstance().apply {
        timeInMillis = localDayStart(now)
        add(Calendar.DAY_OF_YEAR, -83)
    }
    val days = (0 until 84).map {
        val current = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        current
    }
    val weeks = days.chunked(7)
    val totals = weeks.map { week -> week.sumOf { daily[it] ?: 0 } }
    val activeDays = days.count { (daily[it] ?: 0) > 0 }
    val totalReviews = totals.sum()
    val maximum = (totals.maxOrNull() ?: 0).coerceAtLeast(1)
    var selected by remember { mutableIntStateOf(weeks.lastIndex) }
    val safeIndex = selected.coerceIn(0, weeks.lastIndex)
    val dateFormat = remember { SimpleDateFormat("dd MMM", Locale("pt", "BR")) }
    val selectedWeek = weeks[safeIndex]
    val selectedTotal = totals[safeIndex]

    Column {
        HistoryHeader("FREQUÊNCIA DE ESTUDOS")
        Spacer(Modifier.height(10.dp))
        HistoryText("$activeDays dias ativos · $totalReviews revisões em 12 semanas")
        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState())
                .semantics {
                    contentDescription = "Gráfico de frequência das últimas doze semanas. Toque em uma barra para consultar uma semana."
                },
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            weeks.forEachIndexed { index, week ->
                val count = totals[index]
                val isSelected = index == safeIndex
                val height by animateDpAsState(
                    targetValue = (12 + (count.toFloat() / maximum * 91)).dp,
                    animationSpec = tween(durationMillis = 220),
                    label = "altura-semana"
                )
                val color by animateColorAsState(
                    targetValue = if (isSelected) MyDeckDesign.ink
                        else if (MyDeckDesign.darkMode) Color(0xFF696969) else Color(0xFFB7B7B3),
                    animationSpec = tween(durationMillis = 180),
                    label = "cor-semana"
                )
                Column(
                    modifier = Modifier.width(34.dp)
                        .clickable(role = Role.Button) { selected = index }
                        .semantics {
                            contentDescription = "Semana ${index + 1}, $count revisões, " +
                                if (isSelected) "selecionada" else "toque para selecionar"
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BasicText(
                        if (isSelected) "$count" else " ",
                        style = TextStyle(
                            fontFamily = MyDeckDesign.font,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MyDeckDesign.secondary
                        )
                    )
                    Spacer(Modifier.height(7.dp))
                    Box(
                        Modifier.height(104.dp).width(24.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            Modifier.width(24.dp).height(height)
                                .background(color, RoundedCornerShape(8.dp))
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    BasicText(
                        if (index % 2 == 0 || index == weeks.lastIndex)
                            dateFormat.format(Date(week.first())) else " ",
                        style = TextStyle(
                            fontFamily = MyDeckDesign.font, fontSize = 9.sp,
                            color = MyDeckDesign.secondary
                        )
                    )
                }
            }
        }
        Spacer(Modifier.height(28.dp))
        HistoryHeader("SEMANA SELECIONADA")
        Spacer(Modifier.height(10.dp))
        HistoryText(
            "${dateFormat.format(Date(selectedWeek.first()))} – " +
                "${dateFormat.format(Date(selectedWeek.last()))} · " +
                "$selectedTotal revisões · " +
                "${selectedWeek.count { (daily[it] ?: 0) > 0 }} dias ativos"
        )
        Spacer(Modifier.height(10.dp))
        HistoryText(
            if (totalReviews == 0) "Ainda não há registros de estudo neste período."
            else "Toque em outra barra para comparar as semanas."
        )
    }
}
