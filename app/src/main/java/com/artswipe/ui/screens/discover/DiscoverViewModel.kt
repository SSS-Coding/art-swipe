package com.artswipe.ui.screens.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artswipe.domain.model.Artwork
import com.artswipe.domain.model.SwipeRecord
import com.artswipe.domain.repository.ArtworkRepository
import com.artswipe.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DiscoverUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isFetchingMore: Boolean = false,
    val isSaving: Boolean = false
)

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val artworkRepository: ArtworkRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState = _uiState.asStateFlow()
    private val _artworkQueue = MutableStateFlow<List<Artwork>>(emptyList())
    val artworkQueue = _artworkQueue.asStateFlow()

    init {
        viewModelScope.launch {
            artworkRepository.getArtworkQueue().collect { artworks ->
                val ids = artworks.map { it.id }.toSet()
                val oldIds = _artworkQueue.value.map { it.id }.toSet()
                _artworkQueue.value = _artworkQueue.value.filter { it.id in ids } + artworks.filter { it.id !in oldIds }
                if (artworks.size < 8 && _uiState.value.error == null) fetchMore()
            }
        }
    }

    fun retryLoading() = fetchMore()

    private fun fetchMore() {
        if (_uiState.value.isFetchingMore) return
        _uiState.update { it.copy(isFetchingMore = true, isLoading = _artworkQueue.value.isEmpty(), error = null) }
        viewModelScope.launch {
            try {
                artworkRepository.fetchMoreArtworks()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Couldn't load more art. Check your connection and retry.") }
            } finally {
                _uiState.update { it.copy(isLoading = false, isFetchingMore = false) }
            }
        }
    }

    fun onSwipe(artwork: Artwork, liked: Boolean) {
        if (_uiState.value.isSaving || _artworkQueue.value.firstOrNull()?.id != artwork.id) return
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                val user = authRepository.currentUser.first() ?: return@launch
                artworkRepository.saveSwipeRecord(SwipeRecord(user.userId, artwork.id, liked,
                    System.currentTimeMillis(), artwork.styleMovement))
                _artworkQueue.update { queue -> queue.filterNot { it.id == artwork.id } }
                if (_artworkQueue.value.size < 8) fetchMore()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Couldn't save that choice. Please try again.") }
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }
}
