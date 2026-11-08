package com.example.myapplication.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.auth.AccountKind
import com.example.myapplication.auth.AuthResult
import com.example.myapplication.auth.AuthValidation
import com.example.myapplication.auth.FirebaseAuthRepository
import com.example.myapplication.ui.design.MyDeckButton
import com.example.myapplication.ui.design.MyDeckButtonVariant
import com.example.myapplication.ui.design.MyDeckDesign

@Composable
fun TelaLogin(
    kind: AccountKind,
    repository: FirebaseAuthRepository?,
    onVoltar: () -> Unit,
    onCriarConta: () -> Unit,
    onRecuperar: () -> Unit,
    onAuthenticated: (AccountKind) -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailTouched by rememberSaveable { mutableStateOf(false) }
    var passwordTouched by rememberSaveable { mutableStateOf(false) }
    var pending by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf<String?>(null) }

    val emailError = emailTouched && !AuthValidation.validEmail(email)
    val passwordError = passwordTouched && password.isEmpty()

    fun submit() {
        emailTouched = true
        passwordTouched = true
        feedback = null
        if (!AuthValidation.validEmail(email) || password.isEmpty() || pending) return
        if (repository == null) {
            feedback = "Configure o Firebase para entrar com sua conta."
            return
        }
        pending = true
        repository.login(email, password, kind) { result ->
            pending = false
            when (result) {
                is AuthResult.Success -> onAuthenticated(result.kind)
                is AuthResult.Notice -> feedback = result.message
                is AuthResult.Failure -> feedback = result.message
            }
        }
    }

    AuthScreenFrame(onVoltar = onVoltar, title = "Entre na sua\nconta.") {
        AuthLineInput(
            label = "E-mail", hint = "seuemail@exemplo.com", value = email,
            onValueChange = { email = it; emailTouched = false; feedback = null },
            error = if (emailError) "Digite um e-mail válido." else null,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
            onIme = { emailTouched = true }
        )
        Spacer(Modifier.height(29.dp))
        AuthLineInput(
            label = "Senha", hint = "Digite sua senha", value = password,
            onValueChange = { password = it; passwordTouched = false; feedback = null },
            password = true,
            error = if (passwordError) "Informe sua senha." else null,
            imeAction = ImeAction.Done,
            onIme = { submit() }
        )
        Spacer(Modifier.height(15.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            UnderlinedAction("Esqueceu a senha?", onRecuperar)
        }
        Spacer(Modifier.height(44.dp))
        MyDeckButton(
            label = if (pending) "Entrando..." else "Entrar",
            variant = MyDeckButtonVariant.DARK,
            onClick = if (pending) null else ::submit
        )
        if (feedback != null) {
            Spacer(Modifier.height(15.dp))
            StatusMessage(feedback.orEmpty())
        }
        if (kind == AccountKind.STUDENT) {
            Spacer(Modifier.height(22.dp))
            UnderlinedAction("Criar conta", onCriarConta)
        }
    }
}

@Composable
fun TelaRecuperarSenha(
    repository: FirebaseAuthRepository?,
    onVoltar: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var touched by rememberSaveable { mutableStateOf(false) }
    var pending by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf<String?>(null) }
    val invalid = touched && !AuthValidation.validEmail(email)

    fun send() {
        touched = true
        feedback = null
        if (!AuthValidation.validEmail(email) || pending) return
        if (repository == null) {
            feedback = "Configure o Firebase para recuperar sua conta."
            return
        }
        pending = true
        repository.resetPassword(email) { result ->
            pending = false
            feedback = when (result) {
                is AuthResult.Success -> null
                is AuthResult.Notice -> result.message
                is AuthResult.Failure -> result.message
            }
        }
    }

    AuthScreenFrame(onVoltar = onVoltar, title = "Recuperar\nsenha.") {
        AuthLineInput(
            label = "E-mail", hint = "seuemail@exemplo.com", value = email,
            onValueChange = { email = it; touched = false; feedback = null },
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done,
            error = if (invalid) "Digite um e-mail válido." else null,
            onIme = { send() }
        )
        Spacer(Modifier.height(38.dp))
        MyDeckButton(
            label = if (pending) "Enviando..." else "Enviar instruções",
            variant = MyDeckButtonVariant.DARK,
            onClick = if (pending) null else ::send
        )
        if (feedback != null) {
            Spacer(Modifier.height(18.dp))
            StatusMessage(feedback.orEmpty())
        }
    }
}

