package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SignalCellularConnectedNoInternet0Bar
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.Loc
import com.example.model.AppLanguage
import com.example.model.NotificationPreferences
import com.example.ui.theme.BirlikteCyan
import com.example.ui.theme.BirlikteIndigo
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRose

@Composable
fun SettingsScreen(
    lang: AppLanguage,
    isDark: Boolean,
    isOffline: Boolean,
    queuedSyncCount: Int,
    webSocketConnected: Boolean,
    pingMs: Int,
    notifications: NotificationPreferences,
    onToggleDark: () -> Unit,
    onToggleOffline: () -> Unit,
    onSetLang: (AppLanguage) -> Unit,
    onUpdateNotifications: (NotificationPreferences) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Theme & Language Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Görünüm & Dil", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                // Dark mode toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DarkMode, contentDescription = null, tint = BirlikteIndigo)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(Loc.s("dark_mode", lang), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            Text(if (isDark) "Karanlık Mod Aktif" else "Açık Mod Aktif", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Switch(
                        checked = isDark,
                        onCheckedChange = { onToggleDark() },
                        colors = SwitchDefaults.colors(checkedThumbColor = BirlikteIndigo)
                    )
                }

                // Language switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = BirlikteCyan)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(Loc.s("language", lang), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (lang == AppLanguage.TR) BirlikteIndigo else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { onSetLang(AppLanguage.TR) }
                        ) {
                            Text(
                                "Türkçe",
                                color = if (lang == AppLanguage.TR) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (lang == AppLanguage.EN) BirlikteIndigo else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { onSetLang(AppLanguage.EN) }
                        ) {
                            Text(
                                "English",
                                color = if (lang == AppLanguage.EN) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Offline Mode Simulator & Persistence Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isOffline) Icons.Default.WifiOff else Icons.Default.Wifi,
                            contentDescription = null,
                            tint = if (isOffline) StatusRose else StatusGreen
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(Loc.s("offline_simulation", lang), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            Text(
                                if (isOffline) "Çevrimdışı Modda (İnternet Yok)" else "Çevrimiçi (İnternet Bağlı)",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isOffline) StatusRose else StatusGreen
                            )
                        }
                    }

                    Switch(
                        checked = isOffline,
                        onCheckedChange = { onToggleOffline() },
                        colors = SwitchDefaults.colors(checkedThumbColor = StatusRose)
                    )
                }

                Text(
                    Loc.s("offline_desc", lang),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (isOffline) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = StatusRose.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.SignalCellularConnectedNoInternet0Bar, contentDescription = null, tint = StatusRose, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Senkronizasyon Kuyruğu: $queuedSyncCount işlem bekliyor",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = StatusRose
                            )
                        }
                    }
                }
            }
        }

        // Realtime WebSocket Status
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Sensors, contentDescription = null, tint = if (webSocketConnected) StatusGreen else Color.Gray)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Canlı WebSocket Akışı", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                Text(
                    "Protokol: wss://birlikte.live/v1/telemetry/stream",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Durum: " + if (webSocketConnected) "🟢 Bağlı • Gecikme: ${pingMs} ms" else "🔴 Bağlantı Kesildi",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (webSocketConnected) StatusGreen else StatusRose
                )
            }
        }

        // End-to-End Encryption & Security
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = BirlikteIndigo)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(Loc.s("security_privacy", lang), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                Text(
                    Loc.s("encryption_active", lang),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Cihaz Güvenlik Anahtarı (Signal Ratchet):", style = MaterialTheme.typography.labelSmall)
                        Text(
                            "ED25519: 7F9B-4C11-8A33-DE55-01FE-22AC",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BirlikteIndigo
                        )
                    }
                }
            }
        }

        // Customizable Notifications
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = BirlikteIndigo)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(Loc.s("notifications", lang), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                NotificationToggleRow(
                    label = Loc.s("notify_departure", lang),
                    checked = notifications.onDepartureAlert,
                    onCheckedChange = { onUpdateNotifications(notifications.copy(onDepartureAlert = it)) }
                )
                NotificationToggleRow(
                    label = Loc.s("notify_arrival", lang),
                    checked = notifications.onArrivalAlert,
                    onCheckedChange = { onUpdateNotifications(notifications.copy(onArrivalAlert = it)) }
                )
                NotificationToggleRow(
                    label = Loc.s("notify_proximity", lang),
                    checked = notifications.onProximityAlert,
                    onCheckedChange = { onUpdateNotifications(notifications.copy(onProximityAlert = it)) }
                )
                NotificationToggleRow(
                    label = Loc.s("notify_expense", lang),
                    checked = notifications.onNewExpenseAlert,
                    onCheckedChange = { onUpdateNotifications(notifications.copy(onNewExpenseAlert = it)) }
                )
                NotificationToggleRow(
                    label = Loc.s("notify_chat", lang),
                    checked = notifications.onChatMessageAlert,
                    onCheckedChange = { onUpdateNotifications(notifications.copy(onChatMessageAlert = it)) }
                )
            }
        }

        // About & Version
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = BirlikteIndigo, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Birlikte • Organizasyon & Canlı Takip", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Sürüm 1.0.0 • Uçtan uca şifreli, çevrimdışı öncelikli mimari. Katılımcıların canlı konumlarını, yolculuk araçlarını, tahmini varış saatlerini ve masrafları bir araya getirir.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun NotificationToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = BirlikteIndigo)
        )
    }
}
