package com.example.myapplication.ui.teacher

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.CloudMyDeckState
import com.example.myapplication.demo.DemoCardDraft
import com.example.myapplication.demo.DemoCardType
import com.example.myapplication.demo.LocalMyDeckStore
import com.example.myapplication.learning.*
import com.example.myapplication.ui.design.MyDeckButton
import com.example.myapplication.ui.design.MyDeckButtonVariant
import java.util.UUID

/**
 * Aulas, materiais e feedback vivem dentro da turma, não como quinta aba inferior.
 * O formulário mantém apenas uma tarefa aberta por vez.
 */
@Composable
internal fun TeacherLearningScreen(
    local: LocalMyDeckStore,
    learning: LearningFeatureStore,
    cloud: CloudMyDeckState?,
    onBack: () -> Unit
) {
    var selectedClass by rememberSaveable { mutableStateOf("") }
    var section by rememberSaveable { mutableStateOf("aulas") }
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var selectedDeck by rememberSaveable { mutableStateOf("") }
    var selectedLesson by rememberSaveable { mutableStateOf("") }
    var cardQuestion by rememberSaveable { mutableStateOf("") }
    var cardAnswer by rememberSaveable { mutableStateOf("") }
    var materialTitle by rememberSaveable { mutableStateOf("") }
    var materialUrl by rememberSaveable { mutableStateOf("") }
    var materialTags by rememberSaveable { mutableStateOf("") }
    var materialCardId by rememberSaveable { mutableStateOf("") }
    var materialType by rememberSaveable { mutableStateOf(LearningResourceType.LINK.name) }
    var showCreate by rememberSaveable { mutableStateOf(false) }
    val classroom = local.classes.firstOrNull { it.id == selectedClass }
    val decks = local.decks.filter { selectedClass in it.assignedClassIds }
    val lesson = learning.lessons.firstOrNull { it.id == selectedLesson && it.classId == selectedClass }

    Column {
        TeacherAction("← Voltar para turmas", onBack)
        Spacer(Modifier.height(30.dp))
        TeacherTitle("Aulas e\nconteúdos.")
        Spacer(Modifier.height(12.dp))
        TeacherBody("Planeje aulas, prepare cartões e acompanhe o que seus alunos precisam.")
        Spacer(Modifier.height(48.dp))
        TeacherLabel("TURMA")
        Spacer(Modifier.height(12.dp))
        if (local.classes.isEmpty()) TeacherBody("Crie uma turma antes de planejar aulas.")
        local.classes.forEach { item ->
            TeacherSelectionRow(item.name, selectedClass == item.id) {
                selectedClass = item.id
                selectedLesson = ""
                showCreate = false
            }
        }
        if (classroom == null) return@Column
        Spacer(Modifier.height(34.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TeacherAction("Aulas") { section = "aulas" }
            TeacherAction("Materiais") { section = "materiais" }
            TeacherAction("Feedback") { section = "feedback" }
        }
        Spacer(Modifier.height(28.dp))
        when (section) {
            "aulas" -> {
                TeacherLabel("AULAS DE ${classroom.name.uppercase()}")
                Spacer(Modifier.height(12.dp))
                learning.lessons.filter { it.classId == selectedClass }.forEach { item ->
                    TeacherItemRow(item.title,
                        if (item.status == LessonStatus.COMPLETED) "Concluída"
                        else "Planejada · ${item.cards.size} cartões em rascunho") {
                        selectedLesson = item.id
                        showCreate = false
                    }
                }
                Spacer(Modifier.height(16.dp))
                TeacherAction(if (showCreate) "Cancelar nova aula" else "+ Nova aula") {
                    showCreate = !showCreate
                    selectedLesson = ""
                }
                if (showCreate) {
                    Spacer(Modifier.height(16.dp))
                    TeacherInput("Título da aula", "Ex.: Gramática — concordância", title) { title = it.take(140) }
                    Spacer(Modifier.height(12.dp))
                    TeacherInput("Descrição (opcional)", "O que será abordado?", description) { description = it.take(500) }
                    Spacer(Modifier.height(16.dp))
                    TeacherLabel("DECK RELACIONADO (OPCIONAL)")
                    TeacherSelectionRow("Nenhum deck", selectedDeck.isBlank()) { selectedDeck = "" }
                    decks.forEach { deck ->
                        TeacherSelectionRow(deck.title, selectedDeck == deck.id) { selectedDeck = deck.id }
                    }
                    Spacer(Modifier.height(16.dp))
                    MyDeckButton("Salvar aula planejada", MyDeckButtonVariant.DARK,
                        onClick = if (title.isNotBlank()) ({
                            learning.createLesson(selectedClass, title, description, selectedDeck)
                            title = ""; description = ""; showCreate = false
                        }) else null
                    )
                }
                if (lesson != null) {
                    Spacer(Modifier.height(40.dp))
                    TeacherLabel("AULA SELECIONADA")
                    Spacer(Modifier.height(12.dp))
                    TeacherItemRow(lesson.title,
                        if (lesson.status == LessonStatus.COMPLETED) "Aula concluída"
                        else "Aula planejada · ${lesson.cards.size} cartões preparados")
                    if (lesson.description.isNotBlank()) TeacherBody(lesson.description)
                    if (lesson.status == LessonStatus.PLANNED) {
                        Spacer(Modifier.height(24.dp))
                        TeacherLabel("PREPARAR FLASHCARDS")
                        Spacer(Modifier.height(10.dp))
                        TeacherBody("Os cartões permanecem em rascunho até a conclusão da aula.")
                        Spacer(Modifier.height(16.dp))
                        if (decks.isEmpty()) TeacherBody("Atribua um deck à turma antes de preparar cartões.")
                        else {
                            decks.forEach { deck ->
                                TeacherSelectionRow(deck.title,
                                    (if (selectedDeck.isBlank()) lesson.deckId else selectedDeck) == deck.id) {
                                    selectedDeck = deck.id
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            TeacherInput("Pergunta", "Escreva a pergunta", cardQuestion) { cardQuestion = it }
                            Spacer(Modifier.height(10.dp))
                            TeacherInput("Resposta", "Escreva a resposta", cardAnswer) { cardAnswer = it }
                            Spacer(Modifier.height(14.dp))
                            val draftDeck = selectedDeck.ifBlank { lesson.deckId }
                            MyDeckButton("Adicionar ao rascunho", MyDeckButtonVariant.LIGHT,
                                onClick = if (draftDeck.isNotBlank() &&
                                    cardQuestion.isNotBlank() && cardAnswer.isNotBlank()) ({
                                    learning.addDraft(lesson, draftDeck, cardQuestion, cardAnswer)
                                    cardQuestion = ""; cardAnswer = ""
                                }) else null
                            )
                        }
                        lesson.cards.forEach { draft ->
                            TeacherItemRow(draft.question, "Cartão preparado · ${draft.answer}")
                        }
                        Spacer(Modifier.height(26.dp))
                        MyDeckButton("Concluir aula e publicar cartões", MyDeckButtonVariant.DARK, onClick = {
                            learning.completeLesson(lesson,
                                publishLocally = { deckId, question, answer ->
                                    local.addCard(deckId, DemoCardDraft(DemoCardType.BASIC, question, answer))
                                }, onCloudDone = { cloud?.refresh() }
                            )
                        })
                        Spacer(Modifier.height(12.dp))
                        TeacherBody("A conclusão também libera o feedback para os alunos. Uma aula pode não ter cartões.")
                    } else {
                        TeacherBody("Os cartões foram disponibilizados no deck escolhido.")
                    }
                }
            }
            "materiais" -> {
                TeacherLabel("BIBLIOTECA DE APOIO")
                Spacer(Modifier.height(16.dp))
                TeacherBody("Adicione links HTTPS para PDF, vídeos ou páginas e relacione-os a um assunto.")
                Spacer(Modifier.height(20.dp))
                learning.resources.filter { it.classId == selectedClass }.forEach { material ->
                    TeacherItemRow(material.title, "${material.type.name} · ${material.tags.joinToString(", ")}")
                }
                Spacer(Modifier.height(22.dp))
                TeacherInput("Título do material", "Ex.: Revisão de concordância", materialTitle) {
                    materialTitle = it.take(140)
                }
                Spacer(Modifier.height(12.dp))
                TeacherInput("Link HTTPS", "https://...", materialUrl) { materialUrl = it.take(2048) }
                Spacer(Modifier.height(12.dp))
                TeacherLabel("TIPO")
                LearningResourceType.entries.forEach { type ->
                    TeacherSelectionRow(when (type) {
                        LearningResourceType.PDF -> "Documento PDF"
                        LearningResourceType.VIDEO -> "Vídeo"
                        LearningResourceType.LINK -> "Página da web"
                    }, materialType == type.name) { materialType = type.name }
                }
                Spacer(Modifier.height(16.dp))
                TeacherLabel("ASSOCIAR AO DECK")
                TeacherSelectionRow("Todos os decks desta turma", selectedDeck.isBlank()) { selectedDeck = "" }
                decks.forEach { deck ->
                    TeacherSelectionRow(deck.title, selectedDeck == deck.id) { selectedDeck = deck.id }
                }
                Spacer(Modifier.height(12.dp))
                TeacherLabel("ASSOCIAR À AULA")
                TeacherSelectionRow("Sem aula específica", selectedLesson.isBlank()) { selectedLesson = "" }
                learning.lessons.filter { it.classId == selectedClass }.forEach { item ->
                    TeacherSelectionRow(item.title, selectedLesson == item.id) { selectedLesson = item.id }
                }
                Spacer(Modifier.height(12.dp))
                TeacherInput("Tags (separadas por vírgula)", "Ex.: gramática, concordância", materialTags) {
                    materialTags = it
                }
                Spacer(Modifier.height(12.dp))
                TeacherLabel("VINCULAR A UM CARTÃO (OPCIONAL)")
                Spacer(Modifier.height(10.dp))
                TeacherSelectionRow("Sem cartão específico", materialCardId.isBlank()) {
                    materialCardId = ""
                }
                val availableCards = local.cards.filter {
                    selectedDeck.isBlank() || it.deckId == selectedDeck
                }.take(50)
                availableCards.forEach { card ->
                    TeacherSelectionRow(
                        card.question.take(72),
                        materialCardId == card.id
                    ) { materialCardId = card.id }
                }
                Spacer(Modifier.height(18.dp))
                MyDeckButton("Salvar material", MyDeckButtonVariant.DARK,
                    onClick = if (materialTitle.isNotBlank() && validLearningUrl(materialUrl)) ({
                        learning.saveMaterial(LearningResource(UUID.randomUUID().toString(),
                            materialTitle.trim(), materialUrl.trim(),
                            LearningResourceType.valueOf(materialType), selectedClass,
                            selectedDeck, materialTags.split(",").map(String::trim).filter(String::isNotBlank),
                            materialCardId, selectedLesson))
                        materialTitle = ""; materialUrl = ""; materialTags = ""; materialCardId = ""
                    }) else null
                )
            }
            "feedback" -> {
                TeacherLabel("RETORNO DOS ALUNOS")
                Spacer(Modifier.height(16.dp))
                val items = learning.feedbacks.filter { it.classId == selectedClass }
                TeacherBody("${items.size} feedbacks recebidos nesta turma.")
                Spacer(Modifier.height(28.dp))
                val rated = items.filter { it.lessonId.isNotBlank() }
                TeacherMetric("Dificuldade relatada", rated.count { it.understanding == FeedbackUnderstanding.DIFFICULT })
                Spacer(Modifier.height(32.dp))
                items.sortedByDescending { it.createdAt }.forEach { fb ->
                    val label = learning.lessons.firstOrNull { it.id == fb.lessonId }?.title ?: "Feedback geral"
                    TeacherItemRow(label, when (fb.understanding) {
                        FeedbackUnderstanding.CLEAR -> "Entendeu bem"
                        FeedbackUnderstanding.PARTIAL -> "Entendeu parcialmente"
                        FeedbackUnderstanding.DIFFICULT -> "Relatou dificuldade"
                    } + if (fb.comment.isBlank()) "" else " · ${fb.comment}")
                }
                if (items.isEmpty()) TeacherBody("Nenhuma avaliação recebida ainda.")
            }
        }
        learning.error?.let { TeacherBody("Aviso: $it") }
        Spacer(Modifier.height(64.dp))
    }
}
