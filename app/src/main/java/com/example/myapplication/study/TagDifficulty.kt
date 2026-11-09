package com.example.myapplication.study

data class TagDifficulty(val tag: String, val graded: Int, val mistakes: Int, val errorPercent: Int)

fun difficultTags(reviews: List<StudyReview>): List<TagDifficulty> {
    val pairs = reviews.filter { it.correct != null }
        .flatMap { review -> review.tags.distinct().map { tag -> tag to review.correct } }
        .groupBy { it.first }
    return pairs.map { (tag, occurrences) ->
        val mistakes = occurrences.count { it.second == false }
        TagDifficulty(tag, occurrences.size, mistakes, mistakes * 100 / occurrences.size)
    }.sortedWith(compareByDescending<TagDifficulty> { it.errorPercent }
        .thenByDescending { it.graded }.thenBy { it.tag })
}
