package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.GeminiGroundingService
import com.example.ai.GroundingResult
import com.example.model.OrganizationEvent
import com.example.ui.theme.BirlikteCyan
import com.example.ui.theme.BirlikteIndigo
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusGreen
import kotlinx.coroutines.launch

@Composable
fun GeminiVenueIntelligenceDialog(
    event: OrganizationEvent,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val service = remember { GeminiGroundingService() }

    var userQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<GroundingResult?>(null) }

    // Initial inquiry for venue details on launch
    LaunchedEffect(event.venueName) {
        isLoading = true
        result = service.queryVenueAndTraffic(
            venueName = event.venueName,
            address = event.venueAddress,
            userQuery = "Mekanın çalışma saatleri, puanı, otopark durumu ve en rahat ulaşım yöntemleri nedir?"
        )
        isLoading = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = BirlikteCyan.copy(alpha = 0.15f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = BirlikteCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        "Mekan & Rota Rehberi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Google Maps & Search Grounding • Gemini 3.8 Flash",
                        style = MaterialTheme.typography.labelSmall,
                        color = BirlikteCyan,
                        fontSize = 10.sp
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Event Venue Header
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = StatusAmber)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(event.venueName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(event.venueAddress, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Quick Question Suggestion Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SuggestionChip(
                        onClick = {
                            userQuery = "Otopark nerede bulabilirim?"
                            scope.launch {
                                isLoading = true
                                result = service.queryVenueAndTraffic(event.venueName, event.venueAddress, userQuery)
                                isLoading = false
                            }
                        },
                        label = { Text("Otopark?", fontSize = 11.sp) },
                        icon = { Icon(Icons.Default.LocalParking, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    )
                    SuggestionChip(
                        onClick = {
                            userQuery = "Marmaray ve metro ile nasıl gelinir?"
                            scope.launch {
                                isLoading = true
                                result = service.queryVenueAndTraffic(event.venueName, event.venueAddress, userQuery)
                                isLoading = false
                            }
                        },
                        label = { Text("Toplu Taşıma?", fontSize = 11.sp) },
                        icon = { Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    )
                }

                // AI Response Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        if (isLoading) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = BirlikteCyan,
                                    strokeWidth = 2.5.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Google Maps & Search verileri taranıyor...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else if (result != null) {
                            Text(
                                result?.text ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 20.sp
                            )

                            // Grounding Sources if present
                            if (result?.sources?.isNotEmpty() == true) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    "Kaynaklar (Google Maps / Search):",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                result?.sources?.forEach { src ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                try {
                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(src.uri))
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    // no-op
                                                }
                                            }
                                            .padding(vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            androidx.compose.material.icons.Icons.AutoMirrored.Filled.OpenInNew,
                                            contentDescription = null,
                                            tint = BirlikteCyan,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            src.title,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = BirlikteCyan,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Custom Query Input Field
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = userQuery,
                        onValueChange = { userQuery = it },
                        placeholder = { Text("Mekan veya yol hakkında sor...", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledIconButton(
                        onClick = {
                            if (userQuery.isNotBlank()) {
                                scope.launch {
                                    isLoading = true
                                    result = service.queryVenueAndTraffic(event.venueName, event.venueAddress, userQuery.trim())
                                    isLoading = false
                                }
                            }
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = BirlikteIndigo)
                    ) {
                        Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.Send, contentDescription = "Sor", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = BirlikteIndigo)
            ) {
                Text("Kapat", color = Color.White)
            }
        }
    )
}
