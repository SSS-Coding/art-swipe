package com.artswipe.ui.screens.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.artswipe.ui.screens.discover.DiscoverScreen
import com.artswipe.ui.screens.liked.LikedGalleryScreen
import com.artswipe.ui.screens.profile.ProfileScreen
import com.artswipe.ui.screens.profile.ProfileViewModel

@Composable
fun MainScreen(
    onNavigateToAuth: () -> Unit,
    onNavigateToExplanation: (String) -> Unit,
    onNavigateToRecommendations: () -> Unit,
    onNavigateToCompatibility: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf("Discover", "Profile", "Liked")
    val icons = listOf(Icons.Default.Swipe, Icons.Default.Person, Icons.Default.Favorite)
    
    val user by viewModel.currentUser.collectAsState()

    // Global session check: only navigate to auth if we are CERTAIN there is no session.
    // This is the "Remember login" fix - we handle logout at the top level.
    LaunchedEffect(user) {
        if (user == null) {
            onNavigateToAuth()
        }
    }

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
                modifier = modifier
            )
            1 -> ProfileScreen(
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
