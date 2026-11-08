package com.example.myapplication.ui.teacher

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.design.MyDeckButton
import com.example.myapplication.ui.design.MyDeckButtonVariant
import com.example.myapplication.ui.design.MyDeckDesign

@Composable
internal fun TeacherAction(label: String, onClick: () -> Unit) {
    BasicText(
        label,
        modifier = Modifier.clickable(role = Role.Button, onClick = onClick)
            .padding(end = 12.dp, top = 14.dp, bottom = 14.dp),
        style = teacherStyle(12, FontWeight.Medium, MyDeckDesign.ink)
    )
}

@Composable
internal fun TeacherConfirmDelete(description: String, onConfirm: () -> Unit, onCancel: () -> Unit) {
    Column {
        Spacer(Modifier.height(8.dp))
        TeacherBody(description)
        Spacer(Modifier.height(14.dp))
        MyDeckButton("Confirmar exclusão", MyDeckButtonVariant.DARK, onClick = onConfirm)
        Spacer(Modifier.height(8.dp))
        MyDeckButton("Cancelar", MyDeckButtonVariant.LIGHT, onClick = onCancel)
    }
}
