package com.artswipe.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Auth : Screen("auth")
    object Main : Screen("main")
    object Discover : Screen("discover")
    object Profile : Screen("profile")
    object Liked : Screen("liked")
    object Recommendations : Screen("recommendations")
    object Settings : Screen("settings")
    object Explanation : Screen("explanation/{artworkId}") {
        fun createRoute(artworkId: String) = "explanation/$artworkId"
    }
    object Comparison : Screen("comparison?code={code}") {
        fun createRoute(code: String) = "comparison?code=$code"
    }
}
