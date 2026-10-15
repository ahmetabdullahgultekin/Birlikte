package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsSubway
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.Loc
import com.example.model.AppLanguage
import com.example.model.LiveStatus
import com.example.model.Member
import com.example.model.OrganizationEvent
import com.example.model.PrivacyMode
import com.example.model.VehicleType
import com.example.ui.theme.BirlikteCyan
import com.example.ui.theme.BirlikteIndigo
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRose

@Composable
fun CanvasMapView(
    event: OrganizationEvent,
    filteredMembers: List<Member>,
    selectedMember: Member?,
    onMemberSelected: (Member?) -> Unit,
    lang: AppLanguage,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    // Zoom and pan state
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    // Radar pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "radarPulse")
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarPulseFloat"
    )

    val mapBgColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9)
    val gridLineColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
    val waterAreaColor = if (isDark) Color(0xFF0C1322) else Color(0xFFE0F2FE)
    val roadColor = if (isDark) Color(0xFF1F2A3D) else Color(0xFFFFFFFF)

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        panOffsetX += dragAmount.x
                        panOffsetY += dragAmount.y
                    }
                }
                .pointerInput(filteredMembers, zoomScale, panOffsetX, panOffsetY) {
                    detectTapGestures { tapOffset ->
                        val centerX = size.width / 2f + panOffsetX
                        val centerY = size.height / 2f + panOffsetY
                        val scale = 6500f * zoomScale

                        // Find closest member tapped within 40dp radius
                        var hitMember: Member? = null
                        var minDistance = 80f

                        for (member in filteredMembers) {
                            val mx = centerX + ((member.currentLng - event.venueLng) * scale).toFloat()
                            val my = centerY - ((member.currentLat - event.venueLat) * scale).toFloat()

                            val dist = kotlin.math.hypot(tapOffset.x - mx, tapOffset.y - my)
                            if (dist < minDistance) {
                                minDistance = dist
                                hitMember = member
                            }
                        }
                        onMemberSelected(hitMember)
                    }
                }
        ) {
            val canvasW = size.width
            val canvasH = size.height
            val centerX = canvasW / 2f + panOffsetX
            val centerY = canvasH / 2f + panOffsetY
            val scale = 6500f * zoomScale

            // 1. Draw base background
            drawRect(color = mapBgColor)

            // 2. Draw mock stylized water body / coast
            val waterPath = Path().apply {
                moveTo(-200f + panOffsetX, centerY + 220f * zoomScale)
                cubicTo(
                    centerX - 100f, centerY + 180f * zoomScale,
                    centerX + 80f, centerY + 300f * zoomScale,
                    canvasW + 300f, centerY + 190f * zoomScale
                )
                lineTo(canvasW + 300f, canvasH + 300f)
                lineTo(-200f, canvasH + 300f)
                close()
            }
            drawPath(path = waterPath, color = waterAreaColor)

            // 3. Draw stylized grid lines representing streets & avenues
            val gridSize = 70f * zoomScale
            var gx = (centerX % gridSize)
            while (gx < canvasW) {
                drawLine(
                    color = gridLineColor,
                    start = Offset(gx, 0f),
                    end = Offset(gx, canvasH),
                    strokeWidth = 1f
                )
                gx += gridSize
            }
            var gy = (centerY % gridSize)
            while (gy < canvasH) {
                drawLine(
                    color = gridLineColor,
                    start = Offset(0f, gy),
                    end = Offset(canvasW, gy),
                    strokeWidth = 1f
                )
                gy += gridSize
            }

            // 4. Draw stylized main boulevards
            drawLine(
                color = roadColor,
                start = Offset(0f, centerY + 50f * zoomScale),
                end = Offset(canvasW, centerY - 80f * zoomScale),
                strokeWidth = 12f * zoomScale.coerceIn(0.6f, 1.8f)
            )
            drawLine(
                color = roadColor,
                start = Offset(centerX - 90f * zoomScale, 0f),
                end = Offset(centerX + 60f * zoomScale, canvasH),
                strokeWidth = 10f * zoomScale.coerceIn(0.6f, 1.8f)
            )

            // 5. Radar Pulse Waves radiating from Venue
            val maxPulseRadius = 180f * zoomScale
            val currentRadius = maxPulseRadius * pulseProgress
            val pulseAlpha = (1f - pulseProgress).coerceIn(0f, 1f) * 0.45f
            drawCircle(
                color = BirlikteIndigo.copy(alpha = pulseAlpha),
                radius = currentRadius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 3f * zoomScale)
            )
            drawCircle(
                color = BirlikteCyan.copy(alpha = pulseAlpha * 0.6f),
                radius = (currentRadius * 0.6f),
                center = Offset(centerX, centerY),
                style = Stroke(width = 2f * zoomScale)
            )

            // 6. Draw Member Trajectories & Positions
            for (member in filteredMembers) {
                val mx = centerX + ((member.currentLng - event.venueLng) * scale).toFloat()
                val my = centerY - ((member.currentLat - event.venueLat) * scale).toFloat()

                val ox = centerX + ((member.originLng - event.venueLng) * scale).toFloat()
                val oy = centerY - ((member.originLat - event.venueLat) * scale).toFloat()

                // Route trajectory path
                val routePath = Path().apply {
                    moveTo(ox, oy)
                    quadraticTo(
                        (ox + mx) / 2f + 25f * zoomScale,
                        (oy + my) / 2f - 25f * zoomScale,
                        mx, my
                    )
                }

                val memberColor = Color(member.avatarColorHex)
                drawPath(
                    path = routePath,
                    color = memberColor.copy(alpha = 0.5f),
                    style = Stroke(
                        width = 3.5f * zoomScale,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 8f), 0f)
                    )
                )

                // Projected line from current position to venue
                if (member.liveStatus != LiveStatus.ARRIVED) {
                    drawLine(
                        color = memberColor.copy(alpha = 0.3f),
                        start = Offset(mx, my),
                        end = Offset(centerX, centerY),
                        strokeWidth = 2f * zoomScale,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                    )
                }

                // If privacy is approximate, draw fuzzy bubble
                if (member.privacyMode == PrivacyMode.APPROXIMATE) {
                    drawCircle(
                        color = memberColor.copy(alpha = 0.15f),
                        radius = 45f * zoomScale,
                        center = Offset(mx, my)
                    )
                    drawCircle(
                        color = memberColor.copy(alpha = 0.3f),
                        radius = 45f * zoomScale,
                        center = Offset(mx, my),
                        style = Stroke(width = 1.5f)
                    )
                }

                // Member Pin Outer Glow / Selection Ring
                val isSelected = selectedMember?.id == member.id
                if (isSelected || member.isCurrentUser) {
                    drawCircle(
                        color = if (member.isCurrentUser) BirlikteIndigo.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.4f),
                        radius = 28f * zoomScale,
                        center = Offset(mx, my)
                    )
                }

                // Member Pin Body
                drawCircle(
                    color = Color.White,
                    radius = 18f * zoomScale,
                    center = Offset(mx, my)
                )
                drawCircle(
                    color = memberColor,
                    radius = 15f * zoomScale,
                    center = Offset(mx, my)
                )

                // Vehicle Badge pill above member
                val vehicleEmoji = when (member.vehicleType) {
                    VehicleType.CAR -> "🚗"
                    VehicleType.SUBWAY -> "🚇"
                    VehicleType.BUS -> "🚌"
                    VehicleType.WALK -> "🚶"
                    VehicleType.BICYCLE -> "🚲"
                    VehicleType.TAXI -> "🚕"
                }

                // Draw member label text with ETA
                val paint = android.graphics.Paint().apply {
                    color = if (isDark) android.graphics.Color.WHITE else android.graphics.Color.BLACK
                    textSize = (11f * zoomScale).coerceIn(10f, 15f) * density
                    isFakeBoldText = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }

                val subPaint = android.graphics.Paint().apply {
                    color = if (isDark) android.graphics.Color.LTGRAY else android.graphics.Color.DKGRAY
                    textSize = (9.5f * zoomScale).coerceIn(9f, 13f) * density
                    textAlign = android.graphics.Paint.Align.CENTER
                }

                // Background tag behind text for maximum legibility
                val labelText = "${member.avatarInitials} $vehicleEmoji"
                val etaText = if (member.liveStatus == LiveStatus.ARRIVED) "Vardı" else "${member.etaMinutes} dk • ${member.speedKmH} km/h"

                val bgRectY = my + 20f * zoomScale
                val bgTagWidth = 72f * zoomScale * density
                val bgTagHeight = 26f * zoomScale * density

                drawRoundRect(
                    color = if (isDark) Color(0xDD1E293B) else Color(0xEEFFFFFF),
                    topLeft = Offset(mx - bgTagWidth / 2f, bgRectY),
                    size = Size(bgTagWidth, bgTagHeight),
                    cornerRadius = CornerRadius(6f * density, 6f * density)
                )
                drawRoundRect(
                    color = memberColor.copy(alpha = 0.6f),
                    topLeft = Offset(mx - bgTagWidth / 2f, bgRectY),
                    size = Size(bgTagWidth, bgTagHeight),
                    cornerRadius = CornerRadius(6f * density, 6f * density),
                    style = Stroke(width = 1f)
                )

                drawContext.canvas.nativeCanvas.drawText(
                    labelText,
                    mx,
                    bgRectY + 11f * density * zoomScale,
                    paint
                )
                drawContext.canvas.nativeCanvas.drawText(
                    etaText,
                    mx,
                    bgRectY + 22f * density * zoomScale,
                    subPaint
                )
            }

            // 7. Venue Destination Pin at center (venueLat, venueLng)
            // Pin shadow
            drawCircle(
                color = Color(0x44000000),
                radius = 16f * zoomScale,
                center = Offset(centerX, centerY + 8f)
            )
            // Destination Halo
            drawCircle(
                color = StatusAmber.copy(alpha = 0.25f),
                radius = 32f * zoomScale,
                center = Offset(centerX, centerY)
            )
            // Pin core
            drawCircle(
                color = Color.White,
                radius = 19f * zoomScale,
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = StatusAmber,
                radius = 15f * zoomScale,
                center = Offset(centerX, centerY)
            )

            // Venue banner box
            val venuePaint = android.graphics.Paint().apply {
                color = if (isDark) android.graphics.Color.WHITE else android.graphics.Color.BLACK
                textSize = (12f * zoomScale).coerceIn(11f, 16f) * density
                isFakeBoldText = true
                textAlign = android.graphics.Paint.Align.CENTER
            }
            val venueSubPaint = android.graphics.Paint().apply {
                color = StatusAmber.hashCode()
                textSize = (10f * zoomScale).coerceIn(9f, 13f) * density
                isFakeBoldText = true
                textAlign = android.graphics.Paint.Align.CENTER
            }

            val vTagWidth = 120f * zoomScale * density
            val vTagHeight = 32f * zoomScale * density
            val vTagTop = centerY - 52f * zoomScale

            drawRoundRect(
                color = if (isDark) Color(0xF00F172A) else Color(0xF0FFFFFF),
                topLeft = Offset(centerX - vTagWidth / 2f, vTagTop),
                size = Size(vTagWidth, vTagHeight),
                cornerRadius = CornerRadius(8f * density, 8f * density)
            )
            drawRoundRect(
                color = StatusAmber,
                topLeft = Offset(centerX - vTagWidth / 2f, vTagTop),
                size = Size(vTagWidth, vTagHeight),
                cornerRadius = CornerRadius(8f * density, 8f * density),
                style = Stroke(width = 1.8f)
            )

            drawContext.canvas.nativeCanvas.drawText(
                "📍 ${event.venueName}",
                centerX,
                vTagTop + 14f * density * zoomScale,
                venuePaint
            )
            drawContext.canvas.nativeCanvas.drawText(
                "Hedef Noktası",
                centerX,
                vTagTop + 26f * density * zoomScale,
                venueSubPaint
            )
        }

        // Live Controls (Top Right: Zoom In, Zoom Out, Center Venue, Center Me)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FilledIconButton(
                onClick = { zoomScale = (zoomScale * 1.25f).coerceAtMost(3.0f) },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                ),
                modifier = Modifier.size(42.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.height(8.dp))
            FilledIconButton(
                onClick = { zoomScale = (zoomScale / 1.25f).coerceAtLeast(0.4f) },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                ),
                modifier = Modifier.size(42.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.height(8.dp))
            FilledIconButton(
                onClick = {
                    panOffsetX = 0f
                    panOffsetY = 0f
                    zoomScale = 1.0f
                },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                ),
                modifier = Modifier.size(42.dp)
            ) {
                Icon(Icons.Default.ZoomOutMap, contentDescription = "Center Venue", tint = StatusAmber)
            }
            Spacer(modifier = Modifier.height(8.dp))
            FilledIconButton(
                onClick = {
                    // Pan towards current user if present
                    val me = event.members.find { it.isCurrentUser }
                    if (me != null) {
                        val scale = 6500f * zoomScale
                        panOffsetX = -((me.currentLng - event.venueLng) * scale).toFloat()
                        panOffsetY = ((me.currentLat - event.venueLat) * scale).toFloat()
                    }
                },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                ),
                modifier = Modifier.size(42.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Center Me", tint = BirlikteIndigo)
            }
        }

        // Selected Member Details Drawer / Card (at bottom of map)
        if (selectedMember != null) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(selectedMember.avatarColorHex),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    selectedMember.avatarInitials,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    selectedMember.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (selectedMember.isCurrentUser) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = BirlikteIndigo.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            "SİZ",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = BirlikteIndigo,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Text(
                                "${selectedMember.originName} • ${Loc.getVehicleLabel(selectedMember.vehicleType, lang)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Live Status pill
                        val statusBg = when (selectedMember.liveStatus) {
                            LiveStatus.ARRIVED -> StatusGreen
                            LiveStatus.IN_TRAFFIC -> StatusRose
                            LiveStatus.EN_ROUTE -> BirlikteIndigo
                            LiveStatus.DELAYED -> StatusAmber
                            LiveStatus.PREPARING -> Color.Gray
                        }
                        Surface(
                            color = statusBg.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                Loc.getLiveStatusLabel(selectedMember.liveStatus, lang),
                                color = statusBg,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Metrics Row: Distance, Speed, Battery, ETA
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MetricItem(
                            label = Loc.s("distance", lang),
                            value = "${selectedMember.distanceToVenueKm} km",
                            modifier = Modifier.weight(1f)
                        )
                        MetricItem(
                            label = Loc.s("speed", lang),
                            value = "${selectedMember.speedKmH} km/h",
                            modifier = Modifier.weight(1f)
                        )
                        MetricItem(
                            label = Loc.s("battery", lang),
                            value = "%${selectedMember.batteryLevel}",
                            modifier = Modifier.weight(1f)
                        )
                        MetricItem(
                            label = Loc.s("eta", lang),
                            value = if (selectedMember.liveStatus == LiveStatus.ARRIVED) "Alana Vardı" else "${selectedMember.etaMinutes} dk",
                            highlight = true,
                            modifier = Modifier.weight(1.2f)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "🔒 ${Loc.getPrivacyLabel(selectedMember.privacyMode, lang)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricItem(
    label: String,
    value: String,
    highlight: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (highlight) StatusGreen else MaterialTheme.colorScheme.onSurface
        )
    }
}
