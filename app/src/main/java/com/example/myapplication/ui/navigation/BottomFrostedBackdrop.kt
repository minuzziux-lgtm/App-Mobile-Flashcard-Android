package com.example.myapplication.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

@Composable
fun BottomFrostedBackdrop(
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    val style = remember {
        HazeStyle(
            backgroundColor = Color(0xFFF8F8F7),
            blurRadius = 36.dp,
            tint = HazeTint(
                Color.White.copy(alpha = 0.18f)
            ),
            noiseFactor = 0.01f
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(172.dp)
            .hazeEffect(
                state = hazeState,
                style = style
            ) {
                progressive =
                    HazeProgressive.verticalGradient(
                        startIntensity = 0f,
                        endIntensity = 1f
                    )
            }
    )
}