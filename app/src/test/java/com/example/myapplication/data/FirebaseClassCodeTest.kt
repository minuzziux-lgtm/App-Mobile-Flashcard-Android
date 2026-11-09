package com.example.myapplication.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class FirebaseClassCodeTest {
    @Test fun normalizesCodeAndRejectsUnsupportedCharacters() {
        assertEquals("ABCDEFGH", MyDeckDataRepository.normalizeCode("  abcdefgh "))
        assertTrue(MyDeckDataRepository.validJoinCode(" aBcDeFgH "))
        assertFalse(MyDeckDataRepository.validJoinCode("AAAA0000"))
        assertFalse(MyDeckDataRepository.validJoinCode("AAAAO000"))
        assertFalse(MyDeckDataRepository.validJoinCode("aaaa"))
        assertFalse(MyDeckDataRepository.validJoinCode("aaaa bbb"))
    }
}
