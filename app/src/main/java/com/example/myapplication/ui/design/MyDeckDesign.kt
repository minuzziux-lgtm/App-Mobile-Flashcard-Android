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
import androidx.compose.ui.Alignment
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

object MyDeckDesign {
    val font = FontFamily(
        Font(R.font.inter_regular, FontWeight.Normal),
        Font(R.font.inter_medium, FontWeight.Medium),
        Font(R.font.inter_semibold, FontWeight.SemiBold)
    )
    val ink = Color(0xFF171717)
    val secondary = Color(0xFF595959)
    val line = Color(0xFFC7C7C7)
    val lightButton = Color(0xFFF1F1F1)
    val error = Color(0xFFB42318)
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
    val foreground = if (dark) Color.White else Color(0xFF414141)
    val background = if (dark) MyDeckDesign.ink else MyDeckDesign.lightButton
    val emphasis = if (dark) Color.White else MyDeckDesign.ink
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