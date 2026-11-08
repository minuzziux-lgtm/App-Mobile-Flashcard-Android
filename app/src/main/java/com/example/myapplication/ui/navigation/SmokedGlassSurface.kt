package com.example.myapplication.ui.navigation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

// The highlights are translucent so the background remains visible through the blur.
private val glassSheen = Brush.verticalGradient(
    0.00f to Color.White.copy(alpha = 0.19f),
    0.27f to Color.White.copy(alpha = 0.065f),
    0.64f to Color(0x08111216),
    1.00f to Color(0x46080A0D)
)

private val edgeLight = Brush.verticalGradient(
    0.00f to Color.White.copy(alpha = 0.59f),
    0.26f to Color.White.copy(alpha = 0.14f),
    0.72f to Color.White.copy(alpha = 0.06f),
    1.00f to Color.White.copy(alpha = 0.22f)
)

/** Shared dark frosted-glass capsule, capturing the scrolling content behind it. */
@Composable
fun SmokedGlassSurface(
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val blurStyle = remember {
        HazeStyle(
            // Fallback under the captured pixels, not an opaque overlay.
            backgroundColor = Color(0xFF34373C),
            blurRadius = 34.dp,
            tint = HazeTint(Color(0x68191B20)),
            noiseFactor = 0.025f
        )
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = 13.dp,
                shape = CircleShape,
                ambientColor = Color(0x26000000),
                spotColor = Color(0x32000000)
            )
            .clip(CircleShape)
            .hazeEffect(state = hazeState, style = blurStyle)
            .background(glassSheen, CircleShape)
            .border(1.dp, edgeLight, CircleShape)
    ) {
        // Curved reflection concentrated along the upper edge of the capsule.
        Canvas(modifier = Modifier.matchParentSize()) {
            drawOval(
                brush = Brush.verticalGradient(
                    0.00f to Color.White.copy(alpha = 0.19f),
                    0.55f to Color.White.copy(alpha = 0.035f),
                    1.00f to Color.Transparent
                ),
                topLeft = Offset(size.width * 0.06f, -size.height * 0.48f),
                size = androidx.compose.ui.geometry.Size(
                    width = size.width * 0.88f,
                    height = size.height * 0.89f
                )
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(0.73f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.56f), Color.Transparent))
                )
        )
        content()
    }
}
