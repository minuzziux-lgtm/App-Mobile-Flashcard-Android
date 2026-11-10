package com.example.myapplication.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.design.MyDeckDesign
import com.example.myapplication.ui.design.MyDeckDimensions

@Composable
fun StudentSectionRow(title: String, subtitle: String, onClick: () -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth()
                .clickable(role = Role.Button, onClick = onClick)
                .padding(vertical = 28.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                BasicText(
                    title,
                    style = TextStyle(
                        fontFamily = MyDeckDesign.font, fontSize = 16.sp,
                        fontWeight = FontWeight.Medium, color = MyDeckDesign.ink
                    )
                )
                BasicText(
                    subtitle,
                    style = TextStyle(
                        fontFamily = MyDeckDesign.font, fontSize = 14.sp,
                        lineHeight = 21.sp, color = MyDeckDesign.secondary
                    )
                )
            }
            BasicText("›", style = TextStyle(fontSize = 24.sp, color = MyDeckDesign.secondary))
        }
    }
}

@Composable
fun StudentSection(
    title: String,
    description: String,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = MyDeckDimensions.screenPadding, end = MyDeckDimensions.screenPadding, top = MyDeckDimensions.contentTop, bottom = MyDeckDimensions.contentBottom)
    ) {
        item {
            Column {
                BasicText(
                    text = title,
                    style = TextStyle(
                        fontFamily = MyDeckDesign.font, fontSize = 36.sp, lineHeight = 44.sp,
                        fontWeight = FontWeight.SemiBold, color = MyDeckDesign.ink
                    )
                )
                Spacer(Modifier.height(20.dp))
                BasicText(
                    text = description,
                    style = TextStyle(
                        fontFamily = MyDeckDesign.font, fontSize = 14.sp, lineHeight = 23.sp,
                        color = MyDeckDesign.secondary
                    )
                )
                Spacer(Modifier.height(48.dp))
                content()
            }
        }
    }
}
