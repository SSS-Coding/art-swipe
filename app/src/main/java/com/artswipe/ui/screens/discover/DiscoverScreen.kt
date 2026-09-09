package com.artswipe.ui.screens.discover

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
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
    onNavigateToExplanation: (String) -> Unit = {},
    viewModel: DiscoverViewModel = hiltViewModel()
) {
    val queue by viewModel.artworkQueue.collectAsState()
    val state by viewModel.uiState.collectAsState()
    Column(modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Find what moves you.", style = MaterialTheme.typography.headlineLarge)
        Text("A daily wander through the world's collections", style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (state.isFetchingMore) LinearProgressIndicator(Modifier.fillMaxWidth())
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            if (queue.isEmpty()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (state.isLoading || state.isFetchingMore) {
                        CircularProgressIndicator()
                        Text("Opening the gallery…")
                    } else {
                        Text(state.error ?: "You've explored this collection. Ready for more?", textAlign = TextAlign.Center)
                        Button(onClick = viewModel::retryLoading) { Text("Load more art") }
                    }
                }
            } else {
                queue.take(2).reversed().forEach { artwork ->
                    key(artwork.id) {
                        SwipeableCard(artwork, { viewModel.onSwipe(artwork, false) },
                            { viewModel.onSwipe(artwork, true) }, enabled = artwork.id == queue.first().id && !state.isSaving)
                    }
                }
            }
        }
        if (queue.isNotEmpty()) {
            state.error?.let {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(it, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = viewModel::retryLoading) { Text("Retry") }
                }
            }
            val current = queue.first()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = { viewModel.onSwipe(current, false) }, enabled = !state.isSaving) {
                    Icon(Icons.Default.Close, null); Spacer(Modifier.width(6.dp)); Text("Pass")
                }
                IconButton(onClick = { onNavigateToExplanation(current.id) }) { Icon(Icons.Default.Info, "Artwork details") }
                Button(onClick = { viewModel.onSwipe(current, true) }, enabled = !state.isSaving) {
                    Icon(Icons.Default.Favorite, null); Spacer(Modifier.width(6.dp)); Text("Like")
                }
            }
            Text("Swipe left to pass · right to save", Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
