package com.artswipe.ui.screens.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artswipe.domain.model.Artwork
import com.artswipe.domain.model.SwipeRecord
import com.artswipe.domain.repository.ArtworkRepository
import com.artswipe.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DiscoverUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val artworkRepository: ArtworkRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    val artworkQueue: StateFlow<List<Artwork>> = artworkRepository.getArtworkQueue()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadArtworks()
    }

    private fun loadArtworks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            artworkRepository.fetchMoreArtworks()
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun onSwipe(artwork: Artwork, liked: Boolean) {
        viewModelScope.launch {
            val user = authRepository.currentUser.first() ?: return@launch
            
            val record = SwipeRecord(
                userId = user.userId,
                artworkId = artwork.id,
                liked = liked,
                timestamp = System.currentTimeMillis(),
                styleMovement = artwork.styleMovement
            )
            
            artworkRepository.saveSwipeRecord(record)
            
            // Trigger refetch if queue is low
            if (artworkQueue.value.size < 5) {
                artworkRepository.fetchMoreArtworks()
            }
        }
    }
}
