package com.juacie.littlewar.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.juacie.littlewar.ui.battle.BattleScreen
import com.juacie.littlewar.ui.formation.FormationScreen
import com.juacie.littlewar.ui.home.HomeScreen
import com.juacie.littlewar.ui.result.ResultScreen

object Routes {
    const val HOME = "home"
    const val FORMATION = "formation"
    const val BATTLE = "battle"
    const val RESULT = "result"
}

@Composable
fun LittleWarNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(onStartClick = { navController.navigate(Routes.FORMATION) })
        }
        composable(Routes.FORMATION) {
            FormationScreen(onNavigateToBattle = { navController.navigate(Routes.BATTLE) })
        }
        composable(Routes.BATTLE) {
            BattleScreen(
                onNavigateToResult = {
                    navController.navigate(Routes.RESULT) { popUpTo(Routes.HOME) }
                }
            )
        }
        composable(Routes.RESULT) {
            ResultScreen(
                onPlayAgain = {
                    navController.navigate(Routes.FORMATION) { popUpTo(Routes.HOME) }
                },
                onHome = {
                    navController.navigate(Routes.HOME) { popUpTo(Routes.HOME) { inclusive = true } }
                }
            )
        }
    }
}
