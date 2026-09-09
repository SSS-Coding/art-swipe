package com.artswipe.ui.screens.compatibility

import android.content.Intent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.artswipe.domain.model.User
import com.artswipe.domain.util.SharedStyle
import com.artswipe.domain.util.ShareCode
import com.artswipe.domain.util.StyleEngine
import com.artswipe.domain.util.TasteEngine
import androidx.hilt.navigation.compose.hiltViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompatibilityScreen(
    initialCode: String = "",
    onBack: () -> Unit,
    viewModel: CompatibilityViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var input by rememberSaveable(initialCode) { mutableStateOf(initialCode) }
    val context = LocalContext.current
    val keyboard = LocalSoftwareKeyboardController.current
    val compare = { keyboard?.hide(); viewModel.compareWithCode(input) }
    LaunchedEffect(initialCode) {
        if (initialCode.isNotBlank()) viewModel.compareWithCode(initialCode)
    }
    Scaffold(topBar = {
        TopAppBar(title = { Text("Compare tastes") }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
        })
    }) { padding ->
        val result = state.comparisonResult
        if (result == null) {
            Column(
                Modifier.fillMaxSize().padding(padding).imePadding()
                    .verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Spacer(Modifier.height(16.dp))
                Icon(Icons.AutoMirrored.Filled.CompareArrows, null,
                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                Text("Art brings us together.", style = MaterialTheme.typography.displaySmall)
                Text("Discover where your tastes meet, and what you could show each other next.",
                    style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(
                    value = input, onValueChange = { input = it },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    label = { Text("Friend's code or comparison link") },
                    placeholder = { Text("ART-XXXX") }, enabled = !state.isLoading,
                    isError = state.error != null,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { if (!state.isLoading) compare() })
                )
                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Button(onClick = compare, enabled = ShareCode.parse(input) != null && !state.isLoading,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                    if (state.isLoading) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(12.dp))
                        Text("Finding your common ground…")
                    } else Text("Compare our tastes")
                }
                OutlinedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("A little more discovery goes a long way", fontWeight = FontWeight.SemiBold)
                        Text("Try at least 10 swipes each. Your comparison reflects the styles in your liked art and evolves as you explore.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            val taste = result.taste
            LazyColumn(Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                item { MatchCard(result) }
                if (taste.isLimitedData || taste.score == null) {
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(if (taste.score == null) "Your comparison is still taking shape" else "An early impression", fontWeight = FontWeight.Bold)
                                Text(if (taste.score == null) "Both people need liked artwork with known styles before we can calculate a meaningful percentage."
                                    else "One or both of you have fewer than 10 swipes. This score may change as you discover more art.")
                            }
                        }
                    }
                }
                item {
                    Text("Common ground", style = MaterialTheme.typography.headlineSmall)
                    Text("Share of each person's liked art", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (taste.sharedStyles.isEmpty()) {
                    item { Text("No shared liked styles yet. There's more to discover together.") }
                }
                items(taste.sharedStyles, key = { it.style }) { SharedStyleRow(it, result.userB.displayName.ifBlank { "Friend" }) }
                if (taste.differences.isNotEmpty()) {
                    item { Text("A different point of view", style = MaterialTheme.typography.headlineSmall) }
                    items(taste.differences) { Text(it, style = MaterialTheme.typography.bodyLarge) }
                }
                if (taste.sharedDislikes.isNotEmpty()) {
                    item {
                        OutlinedCard {
                            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Styles you've both passed on", fontWeight = FontWeight.Bold)
                                Text(taste.sharedDislikes.joinToString(" · "))
                                Text("Shared passes are context and don't add points to your match.", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                item {
                    Text("How the score works", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("We compare the proportion of likes in each known style and add the overlap. 100% means identical style proportions; 0% means no shared liked styles. Unclassified art is left out. This describes your art preferences, not your relationship.",
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                item {
                    if (taste.score != null) {
                        Button(onClick = {
                            val message = "${taste.score}% shared art taste with ${result.userB.displayName} on ArtSwipe! Compare with me: artswipe://compare?code=${result.userA.shareCode}"
                            context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"; putExtra(Intent.EXTRA_TEXT, message)
                            }, "Share your comparison"))
                        }, enabled = result.userA.shareCode.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Share, null); Spacer(Modifier.width(8.dp)); Text("Share comparison")
                        }
                    }
                    OutlinedButton(onClick = { input = ""; viewModel.reset() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Compare with someone else")
                    }
                }
            }
        }
    }
}

@Composable
fun MatchCard(result: ComparisonResult) {
    val taste = result.taste
    val progress by animateFloatAsState((taste.score ?: 0) / 100f, tween(900), label = "Taste overlap")
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("YOUR SHARED ART TASTE", style = MaterialTheme.typography.labelLarge)
            Box(Modifier.size(168.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxSize(),
                    strokeWidth = 8.dp, trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f))
                Text(taste.score?.let { "$it%" } ?: "—", style = MaterialTheme.typography.displayLarge)
            }
            Text(taste.label, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                UserMiniCard("You", result.userA, Modifier.weight(1f))
                UserMiniCard(result.userB.displayName.ifBlank { "Friend" }, result.userB, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun UserMiniCard(label: String, user: User, modifier: Modifier = Modifier) {
    val topStyle = TasteEngine.likeCounts(user).entries.sortedBy { it.key }.maxByOrNull { it.value }?.key.orEmpty()
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
        Text(StyleEngine.getPersonality(topStyle).first, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text("${user.totalSwipes.coerceAtLeast(0)} swipes", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun SharedStyleRow(shared: SharedStyle, otherName: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(shared.style, style = MaterialTheme.typography.titleMedium)
        TasteBar("You", shared.scoreA, MaterialTheme.colorScheme.primary)
        TasteBar(otherName, shared.scoreB, MaterialTheme.colorScheme.tertiary)
    }
}

@Composable
private fun TasteBar(name: String, percentage: Float, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(name, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
            Text("${percentage.roundToInt()}%", style = MaterialTheme.typography.labelMedium)
        }
        LinearProgressIndicator(progress = { (percentage / 100f).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(8.dp), color = color)
    }
}
