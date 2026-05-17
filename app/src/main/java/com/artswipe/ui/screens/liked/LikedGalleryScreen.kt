package com.artswipe.ui.screens.liked

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.artswipe.domain.model.Artwork

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LikedGalleryScreen(
    onBack: () -> Unit,
    onNavigateToExplanation: (String) -> Unit,
    viewModel: LikedGalleryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Liked Artworks (${uiState.artworks.size})") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Style Filters
            ScrollableTabRow(
                selectedTabIndex = if (uiState.selectedStyle == null) 0 else uiState.styles.indexOf(uiState.selectedStyle) + 1,
                edgePadding = 16.dp,
                divider = {}
            ) {
                Tab(
                    selected = uiState.selectedStyle == null,
                    onClick = { viewModel.selectStyle(null) },
                    text = { Text("All") }
                )
                uiState.styles.forEach { style ->
                    Tab(
                        selected = uiState.selectedStyle == style,
                        onClick = { viewModel.selectStyle(style) },
                        text = { Text(style) }
                    )
                }
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.filteredArtworks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No liked artworks yet.")
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.filteredArtworks) { artwork ->
                        LikedArtworkCard(
                            artwork = artwork,
                            onClick = { onNavigateToExplanation(artwork.id) },
                            onUnlike = { viewModel.unlikeArtwork(artwork) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LikedArtworkCard(
    artwork: Artwork,
    onClick: () -> Unit,
    onUnlike: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.7f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = artwork.imageUrl,
                contentDescription = artwork.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Info Overlay
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                    .padding(8.dp)
            ) {
                Column {
                    Text(
                        text = artwork.title,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
            
            // Unlike Button
            IconButton(
                onClick = onUnlike,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Unlike",
                    tint = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}
