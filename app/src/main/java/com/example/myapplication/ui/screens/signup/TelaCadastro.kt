package com.example.myapplication.ui.screens.signup

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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.selected
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
import com.example.myapplication.R
import com.example.myapplication.auth.AccountKind
import com.example.myapplication.auth.AuthResult
import com.example.myapplication.auth.FirebaseAuthRepository
import com.example.myapplication.ui.design.MyDeckButton
import com.example.myapplication.ui.design.MyDeckButtonVariant
import com.example.myapplication.ui.design.MyDeckDesign

private val usernamePattern = Regex("^[A-Za-z0-9._]{3,24}$")
private val emailPattern = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

@Composable
fun TelaCadastro(
    onVoltar: () -> Unit = {},
    repository: FirebaseAuthRepository? = null,
    onLogin: (AccountKind) -> Unit = {}
) {
    var tipoConta by rememberSaveable { mutableStateOf("aluno") }
    var nome by rememberSaveable { mutableStateOf("") }
    var usuario by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    // Não persistir senhas no estado da Activity.
    var senha by remember { mutableStateOf("") }
    var confirmacao by remember { mutableStateOf("") }

    var validouNome by rememberSaveable { mutableStateOf(false) }
    var validouUsuario by rememberSaveable { mutableStateOf(false) }
    var validouEmail by rememberSaveable { mutableStateOf(false) }
    var validouConfirmacao by rememberSaveable { mutableStateOf(false) }
    var tentouEnviar by rememberSaveable { mutableStateOf(false) }
    var enviando by remember { mutableStateOf(false) }
    var cadastroConcluido by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf<String?>(null) }
    val foco = LocalFocusManager.current

    val nomeInvalido = nome.trim().length < 2
    val usuarioInvalido = !usernamePattern.matches(usuario.removePrefix("@"))
    val emailInvalido = !emailPattern.matches(email.trim())
    val tamanhoSenha = senha.codePointCount(0, senha.length)
    val senhaInvalida = tamanhoSenha < 15
    val confirmacaoInvalida = confirmacao.isEmpty() || confirmacao != senha
    val formularioValido = !nomeInvalido && !usuarioInvalido && !emailInvalido &&
        !senhaInvalida && !confirmacaoInvalida

    // Os critérios de composição são dicas; não bloqueiam uma senha longa.
    val dicaMaiuscula = stringResource(R.string.signup_uppercase)
    val dicaMinuscula = stringResource(R.string.signup_lowercase)
    val dicaNumero = stringResource(R.string.signup_digit)
    val dicaSimbolo = stringResource(R.string.signup_symbol)
    val sugestoes = buildList {
        if (senha.none(Char::isUpperCase)) add(dicaMaiuscula)
        if (senha.none(Char::isLowerCase)) add(dicaMinuscula)
        if (senha.none(Char::isDigit)) add(dicaNumero)
        if (senha.none { !it.isLetterOrDigit() && !it.isWhitespace() }) add(dicaSimbolo)
    }
    val mensagemSenha = when {
        senha.isEmpty() && tentouEnviar -> stringResource(R.string.signup_password_required)
        senha.isEmpty() -> null
        else -> {
            val avisos = mutableListOf<String>()
            if (senhaInvalida) {
                avisos.add(stringResource(R.string.signup_password_remaining, 15 - tamanhoSenha))
            }
            if (sugestoes.isNotEmpty()) {
                avisos.add(stringResource(R.string.signup_password_optional) + " " +
                    sugestoes.joinToString(", ") + ".")
            }
            avisos.joinToString("\n").ifBlank { null }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MyDeckDesign.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.Start
    ) {
        BasicText(
            text = stringResource(R.string.signup_back),
            modifier = Modifier
                .clickable(role = Role.Button, onClick = onVoltar)
                .padding(vertical = 8.dp),
            style = TextStyle(
                fontFamily = MyDeckDesign.font,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MyDeckDesign.ink,
                textDecoration = TextDecoration.Underline
            )
        )

        Spacer(Modifier.height(62.dp))

        BasicText(
            text = stringResource(R.string.signup_title),
            style = TextStyle(
                fontFamily = MyDeckDesign.font,
                fontSize = 34.sp,
                lineHeight = 41.sp,
                fontWeight = FontWeight.SemiBold,
                color = MyDeckDesign.ink
            )
        )

        Spacer(Modifier.height(32.dp))

        BasicText(
            text = stringResource(R.string.signup_account_type),
            style = TextStyle(
                fontFamily = MyDeckDesign.font,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MyDeckDesign.ink
            )
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
            TipoConta(
                label = stringResource(R.string.signup_student),
                selecionado = tipoConta == "aluno",
                onClick = { tipoConta = "aluno"; feedback = null; cadastroConcluido = false }
            )
            TipoConta(
                label = stringResource(R.string.signup_teacher),
                selecionado = tipoConta == "professor",
                onClick = { tipoConta = "professor"; feedback = null; cadastroConcluido = false }
            )
        }

        Spacer(Modifier.height(30.dp))

        CampoLinha(
            rotulo = stringResource(R.string.signup_name),
            dica = stringResource(R.string.signup_name_hint),
            valor = nome,
            aoAlterar = { nome = it; validouNome = false; feedback = null; cadastroConcluido = false },
            aoConcluir = { validouNome = true },
            erro = if (validouNome && nomeInvalido)
                stringResource(R.string.signup_name_error) else null,
            aoProximo = { foco.moveFocus(FocusDirection.Down) }
        )

        Spacer(Modifier.height(22.dp))

        CampoLinha(
            rotulo = stringResource(R.string.signup_username),
            dica = stringResource(R.string.signup_username_hint),
            valor = usuario,
            aoAlterar = { usuario = it; validouUsuario = false; feedback = null; cadastroConcluido = false },
            aoConcluir = { validouUsuario = true },
            erro = if (validouUsuario && usuarioInvalido)
                stringResource(R.string.signup_username_error) else null,
            aoProximo = { foco.moveFocus(FocusDirection.Down) }
        )

        Spacer(Modifier.height(22.dp))

        CampoLinha(
            rotulo = stringResource(R.string.signup_email),
            dica = stringResource(R.string.signup_email_hint),
            valor = email,
            aoAlterar = { email = it; validouEmail = false; feedback = null; cadastroConcluido = false },
            aoConcluir = { validouEmail = true },
            erro = if (validouEmail && emailInvalido)
                stringResource(R.string.signup_email_error) else null,
            teclado = KeyboardType.Email,
            aoProximo = { foco.moveFocus(FocusDirection.Down) }
        )

        Spacer(Modifier.height(22.dp))

        CampoLinha(
            rotulo = stringResource(R.string.signup_password),
            dica = stringResource(R.string.signup_password_hint),
            valor = senha,
            aoAlterar = {
                senha = it
                validouConfirmacao = false
                feedback = null; cadastroConcluido = false
            },
            aoConcluir = {},
            erro = if (senhaInvalida && mensagemSenha != null) mensagemSenha else null,
            informacao = if (!senhaInvalida) mensagemSenha else null,
            senha = true,
            aoProximo = { foco.moveFocus(FocusDirection.Down) }
        )

        Spacer(Modifier.height(22.dp))

        CampoLinha(
            rotulo = stringResource(R.string.signup_confirm),
            dica = stringResource(R.string.signup_confirm_hint),
            valor = confirmacao,
            aoAlterar = { confirmacao = it; validouConfirmacao = false; feedback = null; cadastroConcluido = false },
            aoConcluir = { validouConfirmacao = true },
            erro = if (validouConfirmacao && confirmacaoInvalida) stringResource(
                if (confirmacao.isEmpty()) R.string.signup_confirm_required
                else R.string.signup_confirm_mismatch
            ) else null,
            senha = true,
            imeAction = ImeAction.Done,
            aoProximo = { foco.clearFocus() }
        )

        Spacer(Modifier.height(36.dp))

        MyDeckButton(
            label = when {
                cadastroConcluido -> "Ir para login"
                enviando -> "Criando conta..."
                else -> stringResource(R.string.signup_submit)
            },
            variant = MyDeckButtonVariant.DARK,
            onClick = if (enviando) null else {
                {
                    if (cadastroConcluido) {
                        onLogin(if (tipoConta == "professor") AccountKind.TEACHER else AccountKind.STUDENT)
                    } else {
                        validouNome = true
                        validouUsuario = true
                        validouEmail = true
                        validouConfirmacao = true
                        tentouEnviar = true
                        foco.clearFocus()
                        if (formularioValido) {
                            if (repository == null) {
                                feedback = "Configure o Firebase para criar sua conta."
                            } else {
                                enviando = true
                                val kind = if (tipoConta == "professor") AccountKind.TEACHER else AccountKind.STUDENT
                                repository.register(nome, usuario, email, senha, kind) { result ->
                                    enviando = false
                                    feedback = when (result) {
                                        is AuthResult.Success -> null
                                        is AuthResult.Notice -> {
                                            cadastroConcluido = true
                                            result.message
                                        }
                                        is AuthResult.Failure -> result.message
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )
        if (feedback != null) {
            Spacer(Modifier.height(14.dp))
            BasicText(
                text = feedback.orEmpty(),
                style = TextStyle(
                    fontFamily = MyDeckDesign.font,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = MyDeckDesign.secondary
                )
            )
        }

    }
}

@Composable
private fun TipoConta(label: String, selecionado: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(100.dp)
            .heightIn(min = 48.dp)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .semantics { selected = selecionado }
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BasicText(
            text = label,
            style = TextStyle(
                fontFamily = MyDeckDesign.font,
                fontSize = 15.sp,
                fontWeight = if (selecionado) FontWeight.SemiBold else FontWeight.Medium,
                color = if (selecionado) MyDeckDesign.ink else MyDeckDesign.secondary
            )
        )
        Spacer(Modifier.height(8.dp))
        Spacer(
            Modifier
                .fillMaxWidth()
                .height(if (selecionado) 2.dp else 1.dp)
                .background(if (selecionado) MyDeckDesign.ink else MyDeckDesign.line)
        )
    }
}

@Composable
private fun CampoLinha(
    rotulo: String,
    dica: String,
    valor: String,
    aoAlterar: (String) -> Unit,
    aoConcluir: () -> Unit,
    erro: String?,
    informacao: String? = null,
    senha: Boolean = false,
    teclado: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    aoProximo: () -> Unit
) {
    var comFoco by remember { mutableStateOf(false) }
    var mostrar by rememberSaveable { mutableStateOf(false) }
    val corLinha = when {
        erro != null -> MyDeckDesign.error
        comFoco -> MyDeckDesign.ink
        else -> MyDeckDesign.line
    }

    Column {
        BasicText(
            text = rotulo,
            style = TextStyle(
                fontFamily = MyDeckDesign.font,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = MyDeckDesign.ink
            )
        )
        Spacer(Modifier.height(6.dp))
        BasicTextField(
            value = valor,
            onValueChange = aoAlterar,
            singleLine = true,
            textStyle = TextStyle(
                fontFamily = MyDeckDesign.font,
                fontSize = 15.sp,
                color = MyDeckDesign.ink
            ),
            cursorBrush = SolidColor(MyDeckDesign.ink),
            visualTransformation = if (senha && !mostrar) PasswordVisualTransformation()
            else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (senha) KeyboardType.Password else teclado,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onNext = { aoConcluir(); aoProximo() },
                onDone = { aoConcluir(); aoProximo() }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = rotulo
                    if (erro != null) error(erro)
                }
                .onFocusChanged { comFoco = it.isFocused },
            decorationBox = { campoInterno ->
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                            if (valor.isEmpty()) {
                                BasicText(
                                    text = dica,
                                    style = TextStyle(
                                        fontFamily = MyDeckDesign.font,
                                        fontSize = 15.sp,
                                        color = Color(0xFF888888)
                                    )
                                )
                            }
                            campoInterno()
                        }
                        if (senha) {
                            BasicText(
                                text = stringResource(
                                    if (mostrar) R.string.signup_hide else R.string.signup_show
                                ),
                                modifier = Modifier
                                    .clickable(role = Role.Button) { mostrar = !mostrar }
                                    .padding(start = 12.dp, top = 10.dp, bottom = 10.dp),
                                style = TextStyle(
                                    fontFamily = MyDeckDesign.font,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MyDeckDesign.secondary
                                )
                            )
                        }
                    }
                    Spacer(
                        Modifier
                            .fillMaxWidth()
                            .height(if (comFoco || erro != null) 2.dp else 1.dp)
                            .background(corLinha)
                    )
                }
            }
        )
        if (erro != null || informacao != null) {
            Spacer(Modifier.height(7.dp))
            BasicText(
                text = erro ?: informacao.orEmpty(),
                style = TextStyle(
                    fontFamily = MyDeckDesign.font,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = if (erro != null) MyDeckDesign.error else MyDeckDesign.secondary
                )
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun TelaCadastroPreview() {
    TelaCadastro()
}