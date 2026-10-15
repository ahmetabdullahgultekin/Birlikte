# Birlikte: Modern UI Polish, Firebase Auth/Firestore & Gemini Grounding

Comprehensive redesign to fix cramped UI layouts with spacious, high-contrast Material 3 cards, full-bleed interactive map views, Firebase Google Sign-In with Firestore real-time cloud sync, and Gemini 3.8 Flash with Google Maps and Search grounding for live venue discovery and transit insights.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural and design choices were confirmed via user clarification answers:

- **Confirmed Decision 1 (GUI Redesign)**: Transition from cramped, micro-font widgets to clean spacious cards with tabbed organization, 8dp-grid spacing, minimum 48dp touch targets, and an expanded, immersive map experience with collapsible member inspect sheets.
- **Confirmed Decision 2 (Authentication & Persistence)**: Implement Firebase Google Sign-In via Jetpack Credential Manager as the primary authenticated gate, paired with Firestore cloud database synchronization and offline-first persistence.
- **Confirmed Decision 3 (AI Grounding Scope)**: Implement Gemini 3.8 Flash equipped with `googleMaps` and `googleSearch` grounding tools for smart venue discovery, real-time place details (opening hours, rating, address), and live route/traffic advisory.

---

## 1. Overview & Core Concept

**Birlikte** is an event coordination and live transit tracking application for groups organizing gatherings, dinners, outdoor activities, and trips. 

### What This Upgrade Delivers
1. **Spacious & Polished GUI**: Resolves all layout cramping, small micro-labels (sub-11sp), and congested cards. Provides generous whitespace, legible typography hierarchy, fluid card padding (16dp+), responsive top app bar, and uncluttered navigation.
2. **Expanded Map Experience**: Full-screen canvas map view with non-intrusive floating pill overlays, smooth pan/zoom gestures, distinct transit avatars, animated trajectory paths, and an elegant bottom sheet for selected member telemetry.
3. **Firebase Cloud Sync & Google Sign-In**: Production-grade authentication using Jetpack Credential Manager (`GetSignInWithGoogleOption`) and Firestore real-time snapshot listeners for multi-user event synchronization, chat, and expenses.
4. **AI Assistant with Maps & Search Grounding**: An interactive "Mekan & Rota Rehberi" (Venue & Route Intelligence) powered by `gemini-3.8-flash` with `googleMaps` and `googleSearch` tools, answering questions like "Moda sahilindeki mekanın otopark durumu nedir?", "En yakın metro durağı nerede?", and providing live traffic & place insights.

---

## 2. User Experience & Visual Design

### Key User Flows
1. **Authentication Gate**: User launches app. If not signed in, a clean branded landing screen presents the "Sign in with Google" button with security assurances (E2E encryption badges). On success, user seamlessly enters their active organization.
2. **Spacious Event Hub (Katılımcılar)**:
   - **Hero Card**: Roomy event banner displaying title, category tag, date/time, and one-tap invite code copy.
   - **My Status Hub**: Prominent, accessible card with comfortable buttons to update departure origin, departure time, vehicle, and live status.
   - **Member Attendance Tabs**: Clean tabs (*Gelenler*, *Kararsız*, *Gelmeyenler*) eliminating the cramped 4-column counter row. Each card features generous spacing, high-contrast vehicle badges, live speed, battery %, and a large "Haritada Gör" touch target.
3. **Expanded Live Map (Canlı Harita)**:
   - Full viewport vector map canvas with radar beacon pulses and glowing route trajectories.
   - Floating header pill with live telemetry indicators (WSS status, active commuters).
   - Bottom floating avatar carousel to quickly focus on any member with one tap.
   - Smooth bottom inspect sheet displaying detailed distance, speed, battery, and ETA without blocking map navigation.
4. **Venue & Traffic Intelligence (AI Rehber)**:
   - Accessible from the app bar or venue card. Users can query venue details, live traffic warnings, parking availability, or nearby recommendations backed by Google Maps & Google Search grounding.
5. **Group Chat & Expense Manager**:
   - Spacious chat bubbles with Signal Protocol encryption indicators and quick status broadcast buttons.
   - Visual expense breakdown with simplified debt settlement matrix ("Ali → Ayşe'ye ₺150") and one-tap "Ödendi" resolution.

### Visual Identity & Theme Tokens
- **Primary Aesthetic**: Modern, minimalist, tech-forward with cobalt/indigo accents and high readability.
- **Color Scheme**:
  - `Primary`: Electric Indigo (`#4F46E5` light, `#818CF8` dark)
  - `Secondary`: Cyan Glow (`#06B6D4` light, `#38BDF8` dark)
  - `Accent/Status`: Emerald (`#10B981`), Amber (`#F59E0B`), Rose (`#F43F5E`)
  - `Surfaces`: Light (`#F8FAFC`, `#FFFFFF`), Dark (`#0B0F19`, `#111827`, `#1E293B`)
