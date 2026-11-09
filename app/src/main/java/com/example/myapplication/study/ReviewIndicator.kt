package com.example.myapplication.study

data class ReviewIndicator(val totalPending: Int, val filledDots: Int, val capacity: Int, val overflow: Int)

/** Mantém 40 pontos físicos; acima disso, o contador textual exibe o total. */
fun reviewIndicator(totalPending: Int, capacity: Int = 40): ReviewIndicator {
    require(capacity > 0)
    val pending = totalPending.coerceAtLeast(0)
    return ReviewIndicator(pending, pending.coerceAtMost(capacity), capacity,
        (pending - capacity).coerceAtLeast(0))
}
