package com.example.localization

import com.example.model.AppLanguage
import com.example.model.AttendanceStatus
import com.example.model.ExpenseCategory
import com.example.model.LiveStatus
import com.example.model.PrivacyMode
import com.example.model.VehicleType

object Loc {
    fun getAttendanceLabel(status: AttendanceStatus, lang: AppLanguage): String = when (lang) {
        AppLanguage.TR -> when (status) {
            AttendanceStatus.COMING -> "Geliyor"
            AttendanceStatus.NOT_COMING -> "Gelmiyor"
            AttendanceStatus.MAYBE -> "Kararsız"
        }
        AppLanguage.EN -> when (status) {
            AttendanceStatus.COMING -> "Attending"
            AttendanceStatus.NOT_COMING -> "Not Going"
            AttendanceStatus.MAYBE -> "Undecided"
        }
    }

    fun getLiveStatusLabel(status: LiveStatus, lang: AppLanguage): String = when (lang) {
        AppLanguage.TR -> when (status) {
            LiveStatus.PREPARING -> "Hazırlanıyor"
            LiveStatus.EN_ROUTE -> "Yola Çıktı"
            LiveStatus.IN_TRAFFIC -> "Trafikte"
            LiveStatus.ARRIVED -> "Alana Vardı"
            LiveStatus.DELAYED -> "Gecikiyor"
        }
        AppLanguage.EN -> when (status) {
            LiveStatus.PREPARING -> "Preparing"
            LiveStatus.EN_ROUTE -> "On the Way"
            LiveStatus.IN_TRAFFIC -> "In Traffic"
            LiveStatus.ARRIVED -> "Arrived"
            LiveStatus.DELAYED -> "Delayed"
        }
    }

    fun getVehicleLabel(type: VehicleType, lang: AppLanguage): String = when (lang) {
        AppLanguage.TR -> when (type) {
            VehicleType.CAR -> "Araba"
            VehicleType.SUBWAY -> "Metro/Marmaray"
            VehicleType.BUS -> "Otobüs/Metrobüs"
            VehicleType.WALK -> "Yürüyerek"
            VehicleType.BICYCLE -> "Bisiklet"
            VehicleType.TAXI -> "Taksi"
        }
        AppLanguage.EN -> when (type) {
            VehicleType.CAR -> "Car"
            VehicleType.SUBWAY -> "Subway/Train"
            VehicleType.BUS -> "Bus"
            VehicleType.WALK -> "Walking"
            VehicleType.BICYCLE -> "Bicycle"
            VehicleType.TAXI -> "Taxi"
        }
    }

    fun getPrivacyLabel(mode: PrivacyMode, lang: AppLanguage): String = when (lang) {
        AppLanguage.TR -> when (mode) {
            PrivacyMode.PRECISE -> "Tam Konum (GPS)"
            PrivacyMode.APPROXIMATE -> "Yaklaşık Konum (~500m)"
            PrivacyMode.EN_ROUTE_ONLY -> "Sadece Yoldayken"
            PrivacyMode.GHOST -> "Hayalet Modu (Gizli)"
        }
        AppLanguage.EN -> when (mode) {
            PrivacyMode.PRECISE -> "Precise GPS"
            PrivacyMode.APPROXIMATE -> "Approximate (~500m)"
            PrivacyMode.EN_ROUTE_ONLY -> "Only When En Route"
            PrivacyMode.GHOST -> "Ghost Mode (Hidden)"
        }
    }

    fun getExpenseCategoryLabel(cat: ExpenseCategory, lang: AppLanguage): String = when (lang) {
        AppLanguage.TR -> when (cat) {
            ExpenseCategory.FOOD -> "Yeme & İçme"
            ExpenseCategory.TRANSPORT -> "Ulaşım / Benzin"
            ExpenseCategory.TICKETS -> "Bilet & Giriş"
            ExpenseCategory.SHOPPING -> "Ortak Alışveriş"
            ExpenseCategory.VENUE -> "Mekan / Rezervasyon"
            ExpenseCategory.OTHER -> "Diğer"
        }
        AppLanguage.EN -> when (cat) {
            ExpenseCategory.FOOD -> "Food & Drinks"
            ExpenseCategory.TRANSPORT -> "Transit / Fuel"
            ExpenseCategory.TICKETS -> "Tickets & Entry"
            ExpenseCategory.SHOPPING -> "Group Shopping"
            ExpenseCategory.VENUE -> "Venue / Booking"
            ExpenseCategory.OTHER -> "Other"
        }
    }

