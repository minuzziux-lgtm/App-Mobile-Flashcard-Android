package com.example.myapplication.study

import org.junit.Assert.*
import org.junit.Test

class TagDifficultyTest {
    @Test fun ignoresUngradedAndSortsByErrorRate() {
        fun e(id:String, correct:Boolean?,tag:String) = StudyReview(
            id,"@aluno-demo","deck",id,null,correct,null,listOf(tag),0,0
        )
        val stats = difficultTags(listOf(e("1",false,"A"),e("2",true,"A"),e("3",false,"B"),e("4",null,"B")))
        assertEquals("B", stats.first().tag)
        assertEquals(100, stats.first().errorPercent)
        assertEquals(2, stats.last().graded)
    }
}
