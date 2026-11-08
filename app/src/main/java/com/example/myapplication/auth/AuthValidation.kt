package com.example.myapplication.auth

import java.util.Locale

enum class AccountKind(val value: String) {
    STUDENT("student"),
    TEACHER("teacher");

    companion object {
        fun fromRoute(value: String?): AccountKind =
            entries.firstOrNull { it.value == value } ?: STUDENT
    }
}

object AuthValidation {
    private val usernameRegex = Regex("^[a-z0-9._]{3,24}$")
    private val emailRegex = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

    fun normalizeUsername(value: String): String = value.trim().removePrefix("@").lowercase(Locale.ROOT)
    fun validUsername(value: String): Boolean = usernameRegex.matches(normalizeUsername(value))
    fun validName(value: String): Boolean = value.trim().length >= 2
    fun validEmail(value: String): Boolean = emailRegex.matches(value.trim())
    fun validPassword(value: String): Boolean = value.codePointCount(0, value.length) >= 15
    fun matchingPasswords(password: String, confirmation: String): Boolean =
        confirmation.isNotEmpty() && password == confirmation

    // Requested roles never confer teacher privileges. Only server-side approval can set "teacher".
    fun initialRole(kind: AccountKind): String =
        if (kind == AccountKind.TEACHER) "pending_teacher" else "student"
    fun roleAllowsLogin(storedRole: String?, kind: AccountKind): Boolean =
        storedRole == kind.value
}
