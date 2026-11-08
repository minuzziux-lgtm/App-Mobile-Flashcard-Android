package com.example.myapplication.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthValidationTest {
    @Test fun `username normalization is consistent`() {
        assertEquals("joao.silva", AuthValidation.normalizeUsername(" @Joao.Silva "))
        assertTrue(AuthValidation.validUsername("@Joao.Silva"))
        assertFalse(AuthValidation.validUsername("@nome com espaco"))
    }

    @Test fun `email and name reject invalid input`() {
        assertTrue(AuthValidation.validEmail("aluno@exemplo.com"))
        assertFalse(AuthValidation.validEmail("aluno@"))
        assertFalse(AuthValidation.validName(" "))
    }

    @Test fun `password length counts unicode code points`() {
        assertFalse(AuthValidation.validPassword("curta1!"))
        assertTrue(AuthValidation.validPassword("minha senha bem longa"))
        assertFalse(AuthValidation.matchingPasswords("senha", "outra"))
        assertTrue(AuthValidation.matchingPasswords("senha", "senha"))
    }

    @Test fun `requested teacher does not get teacher role`() {
        assertEquals("pending_teacher", AuthValidation.initialRole(AccountKind.TEACHER))
        assertFalse(AuthValidation.roleAllowsLogin("pending_teacher", AccountKind.TEACHER))
        assertTrue(AuthValidation.roleAllowsLogin("teacher", AccountKind.TEACHER))
        assertFalse(AuthValidation.roleAllowsLogin("student", AccountKind.TEACHER))
    }
}
