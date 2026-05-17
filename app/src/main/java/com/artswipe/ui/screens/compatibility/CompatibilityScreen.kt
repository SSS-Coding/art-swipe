package com.artswipe.ui.screens.compatibility

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompatibilityScreen(
    initialCode: String = "",
    onBack: () -> Unit,
    viewModel: CompatibilityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var codeInput by remember { mutableStateOf(initialCode) }
    val context = LocalContext.current

    LaunchedEffect(initialCode) {
        if (initialCode.isNotEmpty()) {
            viewModel.compareWithCode(initialCode)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Taste Compatibility") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.comparisonResult == null) {
            // Code Entry State
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Compare with a Friend",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Enter their unique share code to see how your art tastes align.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                OutlinedTextField(
                    value = codeInput,
                    onValueChange = { codeInput = it.uppercase() },
                    label = { Text("Friend's Code (e.g. ART-XXXX)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                if (uiState.error != null) {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = { viewModel.compareWithCode(codeInput) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = codeInput.length >= 8 && !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("Compare")
                    }
                }
            }
        } else {
            // Result State
            val result = uiState.comparisonResult!!
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item {
                    MatchCard(result)
                }
                
                item {
                    Text(
                        text = "Shared Styles",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                if (result.sharedStyles.isEmpty()) {
                    item {
                        Text("No significant overlap yet. You both have unique tastes!")
                    }
                } else {
                    items(result.sharedStyles) { shared ->
                        SharedStyleRow(shared, result.userB.displayName)
                    }
                }
                
                item {
                    Text(
                        text = "Where You Differ",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                items(result.differences) { diff ->
                    Text(text = "• $diff", style = MaterialTheme.typography.bodyLarge)
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { 
                            val sendIntent: Intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "My art taste is ${result.score.toInt()}% compatible with ${result.userB.displayName} on ArtSwipe! Check your compatibility with my code: ${result.userA.shareCode}")
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, null)
                            context.startActivity(shareIntent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share Results")
                    }
                }
            }
        }
    }
}

@Composable
fun MatchCard(result: ComparisonResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${result.score.toInt()}%",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Text(
                text = result.label,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                UserMiniCard("You", result.userA)
                Text("×", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                UserMiniCard(result.userB.displayName, result.userB)
            }
        }
    }
}

@Composable
fun UserMiniCard(label: String, user: User) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        // Personality mapping (hardcoded for simplicity in UI)
        val personality = when (user.styleScores.maxByOrNull { it.value }?.key?.lowercase()) {
            "impressionism" -> "Dreamer"
            "baroque" -> "Dramatist"
            "modernism" -> "Visionary"
            else -> "Explorer"
        }
        Text(text = personality, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SharedStyleRow(shared: SharedStyle, otherName: String) {
    Column {
        Text(text = shared.style, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(shared.scoreA)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .weight(shared.scoreB)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.secondary)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "You: ${shared.scoreA.toInt()}%", fontSize = 10.sp)
            Text(text = "$otherName: ${shared.scoreB.toInt()}%", fontSize = 10.sp)
        }
    }
}

// Re-defining User to avoid import issues if needed, but assuming it's available
private typealias User = com.artswipe.domain.model.User
