package com.artswipe.ui.screens.liked

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artswipe.domain.model.Artwork
import com.artswipe.domain.model.SwipeRecord
import com.artswipe.domain.repository.ArtworkRepository
import com.artswipe.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LikedGalleryUiState(
    val artworks: List<Artwork> = emptyList(),
    val filteredArtworks: List<Artwork> = emptyList(),
    val styles: List<String> = emptyList(),
    val selectedStyle: String? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class LikedGalleryViewModel @Inject constructor(
    private val artworkRepository: ArtworkRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _selectedStyle = MutableStateFlow<String?>(null)
    
    val uiState: StateFlow<LikedGalleryUiState> = combine(
        artworkRepository.getLikedArtworks(),
        _selectedStyle
    ) { likedArtworks, selectedStyle ->
        val styles = likedArtworks.map { it.styleMovement }.distinct().sorted()
        val filtered = if (selectedStyle == null) {
            likedArtworks
        } else {
            likedArtworks.filter { it.styleMovement == selectedStyle }
        }
        
        LikedGalleryUiState(
            artworks = likedArtworks,
            filteredArtworks = filtered,
            styles = styles,
            selectedStyle = selectedStyle,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LikedGalleryUiState(isLoading = true))

    fun selectStyle(style: String?) {
        _selectedStyle.value = style
    }

    fun unlikeArtwork(artwork: Artwork) {
        viewModelScope.launch {
            val user = authRepository.currentUser.first() ?: return@launch
            // To unlike, we basically create a "disliked" record or remove the liked one.
            // The requirement says: "Option to unlike from this screen (updates Firestore + recalculates style scores)"
            // We'll implement a 'deleteSwipeRecord' or similar in repository.
            val record = SwipeRecord(
                userId = user.userId,
                artworkId = artwork.id,
                liked = false, // Changing to disliked effectively unlikes it and updates scores
                timestamp = System.currentTimeMillis(),
                styleMovement = artwork.styleMovement
            )
            // But actually, just deleting the record might be cleaner if we want it to reappear in Discover.
            // However, the "style-engine" logic adds +2 for like and -1 for dislike.
            // If we "unlike", we should probably reverse the +2.
            artworkRepository.removeSwipeRecord(user.userId, artwork.id, artwork.styleMovement)
        }
    }
}
