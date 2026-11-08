package com.example.myapplication.ui.teacher

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.design.MyDeckButton
import com.example.myapplication.ui.design.MyDeckButtonVariant

@Composable
internal fun TeacherTagsSection(
    tags: List<String>,
    onAdd: (String) -> Unit,
    onRename: (String, String) -> Unit,
    onDelete: (String) -> Unit
) {
    var newName by rememberSaveable { mutableStateOf("") }
    var editing by rememberSaveable { mutableStateOf<String?>(null) }
    var editName by rememberSaveable { mutableStateOf("") }
    var deleting by rememberSaveable { mutableStateOf<String?>(null) }
    TeacherLabel("06  /  TAGS REUTILIZÁVEIS  ·  " + tags.size)
    Spacer(Modifier.height(15.dp))
    TeacherInput("Nova tag", "Ex.: matemática", newName) { newName = it }
    Spacer(Modifier.height(12.dp))
    val exists = tags.any { it.equals(newName.trim(), ignoreCase = true) }
    MyDeckButton("Criar tag", MyDeckButtonVariant.DARK,
        onClick = if (newName.isNotBlank() && newName.trim().length <= 40 && !exists) ({
            onAdd(newName)
            newName = ""
        }) else null
    )
    Spacer(Modifier.height(19.dp))
    tags.forEach { tag ->
        TeacherDivider()
        TeacherItemRow(tag, "Disponível em todos os cartões da demonstração")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TeacherAction("Editar") {
                editing = tag
                deleting = null
                editName = tag
            }
            TeacherAction("Excluir") {
                deleting = tag
                editing = null
            }
        }
        if (editing == tag) {
            TeacherInput("Novo nome", "Tag", editName) { editName = it }
            Spacer(Modifier.height(12.dp))
            val duplicate = tags.any { it.equals(editName.trim(), ignoreCase = true) && it != tag }
            MyDeckButton("Salvar", MyDeckButtonVariant.DARK,
                onClick = if (editName.isNotBlank() && editName.trim().length <= 40 && !duplicate) ({
                    onRename(tag, editName)
                    editing = null
                }) else null
            )
        }
        if (deleting == tag) {
            TeacherConfirmDelete(
                "Excluir esta tag também a removerá dos cartões que a utilizam.",
                onConfirm = { onDelete(tag); deleting = null },
                onCancel = { deleting = null }
            )
        }
        Spacer(Modifier.height(14.dp))
    }
}
