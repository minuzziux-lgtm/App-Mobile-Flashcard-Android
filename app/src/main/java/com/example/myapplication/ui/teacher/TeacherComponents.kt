package com.example.myapplication.ui.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.design.MyDeckDesign

internal fun teacherStyle(size: Int, weight: FontWeight, color: Color) = TextStyle(
    fontFamily = MyDeckDesign.font,
    fontSize = size.sp,
    fontWeight = weight,
    color = color
)

@Composable
internal fun TeacherLabel(text: String) {
    BasicText(text, style = teacherStyle(11, FontWeight.SemiBold, MyDeckDesign.ink))
}

@Composable
internal fun TeacherBody(text: String) {
    BasicText(
        text,
        style = teacherStyle(14, FontWeight.Normal, MyDeckDesign.secondary)
            .copy(lineHeight = 23.sp)
    )
}

@Composable
internal fun TeacherTitle(text: String) {
    BasicText(
        text,
        style = teacherStyle(39, FontWeight.SemiBold, MyDeckDesign.ink)
            .copy(lineHeight = 46.sp, letterSpacing = (-1.4).sp)
    )
}

@Composable
internal fun TeacherDivider() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE3E3E1)))
}

@Composable
internal fun TeacherMetric(label: String, count: Int, modifier: Modifier = Modifier) {
    Column(modifier) {
        BasicText(count.toString(), style = teacherStyle(36, FontWeight.SemiBold, MyDeckDesign.ink))
        Spacer(Modifier.height(8.dp))
        BasicText(label, style = teacherStyle(12, FontWeight.Normal, MyDeckDesign.secondary))
    }
}

@Composable
internal fun TeacherEmpty(message: String) {
    Column {
        Spacer(Modifier.height(13.dp))
        TeacherBody(message)
        Spacer(Modifier.height(22.dp))
    }
}

@Composable
internal fun TeacherItemRow(title: String, detail: String, onClick: (() -> Unit)? = null) {
    TeacherDivider()
    Column(
        Modifier.fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(vertical = 17.dp)
    ) {
        BasicText(title, style = teacherStyle(16, FontWeight.Medium, MyDeckDesign.ink))
        Spacer(Modifier.height(6.dp))
        TeacherBody(detail)
    }
}

@Composable
internal fun TeacherSelectionRow(title: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth()
            .padding(bottom = 8.dp)
            .border(1.dp, if (isSelected) MyDeckDesign.ink else Color(0xFFDADADA), CircleShape)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .semantics { selected = isSelected }
            .padding(horizontal = 17.dp, vertical = 13.dp)
    ) {
        BasicText(
            title,
            style = teacherStyle(13, if (isSelected) FontWeight.SemiBold else FontWeight.Normal, MyDeckDesign.ink)
        )
    }
}

@Composable
internal fun TeacherInput(label: String, hint: String, value: String, onChange: (String) -> Unit) {
    Column {
        BasicText(label, style = teacherStyle(13, FontWeight.Medium, MyDeckDesign.ink))
        Spacer(Modifier.height(8.dp))
        BasicTextField(
            value = value,
            onValueChange = onChange,
            textStyle = teacherStyle(15, FontWeight.Normal, MyDeckDesign.ink),
            cursorBrush = SolidColor(MyDeckDesign.ink),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth().heightIn(min = 45.dp),
            decorationBox = { field ->
                Column {
                    Box(Modifier.fillMaxWidth().heightIn(min = 44.dp), contentAlignment = Alignment.CenterStart) {
                        if (value.isBlank()) {
                            BasicText(hint, style = teacherStyle(15, FontWeight.Normal, Color(0xFF888888)))
                        }
                        field()
                    }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(MyDeckDesign.line))
                }
            }
        )
    }
}
