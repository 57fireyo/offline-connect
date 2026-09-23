package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConnectionState
import com.example.data.model.MessageStatus
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DistressRed
import com.example.ui.theme.EmergencyAmber
import com.example.ui.theme.RadioCyan
import com.example.ui.theme.TacticalDarkSurfaceHighlight
import com.example.ui.theme.TacticalDarkSurfaceVariant
import com.example.ui.theme.TacticalGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TacticalAvatar(
    name: String,
    callsign: String,
    size: Dp = 48.dp,
    avatarColorIndex: Int = 0,
    isOnline: Boolean = true,
    modifier: Modifier = Modifier
) {
    val avatarColors = listOf(
        TacticalGreen to Color(0xFF064E3B),
        EmergencyAmber to Color(0xFF78350F),
        RadioCyan to Color(0xFF164E63),
        DistressRed to Color(0xFF7F1D1D)
    )
    val (primaryCol, bgCol) = avatarColors[avatarColorIndex % avatarColors.size]
    val initials = name.split(" ").take(2).mapNotNull { it.firstOrNull()?.toString() }.joinToString("")
        .ifEmpty { "OP" }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bgCol)
            .border(1.5.dp, if (isOnline) primaryCol else BorderSubtle, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = primaryCol,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.38f).sp,
            fontFamily = FontFamily.Monospace
        )

        // Status indicator dot
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size((size.value * 0.3f).dp)
                .clip(CircleShape)
                .background(if (isOnline) TacticalGreen else Color.Gray)
                .border(1.5.dp, MaterialTheme.colorScheme.background, CircleShape)
        )
    }
}

@Composable
fun SignalStrengthMeter(
    rssi: Int,
    distanceMeters: Float,
    modifier: Modifier = Modifier
) {
    // RSSI ranges from -100 (poor) to -35 (excellent)
    val bars = when {
        rssi >= -55 -> 4
        rssi >= -70 -> 3
        rssi >= -85 -> 2
        else -> 1
    }

    val barColor = when (bars) {
        4 -> TacticalGreen
        3 -> TacticalGreen
        2 -> EmergencyAmber
        else -> DistressRed
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.height(14.dp)
        ) {
            for (i in 1..4) {
                val height = (i * 3.5).dp
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(height)
                        .clip(RoundedCornerShape(1.dp))
                        .background(if (i <= bars) barColor else BorderSubtle)
                )
            }
        }

        Text(
            text = "${rssi}dBm • ~${"%.1f".format(distanceMeters)}m",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ConnectionStateChip(
    state: ConnectionState,
    modifier: Modifier = Modifier
) {
    val (label, color) = when (state) {
        ConnectionState.CONNECTED -> "CONNECTED (P2P)" to TacticalGreen
        ConnectionState.CONNECTING -> "LINKING..." to EmergencyAmber
        ConnectionState.SEARCHING -> "SCANNING" to RadioCyan
        ConnectionState.OUT_OF_RANGE -> "OUT OF RANGE" to DistressRed
        ConnectionState.DISCONNECTED -> "OFFLINE" to TextSecondary
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = label,
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun TacticalRadarBanner(
    isScanning: Boolean,
    connectedCount: Int,
    discoveredCount: Int,
    onToggleScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_sweep"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = TacticalDarkSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Tactical Radar Mini View
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0B141E))
                        .border(1.dp, RadioCyan.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val radius = size.minDimension / 2
                        val center = Offset(size.width / 2, size.height / 2)

                        // Radar concentric rings
                        drawCircle(RadioCyan.copy(alpha = 0.15f), radius * 0.35f, center, style = Stroke(1f))
                        drawCircle(RadioCyan.copy(alpha = 0.2f), radius * 0.70f, center, style = Stroke(1f))
                        drawCircle(RadioCyan.copy(alpha = 0.3f), radius, center, style = Stroke(1.5f))

                        if (isScanning) {
                            // Sweep ray
                            drawArc(
                                brush = Brush.sweepGradient(
                                    listOf(
                                        Color.Transparent,
                                        RadioCyan.copy(alpha = 0.4f)
                                    ),
                                    center = center
                                ),
                                startAngle = sweepAngle - 45f,
                                sweepAngle = 45f,
                                useCenter = true
                            )
                        }

                        // Simulated peer blips on radar
                        if (connectedCount > 0) {
                            drawCircle(TacticalGreen, 3.5f, Offset(center.x + radius * 0.45f, center.y - radius * 0.35f))
                        }
                        if (discoveredCount > 1) {
                            drawCircle(EmergencyAmber, 3f, Offset(center.x - radius * 0.55f, center.y + radius * 0.2f))
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Radar",
                        tint = RadioCyan.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (isScanning) "P2P MESH ACTIVE" else "MESH IDLE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (isScanning) TacticalGreen else TextSecondary
                        )
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isScanning) TacticalGreen else Color.Gray)
                        )
                    }

                    Text(
                        text = "Bluetooth BLE + Wi-Fi Direct Transport",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    Text(
                        text = "$connectedCount Connected • $discoveredCount In Range",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = EmergencyAmber,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Button(
                onClick = onToggleScan,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isScanning) TacticalDarkSurfaceHighlight else EmergencyAmber,
                    contentColor = if (isScanning) EmergencyAmber else Color.Black
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .height(36.dp)
                    .testTag("toggle_scan_button")
            ) {
                Text(
                    text = if (isScanning) "Active" else "Scan",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun SecurityVerificationBadge(
    isVerified: Boolean,
    onVerifyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onVerifyClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isVerified) TacticalGreen.copy(alpha = 0.15f) else EmergencyAmber.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isVerified) TacticalGreen.copy(alpha = 0.5f) else EmergencyAmber.copy(alpha = 0.5f)
        ),
        modifier = modifier.testTag("verify_security_badge")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (isVerified) Icons.Default.Verified else Icons.Default.Lock,
                contentDescription = "Security Status",
                tint = if (isVerified) TacticalGreen else EmergencyAmber,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = if (isVerified) "E2EE VERIFIED" else "UNVERIFIED E2EE",
                color = if (isVerified) TacticalGreen else EmergencyAmber,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun MessageStatusIcon(
    status: MessageStatus,
    modifier: Modifier = Modifier
) {
    when (status) {
        MessageStatus.QUEUED -> {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = "Queued in store-and-forward",
                tint = EmergencyAmber,
                modifier = modifier.size(12.dp)
            )
        }
        MessageStatus.SENT -> {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Sent",
                tint = TextSecondary,
                modifier = modifier.size(12.dp)
            )
        }
        MessageStatus.DELIVERED -> {
            Icon(
                imageVector = Icons.Default.DoneAll,
                contentDescription = "Delivered to peer",
                tint = TextSecondary,
                modifier = modifier.size(13.dp)
            )
        }
        MessageStatus.SEEN -> {
            Icon(
                imageVector = Icons.Default.DoneAll,
                contentDescription = "Seen by peer",
                tint = TacticalGreen,
                modifier = modifier.size(13.dp)
            )
        }
        MessageStatus.FAILED -> {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = "Failed",
                tint = DistressRed,
                modifier = modifier.size(12.dp)
            )
        }
    }
}

