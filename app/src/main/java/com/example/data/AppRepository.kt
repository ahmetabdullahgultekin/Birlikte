package com.example.data

import com.example.model.AppLanguage
import com.example.model.AttendanceStatus
import com.example.model.ChatMessage
import com.example.model.DebtSettlement
import com.example.model.Expense
import com.example.model.ExpenseCategory
import com.example.model.LiveStatus
import com.example.model.Member
import com.example.model.NotificationPreferences
import com.example.model.OrganizationEvent
import com.example.model.PrivacyMode
import com.example.model.VehicleType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class AppRepository(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    // Current Language & Dark Mode
    private val _appLanguage = MutableStateFlow(AppLanguage.TR)
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // Offline simulation flag
    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    // Offline queued actions count
    private val _queuedSyncCount = MutableStateFlow(0)
    val queuedSyncCount: StateFlow<Int> = _queuedSyncCount.asStateFlow()

    // WebSocket telemetry status
    private val _webSocketConnected = MutableStateFlow(true)
    val webSocketConnected: StateFlow<Boolean> = _webSocketConnected.asStateFlow()

    private val _webSocketPingMs = MutableStateFlow(24)
    val webSocketPingMs: StateFlow<Int> = _webSocketPingMs.asStateFlow()

    // Notification settings
    private val _notifications = MutableStateFlow(NotificationPreferences())
    val notifications: StateFlow<NotificationPreferences> = _notifications.asStateFlow()

    // All available events
    private val _events = MutableStateFlow<List<OrganizationEvent>>(emptyList())
    val events: StateFlow<List<OrganizationEvent>> = _events.asStateFlow()

    // Current selected event
    private val _currentEventId = MutableStateFlow("event-1")
    val currentEventId: StateFlow<String> = _currentEventId.asStateFlow()

    // Chat messages
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // Expenses
    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    // Computed debt settlements
    private val _debtSettlements = MutableStateFlow<List<DebtSettlement>>(emptyList())
    val debtSettlements: StateFlow<List<DebtSettlement>> = _debtSettlements.asStateFlow()

    // Active in-app notification popup message (transient)
    private val _activeNotificationToast = MutableStateFlow<String?>(null)
    val activeNotificationToast: StateFlow<String?> = _activeNotificationToast.asStateFlow()

    // Simulation loop job
    private var simulationJob: Job? = null
    private var simulationStep = 0.0

    init {
        loadInitialData()
        startWebSocketSimulation()
    }

    fun setLanguage(lang: AppLanguage) {
        _appLanguage.value = lang
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun toggleOfflineMode() {
        val newStatus = !_isOffline.value
        _isOffline.value = newStatus
        _webSocketConnected.value = !newStatus
        if (!newStatus) {
            // Reconnected: flush queued sync actions
            _queuedSyncCount.value = 0
            showToast("İnternet bağlantısı sağlandı. Yerel veriler sunucuyla eşitlendi! 🟢")
        } else {
            showToast("Çevrimdışı moda geçildi. Değişiklikler yerel Room önbelleğinde saklanıyor. 📶")
        }
    }

    fun updateNotificationPreferences(prefs: NotificationPreferences) {
        _notifications.value = prefs
    }

    fun selectEvent(eventId: String) {
        _currentEventId.value = eventId
    }

    fun dismissToast() {
        _activeNotificationToast.value = null
    }

    private fun showToast(msg: String) {
        _activeNotificationToast.value = msg
        scope.launch {
            delay(3500)
            if (_activeNotificationToast.value == msg) {
                _activeNotificationToast.value = null
            }
        }
    }

    fun updateMyStatus(
        attendance: AttendanceStatus,
        liveStatus: LiveStatus,
        departureTime: String,
        originName: String,
        vehicleType: VehicleType,
        privacyMode: PrivacyMode
    ) {
        val currentEvId = _currentEventId.value
        _events.update { list ->
            list.map { ev ->
                if (ev.id == currentEvId) {
                    val updatedMembers = ev.members.map { m ->
                        if (m.isCurrentUser) {
                            m.copy(
                                attendance = attendance,
                                liveStatus = liveStatus,
                                departureTime = departureTime,
                                originName = originName,
                                vehicleType = vehicleType,
                                privacyMode = privacyMode,
                                lastUpdatedText = "Şimdi"
                            )
                        } else m
                    }
                    ev.copy(members = updatedMembers)
                } else ev
            }
        }

        if (_isOffline.value) {
            _queuedSyncCount.update { it + 1 }
        }

        // Post an encrypted status notification message to chat
        val statusText = when (liveStatus) {
            LiveStatus.EN_ROUTE -> "🚗 $originName konumundan yola çıktım ($departureTime)"
            LiveStatus.ARRIVED -> "📍 Alana vardım!"
            LiveStatus.IN_TRAFFIC -> "🚦 Trafikteyim, hafif gecikme olabilir"
            LiveStatus.DELAYED -> "⏳ 15 dakika gecikiyorum"
            LiveStatus.PREPARING -> "🎒 Hazırlanıyorum, birazdan çıkıyorum"
        }
        sendChatMessage(text = statusText, isQuickStatus = true)
    }

    fun sendChatMessage(text: String, isQuickStatus: Boolean = false, locationShare: String? = null) {
        val currentEvId = _currentEventId.value
        val newMsg = ChatMessage(
            id = "msg-${System.currentTimeMillis()}",
            eventId = currentEvId,
            senderId = "user-me",
            senderName = "Can Demir (Siz)",
            text = text,
            timestamp = "19:${(10..55).random()}",
            isSentByMe = true,
            isQuickStatus = isQuickStatus,
            isEncrypted = true,
            locationShareText = locationShare
        )
        _chatMessages.update { it + newMsg }

        if (_isOffline.value) {
            _queuedSyncCount.update { it + 1 }
        }
    }

    fun addExpense(
        title: String,
        amount: Double,
        paidByMemberId: String,
        splitMemberIds: List<String>,
        category: ExpenseCategory
    ) {
        val currentEv = getCurrentEvent() ?: return
        val payer = currentEv.members.find { it.id == paidByMemberId }
        val payerName = payer?.name ?: "Bilinmeyen"

        val newExpense = Expense(
            id = "exp-${System.currentTimeMillis()}",
            eventId = currentEv.id,
            title = title,
            amount = amount,
            paidByMemberId = paidByMemberId,
            paidByMemberName = payerName,
            splitMemberIds = splitMemberIds,
            category = category,
            timestamp = "Bugün, 19:30"
        )
        _expenses.update { it + newExpense }
        recalculateDebts()

        if (_isOffline.value) {
            _queuedSyncCount.update { it + 1 }
        } else {
            showToast("Masraf eklendi: $title (₺${amount.toInt()}) 💸")
        }
    }

    fun settleDebt(settlementId: String) {
        _debtSettlements.update { list ->
            list.map {
                if (it.id == settlementId) it.copy(isSettled = true) else it
            }
        }
        showToast("Borç ödendi olarak işaretlendi! ✅")
    }

    fun createNewEvent(
        title: String,
        description: String,
        category: String,
        date: String,
        time: String,
        venueName: String,
        venueAddress: String,
        lat: Double,
        lng: Double
    ) {
        val newId = "event-${System.currentTimeMillis()}"
        val initialMembers = listOf(
            Member(
                id = "user-me",
                name = "Can Demir (Siz)",
                avatarInitials = "CD",
                avatarColorHex = 0xFF4F46E5,
                isCurrentUser = true,
                isOrganizer = true,
                attendance = AttendanceStatus.COMING,
                liveStatus = LiveStatus.EN_ROUTE,
                departureTime = "19:15",
                originName = "Kadıköy",
                vehicleType = VehicleType.CAR,
                currentLat = lat - 0.015,
                currentLng = lng - 0.015,
                originLat = lat - 0.03,
                originLng = lng - 0.03,
                speedKmH = 45,
                batteryLevel = 92,
                etaMinutes = 15,
                distanceToVenueKm = 2.4,
                privacyMode = PrivacyMode.PRECISE
            )
        )

        val newEvent = OrganizationEvent(
            id = newId,
            title = title,
            description = description,
            category = category,
            eventDate = date,
            eventTime = time,
            venueName = venueName,
            venueAddress = venueAddress,
            venueLat = lat,
            venueLng = lng,
            inviteCode = "BRLKT-${(1000..9999).random()}",
            isOfflineCached = true,
            members = initialMembers
        )

        _events.update { it + newEvent }
        _currentEventId.value = newId
        showToast("Yeni organizasyon oluşturuldu: $title 🎉")
    }

    fun getCurrentEvent(): OrganizationEvent? {
        val id = _currentEventId.value
        return _events.value.find { it.id == id } ?: _events.value.firstOrNull()
    }

    private fun recalculateDebts() {
        val currentEv = getCurrentEvent() ?: return
        val currentEvExpenses = _expenses.value.filter { it.eventId == currentEv.id }
        if (currentEvExpenses.isEmpty() || currentEv.members.isEmpty()) {
            _debtSettlements.value = emptyList()
            return
        }

        // Net balance map for each member: balance > 0 means they paid more and are owed money
        val balances = mutableMapOf<String, Double>()
        currentEv.members.forEach { balances[it.id] = 0.0 }

        for (exp in currentEvExpenses) {
            val payerId = exp.paidByMemberId
            val splits = exp.splitMemberIds.ifEmpty { currentEv.members.map { it.id } }
            val splitAmount = exp.amount / splits.size

            balances[payerId] = (balances[payerId] ?: 0.0) + exp.amount
            for (mId in splits) {
                balances[mId] = (balances[mId] ?: 0.0) - splitAmount
            }
        }

        // Separate debtors (owe money < 0) and creditors (are owed money > 0)
        val debtors = mutableListOf<Pair<String, Double>>()
        val creditors = mutableListOf<Pair<String, Double>>()

        balances.forEach { (mId, bal) ->
            if (bal < -0.5) debtors.add(Pair(mId, -bal))
            else if (bal > 0.5) creditors.add(Pair(mId, bal))
        }

        val settlements = mutableListOf<DebtSettlement>()
        var dIdx = 0
        var cIdx = 0
        var counter = 1

        val memberMap = currentEv.members.associateBy { it.id }

        while (dIdx < debtors.size && cIdx < creditors.size) {
            val (debtorId, debtorOwes) = debtors[dIdx]
            val (creditorId, creditorGets) = creditors[cIdx]

            val settleAmount = minOf(debtorOwes, creditorGets)
            if (settleAmount > 0.5) {
                settlements.add(
                    DebtSettlement(
                        id = "debt-$counter",
                        fromMemberId = debtorId,
                        fromMemberName = memberMap[debtorId]?.name ?: "Katılımcı",
                        toMemberId = creditorId,
                        toMemberName = memberMap[creditorId]?.name ?: "Katılımcı",
                        amount = settleAmount,
                        isSettled = false
                    )
                )
                counter++
            }

            debtors[dIdx] = Pair(debtorId, debtorOwes - settleAmount)
            creditors[cIdx] = Pair(creditorId, creditorGets - settleAmount)

            if (debtors[dIdx].second < 0.5) dIdx++
            if (creditors[cIdx].second < 0.5) cIdx++
        }

        _debtSettlements.value = settlements
    }

    private fun startWebSocketSimulation() {
        simulationJob?.cancel()
        simulationJob = scope.launch {
            while (isActive) {
                delay(3000)
                if (!_isOffline.value) {
                    simulationStep += 0.05
                    if (simulationStep > 1.0) simulationStep = 0.05

                    // Fluctuate ping slightly
                    _webSocketPingMs.value = (20..38).random()

                    // Move members toward the venue along curve
                    val currentEvId = _currentEventId.value
                    _events.update { list ->
                        list.map { ev ->
                            if (ev.id == currentEvId) {
                                val updatedMembers = ev.members.map { m ->
                                    if (m.attendance == AttendanceStatus.COMING &&
                                        m.liveStatus != LiveStatus.ARRIVED
                                    ) {
                                        // Interpolate between origin and venue
                                        val factor = ((simulationStep + (m.id.hashCode() % 5) * 0.15) % 0.95).coerceIn(0.1, 0.95)
                                        val newLat = m.originLat + (ev.venueLat - m.originLat) * factor
                                        val newLng = m.originLng + (ev.venueLng - m.originLng) * factor
                                        val dist = calculateDistanceKm(newLat, newLng, ev.venueLat, ev.venueLng)
                                        val speed = if (dist < 0.3) 0 else when (m.vehicleType) {
                                            VehicleType.CAR -> (35..65).random()
                                            VehicleType.TAXI -> (40..70).random()
                                            VehicleType.SUBWAY -> (50..80).random()
                                            VehicleType.BUS -> (25..45).random()
                                            VehicleType.BICYCLE -> (15..22).random()
                                            VehicleType.WALK -> (4..6).random()
                                        }
                                        val eta = (dist / (speed.coerceAtLeast(5) / 60.0)).toInt().coerceAtLeast(1)
                                        val newStatus = if (dist < 0.3) LiveStatus.ARRIVED else m.liveStatus

                                        m.copy(
                                            currentLat = newLat,
                                            currentLng = newLng,
                                            distanceToVenueKm = dist,
                                            speedKmH = speed,
                                            etaMinutes = eta,
                                            liveStatus = newStatus,
                                            batteryLevel = (m.batteryLevel - (0..1).random()).coerceAtLeast(15),
                                            lastUpdatedText = "Şimdi"
                                        )
                                    } else m
                                }
                                ev.copy(members = updatedMembers)
                            } else ev
                        }
                    }
                }
            }
        }
    }

    private fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (r * c * 10).toInt() / 10.0
    }

    private fun loadInitialData() {
        val venueLat = 40.9856
        val venueLng = 29.0278

        val demoMembers1 = listOf(
            Member(
                id = "user-me",
                name = "Can Demir (Siz)",
                avatarInitials = "CD",
                avatarColorHex = 0xFF4F46E5,
                isCurrentUser = true,
                isOrganizer = true,
                attendance = AttendanceStatus.COMING,
                liveStatus = LiveStatus.EN_ROUTE,
                departureTime = "19:15",
                originName = "Levent",
                vehicleType = VehicleType.CAR,
                currentLat = 41.0150,
                currentLng = 29.0250,
                originLat = 41.0800,
                originLng = 29.0100,
                speedKmH = 48,
                batteryLevel = 92,
                etaMinutes = 14,
                distanceToVenueKm = 4.8,
                privacyMode = PrivacyMode.PRECISE
            ),
            Member(
                id = "m-elif",
                name = "Elif Kaya",
                avatarInitials = "EK",
                avatarColorHex = 0xFF06B6D4,
                attendance = AttendanceStatus.COMING,
                liveStatus = LiveStatus.EN_ROUTE,
                departureTime = "19:20",
                originName = "Yenikapı",
                vehicleType = VehicleType.SUBWAY,
                currentLat = 40.9990,
                currentLng = 29.0220,
                originLat = 41.0050,
                originLng = 28.9550,
                speedKmH = 65,
                batteryLevel = 78,
                etaMinutes = 8,
                distanceToVenueKm = 1.9,
                privacyMode = PrivacyMode.PRECISE
            ),
            Member(
                id = "m-mehmet",
                name = "Mehmet Yılmaz",
                avatarInitials = "MY",
                avatarColorHex = 0xFF10B981,
                attendance = AttendanceStatus.COMING,
                liveStatus = LiveStatus.ARRIVED,
                departureTime = "19:00",
                originName = "Moda Tramvay",
                vehicleType = VehicleType.WALK,
                currentLat = venueLat + 0.001,
                currentLng = venueLng + 0.001,
                originLat = 40.9880,
                originLng = 29.0290,
                speedKmH = 0,
                batteryLevel = 84,
                etaMinutes = 0,
                distanceToVenueKm = 0.1,
                privacyMode = PrivacyMode.PRECISE
            ),
            Member(
                id = "m-zeynep",
                name = "Zeynep Aydın",
                avatarInitials = "ZA",
                avatarColorHex = 0xFFF59E0B,
                attendance = AttendanceStatus.COMING,
                liveStatus = LiveStatus.IN_TRAFFIC,
                departureTime = "19:10",
                originName = "Üsküdar",
                vehicleType = VehicleType.BUS,
                currentLat = 41.0020,
                currentLng = 29.0200,
                originLat = 41.0260,
                originLng = 29.0150,
                speedKmH = 22,
                batteryLevel = 54,
                etaMinutes = 19,
                distanceToVenueKm = 3.6,
                privacyMode = PrivacyMode.APPROXIMATE
            ),
            Member(
                id = "m-burak",
                name = "Burak Şen",
                avatarInitials = "BŞ",
                avatarColorHex = 0xFF8B5CF6,
                attendance = AttendanceStatus.COMING,
                liveStatus = LiveStatus.EN_ROUTE,
                departureTime = "19:25",
                originName = "Ataşehir",
                vehicleType = VehicleType.TAXI,
                currentLat = 40.9900,
                currentLng = 29.0600,
                originLat = 40.9830,
                originLng = 29.1120,
                speedKmH = 58,
                batteryLevel = 62,
                etaMinutes = 11,
                distanceToVenueKm = 3.1,
                privacyMode = PrivacyMode.PRECISE
            ),
            Member(
                id = "m-selin",
                name = "Selin Kurt",
                avatarInitials = "SK",
                avatarColorHex = 0xFFEC4899,
                attendance = AttendanceStatus.COMING,
                liveStatus = LiveStatus.EN_ROUTE,
                departureTime = "19:05",
                originName = "Bostancı Sahil",
                vehicleType = VehicleType.BICYCLE,
                currentLat = 40.9750,
                currentLng = 29.0450,
                originLat = 40.9520,
                originLng = 29.0950,
                speedKmH = 18,
                batteryLevel = 88,
                etaMinutes = 16,
                distanceToVenueKm = 2.2,
                privacyMode = PrivacyMode.PRECISE
            ),
            Member(
                id = "m-kaan",
                name = "Kaan Özdemir",
                avatarInitials = "KÖ",
                avatarColorHex = 0xFF64748B,
                attendance = AttendanceStatus.NOT_COMING,
                liveStatus = LiveStatus.PREPARING,
                departureTime = "--:--",
                originName = "Ankara",
                vehicleType = VehicleType.CAR,
                currentLat = 39.9334,
                currentLng = 32.8597,
                originLat = 39.9334,
                originLng = 32.8597,
                speedKmH = 0,
                batteryLevel = 90,
                etaMinutes = 0,
                distanceToVenueKm = 420.0,
                privacyMode = PrivacyMode.GHOST
            ),
            Member(
                id = "m-ayse",
                name = "Ayşe Demir",
                avatarInitials = "AD",
                avatarColorHex = 0xFF14B8A6,
                attendance = AttendanceStatus.MAYBE,
                liveStatus = LiveStatus.PREPARING,
                departureTime = "--:--",
                originName = "Göztepe",
                vehicleType = VehicleType.CAR,
                currentLat = 40.9820,
                currentLng = 29.0600,
                originLat = 40.9820,
                originLng = 29.0600,
                speedKmH = 0,
                batteryLevel = 67,
                etaMinutes = 25,
                distanceToVenueKm = 3.5,
                privacyMode = PrivacyMode.EN_ROUTE_ONLY
            )
        )

        val event1 = OrganizationEvent(
            id = "event-1",
            title = "Boğaziçi Mezunlar & Arkadaşlar Buluşması",
            description = "Akşam çayı ve sohbet organizasyonu. Herkes canlı haritadan konumunu ve tahmini varış süresini anlık görebilir.",
            category = "Akşam Buluşması",
            eventDate = "12 Ekim 2026",
            eventTime = "19:30",
            venueName = "Moda Sahil Çay Bahçesi",
            venueAddress = "Caferağa Mah. Park İçi, Kadıköy / İstanbul",
            venueLat = venueLat,
            venueLng = venueLng,
            inviteCode = "BRLKT-4892",
            isOfflineCached = true,
            members = demoMembers1
        )

        val event2 = OrganizationEvent(
            id = "event-2",
            title = "Hafta Sonu Doğa Yürüyüşü & Kamp",
            description = "Sabah erkenden yola çıkılacak. Konvoy takibi ve ortak harcama bölüşümü.",
            category = "Doğa & Spor",
            eventDate = "18 Ekim 2026",
            eventTime = "08:00",
            venueName = "Polonezköy Tabiat Parkı",
            venueAddress = "Beykoz / İstanbul",
            venueLat = 41.1167,
            venueLng = 29.2083,
            inviteCode = "BRLKT-7721",
            isOfflineCached = true,
            members = demoMembers1.take(4)
        )

        _events.value = listOf(event1, event2)

        _chatMessages.value = listOf(
            ChatMessage(
                id = "msg-1",
                eventId = "event-1",
                senderId = "m-mehmet",
                senderName = "Mehmet Yılmaz",
                text = "Ben alana vardım arkadaşlar! Deniz kenarında 4 kişilik güzel bir masa kaptım 📍",
                timestamp = "19:05",
                isSentByMe = false,
                isQuickStatus = false,
                isEncrypted = true
            ),
            ChatMessage(
                id = "msg-2",
                eventId = "event-1",
                senderId = "m-elif",
                senderName = "Elif Kaya",
                text = "Süpersin Mehmet! Ben Marmaray'dayım, Ayrılık Çeşmesi durağına 3 dakika kaldı.",
                timestamp = "19:12",
                isSentByMe = false,
                isQuickStatus = false,
                isEncrypted = true
            ),
            ChatMessage(
                id = "msg-3",
                eventId = "event-1",
                senderId = "m-zeynep",
                senderName = "Zeynep Aydın",
                text = "Trafik kilitlendi 🚦 Köprü çıkışında biraz yoğunluk var ama 15 dakikaya gelirim.",
                timestamp = "19:16",
                isSentByMe = false,
                isQuickStatus = true,
                isEncrypted = true
            ),
            ChatMessage(
                id = "msg-4",
                eventId = "event-1",
                senderId = "user-me",
                senderName = "Can Demir (Siz)",
                text = "Ben de arabadayım, Levent'ten çıktım navigasyonda 14 dk görünüyor. Masraf için tatlıları ben alıyorum!",
                timestamp = "19:18",
                isSentByMe = true,
                isQuickStatus = false,
                isEncrypted = true
            )
        )

        _expenses.value = listOf(
            Expense(
                id = "exp-1",
                eventId = "event-1",
                title = "Masa Rezervasyon & Çay / Kahve",
                amount = 640.0,
                paidByMemberId = "m-mehmet",
                paidByMemberName = "Mehmet Yılmaz",
                splitMemberIds = listOf("user-me", "m-elif", "m-mehmet", "m-zeynep", "m-burak", "m-selin"),
                category = ExpenseCategory.FOOD,
                timestamp = "Bugün, 19:10"
            ),
            Expense(
                id = "exp-2",
                eventId = "event-1",
                title = "Ortak Tatlı & Atıştırmalıklar",
                amount = 480.0,
                paidByMemberId = "user-me",
                paidByMemberName = "Can Demir (Siz)",
                splitMemberIds = listOf("user-me", "m-elif", "m-mehmet", "m-zeynep", "m-burak", "m-selin"),
                category = ExpenseCategory.FOOD,
                timestamp = "Bugün, 19:22"
            ),
            Expense(
                id = "exp-3",
                eventId = "event-1",
                title = "Otopark Peşinatı",
                amount = 150.0,
                paidByMemberId = "m-burak",
                paidByMemberName = "Burak Şen",
                splitMemberIds = listOf("user-me", "m-burak"),
                category = ExpenseCategory.TRANSPORT,
                timestamp = "Bugün, 19:25"
            )
        )

        recalculateDebts()
    }
}
