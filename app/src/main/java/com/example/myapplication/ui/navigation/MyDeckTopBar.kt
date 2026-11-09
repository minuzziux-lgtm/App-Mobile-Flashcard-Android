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
import com.example.myapplication.ui.design.MyDeckLocale
import com.example.myapplication.ui.design.MyDeckDimensions

@Composable
fun MyDeckTopBar(
    role: String,
    onLogout: () -> Unit,
    onProfile: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MyDeckDesign.background)
            .heightIn(min = MyDeckDimensions.minimumTouchHeight),
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onProfile != null) {
                BasicText(
                    text = MyDeckLocale.text("Perfil", "Profile"),
                    modifier = Modifier.clickable(role = Role.Button, onClick = onProfile)
                        .padding(horizontal = 16.dp, vertical = 18.dp),
                    style = TextStyle(fontFamily = MyDeckDesign.font, fontSize = 12.sp,
                        fontWeight = FontWeight.Medium, color = MyDeckDesign.secondary)
                )
            }
            BasicText(
            text = MyDeckLocale.text("Sair", "Exit"),
            modifier = Modifier
                .clickable(role = Role.Button, onClick = onLogout)
                .padding(horizontal = 16.dp, vertical = 18.dp),
            style = TextStyle(
                fontFamily = MyDeckDesign.font,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MyDeckDesign.secondary
            )
            )
        }
    }
}
