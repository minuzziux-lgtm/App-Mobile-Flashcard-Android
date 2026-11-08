package com.example.myapplication.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.design.MyDeckDesign
import dev.chrisbanes.haze.HazeState

enum class StudentTab(val route: String, val label: String) {
    HOME("inicio", "Início"),
    CLASSES("turmas", "Turmas"),
    DECKS("decks", "Decks"),
    DATA("dados", "Dados")
}

data class MyDeckNavItem(val id: String, val label: String)

private val activeSurface = Brush.verticalGradient(
    listOf(Color(0xFFFFFFFF), Color(0xFFEDEDEB), Color(0xFFE5E5E3))
)

@Composable
fun MyDeckBottomBar(
    currentId: String,
    items: List<MyDeckNavItem>,
    hazeState: HazeState,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    SmokedGlassSurface(hazeState, modifier.fillMaxWidth().height(58.dp)) {
        Row(
            modifier = Modifier.fillMaxSize().padding(5.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentId == item.id
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .then(if (isSelected) Modifier.background(activeSurface, CircleShape) else Modifier)
                        .semantics {
                            selected = isSelected
                            contentDescription = item.label
                        }
                        .clickable(role = Role.Tab) { onNavigate(item.id) },
                    contentAlignment = Alignment.Center
                ) {
                    BasicText(
                        text = item.label,
                        style = TextStyle(
                            fontFamily = MyDeckDesign.font,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (isSelected) MyDeckDesign.ink else Color(0xFFF4F4F2),
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }
        }
    }
}
