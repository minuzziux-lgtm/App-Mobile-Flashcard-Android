package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.screens.signup.TelaCadastro
import com.example.myapplication.ui.screens.welcome.TelaEntrada

@Composable
fun MyDeckNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "entrada") {
        composable("entrada") {
            TelaEntrada(onCriarConta = { navController.navigate("cadastro") })
        }
        composable("cadastro") {
            TelaCadastro(onVoltar = { navController.popBackStack() })
        }
    }
}