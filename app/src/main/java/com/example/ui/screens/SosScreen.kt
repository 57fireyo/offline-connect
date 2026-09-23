package com.example.ui.screens

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EmergencyType
import com.example.data.model.SosAlertEntity
import com.example.data.model.UrgencyLevel
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DistressRed
import com.example.ui.theme.DistressRedLight
import com.example.ui.theme.EmergencyAmber
import com.example.ui.theme.RadioCyan
import com.example.ui.theme.TacticalDarkSurface
import com.example.ui.theme.TacticalDarkSurfaceHighlight
import com.example.ui.theme.TacticalDarkSurfaceVariant
import com.example.ui.theme.TacticalGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SosScreen(
    sosAlerts: List<SosAlertEntity>,
    onBroadcastSos: (EmergencyType, UrgencyLevel, String, Double?, Double?) -> Unit,
    onAcknowledgeAlert: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedType by remember { mutableStateOf(EmergencyType.MEDICAL) }
    var selectedUrgency by remember { mutableStateOf(UrgencyLevel.CRITICAL) }
    var notesText by remember { mutableStateOf("") }
    var broadcastSentSuccess by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "sos_beacon")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "beacon_radius"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // SOS Emergency Beacon Hero Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TacticalDarkSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, DistressRed.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "EMERGENCY P2P DISTRESS BEACON",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = DistressRedLight,
                        letterSpacing = 1.sp
                    )

                    // Big Pulsating Emergency Button
                    Box(
                        modifier = Modifier.size(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = Offset(size.width / 2, size.height / 2)
                            val radius = size.minDimension / 2
                            drawCircle(
                                color = DistressRed.copy(alpha = 0.2f),
                                radius = radius * pulseRadius,
                                center = center,
                                style = Stroke(2f)
                            )
                        }

                        Button(
                            onClick = {
                                onBroadcastSos(
                                    selectedType,
                                    selectedUrgency,
                                    notesText.ifBlank { "Immediate evacuation / emergency response requested at GPS coordinates." },
                                    36.6002,
                                    -118.0583
                                )
                                broadcastSentSuccess = true
                            },
                            modifier = Modifier
                                .size(105.dp)
                                .clip(CircleShape)
                                .testTag("sos_broadcast_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DistressRed,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Emergency,
                                    contentDescription = "SOS",
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    text = "SOS",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    if (broadcastSentSuccess) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = TacticalGreen.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TacticalGreen)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Success",
                                    tint = TacticalGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "DISTRESS BEACON BROADCAST TO ALL NEARBY NODES",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = TacticalGreen
                                )
                            }
                        }
                    }

                    // Emergency Triage Type Selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        EmergencyType.values().forEach { type ->
                            FilterChip(
                                selected = selectedType == type,
                                onClick = { selectedType = type },
                                label = {
                                    Text(
                                        text = type.name,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DistressRed.copy(alpha = 0.3f),
                                    selectedLabelColor = DistressRedLight,
                                    containerColor = TacticalDarkSurface,
                                    labelColor = TextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selectedType == type,
                                    borderColor = if (selectedType == type) DistressRed else BorderSubtle
                                )
                            )
                        }
                    }

                    // Urgency Level Selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UrgencyLevel.values().forEach { urgency ->
                            val color = when (urgency) {
                                UrgencyLevel.CRITICAL -> DistressRed
                                UrgencyLevel.HIGH -> EmergencyAmber
                                UrgencyLevel.MEDIUM -> RadioCyan
                            }
                            Surface(
                                onClick = { selectedUrgency = urgency },
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedUrgency == urgency) color.copy(alpha = 0.25f) else TacticalDarkSurface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (selectedUrgency == urgency) color else BorderSubtle
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = urgency.name,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedUrgency == urgency) color else TextSecondary,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    // GPS Coordinates Preview
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0C141D),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "GPS",
                                tint = EmergencyAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "GPS: 36.6002° N, 118.0583° W • Alt: 2450m",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextPrimary
                            )
                        }
                    }

                    // Optional Notes
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        placeholder = {
                            Text(
                                text = "Distress details (e.g. 2 casualties, need blood type O+, flood zone)...",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sos_notes_field"),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = TacticalDarkSurface,
                            unfocusedContainerColor = TacticalDarkSurface,
                            focusedBorderColor = DistressRed,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }

        // Active Emergency Broadcasts Feed
        item {
            Text(
                text = "ACTIVE MESH DISTRESS ALERTS (${sosAlerts.size})",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
        }

        items(sosAlerts, key = { it.id }) { alert ->
            SosAlertCard(
                alert = alert,
                onAcknowledge = { onAcknowledgeAlert(alert.id) }
            )
        }
    }
}

@Composable
fun SosAlertCard(
    alert: SosAlertEntity,
    onAcknowledge: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val formattedTime = remember(alert.timestamp) { timeFormat.format(Date(alert.timestamp)) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("sos_card_${alert.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TacticalDarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DistressRed.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = DistressRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "${alert.emergencyType.name} ALERT",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        color = DistressRedLight
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = DistressRed.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = alert.urgencyLevel.name,
                        color = DistressRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = "From: ${alert.senderName} [${alert.callsign}] • $formattedTime",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            if (alert.latitude != null && alert.longitude != null) {
                Text(
                    text = "Grid: ${alert.latitude}° N, ${alert.longitude}° W",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = EmergencyAmber
                )
            }

            if (alert.notes.isNotBlank()) {
                Text(
                    text = alert.notes,
                    fontSize = 13.sp,
                    color = TextPrimary,
                    lineHeight = 17.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onAcknowledge,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (alert.acknowledged) TacticalDarkSurfaceHighlight else EmergencyAmber,
                        contentColor = if (alert.acknowledged) TacticalGreen else Color.Black
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = if (alert.acknowledged) "ACKNOWLEDGED ✓" else "ACKNOWLEDGE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
