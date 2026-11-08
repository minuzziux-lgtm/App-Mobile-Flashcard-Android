package com.example.myapplication.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
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

private val editorialDivider = Color(0xFFE3E3E1)
private val editorialMuted = Color(0xFF777774)

/** Editorial student home. Demo review counts until study data is connected. */
@Composable
fun HomeAluno(
    onOpenClasses: () -> Unit,
    onOpenDecks: () -> Unit,
    modifier: Modifier = Modifier,
    sampleReviewCount: Int = 40,
    scrollToReviewsRequest: Int = 0
) {
    val listState = rememberLazyListState()
    val showDemoNotice = remember { mutableStateOf(false) }

    LaunchedEffect(scrollToReviewsRequest) {
        if (scrollToReviewsRequest > 0) listState.animateScrollToItem(1)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(Color.White),
        state = listState,
        contentPadding = PaddingValues(start = 26.dp, end = 26.dp, top = 32.dp, bottom = 168.dp)
    ) {
        item(key = "apresentacao") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                EditorialLabel("MY-DECK")
                EditorialLabel("ALUNO  /  INÍCIO")
            }
            Spacer(Modifier.height(79.dp))
            BasicText(
                text = "Aprenda no\nseu ritmo.",
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
            EditorialBody("Organize seus estudos e retome de onde parou.")
            Spacer(Modifier.height(80.dp))
        }

        item(key = "revisoes") {
            ThinEditorialLine()
            Spacer(Modifier.height(23.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                EditorialLabel("01  /  REVISÕES")
                EditorialLabel("DISPONÍVEIS")
            }
            Spacer(Modifier.height(37.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(13.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                BasicText(
                    sampleReviewCount.toString(),
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

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                maxItemsInEachRow = 20,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(13.dp)
            ) {
                repeat(40) { index ->
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(
                                if (index < sampleReviewCount.coerceIn(0, 40)) MyDeckDesign.ink else editorialDivider,
                                CircleShape
                            )
                    )
                }
            }
            Spacer(Modifier.height(34.dp))
            MyDeckButton(
                label = "Começar revisão",
                variant = MyDeckButtonVariant.DARK,
                onClick = { showDemoNotice.value = true }
            )
            if (showDemoNotice.value) {
                Spacer(Modifier.height(13.dp))
                EditorialBody("O modo de revisão será conectado aos decks na próxima etapa.", 12)
            }
            Spacer(Modifier.height(88.dp))
        }

        item(key = "biblioteca") {
            ThinEditorialLine()
            Spacer(Modifier.height(23.dp))
            EditorialLabel("02  /  BIBLIOTECA")
            Spacer(Modifier.height(39.dp))
            StudentSectionRow("Turmas", "Organize os estudos por turma", onOpenClasses)
            StudentSectionRow("Decks", "Seus conjuntos de flashcards", onOpenDecks)
            Spacer(Modifier.height(88.dp))
        }

        item(key = "atividade") {
            ThinEditorialLine()
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
            EditorialBody("Seu histórico aparecerá após as primeiras sessões de estudo.")
            Spacer(Modifier.height(45.dp))
            ThinEditorialLine()
            Spacer(Modifier.height(24.dp))
            EditorialLabel("CONTINUE APRENDENDO")
            Spacer(Modifier.height(15.dp))
            EditorialBody("Entre em uma turma ou abra um deck para começar.")
        }
    }
}

@Composable
private fun ThinEditorialLine() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(editorialDivider))
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
