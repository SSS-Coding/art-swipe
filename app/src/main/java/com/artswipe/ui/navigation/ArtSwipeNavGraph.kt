package com.artswipe.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.artswipe.ui.screens.auth.AuthScreen
import com.artswipe.ui.screens.compatibility.CompatibilityScreen
import com.artswipe.ui.screens.explanation.ExplanationScreen
import com.artswipe.ui.screens.main.MainScreen
import com.artswipe.ui.screens.recommendations.RecommendationsScreen
import com.artswipe.ui.screens.settings.SettingsScreen
import com.artswipe.ui.screens.splash.SplashScreen

@Composable
fun ArtSwipeNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToAuth = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToDiscover = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Auth.route) {
            AuthScreen(onAuthSuccess = {
                navController.navigate(Screen.Main.route) {
                    popUpTo(Screen.Auth.route) { inclusive = true }
                }
            })
        }

        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToAuth = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToExplanation = { artworkId ->
                    navController.navigate(Screen.Explanation.createRoute(artworkId))
                },
                onNavigateToRecommendations = {
                    navController.navigate(Screen.Recommendations.route)
                },
                onNavigateToCompatibility = {
                    navController.navigate(Screen.Comparison.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.Recommendations.route) {
            RecommendationsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToExplanation = { artworkId ->
                    navController.navigate(Screen.Explanation.createRoute(artworkId))
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onSignOut = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Explanation.route
        ) { backStackEntry ->
            val artworkId = backStackEntry.arguments?.getString("artworkId") ?: ""
            ExplanationScreen(artworkId = artworkId, onBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.Comparison.route,
            deepLinks = listOf(navDeepLink { uriPattern = "artswipe://compare?code={code}" })
        ) { backStackEntry ->
            val code = backStackEntry.arguments?.getString("code") ?: ""
            CompatibilityScreen(
                initialCode = code,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
