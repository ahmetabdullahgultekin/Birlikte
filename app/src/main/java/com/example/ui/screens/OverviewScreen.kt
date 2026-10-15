package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.Loc
import com.example.model.AppLanguage
import com.example.model.AttendanceStatus
import com.example.model.LiveStatus
import com.example.model.Member
import com.example.model.OrganizationEvent
import com.example.model.PrivacyMode
import com.example.model.VehicleType
import com.example.ui.components.GeminiVenueIntelligenceDialog
import com.example.ui.components.QrInviteDialog
import com.example.ui.components.StatusUpdateDialog
import com.example.ui.theme.BirlikteCyan
import com.example.ui.theme.BirlikteIndigo
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRose

@Composable
fun OverviewScreen(
    event: OrganizationEvent,
    lang: AppLanguage,
    onNavigateToMapWithMember: (Member) -> Unit,
    onUpdateMyStatus: (
        attendance: AttendanceStatus,
        liveStatus: LiveStatus,
        departureTime: String,
        originName: String,
        vehicleType: VehicleType,
        privacyMode: PrivacyMode
    ) -> Unit
) {
    val context = LocalContext.current
    var showStatusDialog by remember { mutableStateOf(false) }
    var showQrDialog by remember { mutableStateOf(false) }
    var showAiDialog by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val myMember = event.members.find { it.isCurrentUser } ?: event.members.firstOrNull()

    val attendingList = remember(event.members) { event.members.filter { it.attendance == AttendanceStatus.COMING } }
    val maybeList = remember(event.members) { event.members.filter { it.attendance == AttendanceStatus.MAYBE } }
    val notAttendingList = remember(event.members) { event.members.filter { it.attendance == AttendanceStatus.NOT_COMING } }

    val currentDisplayList = when (selectedTabIndex) {
        0 -> event.members
        1 -> attendingList
        2 -> maybeList
        else -> notAttendingList
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Event Main Hero Card
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            color = BirlikteIndigo,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                event.category,
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                modifier = Modifier.clickable {
                                    val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    cb.setPrimaryClip(ClipData.newPlainText("Davet", event.inviteCode))
                                    Toast.makeText(context, Loc.s("copied", lang), Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(event.inviteCode, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { showQrDialog = true }) {
                                Icon(Icons.Default.QrCode, contentDescription = "QR Kod")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        event.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        event.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = StatusAmber, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "${event.venueName} • ${event.venueAddress}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = BirlikteCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "${event.eventDate} • ${event.eventTime}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    // Gemini Maps & Search Grounding Button
                    FilledTonalButton(
                        onClick = { showAiDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = BirlikteIndigo, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Mekan & Rota Rehberi (Google Maps & Search)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = BirlikteIndigo
                        )
                    }
                }
            }
        }

        // Spacious My Status Card
        if (myMember != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = BirlikteIndigo,
                                    modifier = Modifier.size(46.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(myMember.avatarInitials, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "${myMember.name} (Siz)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "${myMember.originName} • ${Loc.getVehicleLabel(myMember.vehicleType, lang)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Button(
                                onClick = { showStatusDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = BirlikteIndigo),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(44.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Güncelle", fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Durum: ${Loc.getLiveStatusLabel(myMember.liveStatus, lang)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = StatusGreen
                            )
                            Text(
                                "Çıkış: ${myMember.departureTime} • Kalan: ${myMember.etaMinutes} dk",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Roomy Secondary Tabs for attendance filtering
        item {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = BirlikteIndigo
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Tümü (${event.members.size})", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Gelenler (${attendingList.size})", fontWeight = FontWeight.SemiBold, color = StatusGreen) }
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = { Text("Kararsız (${maybeList.size})", fontWeight = FontWeight.SemiBold, color = StatusAmber) }
                )
                Tab(
                    selected = selectedTabIndex == 3,
                    onClick = { selectedTabIndex = 3 },
                    text = { Text("Gelmeyenler (${notAttendingList.size})", fontWeight = FontWeight.SemiBold, color = StatusRose) }
                )
            }
        }

        // Spacious Member Cards
        items(currentDisplayList, key = { it.id }) { member ->
            SpaciousMemberCard(
                member = member,
                lang = lang,
                onShowOnMap = { onNavigateToMapWithMember(member) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showStatusDialog && myMember != null) {
        StatusUpdateDialog(
            currentMember = myMember,
            lang = lang,
            onDismiss = { showStatusDialog = false },
            onConfirm = { att, ls, dt, orig, vh, priv ->
                onUpdateMyStatus(att, ls, dt, orig, vh, priv)
                showStatusDialog = false
            }
        )
    }

    if (showQrDialog) {
        QrInviteDialog(
            event = event,
            lang = lang,
            onDismiss = { showQrDialog = false }
        )
    }

    if (showAiDialog) {
        GeminiVenueIntelligenceDialog(
            event = event,
            onDismiss = { showAiDialog = false }
        )
    }
}

@Composable
private fun SpaciousMemberCard(
    member: Member,
    lang: AppLanguage,
    onShowOnMap: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar with Vehicle badge
                Box {
                    Surface(
                        shape = CircleShape,
                        color = Color(member.avatarColorHex),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                member.avatarInitials,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }

                    val vehicleEmoji = when (member.vehicleType) {
                        VehicleType.CAR -> "🚗"
                        VehicleType.SUBWAY -> "🚇"
                        VehicleType.BUS -> "🚌"
                        VehicleType.WALK -> "🚶"
                        VehicleType.BICYCLE -> "🚲"
                        VehicleType.TAXI -> "🚕"
                    }
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 3.dp,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(vehicleEmoji, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            member.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (member.isOrganizer) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = StatusAmber.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    "Organizatör",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 11.sp,
                                    color = StatusAmber,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "${Loc.s("departure_from", lang)}: ${member.originName} • ${member.departureTime}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val (statusColor, statusText) = when (member.liveStatus) {
                    LiveStatus.ARRIVED -> Pair(StatusGreen, Loc.getLiveStatusLabel(LiveStatus.ARRIVED, lang))
                    LiveStatus.IN_TRAFFIC -> Pair(StatusRose, Loc.getLiveStatusLabel(LiveStatus.IN_TRAFFIC, lang))
                    LiveStatus.EN_ROUTE -> Pair(BirlikteIndigo, Loc.getLiveStatusLabel(LiveStatus.EN_ROUTE, lang))
                    LiveStatus.DELAYED -> Pair(StatusAmber, Loc.getLiveStatusLabel(LiveStatus.DELAYED, lang))
                    LiveStatus.PREPARING -> Pair(Color.Gray, Loc.getLiveStatusLabel(LiveStatus.PREPARING, lang))
                }

                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        statusText,
                        color = statusColor,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sub-row: Telemetry stats and large 48dp touch target "Haritada Göster" action
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (member.attendance == AttendanceStatus.COMING) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "📍 ${member.distanceToVenueKm} km",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "⚡ ${member.speedKmH} km/h",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "🔋 %${member.batteryLevel}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "🕒 ${if (member.liveStatus == LiveStatus.ARRIVED) "Vardı" else "${member.etaMinutes} dk"}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = StatusGreen
                        )
                    }

                    OutlinedButton(
                        onClick = onShowOnMap,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(Loc.s("show_on_map", lang), fontSize = 13.sp)
                    }
                } else {
                    Text(
                        Loc.getAttendanceLabel(member.attendance, lang),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
