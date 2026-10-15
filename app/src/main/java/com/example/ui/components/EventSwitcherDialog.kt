package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.localization.Loc
import com.example.model.AppLanguage
import com.example.model.OrganizationEvent
import com.example.ui.theme.BirlikteIndigo
import com.example.ui.theme.StatusGreen

@Composable
fun EventSwitcherDialog(
    events: List<OrganizationEvent>,
    currentEventId: String,
    lang: AppLanguage,
    onSelectEvent: (String) -> Unit,
    onCreateEvent: (
        title: String,
        description: String,
        category: String,
        date: String,
        time: String,
        venueName: String,
        venueAddress: String,
        lat: Double,
        lng: Double
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // New event form state
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Buluşma") }
    var date by remember { mutableStateOf("15 Ekim 2026") }
    var time by remember { mutableStateOf("20:00") }
    var venueName by remember { mutableStateOf("") }
    var venueAddress by remember { mutableStateOf("Kadıköy / İstanbul") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                Loc.s("change_event", lang),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                TabRow(selectedTabIndex = selectedTabIndex) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text("Mevcut Gruplar (${events.size})") }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text("+ Yeni Oluştur") }
                    )
                }

                if (selectedTabIndex == 0) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        events.forEach { ev ->
                            val isSelected = ev.id == currentEventId
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) BirlikteIndigo.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, BirlikteIndigo) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelectEvent(ev.id)
                                        onDismiss()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            ev.title,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "📍 ${ev.venueName} • 🕒 ${ev.eventTime}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            "${ev.members.size} Katılımcı • ${ev.category}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = BirlikteIndigo
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = StatusGreen
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Create New Event Form
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text(Loc.s("event_title", lang)) },
                            placeholder = { Text("Örn: Şirket Yemeği, Piknik...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = venueName,
                            onValueChange = { venueName = it },
                            label = { Text(Loc.s("venue", lang)) },
                            placeholder = { Text("Örn: Beşiktaş İskele Meydanı") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = date,
                                onValueChange = { date = it },
                                label = { Text("Tarih") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = time,
                                onValueChange = { time = it },
                                label = { Text("Saat") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Açıklama & Detaylar") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (selectedTabIndex == 1) {
                Button(
                    onClick = {
                        if (title.isNotBlank() && venueName.isNotBlank()) {
                            onCreateEvent(
                                title,
                                description.ifBlank { "Organizasyon detayları" },
                                category,
                                date,
                                time,
                                venueName,
                                venueAddress,
                                41.0422, // Beşiktaş coords as default
                                29.0067
                            )
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BirlikteIndigo)
                ) {
                    Text("Organizasyonu Başlat", color = Color.White)
                }
            } else {
                Button(onClick = onDismiss) {
                    Text("Kapat")
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(Loc.s("cancel", lang))
            }
        }
    )
}
