package com.example.myapplication.auth

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FieldValue

sealed interface AuthResult {
    data class Success(val kind: AccountKind) : AuthResult
    data class Notice(val message: String) : AuthResult
    data class Failure(val message: String) : AuthResult
}

class FirebaseAuthRepository private constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    companion object {
        fun create(context: Context): FirebaseAuthRepository? {
            val app = FirebaseApp.initializeApp(context.applicationContext) ?: return null
            return FirebaseAuthRepository(FirebaseAuth.getInstance(app), FirebaseFirestore.getInstance(app))
        }
    }

    fun register(
        name: String,
        username: String,
        email: String,
        password: String,
        kind: AccountKind,
        onResult: (AuthResult) -> Unit
    ) {
        val normalizedHandle = AuthValidation.normalizeUsername(username)
        val normalizedEmail = email.trim()
        if (!AuthValidation.validName(name) || !AuthValidation.validUsername(username) ||
            !AuthValidation.validEmail(normalizedEmail) || !AuthValidation.validPassword(password)
        ) {
            onResult(AuthResult.Failure("Revise os campos e tente novamente."))
            return
        }

        auth.createUserWithEmailAndPassword(normalizedEmail, password)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    onResult(AuthResult.Failure("Não foi possível criar a conta. Verifique os dados ou tente outro e-mail."))
                    return@addOnCompleteListener
                }
                val user = task.result?.user
                if (user == null) {
                    onResult(AuthResult.Failure("Não foi possível concluir o cadastro."))
                    return@addOnCompleteListener
                }
                val userRef = firestore.collection("users").document(user.uid)
                val handleRef = firestore.collection("usernames").document(normalizedHandle)
                firestore.runTransaction { tx ->
                    if (tx.get(handleRef).exists()) {
                        throw FirebaseFirestoreException(
                            "Nome de usuário em uso", FirebaseFirestoreException.Code.ALREADY_EXISTS
                        )
                    }
                    tx.set(userRef, mapOf(
                        "uid" to user.uid,
                        "email" to normalizedEmail,
                        "name" to name.trim(),
                        "username" to normalizedHandle,
                        "role" to AuthValidation.initialRole(kind),
                        "createdAt" to FieldValue.serverTimestamp()
                    ))
                    tx.set(handleRef, mapOf(
                        "uid" to user.uid,
                        "createdAt" to FieldValue.serverTimestamp()
                    ))
                }.addOnSuccessListener {
                    user.sendEmailVerification().addOnCompleteListener { mailTask ->
                        auth.signOut()
                        if (mailTask.isSuccessful) {
                            val extra = if (kind == AccountKind.TEACHER)
                                " Seu acesso como professor depende de aprovação." else ""
                            onResult(AuthResult.Notice("Conta criada. Enviamos um e-mail de verificação.$extra"))
                        } else {
                            onResult(AuthResult.Notice("Conta criada, mas não foi possível enviar a verificação. Solicite um novo envio pelo suporte."))
                        }
                    }
                }.addOnFailureListener { error ->
                    // Best-effort rollback of a new Auth account when the Firestore transaction fails.
                    user.delete().addOnCompleteListener { auth.signOut() }
                    val message = if (error is FirebaseFirestoreException &&
                        error.code == FirebaseFirestoreException.Code.ALREADY_EXISTS
                    ) "Esse nome de usuário já está em uso." else
                        "Não foi possível salvar o cadastro. Verifique a conexão e tente novamente."
                    onResult(AuthResult.Failure(message))
                }
            }
    }

    fun login(email: String, password: String, kind: AccountKind, onResult: (AuthResult) -> Unit) {
        if (!AuthValidation.validEmail(email) || password.isBlank()) {
            onResult(AuthResult.Failure("Informe seu e-mail e sua senha."))
            return
        }
        auth.signInWithEmailAndPassword(email.trim(), password).addOnCompleteListener { result ->
            val user = if (result.isSuccessful) result.result?.user else null
            if (!result.isSuccessful || user == null) {
                onResult(AuthResult.Failure("E-mail ou senha incorretos."))
                return@addOnCompleteListener
            }
            verifyUser(user, kind, onResult)
        }
    }

    private fun verifyUser(user: FirebaseUser, kind: AccountKind, onResult: (AuthResult) -> Unit) {
        user.reload().addOnCompleteListener { result ->
            if (!result.isSuccessful || !user.isEmailVerified) {
                auth.signOut()
                onResult(AuthResult.Failure("Confirme seu e-mail antes de entrar."))
                return@addOnCompleteListener
            }
            firestore.collection("users").document(user.uid).get().addOnCompleteListener { profile ->
                if (!profile.isSuccessful) {
                    auth.signOut()
                    onResult(AuthResult.Failure("Não foi possível verificar seu perfil. Tente novamente."))
                } else {
                    val storedRole = profile.result?.getString("role")
                    if (AuthValidation.roleAllowsLogin(storedRole, kind)) {
                        onResult(AuthResult.Success(kind))
                    } else {
                        auth.signOut()
                        onResult(AuthResult.Failure(
                            if (kind == AccountKind.TEACHER)
                                "O acesso de professor ainda não está autorizado para esta conta."
                            else "Esta conta não possui acesso como aluno."
                        ))
                    }
                }
            }
        }
    }

    fun resetPassword(email: String, onResult: (AuthResult) -> Unit) {
        if (!AuthValidation.validEmail(email)) {
            onResult(AuthResult.Failure("Digite um e-mail válido."))
            return
        }
        auth.sendPasswordResetEmail(email.trim()).addOnCompleteListener {
            // Avoid revealing whether an account with this address exists.
            onResult(AuthResult.Notice("Se houver uma conta para esse e-mail, você receberá instruções de recuperação."))
        }
    }

    fun signOut() = auth.signOut()
}
