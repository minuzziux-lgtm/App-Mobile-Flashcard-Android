package com.example.myapplication.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.design.MyDeckButton
import com.example.myapplication.ui.design.MyDeckButtonVariant
import com.example.myapplication.ui.design.MyDeckDesign
import com.example.myapplication.ui.design.MyDeckLocale
import com.example.myapplication.ui.design.MyDeckDimensions

private val editorialDivider get() = MyDeckDesign.line
private val editorialMuted get() = MyDeckDesign.secondary

/** Home do aluno: recebe contagens reais do repositório local. */
@Composable
fun HomeAluno(
    onOpenClasses: () -> Unit,
    onOpenDecks: () -> Unit,
    modifier: Modifier = Modifier,
    reviewCount: Int = 0,
    scrollToReviewsRequest: Int = 0,
    studyCount: Int = 0,
    recommendationCount: Int = 0,
    onOpenRecommendations: () -> Unit = {},
    onStartStudy: () -> Unit = {},
    onStartReviews: () -> Unit = {},
    cloudStatus: String? = null,
    onRefresh: (() -> Unit)? = null
) {
    val listState = rememberLazyListState()

    LaunchedEffect(scrollToReviewsRequest) {
        if (scrollToReviewsRequest > 0) listState.animateScrollToItem(1)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(MyDeckDesign.background),
        state = listState,
        contentPadding = PaddingValues(start = MyDeckDimensions.screenPadding, end = MyDeckDimensions.screenPadding, top = MyDeckDimensions.contentTop, bottom = MyDeckDimensions.contentBottom)
    ) {
        item(key = "apresentacao") {
            BasicText(
                text = MyDeckLocale.text("Aprenda no\nseu ritmo.", "Learn at\nyour pace."),
                style = TextStyle(
                    fontFamily = MyDeckDesign.font,
                    fontSize = 42.sp,
                    lineHeight = 48.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-1.7).sp,
                    color = MyDeckDesign.ink
                )
            )
            Spacer(Modifier.height(19.dp))
            EditorialBody(MyDeckLocale.text("Organize seus estudos e retome de onde parou.", "Organize your studies and pick up where you left off."))
            if (onRefresh != null) {
                Spacer(Modifier.height(18.dp))
                EditorialBody(cloudStatus ?: "Dados da conta conectada ao Firebase.", 13)
                Spacer(Modifier.height(12.dp))
                MyDeckButton("Atualizar dados", MyDeckButtonVariant.LIGHT, onClick = onRefresh)
            }
            Spacer(Modifier.height(32.dp))
            MyDeckButton(
                label = MyDeckLocale.text("Estudar cartões", "Study flashcards"),
                variant = MyDeckButtonVariant.DARK,
                onClick = if (studyCount > 0) onStartStudy else null
            )
            Spacer(Modifier.height(12.dp))
            if (studyCount == 0) {
                EditorialBody(
                    MyDeckLocale.text(
                        "Nenhum deck disponível. Entre em uma turma com flashcards atribuídos.",
                        "No deck available. Join a class with assigned flashcards."
                    ), 13
                )
            } else {
                EditorialBody(
                    MyDeckLocale.text(
                        "$studyCount cartões disponíveis para estudar.",
                        "$studyCount flashcards available to study."
                    ), 13
                )
            }
            if (recommendationCount > 0) {
                Spacer(Modifier.height(24.dp))
                EditorialLabel("SEU PRÓXIMO PASSO")
                Spacer(Modifier.height(14.dp))
                EditorialBody("Há $recommendationCount assuntos que merecem uma revisão.")
                Spacer(Modifier.height(12.dp))
                MyDeckButton("Ver sugestões de estudo", MyDeckButtonVariant.LIGHT,
                    onClick = onOpenRecommendations)
            }
            Spacer(Modifier.height(56.dp))
        }

        item(key = "revisoes") {
            Spacer(Modifier.height(23.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                EditorialLabel("01  /  REVISÕES")
                EditorialLabel("DISPONÍVEIS")
            }
            Spacer(Modifier.height(24.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(13.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                BasicText(
                    reviewCount.toString(),
                    style = TextStyle(
                        fontFamily = MyDeckDesign.font,
                        fontSize = 76.sp,
                        lineHeight = 81.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-3.2).sp,
                        color = MyDeckDesign.ink
                    )
                )
                Box(Modifier.padding(bottom = 14.dp)) {
                    EditorialBody("cartões para revisar", 13)
                }
            }
            Spacer(Modifier.height(26.dp))

            // Indicador compacto: não limita visualmente o número de pendências.
            Box(
                modifier = Modifier.fillMaxWidth().height(6.dp)
                    .background(editorialDivider, RoundedCornerShape(50))
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(
                        (reviewCount.coerceAtLeast(0).coerceAtMost(40) / 40f)
                    ).fillMaxHeight()
                        .background(MyDeckDesign.ink, RoundedCornerShape(50))
                )
            }
            Spacer(Modifier.height(12.dp))
            EditorialBody(
                if (reviewCount > 40) "$reviewCount cartões pendentes · acima da referência de 40"
                else "$reviewCount de 40 cartões na referência visual", 12
            )
            Spacer(Modifier.height(24.dp))
            MyDeckButton(
                label = MyDeckLocale.text("Começar revisão", "Start reviewing"),
                variant = MyDeckButtonVariant.DARK,
                onClick = if (reviewCount > 0) onStartReviews else null
            )
            Spacer(Modifier.height(13.dp))
            EditorialBody(
                if (reviewCount == 0) "Nenhuma revisão pendente. Crie ou entre em uma turma e estude um deck."
                else if (reviewCount > 40) "Comece pelas revisões pendentes."
                else "Inicia diretamente a revisão dos seus cartões pendentes.",
                12
            )
            Spacer(Modifier.height(56.dp))
        }

        item(key = "biblioteca") {
            Spacer(Modifier.height(23.dp))
            EditorialLabel("02  /  BIBLIOTECA")
            Spacer(Modifier.height(20.dp))
            StudentSectionRow("Turmas", "Organize os estudos por turma", onOpenClasses)
            StudentSectionRow("Decks", "Seus conjuntos de flashcards", onOpenDecks)
            Spacer(Modifier.height(104.dp))
        }

        item(key = "atividade") {
            Spacer(Modifier.height(23.dp))
            EditorialLabel("03  /  ATIVIDADE")
            Spacer(Modifier.height(37.dp))
            BasicText(
                text = "O hábito se\nconstrói aqui.",
                style = TextStyle(
                    fontFamily = MyDeckDesign.font,
                    fontSize = 27.sp,
                    lineHeight = 34.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.7).sp,
                    color = MyDeckDesign.ink
                )
            )
            Spacer(Modifier.height(19.dp))
            EditorialBody("O histórico e a frequência são calculados a partir das suas revisões locais.")
            Spacer(Modifier.height(45.dp))
            Spacer(Modifier.height(24.dp))
            EditorialLabel("CONTINUE APRENDENDO")
            Spacer(Modifier.height(15.dp))
            EditorialBody("Entre em uma turma ou abra um deck para começar.")
        }
    }
}

@Composable
private fun EditorialLabel(text: String) {
    BasicText(
        text = text,
        style = TextStyle(
            fontFamily = MyDeckDesign.font,
            fontSize = 10.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
            color = editorialMuted
        )
    )
}

@Composable
private fun EditorialBody(text: String, fontSize: Int = 14) {
    BasicText(
        text = text,
        style = TextStyle(
            fontFamily = MyDeckDesign.font,
            fontSize = fontSize.sp,
            lineHeight = (fontSize + 9).sp,
            fontWeight = FontWeight.Normal,
            color = MyDeckDesign.secondary
        )
    )
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun HomeAlunoPreview() {
    HomeAluno(onOpenClasses = {}, onOpenDecks = {})
}
