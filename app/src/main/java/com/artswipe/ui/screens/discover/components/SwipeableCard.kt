package com.artswipe.ui.screens.discover.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import com.artswipe.domain.model.Artwork
import kotlin.math.roundToInt

@Composable
fun SwipeableCard(
    artwork: Artwork,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val width = with(LocalDensity.current) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
    val threshold = width * 0.25f
    val offset = remember(artwork.id) { Animatable(0f) }
    var drag by remember(artwork.id) { mutableFloatStateOf(0f) }
    var settling by remember(artwork.id) { mutableStateOf(false) }
    var imageFailed by remember(artwork.id) { mutableStateOf(false) }
    var imageLoading by remember(artwork.id) { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val latestLeft by rememberUpdatedState(onSwipeLeft)
    val latestRight by rememberUpdatedState(onSwipeRight)
    val x = if (settling) offset.value else drag
    Box(modifier.fillMaxSize().offset { IntOffset(x.roundToInt(), 0) }
        .graphicsLayer { rotationZ = (x / width) * 15f }
        .pointerInput(artwork.id, enabled) {
            if (enabled) detectHorizontalDragGestures(
                onHorizontalDrag = { change, amount -> if (!settling) { change.consume(); drag += amount } },
                onDragCancel = { drag = 0f },
                onDragEnd = {
                    if (!settling) {
                        val choice = when { drag > threshold -> true; drag < -threshold -> false; else -> null }
                        scope.launch {
                            offset.snapTo(drag)
                            settling = true
                            if (choice != null) {
                                offset.animateTo(if (choice) width else -width, tween(200))
                                if (choice) latestRight() else latestLeft()
                            }
                            offset.animateTo(0f, tween(200))
                            drag = 0f
                            settling = false
                        }
                    }
                }
            )
        }.clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
        AsyncImage(model = artwork.imageUrl, contentDescription = artwork.title,
            modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit,
            onLoading = { imageLoading = true }, onSuccess = { imageLoading = false; imageFailed = false },
            onError = { imageLoading = false; imageFailed = true })
        if (imageLoading) CircularProgressIndicator(Modifier.align(Alignment.Center))
        if (imageFailed) Text("Image unavailable. Open details to learn about this work.",
            Modifier.align(Alignment.Center).padding(24.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Column(Modifier.align(Alignment.BottomStart).fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.88f))))
            .padding(top = 48.dp, start = 20.dp, end = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(artwork.styleMovement, color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelLarge)
            Text(artwork.title, color = Color.White, style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold, maxLines = 3, overflow = TextOverflow.Ellipsis)
            Text(listOfNotNull(artwork.artist, artwork.year).joinToString(" · "), color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        if (kotlin.math.abs(x) > threshold * 0.25f) {
            Surface(Modifier.align(if (x > 0) Alignment.TopStart else Alignment.TopEnd).padding(20.dp),
                shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (x > 0) Icons.Default.Favorite else Icons.Default.Close, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp)); Text(if (x > 0) "LIKE" else "PASS", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
