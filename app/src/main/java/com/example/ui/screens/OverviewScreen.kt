package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.model.VehicleType
import com.example.ui.components.QrInviteDialog
import com.example.ui.components.StatusUpdateDialog
import com.example.ui.theme.BirlikteCyan
import com.example.ui.theme.BirlikteIndigo
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRose

@OptIn(ExperimentalLayoutApi::class)
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
        privacyMode: com.example.model.PrivacyMode
    ) -> Unit
) {
    val context = LocalContext.current
    var showStatusDialog by remember { mutableStateOf(false) }
    var showQrDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf<AttendanceStatus?>(null) }

    val myMember = event.members.find { it.isCurrentUser } ?: event.members.firstOrNull()

    val filteredMembers = remember(event.members, selectedFilter) {
        if (selectedFilter == null) event.members
        else event.members.filter { it.attendance == selectedFilter }
    }

    val attendingCount = event.members.count { it.attendance == AttendanceStatus.COMING }
    val notAttendingCount = event.members.count { it.attendance == AttendanceStatus.NOT_COMING }
    val maybeCount = event.members.count { it.attendance == AttendanceStatus.MAYBE }
    val arrivedCount = event.members.count { it.liveStatus == LiveStatus.ARRIVED }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Event Main Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            color = BirlikteIndigo,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                event.category,
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Copy code button
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                modifier = Modifier.clickable {
                                    val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    cb.setPrimaryClip(ClipData.newPlainText("Davet", event.inviteCode))
                                    Toast.makeText(context, Loc.s("copied", lang), Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(event.inviteCode, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { showQrDialog = true }) {
                                Icon(Icons.Default.QrCode, contentDescription = "QR Kod")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        event.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        event.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = StatusAmber, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "${event.venueName} • ${event.venueAddress}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = BirlikteCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "${event.eventDate} • ${event.eventTime}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // My Status Card
        if (myMember != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = BirlikteIndigo,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(myMember.avatarInitials, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        "${myMember.name} (Siz)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "${myMember.originName} • ${Loc.getVehicleLabel(myMember.vehicleType, lang)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Button(
                                onClick = { showStatusDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = BirlikteIndigo),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Durumu Güncelle", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Durum: ${Loc.getLiveStatusLabel(myMember.liveStatus, lang)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = StatusGreen
                            )
                            Text(
                                "Çıkış: ${myMember.departureTime} | Kalan: ${myMember.etaMinutes} dk",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Attendance Quick Stats Cards (Gelenler, Gelmeyenler, Kararsız, Varanlar)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Gelenler",
                    count = attendingCount,
                    color = StatusGreen,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Gelmiyor",
                    count = notAttendingCount,
                    color = StatusRose,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Kararsız",
                    count = maybeCount,
                    color = StatusAmber,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Vardı",
                    count = arrivedCount,
                    color = BirlikteCyan,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Filter chips (Tümü / Geliyor / Gelmiyor / Kararsız)
        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { selectedFilter = null },
                    label = { Text("Tümü (${event.members.size})") }
                )
                FilterChip(
                    selected = selectedFilter == AttendanceStatus.COMING,
                    onClick = { selectedFilter = AttendanceStatus.COMING },
                    label = { Text("Gelenler ($attendingCount)") }
                )
                FilterChip(
                    selected = selectedFilter == AttendanceStatus.MAYBE,
                    onClick = { selectedFilter = AttendanceStatus.MAYBE },
                    label = { Text("Kararsız ($maybeCount)") }
                )
                FilterChip(
                    selected = selectedFilter == AttendanceStatus.NOT_COMING,
                    onClick = { selectedFilter = AttendanceStatus.NOT_COMING },
                    label = { Text("Gelmeyenler ($notAttendingCount)") }
                )
            }
        }

        // Member Cards List
        items(filteredMembers, key = { it.id }) { member ->
            MemberCardItem(
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
}

@Composable
private fun StatCard(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                count.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MemberCardItem(
    member: Member,
    lang: AppLanguage,
    onShowOnMap: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar with Vehicle badge
                Box {
                    Surface(
                        shape = CircleShape,
                        color = Color(member.avatarColorHex),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                member.avatarInitials,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    // Small vehicle emoji badge on corner
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
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(vehicleEmoji, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            member.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (member.isOrganizer) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = StatusAmber.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    "Organizatör",
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    fontSize = 9.sp,
                                    color = StatusAmber,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Text(
                        "${Loc.s("departure_from", lang)}: ${member.originName} • ${member.departureTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Live status badge
                val (statusColor, statusText) = when (member.liveStatus) {
                    LiveStatus.ARRIVED -> Pair(StatusGreen, Loc.getLiveStatusLabel(LiveStatus.ARRIVED, lang))
                    LiveStatus.IN_TRAFFIC -> Pair(StatusRose, Loc.getLiveStatusLabel(LiveStatus.IN_TRAFFIC, lang))
                    LiveStatus.EN_ROUTE -> Pair(BirlikteIndigo, Loc.getLiveStatusLabel(LiveStatus.EN_ROUTE, lang))
                    LiveStatus.DELAYED -> Pair(StatusAmber, Loc.getLiveStatusLabel(LiveStatus.DELAYED, lang))
                    LiveStatus.PREPARING -> Pair(Color.Gray, Loc.getLiveStatusLabel(LiveStatus.PREPARING, lang))
                }

                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        statusText,
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sub-row: Telemetry stats and "Haritada Göster" action
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (member.attendance == AttendanceStatus.COMING) {
                        Text(
                            "📍 ${member.distanceToVenueKm} km",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "⚡ ${member.speedKmH} km/h",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "🔋 %${member.batteryLevel}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "🕒 ${if (member.liveStatus == LiveStatus.ARRIVED) "Vardı" else "${member.etaMinutes} dk"}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = StatusGreen
                        )
                    } else {
                        Text(
                            Loc.getAttendanceLabel(member.attendance, lang),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (member.attendance == AttendanceStatus.COMING) {
                    OutlinedButton(
                        onClick = onShowOnMap,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(Loc.s("show_on_map", lang), fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
