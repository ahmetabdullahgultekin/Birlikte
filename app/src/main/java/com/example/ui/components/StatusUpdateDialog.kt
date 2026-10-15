package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.example.model.PrivacyMode
import com.example.model.VehicleType
import com.example.ui.theme.BirlikteIndigo
import com.example.ui.theme.StatusGreen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StatusUpdateDialog(
    currentMember: Member,
    lang: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: (
        attendance: AttendanceStatus,
        liveStatus: LiveStatus,
        departureTime: String,
        originName: String,
        vehicleType: VehicleType,
        privacyMode: PrivacyMode
    ) -> Unit
) {
    var attendance by remember { mutableStateOf(currentMember.attendance) }
    var liveStatus by remember { mutableStateOf(currentMember.liveStatus) }
    var departureTime by remember { mutableStateOf(currentMember.departureTime) }
    var originName by remember { mutableStateOf(currentMember.originName) }
    var vehicleType by remember { mutableStateOf(currentMember.vehicleType) }
    var privacyMode by remember { mutableStateOf(currentMember.privacyMode) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                Loc.s("update_status", lang),
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
                // Attendance Selector (Geliyorum / Gelemiyorum / Kararsız)
                Text(
                    "Katılım Durumu",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AttendanceStatus.entries.forEach { att ->
                        val isSelected = attendance == att
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { attendance = att },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) BirlikteIndigo else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 10.dp)
                            ) {
                                Text(
                                    Loc.getAttendanceLabel(att, lang),
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Live status options (Hazırlanıyor, Yola Çıktı, Trafikte, Alana Vardı, Gecikiyor)
                Text(
                    "Canlı Yolculuk Durumu",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    LiveStatus.entries.forEach { ls ->
                        val isSelected = liveStatus == ls
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) StatusGreen else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { liveStatus = ls }
                        ) {
                            Text(
                                Loc.getLiveStatusLabel(ls, lang),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Departure Point & Time
                OutlinedTextField(
                    value = originName,
                    onValueChange = { originName = it },
                    label = { Text(Loc.s("departure_from", lang)) },
                    leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = departureTime,
                    onValueChange = { departureTime = it },
                    label = { Text(Loc.s("departure_time", lang)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Vehicle Type selector
                Text(
                    Loc.s("vehicle", lang),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    VehicleType.entries.forEach { vh ->
                        val isSelected = vehicleType == vh
                        val emoji = when (vh) {
                            VehicleType.CAR -> "🚗"
                            VehicleType.SUBWAY -> "🚇"
                            VehicleType.BUS -> "🚌"
                            VehicleType.WALK -> "🚶"
                            VehicleType.BICYCLE -> "🚲"
                            VehicleType.TAXI -> "🚕"
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) BirlikteIndigo else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { vehicleType = vh }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(emoji, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    Loc.getVehicleLabel(vh, lang),
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Privacy Mode
                Text(
                    "Konum Gizlilik Ayarı",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                PrivacyMode.entries.forEach { mode ->
                    val isSelected = privacyMode == mode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { privacyMode = mode }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (isSelected) BirlikteIndigo else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            Loc.getPrivacyLabel(mode, lang),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) BirlikteIndigo else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        attendance,
                        liveStatus,
                        departureTime,
                        originName,
                        vehicleType,
                        privacyMode
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = BirlikteIndigo)
            ) {
                Text(Loc.s("save", lang), color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(Loc.s("cancel", lang))
            }
        }
    )
}
