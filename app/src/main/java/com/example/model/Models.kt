package com.example.model

enum class AppLanguage {
    TR, EN
}

enum class AttendanceStatus {
    COMING,
    NOT_COMING,
    MAYBE
}

enum class LiveStatus {
    PREPARING,   // Hazırlanıyor
    EN_ROUTE,    // Yola Çıktı
    IN_TRAFFIC,  // Trafikte
    ARRIVED,     // Alana Vardı
    DELAYED      // Gecikiyor
}

enum class VehicleType {
    CAR,      // Araba
    SUBWAY,   // Metro / Marmaray
    BUS,      // Otobüs / Metrobüs
    WALK,     // Yürüyerek
    BICYCLE,  // Bisiklet / Scooter
    TAXI      // Taksi
}

enum class PrivacyMode {
    PRECISE,       // Tam GPS
    APPROXIMATE,   // Yaklaşık (~500m)
    EN_ROUTE_ONLY, // Sadece yoldayken
    GHOST          // Gizli mod
}

enum class ExpenseCategory {
    FOOD,       // Yeme & İçme
    TRANSPORT,  // Ulaşım / Benzin
    TICKETS,    // Bilet & Giriş
    SHOPPING,   // Ortak Alışveriş
    VENUE,      // Mekan / Rezervasyon
    OTHER       // Diğer
}

data class LocationPoint(
    val latitude: Double,
    val longitude: Double,
    val name: String = ""
)

data class Member(
    val id: String,
    val name: String,
    val avatarInitials: String,
    val avatarColorHex: Long,
    val isCurrentUser: Boolean = false,
    val isOrganizer: Boolean = false,
    val attendance: AttendanceStatus = AttendanceStatus.COMING,
    val liveStatus: LiveStatus = LiveStatus.PREPARING,
    val departureTime: String = "19:00",
    val originName: String = "Kadıköy",
    val vehicleType: VehicleType = VehicleType.CAR,
    val currentLat: Double,
    val currentLng: Double,
    val originLat: Double,
    val originLng: Double,
    val speedKmH: Int = 0,
    val batteryLevel: Int = 85,
    val etaMinutes: Int = 20,
    val distanceToVenueKm: Double = 5.2,
    val privacyMode: PrivacyMode = PrivacyMode.PRECISE,
    val lastUpdatedText: String = "Şimdi"
)

data class OrganizationEvent(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val eventDate: String,
    val eventTime: String,
    val venueName: String,
    val venueAddress: String,
    val venueLat: Double,
    val venueLng: Double,
    val inviteCode: String,
    val isOfflineCached: Boolean = true,
    val members: List<Member> = emptyList()
)

data class ChatMessage(
    val id: String,
    val eventId: String,
    val senderId: String,
    val senderName: String,
    val text: String,
    val timestamp: String,
    val isSentByMe: Boolean = false,
    val isQuickStatus: Boolean = false,
    val isEncrypted: Boolean = true,
    val locationShareText: String? = null
)

data class Expense(
    val id: String,
    val eventId: String,
    val title: String,
    val amount: Double,
    val paidByMemberId: String,
    val paidByMemberName: String,
    val splitMemberIds: List<String>,
    val category: ExpenseCategory,
    val timestamp: String
)

data class DebtSettlement(
    val id: String,
    val fromMemberId: String,
    val fromMemberName: String,
    val toMemberId: String,
    val toMemberName: String,
    val amount: Double,
    val isSettled: Boolean = false
)

data class NotificationPreferences(
    val onDepartureAlert: Boolean = true,
    val onArrivalAlert: Boolean = true,
    val onProximityAlert: Boolean = true, // < 1 km
    val onNewExpenseAlert: Boolean = true,
    val onChatMessageAlert: Boolean = true
)
