package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

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

    val mapBgColor = if (isDark) Color(0xFF0B1120) else Color(0xFFF1F5F9)
    val gridLineColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
    val waterAreaColor = if (isDark) Color(0xFF08101E) else Color(0xFFE0F2FE)
    val roadColor = if (isDark) Color(0xFF1E293B) else Color(0xFFFFFFFF)

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

                        var hitMember: Member? = null
                        var minDistance = 90f

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

            drawRect(color = mapBgColor)

            // Coastline & Water Body
            val waterPath = Path().apply {
                moveTo(-200f + panOffsetX, centerY + 240f * zoomScale)
                cubicTo(
                    centerX - 120f, centerY + 190f * zoomScale,
                    centerX + 100f, centerY + 320f * zoomScale,
                    canvasW + 300f, centerY + 200f * zoomScale
                )
                lineTo(canvasW + 300f, canvasH + 300f)
                lineTo(-200f, canvasH + 300f)
                close()
            }
            drawPath(path = waterPath, color = waterAreaColor)

            // Street Grid lines
            val gridSize = 80f * zoomScale
            var gx = (centerX % gridSize)
            while (gx < canvasW) {
                drawLine(
                    color = gridLineColor,
                    start = Offset(gx, 0f),
                    end = Offset(gx, canvasH),
                    strokeWidth = 1.2f
                )
                gx += gridSize
            }
            var gy = (centerY % gridSize)
            while (gy < canvasH) {
                drawLine(
                    color = gridLineColor,
                    start = Offset(0f, gy),
                    end = Offset(canvasW, gy),
                    strokeWidth = 1.2f
                )
                gy += gridSize
            }

            // Main Arteries
            drawLine(
                color = roadColor,
                start = Offset(0f, centerY + 60f * zoomScale),
                end = Offset(canvasW, centerY - 90f * zoomScale),
                strokeWidth = 14f * zoomScale.coerceIn(0.7f, 1.9f)
            )
            drawLine(
                color = roadColor,
                start = Offset(centerX - 100f * zoomScale, 0f),
                end = Offset(centerX + 70f * zoomScale, canvasH),
                strokeWidth = 12f * zoomScale.coerceIn(0.7f, 1.9f)
            )

            // Radar Pulse Waves radiating from Venue
            val maxPulseRadius = 200f * zoomScale
            val currentRadius = maxPulseRadius * pulseProgress
            val pulseAlpha = (1f - pulseProgress).coerceIn(0f, 1f) * 0.45f
            drawCircle(
                color = BirlikteIndigo.copy(alpha = pulseAlpha),
                radius = currentRadius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 3.5f * zoomScale)
            )
            drawCircle(
                color = BirlikteCyan.copy(alpha = pulseAlpha * 0.6f),
                radius = (currentRadius * 0.65f),
                center = Offset(centerX, centerY),
                style = Stroke(width = 2.5f * zoomScale)
            )

            // Trajectories & Member Pins
            for (member in filteredMembers) {
                val mx = centerX + ((member.currentLng - event.venueLng) * scale).toFloat()
                val my = centerY - ((member.currentLat - event.venueLat) * scale).toFloat()

                val ox = centerX + ((member.originLng - event.venueLng) * scale).toFloat()
                val oy = centerY - ((member.originLat - event.venueLat) * scale).toFloat()

                val routePath = Path().apply {
                    moveTo(ox, oy)
                    quadraticTo(
                        (ox + mx) / 2f + 30f * zoomScale,
                        (oy + my) / 2f - 30f * zoomScale,
                        mx, my
                    )
                }

                val memberColor = Color(member.avatarColorHex)
                drawPath(
                    path = routePath,
                    color = memberColor.copy(alpha = 0.55f),
                    style = Stroke(
                        width = 4f * zoomScale,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f), 0f)
                    )
                )

                if (member.liveStatus != LiveStatus.ARRIVED) {
                    drawLine(
                        color = memberColor.copy(alpha = 0.35f),
                        start = Offset(mx, my),
                        end = Offset(centerX, centerY),
                        strokeWidth = 2.5f * zoomScale,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                    )
                }

                if (member.privacyMode == PrivacyMode.APPROXIMATE) {
                    drawCircle(
                        color = memberColor.copy(alpha = 0.18f),
                        radius = 50f * zoomScale,
                        center = Offset(mx, my)
                    )
                }

                val isSelected = selectedMember?.id == member.id
                if (isSelected || member.isCurrentUser) {
                    drawCircle(
                        color = if (member.isCurrentUser) BirlikteIndigo.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.5f),
                        radius = 32f * zoomScale,
                        center = Offset(mx, my)
                    )
                }

                drawCircle(
                    color = Color.White,
                    radius = 20f * zoomScale,
                    center = Offset(mx, my)
                )
                drawCircle(
                    color = memberColor,
                    radius = 16f * zoomScale,
                    center = Offset(mx, my)
                )

                val vehicleEmoji = when (member.vehicleType) {
                    VehicleType.CAR -> "🚗"
                    VehicleType.SUBWAY -> "🚇"
                    VehicleType.BUS -> "🚌"
                    VehicleType.WALK -> "🚶"
                    VehicleType.BICYCLE -> "🚲"
                    VehicleType.TAXI -> "🚕"
                }

                val paint = android.graphics.Paint().apply {
                    color = if (isDark) android.graphics.Color.WHITE else android.graphics.Color.BLACK
                    textSize = (12f * zoomScale).coerceIn(12f, 16f) * density
                    isFakeBoldText = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }

                val labelText = "${member.avatarInitials} $vehicleEmoji"
                val etaText = if (member.liveStatus == LiveStatus.ARRIVED) "Vardı" else "${member.etaMinutes} dk"

                val bgRectY = my + 22f * zoomScale
                val bgTagWidth = 76f * zoomScale * density
                val bgTagHeight = 28f * zoomScale * density

                drawRoundRect(
                    color = if (isDark) Color(0xEE1E293B) else Color(0xF4FFFFFF),
                    topLeft = Offset(mx - bgTagWidth / 2f, bgRectY),
                    size = Size(bgTagWidth, bgTagHeight),
                    cornerRadius = CornerRadius(8f * density, 8f * density)
                )

                drawContext.canvas.nativeCanvas.drawText(
                    "$labelText • $etaText",
                    mx,
                    bgRectY + 18f * density * zoomScale,
                    paint
                )
            }

            // Venue Destination Marker
            drawCircle(
                color = StatusAmber.copy(alpha = 0.3f),
                radius = 36f * zoomScale,
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = Color.White,
                radius = 22f * zoomScale,
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = StatusAmber,
                radius = 17f * zoomScale,
                center = Offset(centerX, centerY)
            )

            val vTagWidth = 140f * zoomScale * density
            val vTagHeight = 36f * zoomScale * density
            val vTagTop = centerY - 58f * zoomScale

            drawRoundRect(
                color = if (isDark) Color(0xF40F172A) else Color(0xF4FFFFFF),
                topLeft = Offset(centerX - vTagWidth / 2f, vTagTop),
                size = Size(vTagWidth, vTagHeight),
                cornerRadius = CornerRadius(10f * density, 10f * density)
            )

            val venuePaint = android.graphics.Paint().apply {
                color = if (isDark) android.graphics.Color.WHITE else android.graphics.Color.BLACK
                textSize = (13f * zoomScale).coerceIn(12f, 17f) * density
                isFakeBoldText = true
                textAlign = android.graphics.Paint.Align.CENTER
            }
            drawContext.canvas.nativeCanvas.drawText(
                "📍 ${event.venueName}",
                centerX,
                vTagTop + 22f * density * zoomScale,
                venuePaint
            )
        }

        // Roomy Map Controls (Top-Right Floating Rail: 48dp touch targets)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilledIconButton(
                onClick = { zoomScale = (zoomScale * 1.3f).coerceAtMost(3.2f) },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
                ),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Yakınlaş", tint = MaterialTheme.colorScheme.onSurface)
            }
            FilledIconButton(
                onClick = { zoomScale = (zoomScale / 1.3f).coerceAtLeast(0.4f) },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
                ),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Uzaklaş", tint = MaterialTheme.colorScheme.onSurface)
            }
            FilledIconButton(
                onClick = {
                    panOffsetX = 0f
                    panOffsetY = 0f
                    zoomScale = 1.0f
                },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
                ),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.ZoomOutMap, contentDescription = "Mekana Dön", tint = StatusAmber)
            }
            FilledIconButton(
                onClick = {
                    val me = event.members.find { it.isCurrentUser }
                    if (me != null) {
                        val scale = 6500f * zoomScale
                        panOffsetX = -((me.currentLng - event.venueLng) * scale).toFloat()
                        panOffsetY = ((me.currentLat - event.venueLat) * scale).toFloat()
                    }
                },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
                ),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Konumuma Dön", tint = BirlikteIndigo)
            }
        }

        // Spacious Selected Member Telemetry Card
        if (selectedMember != null) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
                ),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(selectedMember.avatarColorHex),
                            modifier = Modifier.size(50.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    selectedMember.avatarInitials,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    selectedMember.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                if (selectedMember.isCurrentUser) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = BirlikteIndigo.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            "SİZ",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = BirlikteIndigo,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Text(
                                "${selectedMember.originName} • ${Loc.getVehicleLabel(selectedMember.vehicleType, lang)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(onClick = { onMemberSelected(null) }) {
                            Icon(Icons.Default.Close, contentDescription = "Kapat")
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Metrics Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SpaciousMetricItem(
                            label = Loc.s("distance", lang),
                            value = "${selectedMember.distanceToVenueKm} km",
                            modifier = Modifier.weight(1f)
                        )
                        SpaciousMetricItem(
                            label = Loc.s("speed", lang),
                            value = "${selectedMember.speedKmH} km/h",
                            modifier = Modifier.weight(1f)
                        )
                        SpaciousMetricItem(
                            label = Loc.s("battery", lang),
                            value = "%${selectedMember.batteryLevel}",
                            modifier = Modifier.weight(1f)
                        )
                        SpaciousMetricItem(
                            label = Loc.s("eta", lang),
                            value = if (selectedMember.liveStatus == LiveStatus.ARRIVED) "Alana Vardı" else "${selectedMember.etaMinutes} dk",
                            highlight = true,
                            modifier = Modifier.weight(1.2f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "🔒 ${Loc.getPrivacyLabel(selectedMember.privacyMode, lang)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SpaciousMetricItem(
    label: String,
    value: String,
    highlight: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (highlight) StatusGreen else MaterialTheme.colorScheme.onSurface
        )
    }
}
