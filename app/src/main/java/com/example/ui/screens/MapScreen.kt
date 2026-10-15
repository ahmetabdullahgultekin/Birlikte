package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.Loc
import com.example.model.AppLanguage
import com.example.model.AttendanceStatus
import com.example.model.LiveStatus
import com.example.model.Member
import com.example.model.OrganizationEvent
import com.example.ui.components.CanvasMapView
import com.example.ui.theme.BirlikteCyan
import com.example.ui.theme.BirlikteIndigo
import com.example.ui.theme.StatusGreen

enum class MapDistanceFilter {
    ALL,
    ON_THE_WAY,
    UNDER_2KM,
    UNDER_5KM,
    ARRIVED
}

@Composable
fun MapScreen(
    event: OrganizationEvent,
    selectedMember: Member?,
    onSelectMember: (Member?) -> Unit,
    webSocketConnected: Boolean,
    pingMs: Int,
    lang: AppLanguage,
    isDark: Boolean
) {
    var activeFilter by remember { mutableStateOf(MapDistanceFilter.ALL) }

    // Filter members according to location criteria
    val filteredMembers = remember(event.members, activeFilter) {
        val attending = event.members.filter { it.attendance == AttendanceStatus.COMING }
        when (activeFilter) {
            MapDistanceFilter.ALL -> attending
            MapDistanceFilter.ON_THE_WAY -> attending.filter { it.liveStatus == LiveStatus.EN_ROUTE || it.liveStatus == LiveStatus.IN_TRAFFIC }
            MapDistanceFilter.UNDER_2KM -> attending.filter { it.distanceToVenueKm <= 2.0 }
            MapDistanceFilter.UNDER_5KM -> attending.filter { it.distanceToVenueKm <= 5.0 }
            MapDistanceFilter.ARRIVED -> attending.filter { it.liveStatus == LiveStatus.ARRIVED }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Core interactive Canvas Map View
        CanvasMapView(
            event = event,
            filteredMembers = filteredMembers,
            selectedMember = selectedMember,
            onMemberSelected = onSelectMember,
            lang = lang,
            isDark = isDark,
            modifier = Modifier.fillMaxSize()
        )

        // Overlay Header: Live Radar telemetry banner and filter chips
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Live Status indicator badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Sensors,
                        contentDescription = null,
                        tint = if (webSocketConnected) StatusGreen else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (webSocketConnected) "Canlı Radar Takibi • WSS: ${pingMs}ms" else "Canlı Takip: Çevrimdışı",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (webSocketConnected) StatusGreen else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "(${filteredMembers.size} Aktif)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Location-based filter chips row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = activeFilter == MapDistanceFilter.ALL,
                    onClick = { activeFilter = MapDistanceFilter.ALL },
                    label = { Text(Loc.s("all", lang), fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BirlikteIndigo,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = activeFilter == MapDistanceFilter.ON_THE_WAY,
                    onClick = { activeFilter = MapDistanceFilter.ON_THE_WAY },
                    label = { Text(Loc.s("filter_on_way", lang), fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BirlikteIndigo,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = activeFilter == MapDistanceFilter.UNDER_2KM,
                    onClick = { activeFilter = MapDistanceFilter.UNDER_2KM },
                    label = { Text(Loc.s("filter_near", lang), fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BirlikteIndigo,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = activeFilter == MapDistanceFilter.UNDER_5KM,
                    onClick = { activeFilter = MapDistanceFilter.UNDER_5KM },
                    label = { Text(Loc.s("filter_5km", lang), fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BirlikteIndigo,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = activeFilter == MapDistanceFilter.ARRIVED,
                    onClick = { activeFilter = MapDistanceFilter.ARRIVED },
                    label = { Text(Loc.s("filter_arrived", lang), fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BirlikteIndigo,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Bottom member selector shortcuts (floating pill avatars)
        if (selectedMember == null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Kişiye Odaklan:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    event.members.filter { it.attendance == AttendanceStatus.COMING }.forEach { m ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(m.avatarColorHex).copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(m.avatarColorHex)),
                            modifier = Modifier.clickable { onSelectMember(m) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(m.avatarColorHex),
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(m.avatarInitials, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    m.name.split(" ").firstOrNull() ?: m.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    if (m.liveStatus == LiveStatus.ARRIVED) "📍" else "${m.etaMinutes}m",
                                    fontSize = 10.sp,
                                    color = StatusGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
