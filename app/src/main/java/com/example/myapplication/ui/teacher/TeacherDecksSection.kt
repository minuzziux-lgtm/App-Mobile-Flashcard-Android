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
    availableTags: List<String>,
    onCreateTag: (String) -> Unit,
    onRenameTag: (String, String) -> Unit,
    onDeleteTag: (String) -> Unit,
    selectedClassId: String?,
    selectedDeckId: String?,
    onSelectClass: (String) -> Unit,
    onCreateDeck: (String, String) -> Unit,
    onRenameDeck: (String, String) -> Unit,
    onAssignDeck: (String, List<String>) -> Unit,
    onDeleteDeck: (String) -> Unit,
    onSelectDeck: (String) -> Unit,
    onAddCard: (String, DemoCardDraft) -> Unit,
    onUpdateCard: (String, DemoCardDraft) -> Unit,
    onDuplicateCard: (String) -> Unit,
    onDeleteCard: (String) -> Unit,
    onOpenClasses: () -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var createAsGeneral by rememberSaveable { mutableStateOf(false) }
    var editingDeckId by rememberSaveable { mutableStateOf<String?>(null) }
    var deletingDeckId by rememberSaveable { mutableStateOf<String?>(null) }
    var editedTitle by rememberSaveable { mutableStateOf("") }
    var editingCardId by rememberSaveable { mutableStateOf<String?>(null) }
    var deletingCardId by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedClass = classes.firstOrNull { it.id == selectedClassId } ?: classes.firstOrNull()
    val classDecks = decks.filter { deck ->
        deck.classId.isBlank() || deck.classId == selectedClass?.id ||
            (selectedClass != null && selectedClass.id in deck.assignedClassIds)
    }
    val selectedDeck = classDecks.firstOrNull { it.id == selectedDeckId } ?: classDecks.firstOrNull()
    val selectedCards = cards.filter { it.deckId == selectedDeck?.id }

    Column {
        TeacherTitle("Decks.")
        Spacer(Modifier.height(14.dp))
        TeacherBody("Gerencie os decks e cartões das suas turmas.")
        Spacer(Modifier.height(35.dp))
        if (classes.isEmpty() && decks.isEmpty()) {
            TeacherBody("Crie um deck geral agora e atribua turmas quando estiverem disponíveis.")
            Spacer(Modifier.height(18.dp))
            TeacherInput("Nome do deck geral", "Ex.: Inglês básico", title) { title = it }
            Spacer(Modifier.height(15.dp))
            MyDeckButton("Criar deck geral", MyDeckButtonVariant.DARK,
                onClick = if (title.isNotBlank()) ({
                    onCreateDeck("", title.trim())
                    title = ""
                }) else null
            )
            Spacer(Modifier.height(12.dp))
            MyDeckButton("Criar turma", MyDeckButtonVariant.LIGHT, onClick = onOpenClasses)
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
            Spacer(Modifier.height(12.dp))
            TeacherSelectionRow("Deck geral", createAsGeneral || classes.isEmpty()) {
                createAsGeneral = true
            }
            if (classes.isNotEmpty()) {
                TeacherSelectionRow("Da turma selecionada", !createAsGeneral) {
                    createAsGeneral = false
                }
            }
            Spacer(Modifier.height(18.dp))
            TeacherInput("Nome do deck", "Ex.: Frações", title) { title = it }
            Spacer(Modifier.height(20.dp))
            MyDeckButton("Criar deck", MyDeckButtonVariant.DARK,
                onClick = if (title.isNotBlank() && (createAsGeneral || classes.isEmpty() || selectedClass != null)) ({
                    val classId = if (createAsGeneral || classes.isEmpty()) "" else selectedClass?.id.orEmpty()
                    onCreateDeck(classId, title.trim())
                    title = ""
                }) else null
            )
            Spacer(Modifier.height(45.dp))
            TeacherDivider()
            Spacer(Modifier.height(24.dp))
            TeacherLabel("03  /  DECKS DISPONÍVEIS  ·  " + classDecks.size)
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
                TeacherLabel(if (editingCardId == null) "04  /  ADICIONAR CARTÃO" else "04  /  EDITAR CARTÃO")
                Spacer(Modifier.height(12.dp))
                TeacherBody("Deck selecionado: " + selectedDeck?.title.orEmpty())
                Spacer(Modifier.height(18.dp))
                if (selectedDeck != null && classes.isNotEmpty()) {
                    TeacherLabel("ATRIBUIR DECK A TURMAS")
                    Spacer(Modifier.height(8.dp))
                    TeacherBody("Somente alunos matriculados nas turmas selecionadas verão este deck.")
                    Spacer(Modifier.height(12.dp))
                    classes.forEach { classroom ->
                        val isAssigned = classroom.id in selectedDeck.assignedClassIds
                        TeacherSelectionRow(classroom.name, isAssigned) {
                            val ids = if (isAssigned) {
                                selectedDeck.assignedClassIds.filterNot { it == classroom.id }
                            } else selectedDeck.assignedClassIds + classroom.id
                            onAssignDeck(selectedDeck.id, ids)
                        }
                    }
                    Spacer(Modifier.height(21.dp))
                }
                if (selectedDeck != null) {
                    if (editingCardId != null) {
                        TeacherAction("Cancelar edição") { editingCardId = null }
                    }
                    val existing = selectedCards.firstOrNull { it.id == editingCardId }
                    androidx.compose.runtime.key(selectedDeck.id, existing?.id) {
                        TeacherCardEditor(initialCard = existing, availableTags = availableTags) { draft ->
                            if (existing == null) onAddCard(selectedDeck.id, draft)
                            else {
                                onUpdateCard(existing.id, draft)
                                editingCardId = null
                            }
                        }
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
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            TeacherAction("Editar") {
                                editingCardId = card.id
                                deletingCardId = null
                            }
                            TeacherAction("Duplicar") { onDuplicateCard(card.id) }
                            TeacherAction("Excluir") {
                                deletingCardId = card.id
                                editingCardId = null
                            }
                        }
                        if (deletingCardId == card.id) {
                            TeacherConfirmDelete(
                                "Excluir este cartão do deck? A ação é irreversível.",
                                onConfirm = { onDeleteCard(card.id); deletingCardId = null },
                                onCancel = { deletingCardId = null }
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
            Spacer(Modifier.height(42.dp))
            TeacherDivider()
            Spacer(Modifier.height(24.dp))
            TeacherTagsSection(
                availableTags, onCreateTag, onRenameTag, onDeleteTag
            )
        }
    }
}
