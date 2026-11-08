package com.example.myapplication.navigation

import android.content.pm.ApplicationInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.auth.AccountKind
import com.example.myapplication.auth.FirebaseAuthRepository
import com.example.myapplication.demo.LocalMyDeckStore
import com.example.myapplication.ui.screens.auth.TelaLogin
import com.example.myapplication.ui.screens.auth.TelaRecuperarSenha
import com.example.myapplication.ui.teacher.TeacherHomeShell
import com.example.myapplication.ui.screens.auth.TelaPerfilAluno
import com.example.myapplication.ui.screens.signup.TelaCadastro
import com.example.myapplication.ui.screens.welcome.TelaEntrada
import com.example.myapplication.ui.student.StudentHomeShell

@Composable
fun MyDeckNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val repository = remember(context) { FirebaseAuthRepository.create(context) }
    val localStore = remember(context) { LocalMyDeckStore(context) }

    fun exitToEntry() {
        repository?.signOut()
        navController.navigate("entrada") {
            popUpTo(navController.graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }

    fun goToLogin(kind: AccountKind) {
        navController.navigate("login/${kind.value}") { launchSingleTop = true }
    }

    NavHost(navController = navController, startDestination = "entrada") {
        composable("entrada") {
            TelaEntrada(
                onCriarConta = { navController.navigate("cadastro") },
                onAlunoClick = { goToLogin(AccountKind.STUDENT) },
                onProfessorClick = { goToLogin(AccountKind.TEACHER) },
                onProfessorPreview = if (
                    (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
                ) {
                    { navController.navigate("area-professor") }
                } else null,
                onAlunoPreview = if (
                    (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
                ) {
                    { navController.navigate("home-aluno") }
                } else null
            )
        }
        composable("cadastro") {
            TelaCadastro(
                onVoltar = { navController.popBackStack() },
                repository = repository,
                onLogin = { kind -> goToLogin(kind) }
            )
        }
        composable(
            route = "login/{tipo}",
            arguments = listOf(navArgument("tipo") { type = NavType.StringType })
        ) { entry ->
            val kind = AccountKind.fromRoute(entry.arguments?.getString("tipo"))
            TelaLogin(
                kind = kind,
                repository = repository,
                onVoltar = { navController.popBackStack() },
                onCriarConta = { navController.navigate("cadastro") },
                onRecuperar = { navController.navigate("recuperar/${kind.value}") },
                onAuthenticated = { grantedRole ->
                    navController.navigate(
                        if (grantedRole == AccountKind.TEACHER) "area-professor" else "home-aluno"
                    ) {
                        popUpTo("entrada") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(
            route = "recuperar/{tipo}",
            arguments = listOf(navArgument("tipo") { type = NavType.StringType })
        ) {
            TelaRecuperarSenha(
                repository = repository,
                onVoltar = { navController.popBackStack() }
            )
        }
        composable("home-aluno") {
            StudentHomeShell(store = localStore, onLogout = ::exitToEntry)
        }
        composable("area-professor") {
            TeacherHomeShell(store = localStore, onLogout = ::exitToEntry)
        }
    }
}
