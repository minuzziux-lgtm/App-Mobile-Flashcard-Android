package com.example.myapplication.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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

@Composable
fun StudentSectionRow(title: String, subtitle: String, onClick: () -> Unit) {
    Column {
        Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE8E8E8)))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button, onClick = onClick)
                .padding(vertical = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                BasicText(
                    title,
                    style = TextStyle(
                        fontFamily = MyDeckDesign.font,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MyDeckDesign.ink
                    )
                )
                BasicText(
                    subtitle,
                    style = TextStyle(
                        fontFamily = MyDeckDesign.font,
                        fontSize = 12.sp,
                        color = MyDeckDesign.secondary
                    )
                )
            }
            BasicText(
                "›",
                style = TextStyle(fontSize = 24.sp, color = MyDeckDesign.secondary)
            )
        }
    }
}

@Composable
fun StudentSection(title: String, description: String) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 27.dp, vertical = 28.dp)
    ) {
        item {
            BasicText(
                text = "MY-DECK",
                style = TextStyle(
                    fontFamily = MyDeckDesign.font,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MyDeckDesign.ink
                )
            )
            Spacer(Modifier.height(70.dp))
            BasicText(
                text = title,
                style = TextStyle(
                    fontFamily = MyDeckDesign.font,
                    fontSize = 35.sp,
                    lineHeight = 43.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MyDeckDesign.ink
                )
            )
            Spacer(Modifier.height(17.dp))
            BasicText(
                text = description,
                style = TextStyle(
                    fontFamily = MyDeckDesign.font,
                    fontSize = 14.sp,
                    lineHeight = 23.sp,
                    color = MyDeckDesign.secondary
                )
            )
        }
    }
}
