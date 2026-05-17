package com.artswipe.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artswipe.domain.model.StylePercentage
import com.artswipe.domain.model.StyleProfile
import com.artswipe.domain.model.User
import com.artswipe.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val currentUser: StateFlow<User?> = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val styleProfile: StateFlow<StyleProfile?> = currentUser.map { user ->
        user?.let { calculateStyleProfile(it) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private fun calculateStyleProfile(user: User): StyleProfile {
        val scores = user.styleScores
        val totalLikes = scores.values.filter { it > 0 }.sum().toFloat()
        
        val breakdown = scores.map { (style, score) ->
            StylePercentage(
                style = style,
                percentage = if (totalLikes > 0 && score > 0) (score / totalLikes) * 100f else 0f,
                likeCount = score
            )
        }.filter { it.likeCount > 0 }.sortedByDescending { it.likeCount }

        val topStyle = breakdown.firstOrNull()?.style ?: "None"
        val personality = getPersonality(topStyle)

        return StyleProfile(
            topStyle = topStyle,
            personalityLabel = personality.first,
            personalityDescription = personality.second,
            styleBreakdown = breakdown.take(5),
            showPersonalityCard = user.totalSwipes >= 10
        )
    }

    private fun getPersonality(style: String): Pair<String, String> {
        return when (style.lowercase()) {
            "impressionism" -> "The Dreamer" to "You find beauty in the fleeting moments and the play of light."
            "baroque" -> "The Dramatist" to "You are drawn to grand scales, intense emotions, and theatrical contrast."
            "modernism" -> "The Visionary" to "You appreciate progress, bold forms, and the breaking of tradition."
            "surrealism" -> "The Wanderer" to "You love the illogical, the dreamlike, and the depth of the subconscious."
            "realism" -> "The Grounded" to "You value truth, authenticity, and the beauty of everyday life."
            "abstract" -> "The Free Spirit" to "You see beyond the surface, finding meaning in color, shape, and emotion."
            "renaissance" -> "The Classicist" to "You admire balance, harmony, and the timeless pursuit of human perfection."
            else -> "The Explorer" to "You are just beginning your journey into the world of art discovery."
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}
