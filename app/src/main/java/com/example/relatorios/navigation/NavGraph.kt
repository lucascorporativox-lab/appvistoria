package com.example.relatorios.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.relatorios.data.model.Edificio
import com.example.relatorios.ui.screens.EdificioScreen
import com.example.relatorios.ui.screens.HomeScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    edificio: Edificio,
    onEdificioUpdated: (Edificio) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                edificio = edificio,
                onEdificioUpdated = onEdificioUpdated
            )
        }
        composable("edificio") {
            EdificioScreen(
                edificio = edificio,
                onEdificioUpdated = onEdificioUpdated
            )
        }
    }
} 