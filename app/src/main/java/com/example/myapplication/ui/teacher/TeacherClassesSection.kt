package com.example.myapplication.ui.teacher

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
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
    onOpenClass: (String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var copiedCode by rememberSaveable { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    Column {
        TeacherTitle("Turmas.")
        Spacer(androidx.compose.ui.Modifier.height(14.dp))
        TeacherBody("Crie turmas e compartilhe o código com o aluno nesta prévia local.")
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
                TeacherDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(role = Role.Button) { onOpenClass(classroom.id) }
                            .padding(vertical = 17.dp)
                    ) {
                        BasicText(
                            classroom.name,
                            style = teacherStyle(16, FontWeight.Medium, MyDeckDesign.ink)
                        )
                        Spacer(Modifier.height(6.dp))
                        TeacherBody("Código: " + classroom.joinCode + "  ·  Abrir decks")
                    }
                    BasicText(
                        if (copiedCode == classroom.joinCode) "Copiado" else "Copiar",
                        modifier = Modifier
                            .clickable(role = Role.Button) {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE)
                                        as ClipboardManager
                                clipboard.setPrimaryClip(
                                    ClipData.newPlainText("Código da turma", classroom.joinCode)
                                )
                                copiedCode = classroom.joinCode
                            }
                            .padding(horizontal = 12.dp, vertical = 18.dp),
                        style = teacherStyle(13, FontWeight.Medium, MyDeckDesign.ink)
                    )
                }
            }
        }
    }
}
