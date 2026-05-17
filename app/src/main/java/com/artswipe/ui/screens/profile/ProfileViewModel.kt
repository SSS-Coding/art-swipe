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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val artworkRepository: ArtworkRepository
) : ViewModel() {

    val currentUser: StateFlow<User?> = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val styleProfile: StateFlow<StyleProfile?> = combine(
        authRepository.currentUser,
        artworkRepository.getLikedArtworks()
    ) { user, likedArtworks ->
        user?.let { calculateStyleProfile(it, likedArtworks) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private fun calculateStyleProfile(user: User, likedArtworks: List<Artwork>): StyleProfile {
        val scores = user.styleScores
        val totalLikes = scores.values.filter { it > 0 }.sum().toFloat()
        
        val breakdown = scores.map { (style, score) ->
            // Find a thumbnail from liked artworks of this style
            val thumbnail = likedArtworks.firstOrNull { it.styleMovement == style }?.imageUrl
            StylePercentage(
                style = style,
                percentage = if (totalLikes > 0 && score > 0) (score / totalLikes) * 100f else 0f,
                likeCount = score,
                thumbnailUrl = thumbnail
            )
        }.filter { it.likeCount > 0 }.sortedByDescending { it.likeCount }

        val topStyle = breakdown.firstOrNull()?.style ?: "None"
        val (label, description) = StyleEngine.getPersonality(topStyle)

        return StyleProfile(
            topStyle = topStyle,
            personalityLabel = label,
            personalityDescription = description,
            styleBreakdown = breakdown,
            showPersonalityCard = user.totalSwipes >= 10
        )
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}
