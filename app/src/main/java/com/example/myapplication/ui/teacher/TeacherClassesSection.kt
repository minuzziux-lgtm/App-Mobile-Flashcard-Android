package com.example.myapplication.ui.teacher

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.demo.DemoClass
import com.example.myapplication.demo.DemoParticipant
import com.example.myapplication.ui.design.MyDeckButton
import com.example.myapplication.ui.design.MyDeckButtonVariant
import com.example.myapplication.ui.design.MyDeckDesign

@Composable
internal fun TeacherClassesSection(
    classes: List<DemoClass>,
    decks: List<com.example.myapplication.demo.DemoDeck>,
    cards: List<com.example.myapplication.demo.DemoCard>,
    participants: List<DemoParticipant>,
    onAddParticipant: (String, String) -> Unit,
    onRemoveParticipant: (String) -> Unit,
    onCreateClass: (String) -> Unit,
    onOpenClass: (String) -> Unit,
    onRenameClass: (String, String) -> Unit,
    onDeleteClass: (String) -> Unit,
    cloudMode: Boolean = false
) {
    var name by rememberSaveable { mutableStateOf("") }
    var copiedCode by rememberSaveable { mutableStateOf<String?>(null) }
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    var deletingId by rememberSaveable { mutableStateOf<String?>(null) }
    var editedName by rememberSaveable { mutableStateOf("") }
    var expandedParticipantsId by rememberSaveable { mutableStateOf<String?>(null) }
    var participantName by rememberSaveable { mutableStateOf("") }
    var deletingParticipantId by rememberSaveable { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    Column {
        TeacherTitle("Turmas.")
        Spacer(Modifier.height(14.dp))
        TeacherBody("Organize suas turmas e compartilhe os códigos de entrada.")
        Spacer(Modifier.height(38.dp))
        TeacherLabel("01  /  NOVA TURMA")
        Spacer(Modifier.height(18.dp))
        TeacherInput("Nome da turma", "Ex.: Matemática — 1º ano", name) { name = it }
        Spacer(Modifier.height(20.dp))
        MyDeckButton("Criar turma", MyDeckButtonVariant.DARK,
            onClick = if (name.isNotBlank()) ({
                onCreateClass(name.trim())
                name = ""
            }) else null
        )
        Spacer(Modifier.height(48.dp))
        TeacherDivider()
        Spacer(Modifier.height(24.dp))
        TeacherLabel("02  /  MINHAS TURMAS  ·  " + classes.size)
        Spacer(Modifier.height(15.dp))
        if (classes.isEmpty()) TeacherEmpty("Nenhuma turma criada ainda.")
        classes.forEach { classroom ->
            Spacer(Modifier.height(28.dp))
            BasicText(classroom.name, style = teacherStyle(16, FontWeight.Medium, MyDeckDesign.ink))
            Spacer(Modifier.height(7.dp))
            TeacherBody("Código: " + classroom.joinCode)
            Spacer(Modifier.height(7.dp))
            val attributedDecks = decks.filter { classroom.id in it.assignedClassIds }
            val ids = attributedDecks.map { it.id }.toSet()
            val flashcards = cards.count { it.deckId in ids }
            TeacherBody(
                "${attributedDecks.size} decks atribuídos · $flashcards " +
                    if (flashcards == 1) "flashcard" else "flashcards"
            )
            if (attributedDecks.isNotEmpty()) {
                Spacer(Modifier.height(5.dp))
                TeacherBody("Decks: " + attributedDecks.joinToString(", ") { it.title })
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                TeacherAction("Abrir decks") { onOpenClass(classroom.id) }
                TeacherAction(if (copiedCode == classroom.joinCode) "Copiado" else "Copiar") {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Código da turma", classroom.joinCode))
                    copiedCode = classroom.joinCode
                }
                TeacherAction("Editar") {
                    editingId = classroom.id
                    deletingId = null
                    editedName = classroom.name
                }
                if (!cloudMode) {
                    TeacherAction("Excluir") {
                        deletingId = classroom.id
                        editingId = null
                    }
                }
                TeacherAction("Alunos") {
                    expandedParticipantsId = if (expandedParticipantsId == classroom.id) null else classroom.id
                    deletingParticipantId = null
                }
            }
            if (editingId == classroom.id) {
                TeacherInput("Novo nome", "Nome da turma", editedName) { editedName = it }
                Spacer(Modifier.height(12.dp))
                MyDeckButton("Salvar nome", MyDeckButtonVariant.DARK,
                    onClick = if (editedName.isNotBlank()) ({
                        onRenameClass(classroom.id, editedName.trim())
                        editingId = null
                    }) else null
                )
                Spacer(Modifier.height(8.dp))
                MyDeckButton("Cancelar", MyDeckButtonVariant.LIGHT, onClick = { editingId = null })
            }
            if (deletingId == classroom.id) {
                TeacherConfirmDelete(
                    "Excluir esta turma remove suas matrículas e atribuições. Os decks e flashcards serão preservados e poderão ser usados em outras turmas.",
                    onConfirm = {
                        onDeleteClass(classroom.id)
                        deletingId = null
                        expandedParticipantsId = null
                    },
                    onCancel = { deletingId = null }
                )
            }
            if (expandedParticipantsId == classroom.id) {
                val members = participants.filter { it.classId == classroom.id }
                Spacer(Modifier.height(16.dp))
                TeacherDivider()
                Spacer(Modifier.height(17.dp))
                TeacherLabel("PARTICIPANTES  ·  " + members.size)
                Spacer(Modifier.height(9.dp))
                TeacherBody(
                    if (cloudMode) "Matrículas reais confirmadas no Firebase. Compartilhe o código para adicionar alunos."
                    else "Participantes demonstrativos, sem contas ou matrículas reais no Firebase."
                )
                if (!cloudMode) {
                    Spacer(Modifier.height(16.dp))
                    TeacherInput("Adicionar @ de teste", "Ex.: @maria", participantName) {
                        participantName = it
                    }
                    Spacer(Modifier.height(12.dp))
                    val clean = participantName.trim().removePrefix("@").lowercase()
                    val valid = Regex("^[a-z0-9_-]{3,24}$").matches(clean)
                    val alreadyExists = members.any { it.username == "@"+clean }
                    MyDeckButton("Adicionar participante", MyDeckButtonVariant.DARK,
                        onClick = if (valid && !alreadyExists) ({
                            onAddParticipant(classroom.id, participantName)
                            participantName = ""
                        }) else null
                    )
                }
                Spacer(Modifier.height(16.dp))
                if (members.isEmpty()) {
                    TeacherEmpty(if (cloudMode) "Ainda não há alunos matriculados." else "Nenhum participante de demonstração.")
                }
                members.forEach { participant ->
                    TeacherItemRow(
                        participant.username,
                        if (cloudMode) "Aluno matriculado no Firebase"
                        else if (participant.isCurrentDemoStudent) "Perfil atual da prévia do aluno" else "Participante fictício"
                    )
                    TeacherAction("Remover") { deletingParticipantId = participant.id }
                    if (deletingParticipantId == participant.id) {
                        TeacherConfirmDelete(
                            if (cloudMode) "Remover a matrícula deste aluno no Firebase?" else "Remover este participante desta turma? Se for o aluno da prévia, sua matrícula local será removida.",
                            onConfirm = {
                                onRemoveParticipant(participant.id)
                                deletingParticipantId = null
                            },
                            onCancel = { deletingParticipantId = null }
                        )
                    }
                }
            }
            Spacer(Modifier.height(29.dp))
        }
    }
}
