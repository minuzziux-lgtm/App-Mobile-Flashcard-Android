package com.example.myapplication.navigation

import android.content.pm.ApplicationInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import com.example.myapplication.ui.design.MyDeckDesign
import com.example.myapplication.ui.design.MyDeckLocale
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.auth.AccountKind
import com.example.myapplication.auth.FirebaseAuthRepository
import com.example.myapplication.demo.LocalMyDeckStore
import com.example.myapplication.data.MyDeckDataRepository
import com.example.myapplication.data.CloudMyDeckState
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
    LaunchedEffect(context) {
        MyDeckDesign.loadTheme(context)
        MyDeckLocale.load(context)
    }
    val repository = remember(context) { FirebaseAuthRepository.create(context) }
    // A prévia mantém armazenamento próprio; contas autenticadas usam namespaces
    // separados por UID até a migração definitiva para Firestore.
    val previewStore = remember(context) { LocalMyDeckStore(context) }
    val firestore = remember(context) { MyDeckDataRepository.create(context) }

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
                    { navController.navigate("preview-professor") }
                } else null,
                onAlunoPreview = if (
                    (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
                ) {
                    { navController.navigate("preview-aluno") }
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
        // Rotas de prévia isoladas: nunca leem nem escrevem no Firebase.
        composable("preview-aluno") {
            StudentHomeShell(
                store = previewStore, onLogout = ::exitToEntry,
                studentId = "@aluno-demo", cloud = null
            )
        }
        composable("preview-professor") {
            TeacherHomeShell(
                store = previewStore, onLogout = ::exitToEntry,
                cloud = null
            )
        }
        composable("home-aluno") {
            val uid = repository?.currentUserId()
            // Para contas reais, partir somente do servidor. Snapshots locais antigos
            // permanecem preservados, sem migração implícita ou atribuição cruzada.
            val store = remember(context, uid) {
                if (uid == null) previewStore else LocalMyDeckStore()
            }
            val cloud = remember(uid, firestore) {
                if (uid != null && firestore != null) {
                    CloudMyDeckState(firestore, store, uid, teacher = false, context = context)
                } else null
            }
            StudentHomeShell(
                store = store, onLogout = ::exitToEntry,
                email = repository?.currentEmail(),
                studentId = uid ?: "@aluno-demo",
                cloud = cloud
            )
        }
        composable("area-professor") {
            val uid = repository?.currentUserId()
            val store = remember(context, uid) {
                if (uid == null) previewStore else LocalMyDeckStore()
            }
            val cloud = remember(uid, firestore) {
                if (uid != null && firestore != null) {
                    CloudMyDeckState(firestore, store, uid, teacher = true, context = context)
                } else null
            }
            TeacherHomeShell(
                store = store, onLogout = ::exitToEntry, email = repository?.currentEmail(),
                cloud = cloud
            )
        }
    }
}
