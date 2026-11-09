package com.example.myapplication.ui.teacher

import android.content.pm.ApplicationInfo
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.myapplication.data.CloudMyDeckState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.design.MyDeckButton
import com.example.myapplication.ui.design.MyDeckButtonVariant
import com.example.myapplication.ui.design.MyDeckDesign
import com.example.myapplication.ui.design.MyDeckLocale
import com.example.myapplication.ui.design.MyDeckDimensions
import com.example.myapplication.ui.navigation.BottomFrostedBackdrop
import com.example.myapplication.ui.navigation.MyDeckBottomBar
import com.example.myapplication.ui.navigation.MyDeckNavItem
import com.example.myapplication.ui.navigation.MyDeckTopBar
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import com.example.myapplication.demo.LocalMyDeckStore

private enum class TeacherTab(private val pt: String, private val en: String) {
    HOME("Início", "Home"), CLASSES("Turmas", "Classes"),
    DECKS("Decks", "Decks"), PERFORMANCE("Dados", "Data");
    val label: String get() = MyDeckLocale.text(pt, en)
}

@Composable
fun TeacherHomeShell(
    store: LocalMyDeckStore,
    onLogout: () -> Unit = {},
    email: String? = null,
    cloud: CloudMyDeckState? = null
) {
    LaunchedEffect(cloud) { cloud?.refresh() }
    val classes = store.classes
    var selectedClassId by rememberSaveable { mutableStateOf<String?>(null) }
    val decks = store.decks
    val cards = store.cards
    var selectedDeckId by rememberSaveable { mutableStateOf<String?>(null) }
    var tab by rememberSaveable { mutableStateOf(TeacherTab.HOME.name) }
    val activeTab = TeacherTab.entries.firstOrNull { it.name == tab } ?: TeacherTab.HOME
    val hazeState = remember { HazeState() }
    var showProfile by rememberSaveable { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(MyDeckDesign.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .hazeSource(hazeState),
            contentPadding = PaddingValues(start = MyDeckDimensions.screenPadding, end = MyDeckDimensions.screenPadding, top = MyDeckDimensions.contentTop, bottom = MyDeckDimensions.contentBottom)
        ) {
            item {
                if (cloud != null) {
                    TeacherBody(
                        when {
                            cloud.loading -> "Carregando dados do Firebase..."
                            cloud.notice != null -> cloud.notice.orEmpty()
                            else -> "Conta autenticada · biblioteca Firebase"
                        }
                    )
                    Spacer(Modifier.height(12.dp))
                    MyDeckButton(
                        "Atualizar dados do Firebase",
                        MyDeckButtonVariant.LIGHT,
                        onClick = { cloud.refresh() }
                    )
                    Spacer(Modifier.height(26.dp))
                }
                if (showProfile) {
                    TeacherTitle("Perfil.")
                    Spacer(Modifier.height(24.dp))
                    TeacherLabel("PROFESSOR")
                    Spacer(Modifier.height(14.dp))
                    TeacherBody(email ?: "Modo demonstrativo sem e-mail autenticado.")
                    Spacer(Modifier.height(28.dp))
                    val uiContext = LocalContext.current
                    MyDeckButton(
                        MyDeckLocale.text("Idioma: Português", "Language: English"),
                        MyDeckButtonVariant.LIGHT,
                        onClick = { MyDeckLocale.toggle(uiContext) }
                    )
                    androidx.compose.foundation.layout.Spacer(Modifier.padding(vertical = 6.dp))
                    MyDeckButton(
                        if (MyDeckDesign.darkMode) "Usar tema claro" else "Usar tema escuro",
                        MyDeckButtonVariant.LIGHT,
                        onClick = { MyDeckDesign.toggleTheme(uiContext) }
                    )
                    androidx.compose.foundation.layout.Spacer(Modifier.padding(vertical = 6.dp))
                    MyDeckButton(MyDeckLocale.text("Voltar", "Back"), MyDeckButtonVariant.LIGHT, onClick = { showProfile = false })
                    Spacer(Modifier.height(12.dp))
                    MyDeckButton(MyDeckLocale.text("Sair da conta", "Sign out"), MyDeckButtonVariant.DARK, onClick = onLogout)
                } else when (activeTab) {
                    TeacherTab.HOME -> TeacherDashboard(
                        classes.size,
                        decks.size,
                        cards.size,
                        onClasses = { tab = TeacherTab.CLASSES.name },
                        onDecks = { tab = TeacherTab.DECKS.name },
                        onLoadDemo = store::seedDemoScenario,
                        cloudMode = cloud != null
                    )
                    TeacherTab.CLASSES -> {
                        TeacherClassesSection(
                            classes = classes,
                            decks = decks,
                            cards = cards,
                            participants = store.participants,
                            onAddParticipant = { classId, name ->
                                if (cloud == null) store.addDemoParticipant(classId, name)
                            },
                            onRemoveParticipant = { id ->
                                if (cloud == null) store.removeDemoParticipant(id)
                                else cloud.removeStudent(id)
                            },
                            onCreateClass = { name ->
                                if (cloud == null) {
                                    val created = store.createClass(name)
                                    selectedClassId = created.id
                                    selectedDeckId = null
                                } else cloud.createClass(name) { createdId ->
                                    selectedClassId = createdId
                                    selectedDeckId = null
                                }
                            },
                            onOpenClass = { id ->
                                selectedClassId = id
                                selectedDeckId = null
                                tab = TeacherTab.DECKS.name
                            },
                            onRenameClass = { id, name ->
                                if (cloud == null) store.renameClass(id, name)
                                else cloud.renameClass(id, name)
                            },
                            onDeleteClass = { id ->
                                if (cloud == null) {
                                    store.deleteClass(id)
                                    if (selectedClassId == id) {
                                        selectedClassId = null
                                        selectedDeckId = null
                                    }
                                }
                            },
                            cloudMode = cloud != null
                        )
                    }
                    TeacherTab.DECKS -> {
                        TeacherDecksSection(
                            classes = classes,
                            decks = decks,
                            cards = cards,
                            availableTags = store.availableTags,
                            onCreateTag = store::addTag,
                            onRenameTag = store::renameTag,
                            onDeleteTag = store::deleteTag,
                            selectedClassId = selectedClassId,
                            selectedDeckId = selectedDeckId,
                            onSelectClass = {
                                selectedClassId = it.takeIf(String::isNotBlank)
                                selectedDeckId = null
                            },
                            onCreateDeck = { classId, title ->
                                if (cloud == null) {
                                    val created = store.createDeck(classId, title)
                                    selectedClassId = classId.takeIf(String::isNotBlank)
                                    selectedDeckId = created.id
                                } else cloud.createDeck(classId, title) { createdId ->
                                    selectedClassId = classId.takeIf(String::isNotBlank)
                                    selectedDeckId = createdId
                                }
                            },
                            onRenameDeck = { id, title ->
                                if (cloud == null) store.renameDeck(id, title)
                                else cloud.renameDeck(id, title)
                            },
                            onAssignDeck = { deckId, classIds ->
                                if (cloud == null) store.assignDeckToClasses(deckId, classIds)
                                else cloud.assignDeck(deckId, classIds)
                                if (selectedClassId != null && selectedClassId !in classIds) {
                                    selectedClassId = null
                                }
                            },
                            onDeleteDeck = { id ->
                                if (cloud == null) {
                                    store.deleteDeck(id)
                                    if (selectedDeckId == id) selectedDeckId = null
                                }
                            },
                            onSelectDeck = { selectedDeckId = it },
                            onAddCard = { deckId, draft ->
                                if (cloud == null) store.addCard(deckId, draft)
                                else cloud.addCard(deckId, draft)
                            },
                            onUpdateCard = { id, draft ->
                                if (cloud == null) store.updateCard(id, draft)
                                else cloud.updateCard(id, draft)
                            },
                            onDuplicateCard = { id ->
                                if (cloud == null) store.duplicateCard(id)
                                else cloud.duplicateCard(id)
                            },
                            onDeleteCard = { id ->
                                if (cloud == null) store.deleteCard(id)
                                else cloud.deleteCard(id)
                            },
                            onOpenClasses = { tab = TeacherTab.CLASSES.name },
                            cloudMode = cloud != null
                        )
                    }
                    TeacherTab.PERFORMANCE -> TeacherPerformanceSection(
                        reviews = store.reviews,
                        classes = store.classes,
                        decks = store.decks,
                        participants = store.participants,
                        cloudMode = cloud != null
                    )
                }
            }
        }

        MyDeckTopBar(
            role = "PROFESSOR",
            onLogout = onLogout,
            onProfile = { showProfile = !showProfile },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 26.dp)
        )

        BottomFrostedBackdrop(hazeState, Modifier.align(Alignment.BottomCenter))
        MyDeckBottomBar(
            currentId = activeTab.name,
            items = TeacherTab.entries.map { MyDeckNavItem(it.name, it.label) },
            hazeState = hazeState,
            onNavigate = { tab = it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(start = 19.dp, end = 19.dp, bottom = 17.dp)
        )
    }
}

