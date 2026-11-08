package com.example.myapplication.ui.teacher

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.myapplication.ui.navigation.BottomFrostedBackdrop
import com.example.myapplication.ui.navigation.SmokedGlassSurface
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import java.util.UUID

private enum class TeacherTab(val label: String) {
    HOME("Início"), CLASSES("Turmas"), DECKS("Decks"), PERFORMANCE("Dados")
}

@Composable
fun TeacherHomeShell(onLogout: () -> Unit = {}) {
    val classes = remember { mutableStateListOf<TeacherClass>() }
    var selectedClassId by rememberSaveable { mutableStateOf<String?>(null) }
    var tab by rememberSaveable { mutableStateOf(TeacherTab.HOME.name) }
    val activeTab = TeacherTab.entries.firstOrNull { it.name == tab } ?: TeacherTab.HOME
    val hazeState = remember { HazeState() }

    Box(Modifier.fillMaxSize().background(Color.White)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .hazeSource(hazeState),
            contentPadding = PaddingValues(start = 26.dp, end = 26.dp, top = 32.dp, bottom = 168.dp)
        ) {
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TeacherLabel("MY-DECK  /  PROFESSOR")
                    BasicText(
                        "Sair",
                        modifier = Modifier.clickable(role = Role.Button, onClick = onLogout)
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        style = teacherStyle(12, FontWeight.Medium, MyDeckDesign.secondary)
                    )
                }
                Spacer(Modifier.height(54.dp))
                when (activeTab) {
                    TeacherTab.HOME -> TeacherDashboard(
                        classes.size,
                        0,
                        0,
                        onClasses = { tab = TeacherTab.CLASSES.name },
                        onDecks = { tab = TeacherTab.DECKS.name }
                    )
                    TeacherTab.CLASSES -> {
                        TeacherClassesSection(
                            classes = classes,
                            onCreateClass = { name ->
                                val created = TeacherClass(UUID.randomUUID().toString(), name)
                                classes.add(created)
                                selectedClassId = created.id
                                
                            },
                            onOpenClass = { id ->
                                selectedClassId = id
                                
                                tab = TeacherTab.DECKS.name
                            }
                        )
                    }
                    TeacherTab.DECKS -> {
                        TeacherPlaceholder("Decks.", "Os decks e cartões serão adicionados na próxima etapa.")
                    }
                    TeacherTab.PERFORMANCE -> {
                        TeacherTitle("Desempenho.")
                        Spacer(Modifier.height(14.dp))
                        TeacherBody("Acompanhe alunos, turmas e decks após as primeiras sessões de estudo.")
                        Spacer(Modifier.height(42.dp))
                        TeacherDivider()
                        Spacer(Modifier.height(22.dp))
                        TeacherLabel("01  /  ATIVIDADE")
                        Spacer(Modifier.height(34.dp))
                        TeacherEmpty("Sem revisões registradas. Gráficos e filtros serão integrados depois.")
                    }
                }
            }
        }

        BottomFrostedBackdrop(hazeState, Modifier.align(Alignment.BottomCenter))
        SmokedGlassSurface(
            hazeState,
            Modifier.align(Alignment.BottomCenter)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(start = 19.dp, end = 19.dp, bottom = 17.dp)
                .height(58.dp)
        ) {
            Row(
                Modifier.fillMaxSize().padding(5.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TeacherTab.entries.forEach { item ->
                    val isActive = item == activeTab
                    Box(
                        modifier = Modifier.weight(1f).fillMaxSize()
                            .background(if (isActive) Color.White else Color.Transparent, CircleShape)
                            .semantics {
                                selected = isActive
                                contentDescription = item.label
                            }
                            .clickable(role = Role.Tab) { tab = item.name },
                        contentAlignment = Alignment.Center
                    ) {
                        BasicText(
                            item.label,
                            style = teacherStyle(
                                12,
                                if (isActive) FontWeight.SemiBold else FontWeight.Medium,
                                if (isActive) MyDeckDesign.ink else Color.White
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TeacherDashboard(
    classCount: Int,
    deckCount: Int,
    cardCount: Int,
    onClasses: () -> Unit,
    onDecks: () -> Unit
) {
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
        TeacherBody("Dados locais de demonstração. Nada foi publicado no Firebase.")
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
    TeacherHomeShell()
}
