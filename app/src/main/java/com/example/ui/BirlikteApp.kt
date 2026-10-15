package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppRepository
import com.example.localization.Loc
import com.example.model.Member
import com.example.ui.components.EventSwitcherDialog
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.ExpenseScreen
import com.example.ui.screens.MapScreen
import com.example.ui.screens.OverviewScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.BirlikteCyan
import com.example.ui.theme.BirlikteIndigo
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRose

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirlikteApp(repository: AppRepository) {
    val lang by repository.appLanguage.collectAsState()
    val isDark by repository.isDarkMode.collectAsState()
    val isOffline by repository.isOffline.collectAsState()
    val queuedSyncCount by repository.queuedSyncCount.collectAsState()
    val webSocketConnected by repository.webSocketConnected.collectAsState()
    val pingMs by repository.webSocketPingMs.collectAsState()
    val notifications by repository.notifications.collectAsState()

    val events by repository.events.collectAsState()
    val currentEventId by repository.currentEventId.collectAsState()
    val currentEvent = events.find { it.id == currentEventId } ?: events.firstOrNull()

    val chatMessages by repository.chatMessages.collectAsState()
    val expenses by repository.expenses.collectAsState()
    val debtSettlements by repository.debtSettlements.collectAsState()
    val activeToast by repository.activeNotificationToast.collectAsState()

    var currentTab by remember { mutableIntStateOf(0) }
    var selectedMapMember by remember { mutableStateOf<Member?>(null) }
    var showEventSwitcherDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { showEventSwitcherDialog = true }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                Loc.s("app_title", lang),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BirlikteIndigo
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            // Online/Offline pill
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isOffline) StatusRose.copy(alpha = 0.15f) else StatusGreen.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    if (isOffline) "📶 Çevrimdışı" else "🟢 Canlı",
                                    color = if (isOffline) StatusRose else StatusGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (currentEvent != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    currentEvent.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { repository.toggleDarkMode() }) {
                        Icon(
                            if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Tema Değiştir",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { showEventSwitcherDialog = true }) {
                        Icon(
                            Icons.Default.SwapHoriz,
                            contentDescription = "Etkinlik Değiştir",
                            tint = BirlikteIndigo
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                // Tab 0: Katılımcılar
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.Group, contentDescription = Loc.s("tab_overview", lang)) },
                    label = { Text(Loc.s("tab_overview", lang), fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BirlikteIndigo,
                        indicatorColor = BirlikteIndigo.copy(alpha = 0.15f)
                    )
                )

                // Tab 1: Canlı Harita
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = {
                        BadgedBox(badge = {
                            if (webSocketConnected) {
                                Badge(containerColor = StatusGreen)
                            }
                        }) {
                            Icon(Icons.Default.Map, contentDescription = Loc.s("tab_map", lang))
                        }
                    },
                    label = { Text(Loc.s("tab_map", lang), fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BirlikteIndigo,
                        indicatorColor = BirlikteIndigo.copy(alpha = 0.15f)
                    )
                )

                // Tab 2: Sohbet
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = {
                        BadgedBox(badge = {
                            if (chatMessages.isNotEmpty()) {
                                Badge(containerColor = BirlikteIndigo) {
                                    Text(chatMessages.size.toString())
                                }
                            }
                        }) {
                            Icon(Icons.Default.ChatBubble, contentDescription = Loc.s("tab_chat", lang))
                        }
                    },
                    label = { Text(Loc.s("tab_chat", lang), fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BirlikteIndigo,
                        indicatorColor = BirlikteIndigo.copy(alpha = 0.15f)
                    )
                )

                // Tab 3: Masraflar
                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { currentTab = 3 },
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = Loc.s("tab_expenses", lang)) },
                    label = { Text(Loc.s("tab_expenses", lang), fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BirlikteIndigo,
                        indicatorColor = BirlikteIndigo.copy(alpha = 0.15f)
                    )
                )

                // Tab 4: Ayarlar
                NavigationBarItem(
                    selected = currentTab == 4,
                    onClick = { currentTab = 4 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = Loc.s("tab_settings", lang)) },
                    label = { Text(Loc.s("tab_settings", lang), fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BirlikteIndigo,
                        indicatorColor = BirlikteIndigo.copy(alpha = 0.15f)
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (currentEvent != null) {
                when (currentTab) {
                    0 -> OverviewScreen(
                        event = currentEvent,
                        lang = lang,
                        onNavigateToMapWithMember = { m ->
                            selectedMapMember = m
                            currentTab = 1
                        },
                        onUpdateMyStatus = { att, ls, dt, orig, vh, priv ->
                            repository.updateMyStatus(att, ls, dt, orig, vh, priv)
                        }
                    )
                    1 -> MapScreen(
                        event = currentEvent,
                        selectedMember = selectedMapMember,
                        onSelectMember = { selectedMapMember = it },
                        webSocketConnected = webSocketConnected,
                        pingMs = pingMs,
                        lang = lang,
                        isDark = isDark
                    )
                    2 -> ChatScreen(
                        messages = chatMessages.filter { it.eventId == currentEvent.id },
                        lang = lang,
                        onSendMessage = { text, isQuick ->
                            repository.sendChatMessage(text, isQuick)
                        },
                        onShareLocation = {
                            val myM = currentEvent.members.find { it.isCurrentUser }
                            if (myM != null) {
                                repository.sendChatMessage(
                                    text = "📍 Canlı konumumu paylaştım: ${myM.originName} civarındayım (${myM.distanceToVenueKm} km kaldı)",
                                    isQuickStatus = true,
                                    locationShare = "${myM.currentLat},${myM.currentLng}"
                                )
                            }
                        }
                    )
                    3 -> ExpenseScreen(
                        expenses = expenses.filter { it.eventId == currentEvent.id },
                        debtSettlements = debtSettlements,
                        members = currentEvent.members,
                        lang = lang,
                        onAddExpense = { title, amt, payerId, splitIds, cat ->
                            repository.addExpense(title, amt, payerId, splitIds, cat)
                        },
                        onSettleDebt = { settlementId ->
                            repository.settleDebt(settlementId)
                        }
                    )
                    4 -> SettingsScreen(
                        lang = lang,
                        isDark = isDark,
                        isOffline = isOffline,
                        queuedSyncCount = queuedSyncCount,
                        webSocketConnected = webSocketConnected,
                        pingMs = pingMs,
                        notifications = notifications,
                        onToggleDark = { repository.toggleDarkMode() },
                        onToggleOffline = { repository.toggleOfflineMode() },
                        onSetLang = { repository.setLanguage(it) },
                        onUpdateNotifications = { repository.updateNotificationPreferences(it) }
                    )
                }
            }

            // In-app live notification toast overlay
            AnimatedVisibility(
                visible = activeToast != null,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp, start = 16.dp, end = 16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shadowElevation = 8.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BirlikteIndigo)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            activeToast ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showEventSwitcherDialog) {
        EventSwitcherDialog(
            events = events,
            currentEventId = currentEventId,
            lang = lang,
            onSelectEvent = { repository.selectEvent(it) },
            onCreateEvent = { title, desc, cat, date, time, venue, addr, lat, lng ->
                repository.createNewEvent(title, desc, cat, date, time, venue, addr, lat, lng)
            },
            onDismiss = { showEventSwitcherDialog = false }
        )
    }
}
