package com.example.myapplication.data

import com.example.myapplication.demo.DemoCard
import com.example.myapplication.demo.DemoCardDraft
import com.example.myapplication.demo.DemoClass
import com.example.myapplication.demo.DemoDeck
import com.example.myapplication.study.StudyReview

/** Fluxo comum para a biblioteca de demonstração e o futuro armazenamento Firestore.
 *
 * Operações são assíncronas por contrato para não bloquear a UI quando houver rede.
 * O contexto de autenticação do servidor, nunca uma flag da tela, define permissões.
 */
interface MyDeckContentRepository {
    fun teacherClasses(onResult: (DataResult<List<DemoClass>>) -> Unit)
    fun studentClasses(studentId: String, onResult: (DataResult<List<DemoClass>>) -> Unit)
    fun decksForClass(classId: String, onResult: (DataResult<List<DemoDeck>>) -> Unit)
    fun cardsForDeck(deckId: String, onResult: (DataResult<List<DemoCard>>) -> Unit)
    fun reviewsForStudent(studentId: String, onResult: (DataResult<List<StudyReview>>) -> Unit)

    fun createClass(name: String, onResult: (DataResult<DemoClass>) -> Unit)
    fun joinClass(code: String, studentId: String, onResult: (DataResult<DemoClass>) -> Unit)
    fun createDeck(title: String, classId: String, onResult: (DataResult<DemoDeck>) -> Unit)
    fun assignDeckToClasses(deckId: String, classIds: List<String>, onResult: (DataResult<Unit>) -> Unit)
    fun addCard(deckId: String, card: DemoCardDraft, onResult: (DataResult<DemoCard>) -> Unit)
    fun recordReview(review: StudyReview, onResult: (DataResult<Unit>) -> Unit)
}
