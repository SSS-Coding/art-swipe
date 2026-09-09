package com.artswipe.ui.screens.explanation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artswipe.data.local.ArtworkDao
import com.artswipe.domain.model.Artwork
import com.artswipe.domain.repository.ArtworkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import com.artswipe.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExplanationUiState(
    val artwork: Artwork? = null,
    val isLiked: Boolean? = null,
    val styleDescription: String = "",
    val isLoading: Boolean = true
)

@HiltViewModel
class ExplanationViewModel @Inject constructor(
    private val artworkRepository: ArtworkRepository,
    private val artworkDao: ArtworkDao,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExplanationUiState())
    val uiState: StateFlow<ExplanationUiState> = _uiState.asStateFlow()

    fun loadArtwork(artworkId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val artwork = artworkRepository.getArtworkById(artworkId)
            val user = authRepository.currentUser.first()
            val swipeRecord = user?.let { artworkDao.getLatestSwipeForArtwork(it.userId, artworkId) }

            _uiState.value = _uiState.value.copy(
                artwork = artwork,
                isLiked = swipeRecord?.liked,
                styleDescription = getStyleDescription(artwork?.styleMovement ?: ""),
                isLoading = false
            )
        }
    }

    private fun getStyleDescription(style: String): String {
        return when (style.lowercase()) {
            "impressionism" -> "Impressionism is a 19th-century art movement characterized by relatively small, thin, yet visible brush strokes, open composition, and emphasis on accurate depiction of light in its changing qualities."
            "baroque" -> "The Baroque is a highly ornate and often extravagant style of architecture, music, dance, painting, sculpture and other arts that flourished in Europe from the early 17th century until the 1740s."
            "modernism" -> "Modernism is both a philosophical movement and an art movement that, along with cultural trends and changes, arose from wide-scale and far-reaching transformations in Western society during the late 19th and early 20th centuries."
            "surrealism" -> "Surrealism is a cultural movement that started in 1917, and is best known for its visual artworks and writings. Artists painted unnerving, illogical scenes with photographic precision, creating strange creatures from everyday objects."
            "realism" -> "Realism was an artistic movement that began in France in the 1850s, after the 1848 Revolution. Realists rejected Romanticism, which had dominated French literature and art since the late 18th century."
            "abstract" -> "Abstract art uses visual language of shape, form, color and line to create a composition which may exist with a degree of independence from visual references in the world."
            "renaissance" -> "The Renaissance was a fervent period of European cultural, artistic, political and economic 'rebirth' following the Middle Ages. Generally described as taking place from the 14th century to the 17th century."
            else -> "A unique art style that contributes to the rich history of human expression."
        }
    }
}
