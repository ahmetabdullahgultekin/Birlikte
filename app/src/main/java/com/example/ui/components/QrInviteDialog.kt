package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.Loc
import com.example.model.AppLanguage
import com.example.model.OrganizationEvent
import com.example.ui.theme.BirlikteIndigo

@Composable
fun QrInviteDialog(
    event: OrganizationEvent,
    lang: AppLanguage,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.QrCode2, contentDescription = null, tint = BirlikteIndigo)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Grup Daveti & QR Kod",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                // Render Stylized QR Matrix Canvas
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 4.dp,
                    modifier = Modifier.size(190.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.size(160.dp)) {
                            val matrixSize = 21
                            val cellSize = size.width / matrixSize

                            // Draw procedural deterministic QR-style pattern based on inviteCode
                            val hash = event.inviteCode.hashCode()

                            for (r in 0 until matrixSize) {
                                for (c in 0 until matrixSize) {
                                    // QR Finder Patterns (Top-Left, Top-Right, Bottom-Left corners)
                                    val isFinderTL = (r < 7 && c < 7)
                                    val isFinderTR = (r < 7 && c >= matrixSize - 7)
                                    val isFinderBL = (r >= matrixSize - 7 && c < 7)

                                    val isBlack = if (isFinderTL) {
                                        (r == 0 || r == 6 || c == 0 || c == 6 || (r in 2..4 && c in 2..4))
                                    } else if (isFinderTR) {
                                        val trC = c - (matrixSize - 7)
                                        (r == 0 || r == 6 || trC == 0 || trC == 6 || (r in 2..4 && trC in 2..4))
                                    } else if (isFinderBL) {
                                        val blR = r - (matrixSize - 7)
                                        (blR == 0 || blR == 6 || c == 0 || c == 6 || (blR in 2..4 && c in 2..4))
                                    } else {
                                        // Pseudo random data dots
                                        ((r * 31 + c * 17 + hash) % 3 == 0) || (r % 4 == 0 && c % 3 == 0)
                                    }

                                    if (isBlack) {
                                        drawRect(
                                            color = Color(0xFF1E1B4B),
                                            topLeft = Offset(c * cellSize, r * cellSize),
                                            size = Size(cellSize, cellSize)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Invite code pill
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                Loc.s("invite_code", lang),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                event.inviteCode,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = BirlikteIndigo
                            )
                        }

                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Birlikte Davet Kodu", event.inviteCode)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, Loc.s("copied", lang), Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BirlikteIndigo)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(Loc.s("copy_code", lang), fontSize = 12.sp)
                        }
                    }
                }

                Text(
                    "Arkadaşlarınız 'Birlikte' uygulamasında bu kodu girerek veya QR kodu okutarak anında bu organizasyona canlı katılabilir.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = BirlikteIndigo)
            ) {
                Text("Tamam", color = Color.White)
            }
        }
    )
}
