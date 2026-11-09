package com.example.myapplication.data

import com.example.myapplication.study.StudyReview
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CloudPerformanceSnapshotTest {
    private val events = listOf(
        StudyReview("1","aluno-a","deck-a","card-a","turma-1",true,null,listOf("tag-a"),1000L,2000L),
        StudyReview("2","aluno-a","deck-a","card-b","turma-1",false,null,listOf("tag-a"),2000L,3000L),
        StudyReview("3","aluno-b","deck-b","card-c","turma-2",null,"Texto",listOf("tag-b"),3000L,4000L)
    )

    @Test fun aggregatesOnlyRequestedStudentAndDeck() {
        val filtered = aggregateCloudPerformance(events, studentId = "aluno-a", deckId = "deck-a")
        assertEquals(2, filtered.summary.total)
        assertEquals(1, filtered.summary.correct)
        assertEquals(1, filtered.summary.incorrect)
        assertEquals(50, filtered.summary.accuracyPercent)
        assertEquals(setOf("turma-1"), filtered.totalByClass.keys)
        assertEquals(setOf("deck-a"), filtered.totalByDeck.keys)
    }

    @Test fun ungradedWrittenResponseDoesNotCountAsIncorrect() {
        val summary = aggregateCloudPerformance(events)
        assertEquals(3, summary.summary.total)
        assertEquals(2, summary.summary.graded)
        assertEquals(1, summary.summary.incorrect)
        val filtered = aggregateCloudPerformance(events, tag = "tag-b")
        assertEquals(0, filtered.summary.graded)
        assertTrue(filtered.difficultTags.isEmpty())
    }
}
