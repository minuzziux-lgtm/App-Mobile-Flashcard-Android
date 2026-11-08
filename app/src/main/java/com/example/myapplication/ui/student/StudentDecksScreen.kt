package com.example.myapplication.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.demo.DemoCard
import com.example.myapplication.demo.DemoCardType
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
    val classDecks = decks.filter {
        selectedClass != null && selectedClass.id in it.assignedClassIds
    }
    val selectedDeck = classDecks.firstOrNull { it.id == selectedDeckId }
    val uriHandler = LocalUriHandler.current

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
                            Divider()
                            Spacer(Modifier.height(19.dp))
                            SectionLabel(card.type.label.uppercase())
                            Spacer(Modifier.height(10.dp))
                            BasicText(
                                card.question,
                                style = TextStyle(
                                    fontFamily = MyDeckDesign.font,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MyDeckDesign.ink
                                )
                            )
                            Spacer(Modifier.height(8.dp))
                            val details = when (card.type) {
                                DemoCardType.MULTIPLE_CHOICE ->
                                    card.options.size.toString() + " alternativas"
                                DemoCardType.REVERSIBLE ->
                                    listOf(card.frontLanguage, card.backLanguage)
                                        .filter(String::isNotBlank)
                                        .joinToString(" ↔ ")
                                        .ifBlank { "Cartão reversível" }
                                DemoCardType.CLOZE -> "Complete a lacuna durante o estudo"
                                DemoCardType.OPEN_ENDED -> "Resposta livre, sem gabarito"
                                DemoCardType.BASIC -> "Pergunta e resposta"
                            }
                            ScreenText(details)
                            if (card.description.isNotBlank()) {
                                Spacer(Modifier.height(8.dp))
                                ScreenText(card.description)
                            }
                            if (card.tags.isNotEmpty()) {
                                Spacer(Modifier.height(8.dp))
                                ScreenText("Tags: " + card.tags.joinToString(", "))
                            }
                            card.mediaLink?.let { material ->
                                Spacer(Modifier.height(8.dp))
                                BasicText(
                                    "Abrir " + material.type.label.lowercase(),
                                    modifier = Modifier
                                        .clickable(role = Role.Button) { uriHandler.openUri(material.url) }
                                        .padding(vertical = 12.dp),
                                    style = TextStyle(
                                        fontFamily = MyDeckDesign.font,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MyDeckDesign.ink
                                    )
                                )
                            }
                            Spacer(Modifier.height(20.dp))
                        }
                        Spacer(Modifier.height(12.dp))
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
            fontFamily = MyDeckDesign.font,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MyDeckDesign.ink
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
            fontFamily = MyDeckDesign.font,
            fontSize = 14.sp,
            lineHeight = 22.sp,
            color = MyDeckDesign.secondary
        )
    )
}
