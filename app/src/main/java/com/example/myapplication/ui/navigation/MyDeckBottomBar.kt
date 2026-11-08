package com.example.myapplication.ui.navigation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
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
    PROFILE("perfil", "Perfil")
}

private val activeSurface = Brush.verticalGradient(
    listOf(Color(0xFFFFFFFF), Color(0xFFEDEDEB), Color(0xFFE5E5E3))
)
private val activeInk = Color(0xFF171717)
private val inactiveInk = Color(0xFFF4F4F2)

/** Three independently blurred capsules, matching the reference's floating layout. */
@Composable
fun MyDeckBottomBar(
    currentRoute: String?,
    hazeState: HazeState,
    reviewCount: Int,
    onReviewClick: () -> Unit,
    onNavigate: (StudentTab) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val compact = maxWidth < 348.dp
        val quickWidth = if (compact) 56.dp else 67.dp
        val profileWidth = if (compact) 49.dp else 54.dp
        val gap = if (compact) 6.dp else 9.dp

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(gap),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SmokedGlassSurface(hazeState, Modifier.width(quickWidth).height(56.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(role = Role.Button, onClick = onReviewClick)
                        .semantics { contentDescription = "$reviewCount revisões disponíveis" },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ClockGlyph(Modifier.size(17.dp), inactiveInk)
                    BasicText(
                        text = reviewCount.toString(),
                        modifier = Modifier.padding(start = 6.dp),
                        style = TextStyle(
                            fontFamily = MyDeckDesign.font,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = inactiveInk
                        )
                    )
                }
            }

            SmokedGlassSurface(hazeState, Modifier.weight(1f).height(58.dp)) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(5.dp),
                    horizontalArrangement = Arrangement.spacedBy(1.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StudentTab.entries.filter { it != StudentTab.PROFILE }.forEach { tab ->
                        val isSelected = currentRoute == tab.route
                        val background = if (isSelected) Modifier.background(activeSurface, CircleShape) else Modifier

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .then(background)
                                .semantics {
                                    selected = isSelected
                                    contentDescription = tab.label
                                }
                                .clickable(role = Role.Tab) { onNavigate(tab) },
                            contentAlignment = Alignment.Center
                        ) {
                            BasicText(
                                text = tab.label,
                                style = TextStyle(
                                    fontFamily = MyDeckDesign.font,
                                    fontSize = if (compact) 11.sp else 12.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                    color = if (isSelected) activeInk else inactiveInk,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    }
                }
            }

            val isProfile = currentRoute == StudentTab.PROFILE.route
            SmokedGlassSurface(hazeState, Modifier.width(profileWidth).height(56.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(if (isProfile) Modifier.background(activeSurface, CircleShape) else Modifier)
                        .semantics {
                            selected = isProfile
                            contentDescription = "Perfil"
                        }
                        .clickable(role = Role.Tab) { onNavigate(StudentTab.PROFILE) },
                    contentAlignment = Alignment.Center
                ) {
                    ProfileGlyph(Modifier.size(19.dp), if (isProfile) activeInk else inactiveInk)
                }
            }
        }
    }
}

@Composable
private fun ClockGlyph(modifier: Modifier, color: Color) {
    Canvas(modifier) {
        val middle = Offset(size.width / 2f, size.height / 2f)
        val thickness = 1.4.dp.toPx()
        drawCircle(color, size.minDimension * 0.40f, middle, style = Stroke(thickness))
        drawLine(color, middle, Offset(middle.x, middle.y - size.height * 0.22f), thickness)
        drawLine(color, middle, Offset(middle.x + size.width * 0.13f, middle.y + size.height * 0.08f), thickness)
    }
}

@Composable
private fun ProfileGlyph(modifier: Modifier, color: Color) {
    Canvas(modifier) {
        val thickness = 1.5.dp.toPx()
        drawCircle(
            color,
            radius = size.minDimension * 0.16f,
            center = Offset(size.width * 0.5f, size.height * 0.29f),
            style = Stroke(thickness)
        )
        drawArc(
            color = color,
            startAngle = 187f,
            sweepAngle = 166f,
            useCenter = false,
            topLeft = Offset(size.width * 0.13f, size.height * 0.50f),
            size = Size(size.width * 0.74f, size.height * 0.56f),
            style = Stroke(thickness)
        )
    }
}
