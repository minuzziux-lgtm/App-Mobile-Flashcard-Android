package com.example.myapplication.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.design.MyDeckDesign
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
    // Recompute when theme changes: a light fallback caused a white strip in dark mode.
    val style = HazeStyle(
        backgroundColor = Color.Transparent,
        blurRadius = 36.dp,
        tint = HazeTint(Color.Transparent),
        noiseFactor = 0f
    )

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