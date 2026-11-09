package com.example.myapplication.ui.design

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.content.Context
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R

/** Spacing and tap-size tokens shared across both roles. */
object MyDeckDimensions {
    val screenPadding = 26.dp
    val contentTop = 112.dp
    val contentBottom = 168.dp
    val minimumTouchHeight = 48.dp
    val sectionGap = 56.dp
    val contentGap = 24.dp
}

object MyDeckDesign {
    val font = FontFamily(
        Font(R.font.inter_regular, FontWeight.Normal),
        Font(R.font.inter_medium, FontWeight.Medium),
        Font(R.font.inter_semibold, FontWeight.SemiBold)
    )
    var darkMode by mutableStateOf(false)
        private set

    val background: Color get() = if (darkMode) Color(0xFF141414) else Color.White
    val ink: Color get() = if (darkMode) Color(0xFFF4F4F4) else Color(0xFF171717)
    val secondary: Color get() = if (darkMode) Color(0xFFC5C5C5) else Color(0xFF595959)
    val line: Color get() = if (darkMode) Color(0xFF444444) else Color(0xFFC7C7C7)
    val lightButton: Color get() = if (darkMode) Color(0xFF333333) else Color(0xFFF1F1F1)
    val fieldBackground: Color get() = if (darkMode) Color(0xFF292929) else Color(0xFFF2F2F1)
    val error: Color get() = if (darkMode) Color(0xFFFFA59B) else Color(0xFFB42318)

    fun loadTheme(context: Context) {
        darkMode = context.getSharedPreferences("mydeck_ui", Context.MODE_PRIVATE)
            .getBoolean("dark_theme", false)
    }

    fun toggleTheme(context: Context) {
        darkMode = !darkMode
        context.getSharedPreferences("mydeck_ui", Context.MODE_PRIVATE)
            .edit().putBoolean("dark_theme", darkMode).apply()
    }
    val buttonStyle = TextStyle(
        fontFamily = font,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center
    )
}

enum class MyDeckButtonVariant { LIGHT, DARK }

@Composable
fun MyDeckButton(
    label: String,
    variant: MyDeckButtonVariant,
    modifier: Modifier = Modifier,
    emphasizeLastWord: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val dark = variant == MyDeckButtonVariant.DARK
    val foreground = if (dark) MyDeckDesign.background else MyDeckDesign.ink
    val background = if (dark) MyDeckDesign.ink else MyDeckDesign.lightButton
    val emphasis = if (dark) foreground else MyDeckDesign.ink
    val content = buildAnnotatedString {
        if (emphasizeLastWord && label.contains(' ')) {
            append(label.substringBeforeLast(' '))
            append(' ')
            withStyle(SpanStyle(color = emphasis)) {
                append(label.substringAfterLast(' '))
            }
        } else {
            append(label)
        }
    }
    val interactive = if (onClick != null) {
        Modifier.clickable(role = Role.Button, onClick = onClick)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .background(background, RoundedCornerShape(50))
            .alpha(if (onClick == null) 0.65f else 1f)
            .then(if (onClick == null) Modifier.semantics { disabled() } else Modifier)
            .then(interactive),
        contentAlignment = Alignment.Center
    ) {
        BasicText(
            text = content,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            style = MyDeckDesign.buttonStyle.copy(color = foreground)
        )
    }
}