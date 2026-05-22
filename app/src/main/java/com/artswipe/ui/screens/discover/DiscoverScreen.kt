package com.artswipe.ui.screens.discover

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.artswipe.ui.screens.discover.components.SwipeableCard

@Composable
fun DiscoverScreen(
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
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Fetching masterpieces...")
            }
        } else if (artworkQueue.isEmpty()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp)
            ) {
                Text(
                    text = uiState.error ?: "No more art for now. Our curators are finding more!",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { viewModel.retryLoading() }) {
                    Text("Retry Discovery")
                }
            }
        } else {
            // Quick Swipes
            artworkQueue.take(2).reversed().forEach { artwork ->
                key(artwork.id) {
                    SwipeableCard(
                        artwork = artwork,
                        onSwipeLeft = {
                            viewModel.onSwipe(artwork, false)
                        },
                        onSwipeRight = {
                            viewModel.onSwipe(artwork, true)
                        }
                    )
                }
            }
            
            // Show a small loader at the bottom if background fetching
            if (uiState.isFetchingMore) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                )
            }
        }
    }
}
