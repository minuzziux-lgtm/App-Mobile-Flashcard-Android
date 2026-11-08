package com.example.myapplication.ui.teacher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.example.myapplication.demo.DemoCard
import com.example.myapplication.demo.DemoCardDraft
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
    onRenameDeck: (String, String) -> Unit,
    onDeleteDeck: (String) -> Unit,
    onSelectDeck: (String) -> Unit,
    onAddCard: (String, DemoCardDraft) -> Unit,
    onOpenClasses: () -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var editingDeckId by rememberSaveable { mutableStateOf<String?>(null) }
    var deletingDeckId by rememberSaveable { mutableStateOf<String?>(null) }
    var editedTitle by rememberSaveable { mutableStateOf("") }
    val selectedClass = classes.firstOrNull { it.id == selectedClassId } ?: classes.firstOrNull()
    val classDecks = decks.filter { it.classId == selectedClass?.id }
    val selectedDeck = classDecks.firstOrNull { it.id == selectedDeckId } ?: classDecks.firstOrNull()
    val selectedCards = cards.filter { it.deckId == selectedDeck?.id }

    Column {
        TeacherTitle("Decks.")
        Spacer(Modifier.height(14.dp))
        TeacherBody("Gerencie os decks e cartões das suas turmas.")
        Spacer(Modifier.height(35.dp))
        if (classes.isEmpty()) {
            TeacherEmpty("Crie uma turma antes de adicionar decks.")
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
            MyDeckButton("Criar deck", MyDeckButtonVariant.DARK,
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
                    TeacherDivider()
                    Spacer(Modifier.height(14.dp))
                    TeacherSelectionRow(deck.title, deck.id == selectedDeck?.id) { onSelectDeck(deck.id) }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        TeacherAction("Editar") {
                            editingDeckId = deck.id
                            deletingDeckId = null
                            editedTitle = deck.title
                        }
                        TeacherAction("Excluir") {
                            deletingDeckId = deck.id
                            editingDeckId = null
                        }
                    }
                    if (editingDeckId == deck.id) {
                        TeacherInput("Novo título", "Nome do deck", editedTitle) { editedTitle = it }
                        Spacer(Modifier.height(12.dp))
                        MyDeckButton("Salvar título", MyDeckButtonVariant.DARK,
                            onClick = if (editedTitle.isNotBlank()) ({
                                onRenameDeck(deck.id, editedTitle.trim())
                                editingDeckId = null
                            }) else null
                        )
                        Spacer(Modifier.height(8.dp))
                        MyDeckButton("Cancelar", MyDeckButtonVariant.LIGHT, onClick = { editingDeckId = null })
                    }
                    if (deletingDeckId == deck.id) {
                        TeacherConfirmDelete(
                            "Excluir este deck também excluirá todos os seus cartões locais.",
                            onConfirm = { onDeleteDeck(deck.id); deletingDeckId = null },
                            onCancel = { deletingDeckId = null }
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }
                Spacer(Modifier.height(38.dp))
                TeacherDivider()
                Spacer(Modifier.height(24.dp))
                TeacherLabel("04  /  ADICIONAR CARTÃO")
                Spacer(Modifier.height(12.dp))
                TeacherBody("Deck selecionado: " + selectedDeck?.title.orEmpty())
                Spacer(Modifier.height(18.dp))
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
                        TeacherItemRow(card.question, card.type.label)
                    }
                }
            }
        }
    }
}
