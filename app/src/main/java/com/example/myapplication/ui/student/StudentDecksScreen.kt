package com.example.myapplication.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.example.myapplication.demo.DemoCard
import com.example.myapplication.demo.DemoClass
import com.example.myapplication.demo.DemoDeck
import com.example.myapplication.ui.design.MyDeckDesign

@Composable
fun StudentDecksScreen(
    classes: List<DemoClass>,
    decks: List<DemoDeck>,
    cards: List<DemoCard>,
    selectedClassId: String?,
    onSelectClass: (String) -> Unit
) {
    var selectedDeckId by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedClass = classes.firstOrNull { it.id == selectedClassId } ?: classes.firstOrNull()
    val classDecks = decks.filter { it.classId == selectedClass?.id }
    val selectedDeck = classDecks.firstOrNull { it.id == selectedDeckId }

    StudentSection(
        title = "Decks.",
        description = "Consulte os decks das suas turmas e os cartões disponíveis."
    ) {
        if (classes.isEmpty()) {
            ScreenText("Entre em uma turma na aba Turmas para visualizar os decks.")
        } else {
            SectionLabel("01  /  SUAS TURMAS")
            Spacer(Modifier.height(15.dp))
            classes.forEach { classroom ->
                StudentSectionRow(
                    classroom.name,
                    if (classroom.id == selectedClass?.id) "Selecionada" else "Toque para visualizar",
                    onClick = { onSelectClass(classroom.id); selectedDeckId = null }
                )
            }
            Spacer(Modifier.height(44.dp))
            Divider()
            Spacer(Modifier.height(24.dp))
            SectionLabel("02  /  DECKS  ·  " + classDecks.size)
            Spacer(Modifier.height(16.dp))
            if (classDecks.isEmpty()) {
                ScreenText("Não há decks nesta turma.")
            } else {
                classDecks.forEach { deck ->
                    val count = cards.count { it.deckId == deck.id }
                    StudentSectionRow(
                        deck.title,
                        count.toString() + (if (count == 1) " cartão" else " cartões"),
                        onClick = { selectedDeckId = if (selectedDeckId == deck.id) null else deck.id }
                    )
                }
                if (selectedDeck != null) {
                    Spacer(Modifier.height(43.dp))
                    Divider()
                    Spacer(Modifier.height(24.dp))
                    SectionLabel("03  /  CARTÕES DE " + selectedDeck.title.uppercase())
                    Spacer(Modifier.height(16.dp))
                    val visibleCards = cards.filter { it.deckId == selectedDeck.id }
                    if (visibleCards.isEmpty()) {
                        ScreenText("Este deck ainda não possui cartões.")
                    } else {
                        visibleCards.forEach { card ->
                            StudentSectionRow(card.question, "Prévia da pergunta") {}
                        }
                        Spacer(Modifier.height(18.dp))
                        ScreenText("O modo de estudo será implementado na próxima etapa.")
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(label: String) {
    BasicText(
        label,
        style = TextStyle(
            fontFamily = MyDeckDesign.font, fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold, color = MyDeckDesign.ink
        )
    )
}

@Composable
private fun Divider() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE3E3E1)))
}

@Composable
private fun ScreenText(text: String) {
    BasicText(
        text,
        style = TextStyle(
            fontFamily = MyDeckDesign.font, fontSize = 14.sp,
            lineHeight = 22.sp, color = MyDeckDesign.secondary
        )
    )
}
