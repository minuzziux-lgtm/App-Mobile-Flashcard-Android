package com.example.myapplication.study

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StudyAnswerEvaluationTest {
    @Test fun validatesCloze() {
        assertEquals(StudyRecall.CORRECT, gradeCloze("  São   Paulo  ", "são paulo"))
        assertEquals(StudyRecall.INCORRECT, gradeCloze("paulo", "São Paulo"))
        assertNull(gradeCloze("", "resposta"))
    }

    @Test fun validatesChoice() {
        assertEquals(StudyRecall.CORRECT, gradeMultipleChoice(1,1,3))
        assertEquals(StudyRecall.INCORRECT, gradeMultipleChoice(0,1,3))
        assertNull(gradeMultipleChoice(-1,1,3))
        assertNull(gradeMultipleChoice(0,null,3))
        assertNull(gradeMultipleChoice(0,0,5))
    }
}
