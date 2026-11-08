package com.example.myapplication.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.myapplication.ui.navigation.BottomFrostedBackdrop
import com.example.myapplication.ui.navigation.MyDeckBottomBar
import com.example.myapplication.ui.navigation.MyDeckNavItem
import com.example.myapplication.ui.navigation.MyDeckTopBar
import com.example.myapplication.ui.navigation.StudentTab
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@Composable
fun StudentHomeShell(store: LocalMyDeckStore, onLogout: () -> Unit = {}) {
    val nav = rememberNavController()
    val currentEntry by nav.currentBackStackEntryAsState()
    val hazeState = remember { HazeState() }
    var selectedClassId by rememberSaveable { mutableStateOf<String?>(null) }
    val currentRoute = currentEntry?.destination?.route ?: StudentTab.HOME.route

    fun navigateTo(tab: StudentTab) {
        if (currentRoute == tab.route) return
        nav.navigate(tab.route) {
            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            
    ) {
        // Full-size content is the blur source; the bar is a separate overlay.
        Box(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars).hazeSource(hazeState)) {
            NavHost(
                navController = nav,
                startDestination = StudentTab.HOME.route
            ) {
                composable(StudentTab.HOME.route) {
                    HomeAluno(
                        onOpenClasses = { navigateTo(StudentTab.CLASSES) },
                        onOpenDecks = { navigateTo(StudentTab.DECKS) }
                    )
                }
                composable(StudentTab.CLASSES.route) {
                    StudentClassesScreen(
                        classes = store.classes.filter { it.id in store.enrolledClassIds },
                        deckCounts = store.classes.associate { classroom ->
                            classroom.id to store.decks.count { classroom.id in it.assignedClassIds }
                        },
                        onJoinClass = store::joinByCode,
                        onOpenDecks = { classId ->
                            selectedClassId = classId
                            navigateTo(StudentTab.DECKS)
                        }
                    )
                }
                composable(StudentTab.DECKS.route) {
                    StudentDecksScreen(
                        classes = store.classes.filter { it.id in store.enrolledClassIds },
                        decks = store.decks,
                        cards = store.cards,
                        selectedClassId = selectedClassId,
                        onSelectClass = { selectedClassId = it }
                    )
                }
                composable(StudentTab.DATA.route) {
                    StudentSection(
                        "Dados.",
                        "Seu histórico e desempenho aparecerão após as primeiras revisões."
                    )
                }
            }
        }

        MyDeckTopBar(
            role = "ALUNO",
            onLogout = onLogout,
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
                .padding(start = 19.dp, end = 19.dp, bottom = 17.dp)
        )
    }
}
