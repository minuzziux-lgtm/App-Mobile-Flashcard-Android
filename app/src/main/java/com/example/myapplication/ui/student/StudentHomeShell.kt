package com.example.myapplication.ui.student

import androidx.activity.compose.BackHandler
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import com.example.myapplication.ui.design.MyDeckDesign
import com.example.myapplication.ui.design.MyDeckDimensions
import com.example.myapplication.ui.design.MyDeckLocale
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.myapplication.data.CloudMyDeckState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.demo.LocalMyDeckStore
import com.example.myapplication.ui.design.MyDeckButton
import com.example.myapplication.ui.design.MyDeckButtonVariant
import com.example.myapplication.ui.navigation.BottomFrostedBackdrop
import com.example.myapplication.ui.navigation.MyDeckBottomBar
import com.example.myapplication.ui.navigation.MyDeckNavItem
import com.example.myapplication.ui.navigation.MyDeckTopBar
import com.example.myapplication.ui.navigation.StudentTab
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@Composable
fun StudentHomeShell(
    store: LocalMyDeckStore,
    onLogout: () -> Unit = {},
    email: String? = null,
    studentId: String = "@aluno-demo",
    cloud: CloudMyDeckState? = null
) {
    LaunchedEffect(cloud) { cloud?.refresh() }
    val nav = rememberNavController()
    val currentEntry by nav.currentBackStackEntryAsState()
    val hazeState = remember { HazeState() }
    var selectedClassId by rememberSaveable { mutableStateOf<String?>(null) }
    var showProfile by rememberSaveable { mutableStateOf(false) }
    var studyLaunchRequest by rememberSaveable { mutableIntStateOf(0) }
    var studyDueOnly by rememberSaveable { mutableStateOf(false) }
    val currentRoute = currentEntry?.destination?.route ?: StudentTab.HOME.route

    fun navigateTo(tab: StudentTab) {
        showProfile = false
        if (tab == StudentTab.HOME) {
            // O início permanece na base da pilha; não empilhe cópias da Home.
            if (!nav.popBackStack(StudentTab.HOME.route, inclusive = false) &&
                currentRoute != StudentTab.HOME.route) {
                nav.navigate(StudentTab.HOME.route) { launchSingleTop = true }
            }
            return
        }
        if (currentRoute == tab.route) return
        nav.navigate(tab.route) {
            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun openStudy(dueOnly: Boolean) {
        studyDueOnly = dueOnly
        studyLaunchRequest += 1
        navigateTo(StudentTab.DECKS)
    }

    BackHandler(enabled = showProfile || currentRoute != StudentTab.HOME.route) {
        if (showProfile) showProfile = false else navigateTo(StudentTab.HOME)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MyDeckDesign.background)
            
    ) {
        // Full-size content is the blur source; the bar is a separate overlay.
        Box(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars).hazeSource(hazeState)) {
            if (showProfile) {
                StudentSection(
                    title = "Perfil.",
                    description = "Identificação do acesso atual."
                ) {
                    HistoryHeader("ALUNO")
                    androidx.compose.foundation.layout.Spacer(Modifier.padding(vertical = 12.dp))
                    HistoryText(email ?: "Modo de demonstração: sem e-mail autenticado.")
                    androidx.compose.foundation.layout.Spacer(Modifier.padding(vertical = 12.dp))
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
                    androidx.compose.foundation.layout.Spacer(Modifier.padding(vertical = 6.dp))
                    MyDeckButton(MyDeckLocale.text("Sair da conta", "Sign out"), MyDeckButtonVariant.DARK, onClick = onLogout)
                }
            } else NavHost(
                navController = nav,
                startDestination = StudentTab.HOME.route,
                enterTransition = { fadeIn(animationSpec = tween(durationMillis = 140)) },
                exitTransition = { fadeOut(animationSpec = tween(durationMillis = 90)) },
                popEnterTransition = { fadeIn(animationSpec = tween(durationMillis = 140)) },
                popExitTransition = { fadeOut(animationSpec = tween(durationMillis = 90)) }
            ) {
                composable(StudentTab.HOME.route) {
                    HomeAluno(
                        onOpenClasses = { navigateTo(StudentTab.CLASSES) },
                        onOpenDecks = { navigateTo(StudentTab.DECKS) },
                        reviewCount = store.pendingCards(studentId = studentId).size,
                        studyCount = store.cards.count { card ->
                            store.decks.any { deck ->
                                deck.id == card.deckId &&
                                    deck.assignedClassIds.any { it in store.enrolledForStudent(studentId) }
                            }
                        },
                        onStartStudy = { openStudy(dueOnly = false) },
                        onStartReviews = { openStudy(dueOnly = true) },
                        cloudStatus = if (cloud == null) null else if (cloud.loading) {
                            "Atualizando seus dados do Firebase..."
                        } else cloud.notice ?: "Dados da conta carregados do Firebase.",
                        onRefresh = cloud?.let { { it.refresh() } }
                    )
                }
                composable(StudentTab.CLASSES.route) {
                    StudentClassesScreen(
                        classes = store.classes.filter { it.id in store.enrolledForStudent(studentId) },
                        deckCounts = store.classes.associate { classroom ->
                            classroom.id to store.decks.count { classroom.id in it.assignedClassIds }
                        },
                        onJoinClass = { code -> store.joinByCode(code, studentId) },
                        onOpenDecks = { classId ->
                            selectedClassId = classId
                            navigateTo(StudentTab.DECKS)
                        },
                        onJoinClassAsync = if (cloud != null) {
                            { code, callback -> cloud.joinClass(code, callback) }
                        } else null
                    )
                }
                composable(StudentTab.DECKS.route) {
                    StudentDecksScreen(
                        classes = store.classes.filter { it.id in store.enrolledForStudent(studentId) },
                        decks = store.decks,
                        cards = store.cards,
                        selectedClassId = selectedClassId,
                        onSelectClass = { selectedClassId = it },
                        onRecordReview = { review ->
                            if (cloud == null) store.recordReview(review)
                            else cloud.recordReview(review)
                        },
                        dueCardIds = store.pendingCards(studentId = studentId).map { it.id }.toSet(),
                        studentId = studentId,
                        launchStudyRequest = studyLaunchRequest,
                        launchDueOnly = studyDueOnly,
                        onReturnHome = { navigateTo(StudentTab.HOME) }
                    )
                }
                composable(StudentTab.DATA.route) {
                    StudentHistoryScreen(
                        reviews = store.reviews.filter { it.studentId == studentId },
                        decks = store.decks
                    )
                }
            }
        }

        MyDeckTopBar(
            role = "ALUNO",
            onLogout = onLogout,
            onProfile = { showProfile = !showProfile },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 26.dp)
        )

        BottomFrostedBackdrop(
            hazeState = hazeState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        MyDeckBottomBar(
            currentId = currentRoute,
            items = StudentTab.entries.map { MyDeckNavItem(it.route, it.label) },
            hazeState = hazeState,
            onNavigate = { id ->
                StudentTab.entries.firstOrNull { it.route == id }?.let(::navigateTo)
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(start = MyDeckDimensions.navigationHorizontalInset, end = MyDeckDimensions.navigationHorizontalInset, bottom = MyDeckDimensions.navigationBottomInset)
        )
    }
}
