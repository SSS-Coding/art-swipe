package com.artswipe.ui.screens.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.artswipe.ui.screens.discover.DiscoverScreen
import com.artswipe.ui.screens.liked.LikedGalleryScreen
import com.artswipe.ui.screens.profile.ProfileScreen

@Composable
fun MainScreen(
    onNavigateToAuth: () -> Unit,
    onNavigateToExplanation: (String) -> Unit,
    onNavigateToRecommendations: () -> Unit,
    onNavigateToCompatibility: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf("Discover", "Profile", "Liked")
    val icons = listOf(Icons.Default.Swipe, Icons.Default.Person, Icons.Default.Favorite)

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index }
                    )
                }
            }
        }
    ) { padding ->
        val modifier = Modifier.padding(padding)
        when (selectedItem) {
            0 -> DiscoverScreen(
                onNavigateToExplanation = onNavigateToExplanation,
                modifier = modifier
            )
            1 -> ProfileScreen(
                onNavigateToAuth = onNavigateToAuth,
                onNavigateToRecommendations = onNavigateToRecommendations,
                onNavigateToCompatibility = onNavigateToCompatibility,
                onNavigateToSettings = onNavigateToSettings,
                modifier = modifier
            )
            2 -> LikedGalleryScreen(
                onNavigateToExplanation = onNavigateToExplanation,
                modifier = modifier
            )
        }
    }
}
