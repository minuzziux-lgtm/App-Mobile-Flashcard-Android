package com.example.myapplication.ui.screens.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.ui.design.MyDeckButton
import com.example.myapplication.ui.design.MyDeckButtonVariant

private val fonteMyDeck = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold)
)

private val corPrincipal = Color(0xFF171717)
private val corSecundaria = Color(0xFF595959)
private val corBotao = Color(0xFFF1F1F1)

@Composable
fun TelaEntrada(
    onCriarConta: () -> Unit = {},
    onAlunoClick: () -> Unit = {},
    onProfessorClick: () -> Unit = {},
    onProfessorPreview: (() -> Unit)? = null
) {
    val descricao = stringResource(R.string.welcome_description)
    val marca = stringResource(R.string.welcome_brand)
    val cadastro = stringResource(R.string.welcome_signup_prompt)
    val criarConta = stringResource(R.string.welcome_signup_action)

    val funcionalidades = listOf(
        stringResource(R.string.welcome_tag_flashcards),
        stringResource(R.string.welcome_tag_decks),
        stringResource(R.string.welcome_tag_classes),
        stringResource(R.string.welcome_tag_reviews),
        stringResource(R.string.welcome_tag_performance)
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        val espacoParaImagem = maxHeight * 0.34f

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Bottom
        ) {
            Spacer(Modifier.height(espacoParaImagem))

            BasicText(
                text = buildAnnotatedString {
                    append(descricao)
                    append(" ")

                    withStyle(
                        SpanStyle(
                            color = corPrincipal,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(marca)
                    }
                },
                style = TextStyle(
                    fontFamily = fonteMyDeck,
                    fontSize = 18.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.Medium,
                    color = corSecundaria
                )
            )

            Spacer(Modifier.height(18.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                funcionalidades.forEach { funcionalidade ->
                    Etiqueta(funcionalidade)
                }
            }

            Spacer(Modifier.height(30.dp))

            OpcaoEntrada(
                stringResource(R.string.welcome_teacher),
                onClick = onProfessorClick
            )

            Spacer(Modifier.height(10.dp))

            OpcaoEntrada(
                stringResource(R.string.welcome_student),
                onClick = onAlunoClick
            )

            Spacer(Modifier.height(18.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                BasicText(
                    text = cadastro,
                    style = TextStyle(
                        fontFamily = fonteMyDeck,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = corSecundaria
                    )
                )
                Spacer(Modifier.width(5.dp))
                BasicText(
                    text = criarConta,
                    modifier = Modifier.clickable(onClick = onCriarConta),
                    style = TextStyle(
                        fontFamily = fonteMyDeck,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = corPrincipal,
                        textDecoration = TextDecoration.Underline
                    )
                )
            }

            if (onProfessorPreview != null) {
                Spacer(Modifier.height(12.dp))
                BasicText(
                    "Prévia do professor · sem Firebase",
                    modifier = Modifier.clickable(onClick = onProfessorPreview)
                        .padding(vertical = 12.dp),
                    style = TextStyle(
                        fontFamily = fonteMyDeck,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = corSecundaria,
                        textDecoration = TextDecoration.Underline
                    )
                )
            }
        }
    }
}

@Composable
private fun Etiqueta(texto: String) {
    Box(
        modifier = Modifier
            .background(
                color = corBotao,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 10.dp, vertical = 7.dp)
    ) {
        BasicText(
            text = texto,
            style = TextStyle(
                fontFamily = fonteMyDeck,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF494949)
            )
        )
    }
}

@Composable
private fun OpcaoEntrada(texto: String, onClick: (() -> Unit)? = null) {
    MyDeckButton(
        label = texto,
        variant = MyDeckButtonVariant.LIGHT,
        emphasizeLastWord = true,
        onClick = onClick
    )
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun TelaEntradaPreview() {
    TelaEntrada()
}