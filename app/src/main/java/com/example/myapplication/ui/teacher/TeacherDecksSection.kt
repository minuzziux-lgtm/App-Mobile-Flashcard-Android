package com.example.myapplication.ui.teacher

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
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
    onOpenClasses: () -> Unit,
    cloudMode: Boolean = false
) {
    var title by rememberSaveable { mutableStateOf("") }
    var createAsGeneral by rememberSaveable { mutableStateOf(false) }
    var editingDeckId by rememberSaveable { mutableStateOf<String?>(null) }
    var deletingDeckId by rememberSaveable { mutableStateOf<String?>(null) }
    var editedTitle by rememberSaveable { mutableStateOf("") }
    var editingCardId by rememberSaveable { mutableStateOf<String?>(null) }
    var deletingCardId by rememberSaveable { mutableStateOf<String?>(null) }
    var showUnassignedOnly by rememberSaveable { mutableStateOf(false) }
    var editingAssignmentsId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingClassIds by rememberSaveable { mutableStateOf(arrayListOf<String>()) }
    var savedAssignmentDeckId by rememberSaveable { mutableStateOf<String?>(null) }
    var savedAssignmentMessage by rememberSaveable { mutableStateOf<String?>(null) }

    // Atribuições efetivas (não a turma de origem) determinam quais alunos verão um deck.
    val selectedClass = classes.firstOrNull { it.id == selectedClassId }
    val classDecks = when {
        selectedClass != null -> decks.filter { selectedClass.id in it.assignedClassIds }
        showUnassignedOnly -> decks.filter { it.assignedClassIds.isEmpty() }
        else -> decks
    }
    val selectedDeck = classDecks.firstOrNull { it.id == selectedDeckId }
    val selectedCards = cards.filter { it.deckId == selectedDeck?.id }

    Column {
        TeacherTitle("Decks.")
        Spacer(Modifier.height(14.dp))
        TeacherBody("Cada deck guarda seus próprios flashcards e pode ser atribuído a uma ou várias turmas.")
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
            TeacherLabel("01  /  FILTRAR DECKS")
            Spacer(Modifier.height(14.dp))
            TeacherSelectionRow(
                "Todos os decks · ${decks.size}",
                selectedClass == null && !showUnassignedOnly
            ) {
                showUnassignedOnly = false
                editingAssignmentsId = null
                onSelectClass("")
            }
            TeacherSelectionRow(
                "Sem turma · ${decks.count { it.assignedClassIds.isEmpty() }}",
                selectedClass == null && showUnassignedOnly
            ) {
                showUnassignedOnly = true
                editingAssignmentsId = null
                onSelectClass("")
            }
            classes.forEach { classroom ->
                val number = decks.count { classroom.id in it.assignedClassIds }
                TeacherSelectionRow("${classroom.name} · $number decks", classroom.id == selectedClass?.id) {
                    showUnassignedOnly = false
                    editingAssignmentsId = null
                    onSelectClass(classroom.id)
                }
            }
            Spacer(Modifier.height(27.dp))
            TeacherLabel("02  /  NOVO DECK")
            Spacer(Modifier.height(12.dp))
            TeacherSelectionRow("Criar sem turma (atribuir depois)", createAsGeneral || selectedClass == null) {
                createAsGeneral = true
            }
            if (selectedClass != null) {
                TeacherSelectionRow("Criar e atribuir à turma ${selectedClass.name}", !createAsGeneral) {
                    createAsGeneral = false
                }
            }
            Spacer(Modifier.height(18.dp))
            TeacherInput("Nome do deck", "Ex.: Frações", title) { title = it }
            Spacer(Modifier.height(20.dp))
            MyDeckButton("Criar deck", MyDeckButtonVariant.DARK,
                onClick = if (title.isNotBlank()) ({
                    val classId = if (createAsGeneral || selectedClass == null) "" else selectedClass.id
                    onCreateDeck(classId, title.trim())
                    showUnassignedOnly = false
                    title = ""
                }) else null
            )
            Spacer(Modifier.height(45.dp))
            TeacherDivider()
            Spacer(Modifier.height(24.dp))
            TeacherLabel("03  /  DECKS NESTE FILTRO  ·  " + classDecks.size)
            Spacer(Modifier.height(14.dp))
            if (classDecks.isEmpty()) {
                TeacherEmpty("Nenhum deck neste filtro. Selecione Todos os decks para encontrar e atribuir um deck existente.")
            } else {
                classDecks.forEach { deck ->
                    Spacer(Modifier.height(22.dp))
                    TeacherSelectionRow(deck.title, deck.id == selectedDeck?.id) {
                        onSelectDeck(deck.id)
                        editingAssignmentsId = null
                        editingCardId = null
                    }
                    val assignedNames = classes.filter { it.id in deck.assignedClassIds }.map { it.name }
                    val count = cards.count { it.deckId == deck.id }
                    TeacherBody(
                        "$count ${if (count == 1) "flashcard" else "flashcards"} · " +
                            if (assignedNames.isEmpty()) "Sem turma — não visível para alunos"
                            else "Turmas: ${assignedNames.joinToString(", ")}"
                    )
                    Spacer(Modifier.height(7.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        TeacherAction("Turmas") {
                            onSelectDeck(deck.id)
                            editingCardId = null
                            editingAssignmentsId = deck.id
                            pendingClassIds = ArrayList(deck.assignedClassIds)
                        }
                        TeacherAction("Editar") {
                            editingDeckId = deck.id
                            deletingDeckId = null
                            editedTitle = deck.title
                        }
                        if (!cloudMode) {
                            TeacherAction("Excluir") {
                                deletingDeckId = deck.id
                                editingDeckId = null
                            }
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
                    Spacer(Modifier.height(26.dp))
                }
                Spacer(Modifier.height(42.dp))
                TeacherDivider()
                Spacer(Modifier.height(24.dp))
                TeacherLabel(if (editingCardId == null) "04  /  GERENCIAR DECK" else "04  /  EDITAR CARTÃO")
                Spacer(Modifier.height(12.dp))
                if (selectedDeck == null) {
                    TeacherBody("Selecione um deck na lista acima para definir suas turmas e gerenciar os flashcards.")
                } else {
                    TeacherBody("Deck selecionado: ${selectedDeck.title}")
                    Spacer(Modifier.height(18.dp))
                    TeacherLabel("TURMAS QUE RECEBEM ESTE DECK")
                    Spacer(Modifier.height(10.dp))
                    val assignedNames = classes.filter { it.id in selectedDeck.assignedClassIds }.map { it.name }
                    TeacherBody(
                        if (assignedNames.isEmpty()) "Nenhuma turma atribuída. Os alunos não podem ver este deck."
                        else "Atribuído a: ${assignedNames.joinToString(", ")}"
                    )
                    Spacer(Modifier.height(11.dp))
                    TeacherBody("Os mesmos flashcards serão disponibilizados em todas as turmas escolhidas.")
                    AnimatedVisibility(
                        visible = savedAssignmentDeckId == selectedDeck.id && savedAssignmentMessage != null,
                        enter = fadeIn(animationSpec = tween(160)),
                        exit = fadeOut(animationSpec = tween(110))
                    ) {
                        Column {
                            Spacer(Modifier.height(12.dp))
                            TeacherBody(savedAssignmentMessage.orEmpty())
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    if (classes.isEmpty()) {
                        MyDeckButton("Criar turma para atribuir deck", MyDeckButtonVariant.LIGHT, onClick = onOpenClasses)
                    } else if (editingAssignmentsId == selectedDeck.id) {
                        TeacherBody("Selecione as turmas desejadas e confirme para salvar.")
                        Spacer(Modifier.height(12.dp))
                        classes.forEach { classroom ->
                            val chosen = classroom.id in pendingClassIds
                            TeacherSelectionRow(
                                "${classroom.name} · ${if (chosen) "Selecionada" else "Não selecionada"}",
                                chosen,
                                multiSelect = true
                            ) {
                                pendingClassIds = ArrayList(
                                    if (chosen) pendingClassIds.filterNot { it == classroom.id }
                                    else pendingClassIds + classroom.id
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        if (pendingClassIds.isEmpty()) {
                            TeacherBody("Sem turmas selecionadas: o deck ficará oculto para os alunos.")
                            Spacer(Modifier.height(12.dp))
                        }
                        MyDeckButton("Salvar atribuições", MyDeckButtonVariant.DARK, onClick = {
                            showUnassignedOnly = false
                            val selectedIds = pendingClassIds.toList()
                            onAssignDeck(selectedDeck.id, selectedIds)
                            savedAssignmentDeckId = selectedDeck.id
                            savedAssignmentMessage = if (selectedIds.isEmpty()) {
                                "Atribuições salvas. Este deck não está disponível para alunos."
                            } else {
                                "Atribuições salvas em ${selectedIds.size} turmas."
                            }
                            editingAssignmentsId = null
                        })
                        Spacer(Modifier.height(8.dp))
                        MyDeckButton("Cancelar", MyDeckButtonVariant.LIGHT, onClick = {
                            editingAssignmentsId = null
                        })
                    } else {
                        MyDeckButton("Alterar turmas", MyDeckButtonVariant.LIGHT, onClick = {
                            editingAssignmentsId = selectedDeck.id
                            pendingClassIds = ArrayList(selectedDeck.assignedClassIds)
                        })
                    }
                    Spacer(Modifier.height(30.dp))
                    TeacherDivider()
                    Spacer(Modifier.height(20.dp))
                    TeacherLabel(if (editingCardId == null) "ADICIONAR FLASHCARD" else "EDITAR FLASHCARD")
                    Spacer(Modifier.height(14.dp))
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
                    Spacer(Modifier.height(43.dp))
                    TeacherLabel("05  /  FLASHCARDS DESTE DECK  ·  " + selectedCards.size)
                    Spacer(Modifier.height(12.dp))
                    if (selectedCards.isEmpty()) {
                        TeacherEmpty("Este deck ainda não possui flashcards. Adicione um para permitir o estudo.")
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
            }
            Spacer(Modifier.height(42.dp))
            TeacherDivider()
            Spacer(Modifier.height(24.dp))
            if (cloudMode) {
                TeacherBody("Tags são associadas aos flashcards e salvas no Firebase com cada cartão.")
            } else {
                TeacherTagsSection(availableTags, onCreateTag, onRenameTag, onDeleteTag)
            }
        }
    }
}
