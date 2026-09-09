package com.artswipe

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.artswipe.domain.model.User
import com.artswipe.domain.repository.AuthRepository
import com.artswipe.ui.screens.compatibility.CompatibilityScreen
import com.artswipe.ui.screens.compatibility.CompatibilityViewModel
import com.artswipe.ui.theme.ArtSwipeTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.junit.Rule
import org.junit.Test
import java.io.File

class CompatibilityScreenTest {
    @get:Rule val compose = createComposeRule()
    private val me = User("a", "You", "", "", 20,
        mapOf("Abstract" to 6, "Realism" to 2), shareCode = "ART-AAAA")
    private val friend = User("b", "Alex", "", "", 30,
        mapOf("Abstract" to 2, "Realism" to 6), shareCode = "ART-BBBB")

    private fun show(current: User = me, other: User = friend, dark: Boolean = false) {
        val repository = object : AuthRepository {
            override val currentUser: Flow<User?> = flow {
                emit(current.copy(styleScores = emptyMap(), isProfileLoaded = false))
                delay(150)
                emit(current)
            }
            override suspend fun findUserByShareCode(code: String) = Result.success(other.takeIf { it.shareCode == code })
            override suspend fun signInWithEmail(email: String, pass: String) = Result.success(Unit)
            override suspend fun signUpWithEmail(email: String, pass: String, displayName: String) = Result.success(Unit)
            override suspend fun signInWithGoogle(idToken: String) = Result.success(Unit)
            override suspend fun signOut() = Unit
            override suspend fun sendPasswordResetEmail(email: String) = Result.success(Unit)
        }
        val viewModel = CompatibilityViewModel(repository)
        compose.setContent { ArtSwipeTheme(darkTheme = dark) {
            CompatibilityScreen(onBack = {}, viewModel = viewModel)
        } }
    }
    private fun compare() {
        compose.onNodeWithText("Friend's code or comparison link").performTextInput("artswipe://compare?code=ART-BBBB")
        compose.onNodeWithText("Compare our tastes").performClick()
    }
    private fun screenshot(name: String) {
        compose.waitForIdle()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val file = File(context.getExternalFilesDir(null), "$name.png")
        file.outputStream().use { compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
    @Test fun waitsForRealProfileAndCanCompareAgain() {
        show()
        screenshot("compare-entry")
        compare()
        compose.waitUntil(10_000) { compose.onAllNodesWithText("50%").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Curious Contrast").assertIsDisplayed()
        screenshot("compare-result")
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Compare with someone else"))
        compose.onNodeWithText("Compare with someone else").performClick()
        compose.onNodeWithText("Compare our tastes").assertIsDisplayed()
    }
    @Test fun emptyProfileDoesNotPretendToBeZeroCompatible() {
        show(current = me.copy(styleScores = emptyMap(), totalSwipes = 0))
        compare()
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Still discovering").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("0%").assertDoesNotExist()
        screenshot("compare-empty")
    }
    @Test fun darkThemeShowsLimitedDataForEitherPerson() {
        show(current = me.copy(totalSwipes = 4), dark = true)
        compare()
        compose.waitUntil(10_000) { compose.onAllNodesWithText("50%").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("An early impression").performScrollTo().assertIsDisplayed()
        screenshot("compare-dark")
    }
}