@Composable
fun TelaPerfilAluno(onSair: () -> Unit) {
    AuthScreenFrame(onVoltar = onSair, title = "Perfil.") {
        StatusMessage("Sua conta de aluno está autenticada.")
        Spacer(Modifier.height(32.dp))
        MyDeckButton("Sair", MyDeckButtonVariant.DARK, onClick = onSair)
    }
}

@Composable
private fun AuthScreenFrame(
    onVoltar: () -> Unit,
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.Start
    ) {
        UnderlinedAction("Voltar", onVoltar)
        Spacer(Modifier.height(75.dp))
        BasicText(
            title,
            style = TextStyle(
                fontFamily = MyDeckDesign.font,
                fontSize = 34.sp,
                lineHeight = 41.sp,
                fontWeight = FontWeight.SemiBold,
                color = MyDeckDesign.ink
            )
        )
        Spacer(Modifier.height(50.dp))
        content()
    }
}

@Composable
private fun UnderlinedAction(label: String, onClick: () -> Unit) {
    BasicText(
        label,
        modifier = Modifier
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 12.dp),
        style = TextStyle(
            fontFamily = MyDeckDesign.font,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MyDeckDesign.ink,
            textDecoration = TextDecoration.Underline
        )
    )
}

@Composable
private fun StatusMessage(message: String) {
    BasicText(
        message,
        modifier = Modifier.semantics { contentDescription = message },
        style = TextStyle(
            fontFamily = MyDeckDesign.font,
            fontSize = 13.sp,
            lineHeight = 20.sp,
            color = MyDeckDesign.secondary
        )
    )
}

@Composable
private fun AuthLineInput(
    label: String,
    hint: String,
    value: String,
    onValueChange: (String) -> Unit,
    error: String? = null,
    password: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction,
    onIme: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    var visible by rememberSaveable { mutableStateOf(false) }
    Column {
        BasicText(
            label,
            style = TextStyle(
                fontFamily = MyDeckDesign.font,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MyDeckDesign.ink
            )
        )
        Spacer(Modifier.height(9.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(
                fontFamily = MyDeckDesign.font,
                fontSize = 15.sp,
                color = MyDeckDesign.ink
            ),
            cursorBrush = SolidColor(MyDeckDesign.ink),
            visualTransformation = if (password && !visible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (password) KeyboardType.Password else keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(onNext = { onIme() }, onDone = { onIme() }),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focused = it.isFocused }
                .semantics {
                    contentDescription = label
                    if (error != null) error(error)
                },
            decorationBox = { field ->
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().heightIn(min = 46.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                            if (value.isEmpty()) BasicText(
                                hint,
                                style = TextStyle(
                                    fontFamily = MyDeckDesign.font,
                                    fontSize = 15.sp,
                                    color = Color(0xFF888888)
                                )
                            )
                            field()
                        }
                        if (password) {
                            BasicText(
                                if (visible) "Ocultar" else "Mostrar",
                                modifier = Modifier
                                    .clickable(role = Role.Button) { visible = !visible }
                                    .padding(start = 10.dp, top = 12.dp, bottom = 12.dp),
                                style = TextStyle(
                                    fontFamily = MyDeckDesign.font,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MyDeckDesign.secondary
                                )
                            )
                        }
                    }
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(if (focused || error != null) 2.dp else 1.dp)
                            .background(
                                when {
                                    error != null -> MyDeckDesign.error
                                    focused -> MyDeckDesign.ink
                                    else -> MyDeckDesign.line
                                }
                            )
                    )
                }
            }
        )
        if (error != null) {
            Spacer(Modifier.height(8.dp))
            BasicText(
                error,
                style = TextStyle(
                    fontFamily = MyDeckDesign.font,
                    fontSize = 12.sp,
                    color = MyDeckDesign.error
                )
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun TelaLoginPreview() {
    TelaLogin(AccountKind.STUDENT, null, {}, {}, {}, {})
}
