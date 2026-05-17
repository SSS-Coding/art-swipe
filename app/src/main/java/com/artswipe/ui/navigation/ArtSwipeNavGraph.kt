package com.artswipe.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.artswipe.ui.screens.auth.AuthScreen
import com.artswipe.ui.screens.compatibility.CompatibilityScreen
import com.artswipe.ui.screens.discover.DiscoverScreen
import com.artswipe.ui.screens.explanation.ExplanationScreen
import com.artswipe.ui.screens.liked.LikedGalleryScreen
import com.artswipe.ui.screens.profile.ProfileScreen
import com.artswipe.ui.screens.recommendations.RecommendationsScreen
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
            SplashScreen(onNavigateToAuth = {
                navController.navigate(Screen.Auth.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }, onNavigateToDiscover = {
                navController.navigate(Screen.Discover.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            })
        }
        composable(Screen.Auth.route) {
            AuthScreen(onAuthSuccess = {
                navController.navigate(Screen.Discover.route) {
                    popUpTo(Screen.Auth.route) { inclusive = true }
                }
            })
        }
        composable(Screen.Discover.route) {
            DiscoverScreen(
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToLiked = { navController.navigate(Screen.Liked.route) },
                onNavigateToExplanation = { artworkId ->
                    navController.navigate(Screen.Explanation.createRoute(artworkId))
                }
            )
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onNavigateToAuth = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.Discover.route) { inclusive = true }
                    }
                },
                onNavigateToRecommendations = { navController.navigate(Screen.Recommendations.route) },
                onNavigateToCompatibility = { navController.navigate(Screen.Comparison.route) }
            )
        }
        composable(Screen.Liked.route) {
            LikedGalleryScreen(
                onBack = { navController.popBackStack() },
                onNavigateToExplanation = { artworkId ->
                    navController.navigate(Screen.Explanation.createRoute(artworkId))
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
