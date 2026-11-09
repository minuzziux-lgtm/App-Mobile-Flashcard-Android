package com.example.myapplication.study

/** Validação determinística do gabarito de múltipla escolha. */
fun gradeMultipleChoice(selected: Int, correctIndex: Int?, optionCount: Int): StudyRecall? {
    if (optionCount !in 2..4 || correctIndex == null || correctIndex !in 0 until optionCount ||
        selected !in 0 until optionCount) return null
    return if (selected == correctIndex) StudyRecall.CORRECT else StudyRecall.INCORRECT
}

/** Confere resposta textual com espaços normalizados e capitalização ignorada. */
fun gradeCloze(typed: String, expected: String): StudyRecall? {
    val normalizedTyped = typed.trim().replace(Regex("\\s+"), " ")
    val normalizedExpected = expected.trim().replace(Regex("\\s+"), " ")
    if (normalizedTyped.isEmpty() || normalizedExpected.isEmpty()) return null
    return if (normalizedTyped.equals(normalizedExpected, ignoreCase = true)) {
        StudyRecall.CORRECT
    } else StudyRecall.INCORRECT
}
