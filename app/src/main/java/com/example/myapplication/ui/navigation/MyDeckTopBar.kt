package com.example.myapplication.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
fun MyDeckTopBar(
    role: String,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .heightIn(min = 56.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        BasicText(
            text = role,
            style = TextStyle(
                fontFamily = MyDeckDesign.font,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MyDeckDesign.ink
            )
        )
        BasicText(
            text = "Sair",
            modifier = Modifier
                .clickable(role = Role.Button, onClick = onLogout)
                .padding(horizontal = 8.dp, vertical = 16.dp),
            style = TextStyle(
                fontFamily = MyDeckDesign.font,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MyDeckDesign.secondary
            )
        )
    }
}
