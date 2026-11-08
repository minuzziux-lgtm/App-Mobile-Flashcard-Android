package com.example.myapplication.ui.teacher

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.demo.DemoCard
import com.example.myapplication.demo.DemoCardDraft
import com.example.myapplication.demo.DemoClass
import com.example.myapplication.demo.DemoDeck
import com.example.myapplication.ui.design.MyDeckButton
import com.example.myapplication.ui.design.MyDeckButtonVariant

@Composable
internal fun TeacherDecksSection(
    classes: List<DemoClass>,
    decks: List<DemoDeck>,
    cards: List<DemoCard>,
    selectedClassId: String?,
    selectedDeckId: String?,
    onSelectClass: (String) -> Unit,
    onCreateDeck: (String, String) -> Unit,
    onSelectDeck: (String) -> Unit,
    onAddCard: (String, DemoCardDraft) -> Unit,
    onOpenClasses: () -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    val selectedClass = classes.firstOrNull { it.id == selectedClassId } ?: classes.firstOrNull()
    val classDecks = decks.filter { it.classId == selectedClass?.id }
    val selectedDeck = classDecks.firstOrNull { it.id == selectedDeckId } ?: classDecks.firstOrNull()
    val selectedCards = cards.filter { it.deckId == selectedDeck?.id }

    Column {
        TeacherTitle("Decks.")
        Spacer(Modifier.height(14.dp))
        TeacherBody("Cada deck pertence a uma turma e reúne os flashcards daquela matéria.")
        Spacer(Modifier.height(35.dp))
        if (classes.isEmpty()) {
            TeacherEmpty("Crie uma turma antes de adicionar decks.")
            Spacer(Modifier.height(20.dp))
            MyDeckButton("Ir para turmas", MyDeckButtonVariant.DARK, onClick = onOpenClasses)
        } else {
            TeacherLabel("01  /  SELECIONAR TURMA")
            Spacer(Modifier.height(14.dp))
            classes.forEach { classroom ->
                TeacherSelectionRow(classroom.name, classroom.id == selectedClass?.id) {
                    onSelectClass(classroom.id)
                }
            }
            Spacer(Modifier.height(27.dp))
            TeacherLabel("02  /  NOVO DECK")
            Spacer(Modifier.height(18.dp))
            TeacherInput("Nome do deck", "Ex.: Frações", title) { title = it }
            Spacer(Modifier.height(20.dp))
            MyDeckButton(
                "Criar deck", MyDeckButtonVariant.DARK,
                onClick = if (title.isNotBlank() && selectedClass != null) ({
                    onCreateDeck(selectedClass.id, title.trim())
                    title = ""
                }) else null
            )
            Spacer(Modifier.height(45.dp))
            TeacherDivider()
            Spacer(Modifier.height(24.dp))
            TeacherLabel("03  /  DECKS DA TURMA  ·  " + classDecks.size)
            Spacer(Modifier.height(14.dp))
            if (classDecks.isEmpty()) {
                TeacherEmpty("Nenhum deck criado para esta turma.")
            } else {
                classDecks.forEach { deck ->
                    TeacherSelectionRow(deck.title, deck.id == selectedDeck?.id) {
                        onSelectDeck(deck.id)
                    }
                }
                Spacer(Modifier.height(45.dp))
                TeacherDivider()
                Spacer(Modifier.height(24.dp))
                TeacherLabel("04  /  ADICIONAR CARTÃO")
                Spacer(Modifier.height(12.dp))
                TeacherBody("Deck selecionado: " + selectedDeck?.title.orEmpty())
                Spacer(Modifier.height(19.dp))
                if (selectedDeck != null) {
                    androidx.compose.runtime.key(selectedDeck.id) {
                        TeacherCardEditor { draft -> onAddCard(selectedDeck.id, draft) }
                    }
                }
                Spacer(Modifier.height(43.dp))
                TeacherLabel("05  /  CARTÕES  ·  " + selectedCards.size)
                Spacer(Modifier.height(12.dp))
                if (selectedCards.isEmpty()) {
                    TeacherEmpty("Este deck ainda não possui cartões.")
                } else {
                    selectedCards.forEach { card ->
                        val details = buildString {
                            append(card.type.label)
                            if (card.answer.isNotBlank()) append("  ·  Resposta: " + card.answer)
                            if (card.mediaLink != null) append("  ·  " + card.mediaLink.type.label + " por link")
                        }
                        TeacherItemRow(card.question, details)
                    }
                }
            }
        }
    }
}
