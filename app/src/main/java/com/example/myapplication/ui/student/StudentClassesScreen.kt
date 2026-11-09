package com.example.myapplication.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.demo.DemoClass
import com.example.myapplication.demo.JoinClassResult
import com.example.myapplication.ui.design.MyDeckButton
import com.example.myapplication.ui.design.MyDeckButtonVariant
import com.example.myapplication.ui.design.MyDeckDesign
import java.util.Locale

@Composable
fun StudentClassesScreen(
    classes: List<DemoClass>,
    deckCounts: Map<String, Int>,
    onJoinClass: (String) -> JoinClassResult,
    onOpenDecks: (String) -> Unit,
    onJoinClassAsync: ((String, (Boolean, String) -> Unit) -> Unit)? = null
) {
    var code by rememberSaveable { mutableStateOf("") }
    var feedback by rememberSaveable { mutableStateOf<String?>(null) }

    StudentSection(
        title = "Turmas.",
        description = "Entre com o código fornecido pelo professor e acesse seus decks."
    ) {
        BasicText(
            "01  /  ENTRAR EM UMA TURMA",
            style = TextStyle(
                fontFamily = MyDeckDesign.font, fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold, color = MyDeckDesign.ink
            )
        )
        Spacer(Modifier.height(18.dp))
        BasicTextField(
            value = code,
            onValueChange = { input ->
                code = input.uppercase(Locale.ROOT).filter {
                    it in "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
                }.take(8)
                feedback = null
            },
            singleLine = true,
            textStyle = TextStyle(
                fontFamily = MyDeckDesign.font, fontSize = 17.sp,
                color = MyDeckDesign.ink, letterSpacing = 2.sp
            ),
            cursorBrush = SolidColor(MyDeckDesign.ink),
            modifier = Modifier.fillMaxWidth().heightIn(min = 49.dp),
            decorationBox = { field ->
                Column {
                    Box(Modifier.heightIn(min = 47.dp), contentAlignment = Alignment.CenterStart) {
                        if (code.isEmpty()) {
                            BasicText(
                                "Código de 8 caracteres",
                                style = TextStyle(
                                    fontFamily = MyDeckDesign.font, fontSize = 15.sp,
                                    color = Color(0xFF888888)
                                )
                            )
                        }
                        field()
                    }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(MyDeckDesign.line))
                }
            }
        )
        Spacer(Modifier.height(20.dp))
        MyDeckButton(
            "Entrar na turma", MyDeckButtonVariant.DARK,
            onClick = if (code.length == 8) ({
                if (onJoinClassAsync != null) {
                    feedback = "Validando matrícula no Firebase..."
                    onJoinClassAsync(code) { success, message ->
                        if (success) code = ""
                        feedback = message
                    }
                } else {
                    feedback = when (val result = onJoinClass(code)) {
                        is JoinClassResult.Joined -> {
                            code = ""
                            "Você entrou na turma " + result.classroom.name + "."
                        }
                        JoinClassResult.AlreadyJoined -> "Você já participa desta turma."
                        JoinClassResult.NotFound -> "Código não encontrado nesta prévia."
                    }
                }
            }) else null
        )
        if (feedback != null) {
            Spacer(Modifier.height(12.dp))
            BasicText(
                feedback.orEmpty(),
                style = TextStyle(
                    fontFamily = MyDeckDesign.font, fontSize = 13.sp,
                    lineHeight = 19.sp, color = MyDeckDesign.secondary
                )
            )
        }
        Spacer(Modifier.height(48.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE3E3E1)))
        Spacer(Modifier.height(24.dp))
        BasicText(
            "02  /  MINHAS TURMAS  ·  " + classes.size,
            style = TextStyle(
                fontFamily = MyDeckDesign.font, fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold, color = MyDeckDesign.ink
            )
        )
        Spacer(Modifier.height(17.dp))
        if (classes.isEmpty()) {
            BasicText(
                "Você ainda não entrou em nenhuma turma.",
                style = TextStyle(
                    fontFamily = MyDeckDesign.font, fontSize = 14.sp,
                    lineHeight = 22.sp, color = MyDeckDesign.secondary
                )
            )
        } else {
            classes.forEach { classroom ->
                val count = deckCounts[classroom.id] ?: 0
                StudentSectionRow(
                    classroom.name,
                    count.toString() + (if (count == 1) " deck" else " decks"),
                    onClick = { onOpenDecks(classroom.id) }
                )
            }
        }
    }
}
