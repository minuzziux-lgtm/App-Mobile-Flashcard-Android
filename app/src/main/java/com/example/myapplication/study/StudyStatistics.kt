package com.example.myapplication.study

data class StudySummary(
    val total: Int,
    val graded: Int,
    val correct: Int,
    val incorrect: Int,
    val accuracyPercent: Int
)

fun summarizeReviews(reviews: List<StudyReview>): StudySummary {
    val correct = reviews.count { it.correct == true }
    val incorrect = reviews.count { it.correct == false }
    val graded = correct + incorrect
    return StudySummary(
        total = reviews.size,
        graded = graded,
        correct = correct,
        incorrect = incorrect,
        accuracyPercent = if (graded == 0) 0 else correct * 100 / graded
    )
}
