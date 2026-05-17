package com.artswipe.ui.screens.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToAuth: () -> Unit,
    onNavigateToDiscover: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val isLoggedIn by viewModel.isUserLoggedIn.collectAsState()

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "ArtSwipe", style = MaterialTheme.typography.headlineLarge)
    }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn != null) {
            delay(1000) // Give user a moment to see the logo
            if (isLoggedIn == true) {
                onNavigateToDiscover()
            } else {
                onNavigateToAuth()
            }
        }
    }
}
