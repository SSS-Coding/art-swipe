package com.artswipe.ui.screens.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artswipe.domain.model.Artwork
import com.artswipe.domain.model.SwipeRecord
import com.artswipe.domain.repository.ArtworkRepository
import com.artswipe.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DiscoverUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isFetchingMore: Boolean = false
)

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val artworkRepository: ArtworkRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    private val _artworkQueue = MutableStateFlow<List<Artwork>>(emptyList())
    val artworkQueue: StateFlow<List<Artwork>> = _artworkQueue.asStateFlow()

    init {
        observeArtworkQueue()
        loadInitialArtworks()
    }

    private fun observeArtworkQueue() {
        viewModelScope.launch {
            artworkRepository.getArtworkQueue().collectLatest { remoteArtworks ->
                val currentList = _artworkQueue.value
                
                // Keep the current items that are still in the remote list (haven't been swiped)
                val stillValid = currentList.filter { current -> remoteArtworks.any { it.id == current.id } }
                
                // Find new items that aren't in our current in-memory queue
                val newItems = remoteArtworks.filter { remote -> currentList.none { it.id == remote.id } }
                
                // Stable append: Add new items to the end so the current view doesn't jump
                _artworkQueue.value = stillValid + newItems

                // If queue is empty and we aren't loading, try to get more
                if (_artworkQueue.value.isEmpty() && !_uiState.value.isLoading && !_uiState.value.isFetchingMore) {
                    fetchMore()
                }
            }
        }
    }

    private fun loadInitialArtworks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                artworkRepository.fetchMoreArtworks()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Connection issue. Please try again.")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun retryLoading() {
        loadInitialArtworks()
    }

    private fun fetchMore() {
        if (_uiState.value.isFetchingMore) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isFetchingMore = true)
            try {
                artworkRepository.fetchMoreArtworks()
            } catch (e: Exception) {
                // Background fetch fail is silent but we stop the flag
            } finally {
                _uiState.value = _uiState.value.copy(isFetchingMore = false)
            }
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
            
            // Proactively fetch more if the queue is getting low
            if (_artworkQueue.value.size < 8) {
                fetchMore()
            }
        }
    }
}
