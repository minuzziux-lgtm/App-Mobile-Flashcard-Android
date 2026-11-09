package com.example.myapplication.study

import java.util.TimeZone
import org.junit.Assert.*
import org.junit.Test

class StudyActivityTest {
    @Test fun dayGroupsAtMidnightInUtc() {
        val utc = TimeZone.getTimeZone("UTC")
        assertEquals(0L, localDayStart(86_399_999L, utc))
        assertEquals(86_400_000L, localDayStart(86_400_001L, utc))
    }
}
