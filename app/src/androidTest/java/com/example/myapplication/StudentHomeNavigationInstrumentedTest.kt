package com.example.myapplication

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.myapplication.demo.LocalMyDeckStore
import com.example.myapplication.ui.student.StudentHomeShell
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Testes de navegação real, sem depender da autenticação Firebase. */
@RunWith(AndroidJUnit4::class)
class StudentHomeNavigationInstrumentedTest {
    @get:Rule val compose = createComposeRule()

    private fun launchWithOneDeck() {
        val store = LocalMyDeckStore()
        val classroom = store.createClass("Turma de testes")
        val deck = store.createDeck(classroom.id, "Flashcards de teste")
        store.addCard(deck.id, "Pergunta de navegação", "Resposta", emptyList())
        store.joinByCode(classroom.joinCode)
        compose.setContent { StudentHomeShell(store = store) }
    }

    @Test fun studyButtonOpensSessionAndBottomTabReturnsToHome() {
        launchWithOneDeck()
        compose.onNodeWithText("Estudar cartões").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Estudar.").assertExists()
        compose.onNodeWithText("Pergunta de navegação").assertExists()
        compose.onNodeWithContentDescription("Início").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Aprenda no\nseu ritmo.").assertExists()
    }

    @Test fun reviewButtonStartsDueCardsWithoutIntermediateDeckSelection() {
        launchWithOneDeck()
        compose.onNodeWithText("Começar revisão").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Estudar.").assertExists()
        compose.onNodeWithText("Pergunta de navegação").assertExists()
    }

    @Test fun studyButtonShowsEmptyLibraryInsteadOfLaunchingInvalidSession() {
        compose.setContent { StudentHomeShell(store = LocalMyDeckStore()) }
        compose.onNodeWithText("Nenhum deck disponível.", substring = true).assertExists()
        compose.onNodeWithContentDescription("Decks").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Voltar ao início", substring = true).performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Aprenda no\nseu ritmo.").assertExists()
    }
}
