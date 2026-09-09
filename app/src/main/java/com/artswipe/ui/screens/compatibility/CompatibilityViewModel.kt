package com.artswipe.ui.screens.compatibility

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artswipe.domain.model.User
import com.artswipe.domain.repository.AuthRepository
import com.artswipe.domain.util.ShareCode
import com.artswipe.domain.util.TasteComparison
import com.artswipe.domain.util.TasteEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

data class CompatibilityUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val comparisonResult: ComparisonResult? = null
)

data class ComparisonResult(val userA: User, val userB: User, val taste: TasteComparison)

@HiltViewModel
class CompatibilityViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CompatibilityUiState())
    val uiState = _uiState.asStateFlow()
    private var comparisonJob: Job? = null

    fun reset() {
        comparisonJob?.cancel()
        _uiState.value = CompatibilityUiState()
    }

    fun compareWithCode(input: String) {
        comparisonJob?.cancel()
        val code = ShareCode.parse(input)
        if (code == null) {
            _uiState.value = CompatibilityUiState(error = "Enter an ART share code or paste an ArtSwipe comparison link.")
            return
        }
        comparisonJob = viewModelScope.launch {
            _uiState.value = CompatibilityUiState(isLoading = true)
            try {
                withTimeout(15_000) {
                    val currentUser = authRepository.currentUser.first { it == null || it.isProfileLoaded }
                    if (currentUser == null) {
                        _uiState.value = CompatibilityUiState(error = "Sign in to compare your taste, then open this code again.")
                        return@withTimeout
                    }
                    if (currentUser.shareCode.equals(code, ignoreCase = true)) {
                        _uiState.value = CompatibilityUiState(error = "That's your code! Try a friend's code to compare.")
                        return@withTimeout
                    }
                    val other = authRepository.findUserByShareCode(code).getOrThrow()
                    _uiState.value = when {
                        other == null -> CompatibilityUiState(error = "We couldn't find that code. Check it and try again.")
                        other.userId == currentUser.userId -> CompatibilityUiState(error = "That's your profile. Try a friend's code.")
                        else -> CompatibilityUiState(comparisonResult = ComparisonResult(currentUser, other, TasteEngine.compare(currentUser, other)))
                    }
                }
            } catch (e: TimeoutCancellationException) {
                _uiState.value = CompatibilityUiState(error = "Your profile took too long to load. Check your connection and try again.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = CompatibilityUiState(error = "Couldn't load the comparison. Check your connection and try again.")
            }
        }
    }
}
