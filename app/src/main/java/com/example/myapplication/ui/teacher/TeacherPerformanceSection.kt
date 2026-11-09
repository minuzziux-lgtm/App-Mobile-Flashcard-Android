package com.example.myapplication.ui.teacher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.demo.DemoClass
import com.example.myapplication.demo.DemoDeck
import com.example.myapplication.demo.DemoParticipant
import com.example.myapplication.study.StudyReview
import com.example.myapplication.study.summarizeReviews
import com.example.myapplication.study.difficultTags
import com.example.myapplication.study.filterReviewsByPeriod
import java.util.Calendar

/** Dashboard baseado somente em eventos locais de demonstração. */
@Composable
internal fun TeacherPerformanceSection(
    reviews: List<StudyReview>,
    classes: List<DemoClass>,
    decks: List<DemoDeck>,
    participants: List<DemoParticipant>,
    cloudMode: Boolean = false
) {
    var student by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedYear by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectedMonth by rememberSaveable { mutableStateOf<Int?>(null) }
    val visibleReviews = filterReviewsByPeriod(reviews, selectedYear, selectedMonth)
        .filter { student == null || it.studentId == student }
    val availableYears = reviews.map { review ->
        Calendar.getInstance().apply { timeInMillis = review.reviewedAtEpochMillis }.get(Calendar.YEAR)
    }.distinct().sortedDescending()
    val summary = summarizeReviews(visibleReviews)
    TeacherTitle("Desempenho.")
    Spacer(Modifier.height(14.dp))
    TeacherBody(
        if (cloudMode) "Resultados dos alunos matriculados, carregados do Firestore."
        else "Dados locais deste aparelho. O vínculo com usuários reais será feito no Firebase."
    )
    Spacer(Modifier.height(38.dp))
    TeacherDivider()
    Spacer(Modifier.height(20.dp))
    TeacherLabel("PERÍODO")
    Spacer(Modifier.height(12.dp))
    TeacherSelectionRow("Todos os anos", selectedYear == null) {
        selectedYear = null
        selectedMonth = null
    }
    availableYears.forEach { year ->
        TeacherSelectionRow(year.toString(), selectedYear == year) {
            selectedYear = year
            selectedMonth = null
        }
    }
    if (selectedYear != null) {
        val months = listOf("Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez")
        TeacherSelectionRow("Todos os meses", selectedMonth == null) { selectedMonth = null }
        months.forEachIndexed { index, name ->
            TeacherSelectionRow(name, selectedMonth == index) {
                selectedMonth = if (selectedMonth == index) null else index
            }
        }
    }
    Spacer(Modifier.height(30.dp))
    TeacherLabel("01 / RESUMO")
    Spacer(Modifier.height(22.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        TeacherMetric("Revisões", summary.total, Modifier.weight(1f))
        TeacherMetric("Acertos", summary.correct, Modifier.weight(1f))
        TeacherMetric("Taxa (%)", summary.accuracyPercent, Modifier.weight(1f))
    }
    Spacer(Modifier.height(35.dp))
    TeacherDivider()
    Spacer(Modifier.height(20.dp))
    TeacherLabel("02 / TURMAS")
    Spacer(Modifier.height(16.dp))
    if (classes.isEmpty()) TeacherEmpty("Nenhuma turma disponível.")
    classes.forEach { classroom ->
        val ownedDeckIds = decks.filter { classroom.id in it.assignedClassIds }.map { it.id }.toSet()
        val classReviews = visibleReviews.filter { it.classId == classroom.id || (it.classId == null && it.deckId in ownedDeckIds) }
        val part = summarizeReviews(classReviews)
        TeacherItemRow(classroom.name, "${part.total} revisões · ${part.accuracyPercent}% de acertos")
    }
    Spacer(Modifier.height(28.dp))
    TeacherDivider()
    Spacer(Modifier.height(18.dp))
    TeacherLabel("03 / ESTUDANTES")
    Spacer(Modifier.height(15.dp))
    TeacherSelectionRow("Todos os estudantes", student == null) { student = null }
    val studentIds = (
        reviews.map { it.studentId } +
            participants.map { if (cloudMode) it.id else it.username }
    ).distinct().sorted()
    if (studentIds.isEmpty()) TeacherEmpty("Não há participantes com atividade.")
    studentIds.forEach { id ->
        val studentSummary = summarizeReviews(reviews.filter { it.studentId == id })
        val name = if (cloudMode) participants.firstOrNull { it.id == id }?.username ?: id else id
        TeacherSelectionRow("$name · ${studentSummary.total} revisões", student == id) {
            student = if (student == id) null else id
        }
    }
    Spacer(Modifier.height(28.dp))
    TeacherDivider()
    Spacer(Modifier.height(18.dp))
    TeacherLabel("04 / DIFICULDADE POR TAG")
    Spacer(Modifier.height(15.dp))
    val hardTags = difficultTags(visibleReviews)
    if (hardTags.isEmpty()) TeacherEmpty("Não há respostas avaliadas com tags.")
    hardTags.forEach { tag ->
        TeacherItemRow("#${tag.tag}", "${tag.mistakes} erros em ${tag.graded} avaliações · ${tag.errorPercent}% de erros")
    }
}