@Composable
private fun TeacherDashboard(
    classCount: Int,
    deckCount: Int,
    cardCount: Int,
    onClasses: () -> Unit,
    onDecks: () -> Unit,
    onLoadDemo: () -> Boolean,
    cloudMode: Boolean = false
) {
    val ctx = LocalContext.current
    val debugMode = (ctx.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    Column {
        TeacherTitle("Sua área de\nensino.")
        Spacer(Modifier.height(16.dp))
        TeacherBody("Organize suas turmas, prepare os cartões e acompanhe o aprendizado.")
        Spacer(Modifier.height(42.dp))
        TeacherDivider()
        Spacer(Modifier.height(22.dp))
        TeacherLabel("01  /  VISÃO GERAL")
        Spacer(Modifier.height(26.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(15.dp)) {
            TeacherMetric("Turmas", classCount, Modifier.weight(1f))
            TeacherMetric("Decks", deckCount, Modifier.weight(1f))
            TeacherMetric("Cartões", cardCount, Modifier.weight(1f))
        }
        Spacer(Modifier.height(52.dp))
        TeacherDivider()
        Spacer(Modifier.height(22.dp))
        TeacherLabel("02  /  COMEÇAR")
        Spacer(Modifier.height(24.dp))
        MyDeckButton("Criar turma", MyDeckButtonVariant.DARK, onClick = onClasses)
        Spacer(Modifier.height(12.dp))
        MyDeckButton("Gerenciar decks", MyDeckButtonVariant.LIGHT, onClick = onDecks)
        Spacer(Modifier.height(52.dp))
        TeacherBody(
            if (cloudMode) "Dados carregados do Firebase para esta conta."
            else "Dados locais de demonstração. Nada foi publicado no Firebase."
        )
        if (!cloudMode && debugMode && classCount == 0 && deckCount == 0) {
            Spacer(Modifier.height(16.dp))
            MyDeckButton(
                "Carregar cenário de teste",
                MyDeckButtonVariant.LIGHT,
                onClick = { onLoadDemo() }
            )
        }
    }
}

@Composable
private fun TeacherPlaceholder(title: String, text: String) {
    Column {
        TeacherTitle(title)
        Spacer(Modifier.height(18.dp))
        TeacherBody(text)
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun TeacherHomePreview() {
    TeacherHomeShell(store = remember { LocalMyDeckStore() })
}
