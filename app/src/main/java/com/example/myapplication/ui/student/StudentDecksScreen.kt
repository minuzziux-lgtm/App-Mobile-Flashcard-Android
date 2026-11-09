package com.example.myapplication.ui.student

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.demo.DemoCard
import com.example.myapplication.demo.DemoCardType
import com.example.myapplication.demo.DemoClass
import com.example.myapplication.demo.DemoDeck
import com.example.myapplication.study.StudyRecall
import com.example.myapplication.study.StudyReview
import java.util.UUID
import com.example.myapplication.study.StudySessionState
import com.example.myapplication.study.StudySessionStatus
import com.example.myapplication.study.startStudySession
import com.example.myapplication.ui.design.MyDeckButton
import com.example.myapplication.ui.design.MyDeckButtonVariant
import com.example.myapplication.ui.design.MyDeckDesign
import com.example.myapplication.ui.design.MyDeckLocale
import androidx.compose.foundation.layout.heightIn

@Composable
fun StudentDecksScreen(
    classes: List<DemoClass>,
    decks: List<DemoDeck>,
    cards: List<DemoCard>,
    selectedClassId: String?,
    onSelectClass: (String) -> Unit,
    onRecordReview: (StudyReview) -> Unit = {},
    dueCardIds: Set<String> = emptySet(),
    studentId: String = "@aluno-demo",
    launchStudyRequest: Int = 0,
    launchDueOnly: Boolean = false,
    onReturnHome: () -> Unit = {}
) {
    var selectedDeckId by rememberSaveable { mutableStateOf<String?>(null) }
    var lastHandledStudyRequest by rememberSaveable { mutableIntStateOf(0) }
    var launchedFromHome by rememberSaveable { mutableStateOf(false) }
    var showStudyUnavailable by rememberSaveable { mutableStateOf(false) }
    var sessionId by rememberSaveable { mutableStateOf("") }
    var sessionDeckId by rememberSaveable { mutableStateOf<String?>(null) }
    var sessionCardIds by rememberSaveable { mutableStateOf(arrayListOf<String>()) }
    var sessionIndex by rememberSaveable { mutableStateOf(0) }
    var sessionRevealed by rememberSaveable { mutableStateOf(false) }
    var sessionStatus by rememberSaveable { mutableStateOf(StudySessionStatus.ACTIVE.name) }
    var sessionBasicIds by rememberSaveable { mutableStateOf(arrayListOf<String>()) }
    var sessionCorrectIds by rememberSaveable { mutableStateOf(arrayListOf<String>()) }
    var sessionIncorrectIds by rememberSaveable { mutableStateOf(arrayListOf<String>()) }
    var sessionWrittenIds by rememberSaveable { mutableStateOf(arrayListOf<String>()) }
    var sessionAnswerIds by rememberSaveable { mutableStateOf(arrayListOf<String>()) }
    var sessionAnswerValues by rememberSaveable { mutableStateOf(arrayListOf<String>()) }

    fun setSession(next: StudySessionState) {
        if (sessionId.isBlank()) sessionId = UUID.randomUUID().toString()
        val oldRatings = sessionCorrectIds.associateWith { StudyRecall.CORRECT } +
            sessionIncorrectIds.associateWith { StudyRecall.INCORRECT }
        val oldWritten = sessionAnswerIds.zip(sessionAnswerValues).toMap()
        val now = System.currentTimeMillis()
        val reviewClassId = resolveStudyClassId(
            selectedClassId,
            decks.firstOrNull { it.id == next.deckId },
            classes
        )
        val updatedIds = (next.ratings.keys.filter { oldRatings[it] != next.ratings[it] } +
            next.writtenResponses.keys.filter { oldWritten[it] != next.writtenResponses[it] }).distinct()
        updatedIds.forEach { id ->
            val card = cards.firstOrNull { it.id == id && it.deckId == next.deckId }
            if (card != null) {
                onRecordReview(StudyReview(
                    id = "${sessionId}:$id", studentId = studentId,
                    deckId = next.deckId, cardId = id, classId = reviewClassId,
                    correct = next.ratings[id]?.let { it == StudyRecall.CORRECT },
                    writtenResponse = next.writtenResponses[id],
                    tags = card.tags, reviewedAtEpochMillis = now, dueAtEpochMillis = now
                ))
            }
        }
        sessionDeckId = next.deckId
        sessionCardIds = ArrayList(next.cardIds)
        sessionIndex = next.currentIndex
        sessionRevealed = next.isAnswerRevealed
        sessionStatus = next.status.name
        sessionBasicIds = ArrayList(next.requiredRatingCardIds)
        sessionCorrectIds = ArrayList(next.ratings.filterValues { it == StudyRecall.CORRECT }.keys)
        sessionIncorrectIds = ArrayList(next.ratings.filterValues { it == StudyRecall.INCORRECT }.keys)
        sessionWrittenIds = ArrayList(next.requiredWrittenCardIds)
        sessionAnswerIds = ArrayList(next.writtenResponses.keys)
        sessionAnswerValues = ArrayList(next.writtenResponses.values)
    }
    fun closeSession() {
        sessionId = ""
        sessionDeckId = null
        sessionCardIds = arrayListOf()
        sessionIndex = 0
        sessionRevealed = false
        sessionStatus = StudySessionStatus.ACTIVE.name
        sessionBasicIds = arrayListOf()
        sessionCorrectIds = arrayListOf()
        sessionIncorrectIds = arrayListOf()
        sessionWrittenIds = arrayListOf()
        sessionAnswerIds = arrayListOf()
        sessionAnswerValues = arrayListOf()
    }

    val session = sessionDeckId?.takeIf { sessionCardIds.isNotEmpty() }?.let { deckId ->
        StudySessionState(
            deckId = deckId,
            cardIds = sessionCardIds,
            currentIndex = sessionIndex,
            isAnswerRevealed = sessionRevealed,
            status = StudySessionStatus.valueOf(sessionStatus),
            requiredRatingCardIds = sessionBasicIds.toSet(),
            ratings = (
                sessionCorrectIds.associateWith { StudyRecall.CORRECT } +
                sessionIncorrectIds.associateWith { StudyRecall.INCORRECT }
            ),
            requiredWrittenCardIds = sessionWrittenIds.toSet(),
            writtenResponses = sessionAnswerIds.zip(sessionAnswerValues).toMap()
        )
    }
    // A ação principal da Home inicia o estudo, não apenas abre uma lista de decks.
    LaunchedEffect(launchStudyRequest) {
        if (launchStudyRequest > 0 && launchStudyRequest != lastHandledStudyRequest) {
            lastHandledStudyRequest = launchStudyRequest
            val target = findStudySelection(classes, decks, cards, dueCardIds, launchDueOnly)
            if (target == null) {
                showStudyUnavailable = true
            } else {
                val targetCards = target.cards
                onSelectClass(target.classId)
                selectedDeckId = target.deckId
                launchedFromHome = true
                showStudyUnavailable = false
                setSession(
                    startStudySession(
                        target.deckId,
                        targetCards.map { it.id },
                        targetCards.filter { it.type != DemoCardType.OPEN_ENDED }
                            .map { it.id }.toSet(),
                        targetCards.filter { it.type == DemoCardType.OPEN_ENDED }
                            .map { it.id }.toSet()
                    )
                )
            }
        }
    }

    fun exitStudy() {
        val returnHome = launchedFromHome
        closeSession()
        launchedFromHome = false
        if (returnHome) onReturnHome()
    }

    BackHandler(enabled = session != null) { exitStudy() }

    if (session != null) {
        StudySessionScreen(
            deckTitle = decks.firstOrNull { it.id == session.deckId }?.title ?: "Deck indisponível",
            currentCard = cards.firstOrNull {
                it.id == session.currentCardId && it.deckId == session.deckId
            },
            state = session,
            onStateChange = { setSession(it) },
            onClose = { exitStudy() }
        )
        return
    }

    val selectedClass = classes.firstOrNull { it.id == selectedClassId } ?: classes.firstOrNull()
    val classDecks = decks.filter {
        selectedClass != null && selectedClass.id in it.assignedClassIds
    }
    val selectedDeck = classDecks.firstOrNull { it.id == selectedDeckId }
    val uriHandler = LocalUriHandler.current

    StudentSection(
        title = MyDeckLocale.text("Decks.", "Decks."),
        description = MyDeckLocale.text("Consulte os decks das suas turmas e os cartões disponíveis.", "Browse your classes, decks and available cards.")
    ) {
        BasicText(
            text = MyDeckLocale.text("← Voltar ao início", "← Back to home"),
            modifier = Modifier.heightIn(min = 48.dp)
                .clickable(role = Role.Button) { onReturnHome() }
                .padding(vertical = 13.dp),
            style = TextStyle(
                fontFamily = MyDeckDesign.font,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MyDeckDesign.ink
            )
        )
        Spacer(Modifier.height(12.dp))
        if (showStudyUnavailable) {
            ScreenText("Nenhum cartão disponível para este estudo. Escolha um deck abaixo ou verifique suas revisões.")
            Spacer(Modifier.height(22.dp))
        }
        if (classes.isEmpty()) {
            ScreenText("Entre em uma turma na aba Turmas para visualizar os decks.")
        } else {
            SectionLabel("01  /  SUAS TURMAS")
            Spacer(Modifier.height(15.dp))
            classes.forEach { classroom ->
                StudentSectionRow(
                    classroom.name,
                    if (classroom.id == selectedClass?.id) "Selecionada" else "Toque para visualizar",
                    onClick = { onSelectClass(classroom.id); selectedDeckId = null }
                )
            }
            Spacer(Modifier.height(44.dp))
            Divider()
            Spacer(Modifier.height(24.dp))
            SectionLabel("02  /  DECKS  ·  " + classDecks.size)
            Spacer(Modifier.height(16.dp))
            if (classDecks.isEmpty()) {
                ScreenText("Não há decks nesta turma.")
            } else {
                classDecks.forEach { deck ->
                    val count = cards.count { it.deckId == deck.id }
                    StudentSectionRow(
                        deck.title,
                        count.toString() + (if (count == 1) " cartão" else " cartões"),
                        onClick = { selectedDeckId = if (selectedDeckId == deck.id) null else deck.id }
                    )
                }
                if (selectedDeck != null) {
                    Spacer(Modifier.height(43.dp))
                    Divider()
                    Spacer(Modifier.height(24.dp))
                    SectionLabel("03  /  CARTÕES DE " + selectedDeck.title.uppercase())
                    Spacer(Modifier.height(16.dp))
                    val visibleCards = cards.filter { it.deckId == selectedDeck.id }
                    if (visibleCards.isEmpty()) {
                        ScreenText("Este deck ainda não possui cartões.")
                    } else {
                        MyDeckButton(
                            label = MyDeckLocale.text("Iniciar estudo", "Start studying"),
                            variant = MyDeckButtonVariant.DARK,
                            onClick = {
                                setSession(
                                    startStudySession(
                                        selectedDeck.id,
                                        visibleCards.map { it.id },
                                        visibleCards.filter { it.type == DemoCardType.BASIC || it.type == DemoCardType.MULTIPLE_CHOICE || it.type == DemoCardType.CLOZE || it.type == DemoCardType.REVERSIBLE }.map { it.id }.toSet(),
                                        visibleCards.filter { it.type == DemoCardType.OPEN_ENDED }.map { it.id }.toSet()
                                    )
                                )
                            }
                        )
                        val pending = visibleCards.filter { it.id in dueCardIds }
                        if (pending.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            MyDeckButton(
                                label = "Revisar pendentes · ${pending.size}",
                                variant = MyDeckButtonVariant.LIGHT,
                                onClick = {
                                    setSession(
                                        startStudySession(
                                            selectedDeck.id,
                                            pending.map { it.id },
                                            pending.filter { it.type in setOf(DemoCardType.BASIC, DemoCardType.MULTIPLE_CHOICE, DemoCardType.CLOZE, DemoCardType.REVERSIBLE) }.map { it.id }.toSet(),
                                            pending.filter { it.type == DemoCardType.OPEN_ENDED }.map { it.id }.toSet()
                                        )
                                    )
                                }
                            )
                        }
                        Spacer(Modifier.height(24.dp))
                        visibleCards.forEach { card ->
                            Divider()
                            Spacer(Modifier.height(19.dp))
                            SectionLabel(card.type.label.uppercase())
                            Spacer(Modifier.height(10.dp))
                            BasicText(
                                card.question,
                                style = TextStyle(
                                    fontFamily = MyDeckDesign.font,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MyDeckDesign.ink
                                )
                            )
                            Spacer(Modifier.height(8.dp))
                            val details = when (card.type) {
                                DemoCardType.MULTIPLE_CHOICE ->
                                    card.options.size.toString() + " alternativas"
                                DemoCardType.REVERSIBLE ->
                                    listOf(card.frontLanguage, card.backLanguage)
                                        .filter(String::isNotBlank)
                                        .joinToString(" ↔ ")
                                        .ifBlank { "Cartão reversível" }
                                DemoCardType.CLOZE -> "Complete a lacuna durante o estudo"
                                DemoCardType.OPEN_ENDED -> "Resposta livre, sem gabarito"
                                DemoCardType.BASIC -> "Pergunta e resposta"
                            }
                            ScreenText(details)
                            if (card.description.isNotBlank()) {
                                Spacer(Modifier.height(8.dp))
                                ScreenText(card.description)
                            }
                            if (card.tags.isNotEmpty()) {
                                Spacer(Modifier.height(8.dp))
                                ScreenText("Tags: " + card.tags.joinToString(", "))
                            }
                            card.mediaLink?.let { material ->
                                Spacer(Modifier.height(8.dp))
                                BasicText(
                                    "Abrir " + material.type.label.lowercase(),
                                    modifier = Modifier
                                        .clickable(role = Role.Button) { uriHandler.openUri(material.url) }
                                        .padding(vertical = 12.dp),
                                    style = TextStyle(
                                        fontFamily = MyDeckDesign.font,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MyDeckDesign.ink
                                    )
                                )
                            }
                            Spacer(Modifier.height(20.dp))
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(label: String) {
    BasicText(
        label,
        style = TextStyle(
            fontFamily = MyDeckDesign.font,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MyDeckDesign.ink
        )
    )
}

@Composable
private fun Divider() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(MyDeckDesign.line))
}

@Composable
private fun ScreenText(text: String) {
    BasicText(
        text,
        style = TextStyle(
            fontFamily = MyDeckDesign.font,
            fontSize = 14.sp,
            lineHeight = 22.sp,
            color = MyDeckDesign.secondary
        )
    )
}