- **Typography**: M3 typography with body sizes ≥ 14sp, titles 18–22sp, and minimum touch targets strictly ≥ 48dp.

---

## 3. Key Product Decisions & Trade-Offs

### Decision 1: GUI Restructuring & Layout Hierarchy
- **Chosen Approach**: Replace squished 4-box stat rows and micro-cards with segmented tabbed sections (*Tümü*, *Gelenler*, *Kararsız*, *Gelmeyenler*), card padding bumped from 10dp to 16dp, and separate focused inspection sheets on the map.
- **Why**: The user specifically reported cramped and confusing UI. Segmented sections give each member card breathing room, preventing text overlapping and icon collision.
- **Alternatives Considered**: Full modal popups for each item (too disruptive to quick status scanning).

### Decision 2: Firebase Google Sign-In & Firestore Architecture
- **Chosen Approach**: Use Jetpack Credential Manager with `GetSignInWithGoogleOption` referencing `R.string.default_web_client_id`, paired with custom Firestore database ID (`R.string.firestore_database_id`) and zero-trust security rules (`firestore.rules`).
- **Why**: Follows Android best practices and Google Play policy. Anonymous auth is disabled; Google Sign-In guarantees authentic member identities and robust multi-device sync.
- **Alternatives Considered**: Email/password auth (clunky mobile experience with high drop-off).

### Decision 3: Gemini Grounding with Google Maps & Google Search Tools
- **Chosen Approach**: Integrate `gemini-3.8-flash` using Retrofit REST client configured with `tools = [{ "googleMaps": {} }, { "googleSearch": {} }]`.
- **Why**: Provides real-time venue hours, ratings, parking details, and transit/traffic information directly inside the app, grounded in live Google Maps and Search data.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                              MainActivity                              │
│         Auth Gate: if (currentUser == null) AuthScreen else ...        │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
┌───────────────────────────────────▼────────────────────────────────────┐
│                              BirlikteApp                               │
│              Scaffold + NavigationBar + TopAppBar Actions              │
├───────────────┬────────────────┬────────────────┬──────────────────────┤
│ 1. Overview   │ 2. Live Map    │ 3. Chat & AI   │ 4. Expenses & Settler│
│  - Hero Card  │  - Canvas Map  │  - E2E Chat    │  - Total & Avg KPI   │
│  - Status Card│  - Telemetry   │  - Maps/Search │  - Debt Matrix       │
│  - Member List│  - Bottom Sheet│    AI Rehber   │  - Add Expense Modal │
└───────┬───────┴────────┬───────┴────────┬───────┴──────────────┬───────┘
        │                │                │                      │
┌───────▼────────────────▼────────────────▼──────────────────────▼───────┐
│                             AppRepository                              │
│         - Firestore Collection Observers (Snapshot Listeners)          │
│         - Local Cache & Offline Sync Queue                             │
│         - Gemini Grounding Client (gemini-3.8-flash)                   │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
        ┌───────────────────────────┴───────────────────────────┐
        ▼                                                       ▼
┌───────────────────────────────┐               ┌───────────────────────────────┐
│     Firebase Cloud Backend    │               │       Gemini 3.8 Flash        │
│ - Firebase Auth (Google Sign) │               │ - Google Maps Grounding Tool  │
│ - Firestore Database & Rules  │               │ - Google Search Grounding Tool│
└───────────────────────────────┘               └───────────────────────────────┘
```

### Data Model & Firestore Collections
- `/events/{eventId}`: Organization details, venue coordinates, date, invite code, organizerId.
- `/events/{eventId}/members/{userId}`: Attendance status, departure origin, departure time, vehicle type, live coordinates, speed, battery level, ETA, privacy mode.
- `/events/{eventId}/messages/{messageId}`: E2E encrypted chat messages, quick status updates, location pins.
- `/events/{eventId}/expenses/{expenseId}`: Title, amount, payerId, splitMemberIds, category, timestamp.

### Gemini Grounding Request Schema
```kotlin
val request = GenerateContentRequest(
    contents = listOf(Content(parts = listOf(Part(text = userQuery)))),
    tools = listOf(
        buildJsonObject { putJsonObject("googleMaps") {} },
        buildJsonObject { putJsonObject("googleSearch") {} }
    )
)
```

### Verification & Testing Plan
1. **Compilation Check**: Verify successful compilation via `compile_applet`.
2. **UI Inspection**: Test light and dark theme contrast, verify touch targets (≥ 48dp), and confirm map pan/zoom responsiveness on emulator.
3. **Unit & Logic Tests**: Validate debt settlement algorithm, member telemetry calculations, and offline queue transitions via `gradle :app:testDebugUnitTest`.
