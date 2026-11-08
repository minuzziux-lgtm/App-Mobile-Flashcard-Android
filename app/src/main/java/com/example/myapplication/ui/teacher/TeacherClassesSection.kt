package com.example.myapplication.ui.teacher

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.design.MyDeckButton
import com.example.myapplication.ui.design.MyDeckButtonVariant

internal data class TeacherClass(val id: String, val name: String)

@Composable
internal fun TeacherClassesSection(
    classes: List<TeacherClass>,
    onCreateClass: (String) -> Unit,
    onOpenClass: (String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    Column {
        TeacherTitle("Turmas.")
        Spacer(androidx.compose.ui.Modifier.height(14.dp))
        TeacherBody("Crie e organize suas turmas. Os códigos de entrada serão gerados ao integrar com Firebase.")
        Spacer(androidx.compose.ui.Modifier.height(38.dp))
        TeacherLabel("01  /  NOVA TURMA")
        Spacer(androidx.compose.ui.Modifier.height(18.dp))
        TeacherInput("Nome da turma", "Ex.: Matemática — 1º ano", name) { name = it }
        Spacer(androidx.compose.ui.Modifier.height(20.dp))
        MyDeckButton(
            "Criar turma",
            MyDeckButtonVariant.DARK,
            onClick = if (name.isNotBlank()) ({
                onCreateClass(name.trim())
                name = ""
            }) else null
        )
        Spacer(androidx.compose.ui.Modifier.height(48.dp))
        TeacherDivider()
        Spacer(androidx.compose.ui.Modifier.height(24.dp))
        TeacherLabel("02  /  MINHAS TURMAS  ·  " + classes.size)
        Spacer(androidx.compose.ui.Modifier.height(15.dp))
        if (classes.isEmpty()) {
            TeacherEmpty("Nenhuma turma criada ainda.")
        } else {
            classes.forEach { classroom ->
                TeacherItemRow(classroom.name, "Abrir decks desta turma") {
                    onOpenClass(classroom.id)
                }
            }
        }
    }
}
