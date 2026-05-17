package com.artswipe.ui.screens.compatibility

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artswipe.domain.model.User
import com.artswipe.domain.repository.AuthRepository
import com.artswipe.domain.util.StyleEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CompatibilityUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val comparisonResult: ComparisonResult? = null
)

data class ComparisonResult(
    val userA: User,
    val userB: User,
    val score: Float,
    val label: String,
    val sharedStyles: List<SharedStyle>,
    val differences: List<String>,
    val isLimitedData: Boolean
)

data class SharedStyle(
    val style: String,
    val scoreA: Float,
    val scoreB: Float
)

@HiltViewModel
class CompatibilityViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompatibilityUiState())
    val uiState: StateFlow<CompatibilityUiState> = _uiState.asStateFlow()

    fun compareWithCode(code: String) {
        viewModelScope.launch {
            _uiState.value = CompatibilityUiState(isLoading = true)
            
            val currentUser = authRepository.currentUser.first()
            if (currentUser == null) {
                _uiState.value = CompatibilityUiState(error = "You must be logged in.")
                return@launch
            }

            if (currentUser.shareCode == code.uppercase()) {
                _uiState.value = CompatibilityUiState(error = "You cannot compare with yourself!")
                return@launch
            }

            val result = authRepository.findUserByShareCode(code)
            if (result.isSuccess) {
                val otherUser = result.getOrNull()
                if (otherUser != null) {
                    val comparison = computeCompatibility(currentUser, otherUser)
                    _uiState.value = CompatibilityUiState(comparisonResult = comparison)
                } else {
                    _uiState.value = CompatibilityUiState(error = "User code not found.")
                }
            } else {
                _uiState.value = CompatibilityUiState(error = result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    private fun computeCompatibility(userA: User, userB: User): ComparisonResult {
        val scoresA = userA.styleScores
        val scoresB = userB.styleScores
        
        val allStyles = (scoresA.keys + scoresB.keys).distinct()
        val totalA = scoresA.values.sum().toFloat().coerceAtLeast(1f)
        val totalB = scoresB.values.sum().toFloat().coerceAtLeast(1f)

        var overlap = 0f
        val sharedStylesList = mutableListOf<SharedStyle>()
        
        for (style in allStyles) {
            val pctA = (scoresA[style] ?: 0) / totalA
            val pctB = (scoresB[style] ?: 0) / totalB
            val minOverlap = minOf(pctA, pctB)
            overlap += minOverlap
            
            if (minOverlap > 0.05f) { // Significant shared interest
                sharedStylesList.add(SharedStyle(style, pctA * 100f, pctB * 100f))
            }
        }

        val score = overlap * 100f
        val label = when {
            score >= 85 -> "Kindred Spirits"
            score >= 65 -> "Fellow Admirers"
            score >= 45 -> "Curious Contrast"
            score >= 25 -> "Worlds Apart"
            else -> "Total Opposites"
        }

        val diffs = mutableListOf<String>()
        val topA = scoresA.entries.maxByOrNull { it.value }?.key
        val topB = scoresB.entries.maxByOrNull { it.value }?.key
        
        if (topA != null && (scoresB[topA] ?: 0) <= 0) {
            diffs.add("You love $topA, but ${userB.displayName} hasn't discovered it yet.")
        }
        if (topB != null && (scoresA[topB] ?: 0) <= 0) {
            diffs.add("${userB.displayName} is a big fan of $topB.")
        }

        return ComparisonResult(
            userA = userA,
            userB = userB,
            score = score,
            label = label,
            sharedStyles = sharedStylesList.sortedByDescending { minOf(it.scoreA, it.scoreB) },
            differences = diffs,
            isLimitedData = userB.totalSwipes < 10
        )
    }
}
