package com.artswipe.ui.screens.recommendations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artswipe.domain.model.Artwork
import com.artswipe.domain.repository.ArtworkRepository
import com.artswipe.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class RecommendationsViewModel @Inject constructor(
    private val artworkRepository: ArtworkRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    val recommendedArtworks: StateFlow<List<Artwork>> = authRepository.currentUser
        .flatMapLatest { user ->
            if (user == null) return@flatMapLatest flowOf(emptyList())

            val topStyles = com.artswipe.domain.util.TasteEngine.likeCounts(user).entries
                .filter { it.value > 0 }
                .sortedByDescending { it.value }
                .take(3)
                .map { it.key }

            artworkRepository.getRecommendations(topStyles)
        }
        .onEach { _isLoading.value = false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