    fun s(key: String, lang: AppLanguage): String {
        val dict = if (lang == AppLanguage.TR) trMap else enMap
        return dict[key] ?: key
    }

    private val trMap = mapOf(
        "app_title" to "Birlikte",
        "tab_overview" to "Katılımcılar",
        "tab_map" to "Canlı Harita",
        "tab_chat" to "Ortak Sohbet",
        "tab_expenses" to "Masraflar",
        "tab_settings" to "Ayarlar",
        "online" to "Çevrimiçi",
        "offline" to "Çevrimdışı (Yerel Mod)",
        "websocket_connected" to "Canlı Akış (WSS): Bağlı",
        "websocket_offline" to "Canlı Akış: Kesildi",
        "invite_code" to "Davet Kodu",
        "copy_code" to "Kodu Kopyala",
        "copied" to "Kopyalandı!",
        "show_qr" to "QR Göster",
        "my_status" to "Benim Durumum",
        "update_status" to "Durumunu Güncelle",
        "attending" to "Geliyorum",
        "not_attending" to "Gelemiyorum",
        "undecided" to "Kararsızım",
        "all" to "Tümü",
        "filter_on_way" to "Yoldakiler",
        "filter_near" to "< 2 km Yakında",
        "filter_5km" to "< 5 km",
        "filter_arrived" to "Varanlar",
        "eta" to "Tahmini Varış",
        "distance" to "Mesafe",
        "speed" to "Hız",
        "battery" to "Şarj",
        "departure_from" to "Çıkış Noktası",
        "departure_time" to "Çıkış Saati",
        "vehicle" to "Araç",
        "privacy" to "Gizlilik",
        "show_on_map" to "Haritada Göster",
        "encrypted_chat_note" to "Uçtan Uca Şifreli Kanal • Signal Protocol 256-bit",
        "quick_status" to "Hızlı Durum",
        "quick_5min" to "5 dk'ya oradayım! 🏃",
        "quick_traffic" to "Trafik kilitlendi 🚦",
        "quick_arrived" to "Geldik, masa ayrıldı 📍",
        "quick_need_anything" to "Bir şeye ihtiyacı olan var mı? ☕",
        "type_message" to "Bir mesaj yazın...",
        "share_location" to "Konum Paylaş",
        "total_spent" to "Toplam Masraf",
        "per_person" to "Kişi Başı Ortalama",
        "my_balance" to "Benim Bakiyem",
        "you_are_owed" to "Alacaklısınız",
        "you_owe" to "Borçlusunuz",
        "balanced" to "Dengede",
        "smart_settlement" to "Akıllı Borç Takası",
        "add_expense" to "Masraf Ekle",
        "expense_title" to "Harcama Başlığı",
        "expense_amount" to "Tutar (₺)",
        "payer" to "Ödeyen Kişi",
        "split_with" to "Bölüşenler",
        "save" to "Kaydet",
        "cancel" to "İptal",
        "settle_up" to "Ödendi / Borcu Kapat",
        "settled" to "Ödendi",
        "pending" to "Bekliyor",
        "radar_tracking" to "Canlı Radar Takibi",
        "center_venue" to "Mekana Odaklan",
        "center_me" to "Konumuma Odaklan",
        "change_event" to "Etkinlik Değiştir / Oluştur",
        "new_event" to "Yeni Organizasyon",
        "event_title" to "Etkinlik Adı",
        "event_date" to "Tarih & Saat",
        "venue" to "Buluşma Noktası",
        "security_privacy" to "Güvenlik & Gizlilik",
        "e2e_encryption" to "Uçtan Uca Şifreleme",
        "encryption_active" to "Aktif (Tüm konumlar ve mesajlar cihazda şifrelenir)",
        "notifications" to "Özelleştirilebilir Bildirimler",
        "notify_departure" to "Biri yola çıktığında bildir",
        "notify_arrival" to "Biri mekana vardığında bildir",
        "notify_proximity" to "Bir katılımcı 1 km yaklaştığında bildir",
        "notify_expense" to "Yeni masraf eklendiğinde bildir",
        "notify_chat" to "Yeni sohbet mesajlarında bildir",
        "language" to "Dil / Language",
        "dark_mode" to "Karanlık Mod",
        "offline_simulation" to "Çevrimdışı Çalışma Simülasyonu",
        "offline_desc" to "İnternet kesildiğinde tüm veriler yerel Room veritabanında saklanır ve kuyruğa alınır."
    )

