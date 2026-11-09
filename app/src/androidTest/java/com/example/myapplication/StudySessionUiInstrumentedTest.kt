package com.example.myapplication

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.myapplication.demo.DemoCard
import com.example.myapplication.demo.DemoCardType
import com.example.myapplication.study.StudySessionState
import com.example.myapplication.study.startStudySession
import com.example.myapplication.ui.student.StudySessionScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Confirma fluxos visíveis sem conectar Firebase nem depender de dados simulados persistidos. */
@RunWith(AndroidJUnit4::class)
class StudySessionUiInstrumentedTest {
    @get:Rule val compose = createComposeRule()

    private fun render(card: DemoCard, initial: StudySessionState) {
        var state by mutableStateOf(initial)
        compose.setContent {
            StudySessionScreen(
                deckTitle = "Deck de testes",
                currentCard = if (state.currentCardId == card.id) card else null,
                state = state,
                onStateChange = { state = it },
                onClose = {}
            )
        }
    }

    @Test fun basicCardCanBeRevealedRatedAndCompleted() {
        val card = DemoCard("basic", "deck", "Pergunta de teste", "Resposta de teste", emptyList())
        render(card, startStudySession("deck", listOf(card.id), setOf(card.id)))
        compose.onNodeWithText("Pergunta de teste").assertExists()
        compose.onNodeWithText("Virar cartão").performScrollTo().performClick()
        compose.onNodeWithText("Resposta de teste").assertExists()
        compose.onNodeWithText("Acertei").performScrollTo().performClick()
        compose.onNodeWithText("Concluir sessão").performScrollTo().performClick()
        compose.onNodeWithText("SESSÃO CONCLUÍDA").assertExists()
    }

    @Test fun multipleChoiceGradesAndCompletes() {
        val card = DemoCard(
            id = "choice", deckId = "deck", question = "Quanto é 2 + 2?",
            answer = "4", tags = emptyList(), type = DemoCardType.MULTIPLE_CHOICE,
            options = listOf("3", "4", "5"), correctOptionIndex = 1
        )
        render(card, startStudySession("deck", listOf(card.id), setOf(card.id)))
        compose.onNodeWithText("2. 4").performScrollTo().performClick()
        compose.onNodeWithText("Confirmar alternativa").performScrollTo().performClick()
        compose.onNodeWithText("Resposta correta.").assertExists()
        compose.onNodeWithText("Concluir sessão").performScrollTo().performClick()
        compose.onNodeWithText("SESSÃO CONCLUÍDA").assertExists()
    }

    @Test fun openEndedRequiresWrittenResponse() {
        val card = DemoCard(
            id = "free", deckId = "deck", question = "Explique seu raciocínio",
            answer = "", tags = emptyList(), type = DemoCardType.OPEN_ENDED
        )
        render(card, startStudySession("deck", listOf(card.id), requiredWrittenCardIds = setOf(card.id)))
        compose.onNodeWithContentDescription("Campo para resposta livre")
            .performTextInput("Minha explicação")
        compose.onNodeWithText("Registrar resposta").performScrollTo().performClick()
        compose.onNodeWithText("Resposta registrada: Minha explicação").assertExists()
        compose.onNodeWithText("Concluir sessão").performScrollTo().performClick()
        compose.onNodeWithText("SESSÃO CONCLUÍDA").assertExists()
    }
}
