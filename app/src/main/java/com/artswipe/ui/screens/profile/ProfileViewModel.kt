package com.artswipe.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artswipe.domain.model.Artwork
import com.artswipe.domain.model.StylePercentage
import com.artswipe.domain.model.StyleProfile
import com.artswipe.domain.model.User
import com.artswipe.domain.repository.ArtworkRepository
import com.artswipe.domain.repository.AuthRepository
import com.artswipe.domain.util.StyleEngine
import com.artswipe.domain.util.TasteEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val artworkRepository: ArtworkRepository
) : ViewModel() {

    private var syncedUserId: String? = null
    private val _resetState = MutableStateFlow<String?>(null)
    val resetState = _resetState.asStateFlow()
    private val _isResetting = MutableStateFlow(false)
    val isResetting = _isResetting.asStateFlow()
    private val _authStateResolved = MutableStateFlow(false)
    val authStateResolved = _authStateResolved.asStateFlow()

    val currentUser: StateFlow<User?> = authRepository.currentUser
        .onEach { user ->
            _authStateResolved.value = true
            if (user == null) syncedUserId = null
            user?.takeIf { it.isProfileLoaded && it.userId != syncedUserId }?.let {
                syncedUserId = it.userId
                viewModelScope.launch {
                    artworkRepository.syncLikedArtworksFromRemote(it.userId)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val styleProfile: StateFlow<StyleProfile?> = combine(
        authRepository.currentUser,
        artworkRepository.getLikedArtworks()
    ) { user, likedArtworks ->
        user?.takeIf { it.isProfileLoaded }?.let { calculateStyleProfile(it, likedArtworks) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private fun calculateStyleProfile(user: User, likedArtworks: List<Artwork>): StyleProfile {
        val scores = TasteEngine.likeCounts(user)
        val distribution = TasteEngine.distribution(user)

        val breakdown = scores.map { (style, score) ->
            // Find a thumbnail from liked artworks of this style
            val thumbnail = likedArtworks.firstOrNull { TasteEngine.normalizeStyle(it.styleMovement) == style }?.imageUrl
            StylePercentage(
                style = style,
                percentage = distribution[style] ?: 0f,
                likeCount = score,
                thumbnailUrl = thumbnail
            )
        }.filter { it.likeCount > 0 }.sortedByDescending { it.likeCount }

        val leastLiked = user.styleDislikes.entries.filter { it.value > 0 }
            .sortedByDescending { it.value }
            .take(3)
            .map { it.key }

        val topStyle = breakdown.firstOrNull()?.style ?: "None"
        val (label, description) = StyleEngine.getPersonality(topStyle)

        return StyleProfile(
            topStyle = topStyle,
            personalityLabel = label,
            personalityDescription = description,
            styleBreakdown = breakdown,
            leastLikedStyles = leastLiked,
            showPersonalityCard = user.totalSwipes >= 10 && breakdown.isNotEmpty(),
            totalSwipes = user.totalSwipes
        )
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    fun resetPreferences() {
        val userId = currentUser.value?.userId ?: return
        if (_isResetting.value) return
        _isResetting.value = true
        _resetState.value = null
        viewModelScope.launch {
            try {
                artworkRepository.resetPreferences(userId)
                _resetState.value = "Your preferences have been reset."
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                _resetState.value = "Couldn't finish resetting. Check your connection and try again."
            } finally {
                _isResetting.value = false
            }
        }
    }
}