    private val enMap = mapOf(
        "app_title" to "Birlikte",
        "tab_overview" to "Members",
        "tab_map" to "Live Map",
        "tab_chat" to "Group Chat",
        "tab_expenses" to "Expenses",
        "tab_settings" to "Settings",
        "online" to "Online",
        "offline" to "Offline (Local Mode)",
        "websocket_connected" to "Live Feed (WSS): Connected",
        "websocket_offline" to "Live Feed: Disconnected",
        "invite_code" to "Invite Code",
        "copy_code" to "Copy Code",
        "copied" to "Copied!",
        "show_qr" to "Show QR",
        "my_status" to "My Status",
        "update_status" to "Update Status",
        "attending" to "Attending",
        "not_attending" to "Can't Go",
        "undecided" to "Undecided",
        "all" to "All",
        "filter_on_way" to "En Route",
        "filter_near" to "< 2 km Nearby",
        "filter_5km" to "< 5 km",
        "filter_arrived" to "Arrived",
        "eta" to "Estimated Arrival",
        "distance" to "Distance",
        "speed" to "Speed",
        "battery" to "Battery",
        "departure_from" to "Departure Point",
        "departure_time" to "Departure Time",
        "vehicle" to "Vehicle",
        "privacy" to "Privacy",
        "show_on_map" to "Show on Map",
        "encrypted_chat_note" to "End-to-End Encrypted Channel • Signal Protocol 256-bit",
        "quick_status" to "Quick Status",
        "quick_5min" to "5 mins away! 🏃",
        "quick_traffic" to "Traffic jam 🚦",
        "quick_arrived" to "Arrived, got table 📍",
        "quick_need_anything" to "Need anything picked up? ☕",
        "type_message" to "Type a message...",
        "share_location" to "Share Location",
        "total_spent" to "Total Expenses",
        "per_person" to "Average Per Person",
        "my_balance" to "My Balance",
        "you_are_owed" to "You are owed",
        "you_owe" to "You owe",
        "balanced" to "Settled",
        "smart_settlement" to "Smart Debt Settlement",
        "add_expense" to "Add Expense",
        "expense_title" to "Expense Title",
        "expense_amount" to "Amount (₺)",
        "payer" to "Paid by",
        "split_with" to "Split with",
        "save" to "Save",
        "cancel" to "Cancel",
        "settle_up" to "Mark Paid / Settle",
        "settled" to "Paid",
        "pending" to "Pending",
        "radar_tracking" to "Live Radar Tracking",
        "center_venue" to "Focus Venue",
        "center_me" to "Focus My Location",
        "change_event" to "Switch / Create Event",
        "new_event" to "New Event",
        "event_title" to "Event Name",
        "event_date" to "Date & Time",
        "venue" to "Meeting Venue",
        "security_privacy" to "Security & Privacy",
        "e2e_encryption" to "End-to-End Encryption",
        "encryption_active" to "Active (All telemetry & messages are client-encrypted)",
        "notifications" to "Customizable Notifications",
        "notify_departure" to "Notify when someone departs",
        "notify_arrival" to "Notify when someone arrives",
        "notify_proximity" to "Notify when someone is < 1 km away",
        "notify_expense" to "Notify on new expenses",
        "notify_chat" to "Notify on new chat messages",
        "language" to "Language / Dil",
        "dark_mode" to "Dark Mode",
        "offline_simulation" to "Offline Operation Simulation",
        "offline_desc" to "When network is lost, all changes persist locally in Room database and queue for auto-sync."
    )
}
