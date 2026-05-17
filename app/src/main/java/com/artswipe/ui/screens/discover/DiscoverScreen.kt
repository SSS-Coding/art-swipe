package com.artswipe.ui.screens.discover

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.artswipe.ui.screens.discover.components.SwipeableCard

@Composable
fun DiscoverScreen(
    onNavigateToExplanation: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DiscoverViewModel = hiltViewModel()
) {
    val artworkQueue by viewModel.artworkQueue.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (uiState.isLoading && artworkQueue.isEmpty()) {
            CircularProgressIndicator()
        } else if (artworkQueue.isEmpty()) {
            Text("No more art for now. Check back later!")
        } else {
            // Render top items. Using reversed to show first item on top of the stack.
            artworkQueue.take(2).reversed().forEach { artwork ->
                key(artwork.id) {
                    SwipeableCard(
                        artwork = artwork,
                        onSwipeLeft = {
                            viewModel.onSwipe(artwork, false)
                            onNavigateToExplanation(artwork.id)
                        },
                        onSwipeRight = {
                            viewModel.onSwipe(artwork, true)
                            onNavigateToExplanation(artwork.id)
                        }
                    )
                }
            }
        }
    }
}
