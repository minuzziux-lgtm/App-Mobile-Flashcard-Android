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
import com.example.myapplication.ui.design.MyDeckButton
import com.example.myapplication.ui.design.MyDeckButtonVariant
import com.example.myapplication.ui.design.MyDeckDesign

@Composable
internal fun TeacherClassesSection(
    classes: List<DemoClass>,
    onCreateClass: (String) -> Unit,
    onOpenClass: (String) -> Unit,
    onRenameClass: (String, String) -> Unit,
    onDeleteClass: (String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var copiedCode by rememberSaveable { mutableStateOf<String?>(null) }
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    var deletingId by rememberSaveable { mutableStateOf<String?>(null) }
    var editedName by rememberSaveable { mutableStateOf("") }
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
            TeacherDivider()
            Spacer(Modifier.height(15.dp))
            BasicText(classroom.name, style = teacherStyle(16, FontWeight.Medium, MyDeckDesign.ink))
            Spacer(Modifier.height(7.dp))
            TeacherBody("Código: " + classroom.joinCode)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                TeacherAction("Abrir") { onOpenClass(classroom.id) }
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
                TeacherAction("Excluir") {
                    deletingId = classroom.id
                    editingId = null
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
                    "Excluir a turma também excluirá os seus decks e cartões locais.",
                    onConfirm = { onDeleteClass(classroom.id); deletingId = null },
                    onCancel = { deletingId = null }
                )
            }
            Spacer(Modifier.height(17.dp))
        }
    }
}
