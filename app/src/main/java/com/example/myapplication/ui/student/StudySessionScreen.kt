package com.example.myapplication.ui.student

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.demo.DemoCard
import com.example.myapplication.demo.DemoCardType
import com.example.myapplication.demo.isValidExternalLink
import com.example.myapplication.study.StudyRecall
import com.example.myapplication.study.gradeMultipleChoice
import com.example.myapplication.study.gradeCloze
import com.example.myapplication.study.StudySessionState
import com.example.myapplication.study.StudySessionStatus
import com.example.myapplication.ui.design.MyDeckButton
import com.example.myapplication.ui.design.MyDeckButtonVariant
import com.example.myapplication.ui.design.MyDeckDesign

/** Interface de estudo local; as avaliações são persistidas pelo repositório da sessão. */
@Composable
fun StudySessionScreen(
    deckTitle: String,
    currentCard: DemoCard?,
    state: StudySessionState,
    onStateChange: (StudySessionState) -> Unit,
    onClose: () -> Unit
) {
    var selectedOption by rememberSaveable(currentCard?.id) { mutableIntStateOf(-1) }
    var clozeInput by rememberSaveable(currentCard?.id) { mutableStateOf("") }
    var openInput by rememberSaveable(currentCard?.id) { mutableStateOf("") }
    val uriHandler = LocalUriHandler.current
    StudentSection(
        title = "Estudar.",
        description = deckTitle
    ) {
        when (state.status) {
            StudySessionStatus.COMPLETED -> {
                StudyLabel("SESSÃO CONCLUÍDA")
                Spacer(Modifier.height(18.dp))
                StudyText("Você percorreu os ${state.totalCards} cartões deste deck. Progresso: 100%.")
                Spacer(Modifier.height(30.dp))
                StudyText("Cartões avaliados: ${state.ratings.size}. Acertos: ${state.correctCount}. Erros: ${state.incorrectCount}.")
                Spacer(Modifier.height(12.dp))
                StudyText("Avaliações registradas no histórico local deste aparelho. O envio ao Firebase será implementado posteriormente.")
                Spacer(Modifier.height(30.dp))
                MyDeckButton(
                    label = "Voltar aos decks",
                    variant = MyDeckButtonVariant.DARK,
                    onClick = onClose
                )
            }
            StudySessionStatus.PAUSED -> {
                StudyLabel("SESSÃO PAUSADA")
                Spacer(Modifier.height(18.dp))
                StudyText("Você parou no cartão ${state.currentIndex + 1} de ${state.totalCards}. Avanço: ${state.viewedCards} de ${state.totalCards}.")
                Spacer(Modifier.height(30.dp))
                MyDeckButton(
                    label = "Retomar estudo",
                    variant = MyDeckButtonVariant.DARK,
                    onClick = { onStateChange(state.resume()) }
                )
                Spacer(Modifier.height(12.dp))
                MyDeckButton(
                    label = "Encerrar sessão",
                    variant = MyDeckButtonVariant.LIGHT,
                    onClick = onClose
                )
            }
            StudySessionStatus.ACTIVE -> {
                StudyLabel("CARTÃO ${state.currentIndex + 1} DE ${state.totalCards}")
                Spacer(Modifier.height(8.dp))
                StudyText("Progresso: ${state.viewedCards} de ${state.totalCards} visualizados · ${(state.progress * 100).toInt()}%")
                Spacer(Modifier.height(17.dp))
                Box(Modifier.fillMaxWidth().height(3.dp).background(MyDeckDesign.line)) {
                    Box(
                        Modifier.fillMaxWidth(state.progress).height(3.dp)
                            .background(MyDeckDesign.ink)
                    )
                }
                Spacer(Modifier.height(30.dp))
                if (currentCard == null) {
                    StudyText("Este cartão foi removido ou não está disponível. Encerre a sessão e inicie outra.")
                    Spacer(Modifier.height(26.dp))
                    MyDeckButton(
                        label = "Voltar aos decks",
                        variant = MyDeckButtonVariant.DARK,
                        onClick = onClose
                    )
                } else {
                    val isBasic = currentCard.type == DemoCardType.BASIC
                    val isReversible = currentCard.type == DemoCardType.REVERSIBLE
                    val isSelfRated = isBasic || isReversible
                    if (isSelfRated) {
                        if (isReversible) {
                            StudyLabel(
                                listOf(currentCard.frontLanguage, currentCard.backLanguage)
                                    .filter(String::isNotBlank).joinToString(" → ").ifBlank { "TRADUÇÃO" }
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                        BasicFlashcard(
                            question = currentCard.question,
                            answer = currentCard.answer,
                            revealed = state.isAnswerRevealed
                        )
                    } else {
                        StudyLabel(currentCard.type.label.uppercase())
                        Spacer(Modifier.height(12.dp))
                        StudyQuestion(currentCard.question)
                        if (currentCard.type == DemoCardType.OPEN_ENDED && !state.isAnswerRevealed) {
                            Spacer(Modifier.height(16.dp))
                            StudyText("Escreva sua resposta. Este tipo não possui gabarito automático.")
                            Spacer(Modifier.height(12.dp))
                            BasicTextField(
                                value = openInput,
                                onValueChange = { openInput = it.take(2000) },
                                textStyle = TextStyle(fontFamily = MyDeckDesign.font, fontSize = 16.sp, color = MyDeckDesign.ink),
                                modifier = Modifier.fillMaxWidth().background(MyDeckDesign.fieldBackground).padding(16.dp)
                                    .semantics { contentDescription = "Campo para resposta livre" }
                            )
                        }
                        if (currentCard.type == DemoCardType.CLOZE && !state.isAnswerRevealed) {
                            Spacer(Modifier.height(16.dp))
                            BasicTextField(
                                value = clozeInput,
                                onValueChange = { clozeInput = it.take(2000) },
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontFamily = MyDeckDesign.font, fontSize = 16.sp,
                                    color = MyDeckDesign.ink
                                ),
                                modifier = Modifier.fillMaxWidth().background(MyDeckDesign.fieldBackground).padding(16.dp)
                                    .semantics { contentDescription = "Campo para completar a lacuna" }
                            )
                        }
                        if (currentCard.type == DemoCardType.MULTIPLE_CHOICE) {
                            Spacer(Modifier.height(18.dp))
                            currentCard.options.forEachIndexed { index, option ->
                                MyDeckButton(
                                    label = "${index + 1}. $option",
                                    variant = if (selectedOption == index) MyDeckButtonVariant.DARK else MyDeckButtonVariant.LIGHT,
                                    onClick = if (state.isAnswerRevealed) null else ({ selectedOption = index })
                                )
                                Spacer(Modifier.height(9.dp))
                            }
                        }
                    }
                    currentCard.mediaLink?.takeIf { isValidExternalLink(it.url) }?.let { media ->
                        Spacer(Modifier.height(20.dp))
                        MyDeckButton(
                            label = "Abrir " + media.type.label.lowercase(),
                            variant = MyDeckButtonVariant.LIGHT,
                            onClick = { uriHandler.openUri(media.url) }
                        )
                    }
                    Spacer(Modifier.height(30.dp))
                    if (state.isAnswerRevealed) {
                        if (isSelfRated) {
                            StudyLabel("COMO FOI A SUA RESPOSTA?")
                            Spacer(Modifier.height(12.dp))
                            StudyText("Marque sua própria avaliação para continuar. Não há correção automática.")
                            Spacer(Modifier.height(16.dp))
                            MyDeckButton(
                                label = "Acertei",
                                variant = if (state.ratings[currentCard.id] == StudyRecall.CORRECT) {
                                    MyDeckButtonVariant.DARK
                                } else {
                                    MyDeckButtonVariant.LIGHT
                                },
                                onClick = { onStateChange(state.rateCurrentCard(StudyRecall.CORRECT)) }
                            )
                            Spacer(Modifier.height(10.dp))
                            MyDeckButton(
                                label = "Errei",
                                variant = if (state.ratings[currentCard.id] == StudyRecall.INCORRECT) {
                                    MyDeckButtonVariant.DARK
                                } else {
                                    MyDeckButtonVariant.LIGHT
                                },
                                onClick = { onStateChange(state.rateCurrentCard(StudyRecall.INCORRECT)) }
                            )
                        } else {
                            if (currentCard.type in setOf(DemoCardType.MULTIPLE_CHOICE, DemoCardType.CLOZE)) {
                                StudyText(if (state.ratings[currentCard.id] == StudyRecall.CORRECT) "Resposta correta." else "Resposta incorreta.")
                                Spacer(Modifier.height(12.dp))
                            }
                            StudyLabel("RESPOSTA")
                            Spacer(Modifier.height(12.dp))
                            StudyText(
                                if (currentCard.type == DemoCardType.OPEN_ENDED) {
                                    "Resposta registrada: " + state.writtenResponses[currentCard.id].orEmpty() + "\nSem gabarito; não contabilizada como acerto ou erro."
                                } else {
                                    currentCard.answer
                                }
                            )
                        }
                        if (!isSelfRated || state.ratings[currentCard.id] != null) {
                            Spacer(Modifier.height(30.dp))
                            MyDeckButton(
                                label = if (state.currentIndex == state.cardIds.lastIndex) {
                                    "Concluir sessão"
                                } else {
                                    "Próximo cartão"
                                },
                                variant = MyDeckButtonVariant.DARK,
                                onClick = { onStateChange(state.nextCard()) }
                            )
                        }
                    } else {
                        if (currentCard.type == DemoCardType.MULTIPLE_CHOICE) {
                            MyDeckButton(
                                label = "Confirmar alternativa",
                                variant = MyDeckButtonVariant.DARK,
                                onClick = if (selectedOption in currentCard.options.indices) ({
                                    val result = gradeMultipleChoice(selectedOption, currentCard.correctOptionIndex, currentCard.options.size)
                                    if (result != null) onStateChange(state.revealAnswer().rateCurrentCard(result))
                                }) else null
                            )
                        } else if (currentCard.type == DemoCardType.CLOZE) {
                            MyDeckButton(
                                label = "Conferir lacuna",
                                variant = MyDeckButtonVariant.DARK,
                                onClick = if (clozeInput.isNotBlank()) ({
                                    val result = gradeCloze(clozeInput, currentCard.answer)
                                    if (result != null) onStateChange(state.revealAnswer().rateCurrentCard(result))
                                }) else null
                            )
                        } else if (currentCard.type == DemoCardType.OPEN_ENDED) {
                            MyDeckButton(
                                label = "Registrar resposta",
                                variant = MyDeckButtonVariant.DARK,
                                onClick = if (openInput.isNotBlank()) ({
                                    onStateChange(state.submitOpenResponse(openInput).revealAnswer())
                                }) else null
                            )
                        } else MyDeckButton(
                            label = if (currentCard.type == DemoCardType.OPEN_ENDED) {
                                "Continuar"
                            } else if (isSelfRated) {
                                "Virar cartão"
                            } else {
                                "Revelar resposta"
                            },
                            variant = MyDeckButtonVariant.DARK,
                            onClick = { onStateChange(state.revealAnswer()) }
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    MyDeckButton(
                        label = "Pausar sessão",
                        variant = MyDeckButtonVariant.LIGHT,
                        onClick = { onStateChange(state.pause()) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StudyLabel(label: String) {
    BasicText(
        label,
        style = TextStyle(
            fontFamily = MyDeckDesign.font, fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold, color = MyDeckDesign.ink
        )
    )
}

@Composable
private fun StudyQuestion(text: String) {
    BasicText(
        text,
        style = TextStyle(
            fontFamily = MyDeckDesign.font, fontSize = 22.sp, lineHeight = 32.sp,
            fontWeight = FontWeight.Medium, color = MyDeckDesign.ink
        )
    )
}

@Composable
private fun StudyText(text: String) {
    BasicText(
        text,
        style = TextStyle(
            fontFamily = MyDeckDesign.font, fontSize = 15.sp,
            lineHeight = 24.sp, color = MyDeckDesign.secondary
        )
    )
}

@Composable
private fun BasicFlashcard(question: String, answer: String, revealed: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(MyDeckDesign.fieldBackground, RoundedCornerShape(18.dp))
            .padding(24.dp)
    ) {
        StudyLabel(if (revealed) "VERSO / RESPOSTA" else "FRENTE / PERGUNTA")
        Spacer(Modifier.height(20.dp))
        Crossfade(targetState = revealed, label = "Virar cartão básico") { showAnswer ->
            StudyQuestion(if (showAnswer) answer else question)
        }
    }
}
