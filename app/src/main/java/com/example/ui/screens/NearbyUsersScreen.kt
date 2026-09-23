package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConnectionState
import com.example.data.model.ContactEntity
import com.example.ui.components.ConnectionStateChip
import com.example.ui.components.SecurityVerificationBadge
import com.example.ui.components.SignalStrengthMeter
import com.example.ui.components.TacticalAvatar
import com.example.ui.components.TacticalRadarBanner
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.EmergencyAmber
import com.example.ui.theme.RadioCyan
import com.example.ui.theme.TacticalDarkSurface
import com.example.ui.theme.TacticalDarkSurfaceHighlight
import com.example.ui.theme.TacticalDarkSurfaceVariant
import com.example.ui.theme.TacticalGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun NearbyUsersScreen(
    contacts: List<ContactEntity>,
    isDiscovering: Boolean,
    isDemoMode: Boolean,
    onToggleScan: () -> Unit,
    onToggleDemoMode: () -> Unit,
    onSelectContact: (ContactEntity) -> Unit,
    onStartCall: (ContactEntity, Boolean) -> Unit,
    onVerifyContact: (ContactEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val connectedCount = contacts.count { it.connectionState == ConnectionState.CONNECTED }
    val discoveredCount = contacts.size

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Radar Scanner Header
        item {
            TacticalRadarBanner(
                isScanning = isDiscovering,
                connectedCount = connectedCount,
                discoveredCount = discoveredCount,
                onToggleScan = onToggleScan
            )
        }

        // Demo Simulator Notice / Toggle
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isDemoMode) EmergencyAmber.copy(alpha = 0.12f) else TacticalDarkSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isDemoMode) EmergencyAmber.copy(alpha = 0.4f) else BorderSubtle
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WifiTethering,
                            contentDescription = "Demo Mode",
                            tint = if (isDemoMode) EmergencyAmber else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Single-Device Demo Simulator",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (isDemoMode) "Virtual nodes active for instant judging without 2nd phone" else "Off: Using real device-to-device radio only",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 14.sp
                            )
                        }
                    }

                    Switch(
                        checked = isDemoMode,
                        onCheckedChange = { onToggleDemoMode() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = EmergencyAmber,
                            checkedTrackColor = EmergencyAmber.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.testTag("demo_mode_switch")
                    )
                }
            }
        }

        // Section Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "NEARBY NODES IN RANGE (${contacts.size})",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "AUTO-FORWARD ON",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = TacticalGreen
                )
            }
        }

        // List of Nearby Peers
        if (contacts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = TacticalDarkSurfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Searching",
                            tint = RadioCyan,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "Scanning for Nearby OffGrid Peers",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Ensure other devices have Bluetooth and Wi-Fi enabled. No router or internet needed.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(contacts, key = { it.peerId }) { contact ->
                NearbyPeerCard(
                    contact = contact,
                    onChatClick = { onSelectContact(contact) },
                    onVoiceCallClick = { onStartCall(contact, false) },
                    onVideoCallClick = { onStartCall(contact, true) },
                    onVerifyClick = { onVerifyContact(contact) }
                )
            }
        }
    }
}

@Composable
fun NearbyPeerCard(
    contact: ContactEntity,
    onChatClick: () -> Unit,
    onVoiceCallClick: () -> Unit,
    onVideoCallClick: () -> Unit,
    onVerifyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("peer_card_${contact.peerId}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = TacticalDarkSurface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (contact.connectionState == ConnectionState.CONNECTED) TacticalGreen.copy(alpha = 0.4f) else BorderSubtle
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Avatar, Name, Callsign, Signal Meter & Status Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TacticalAvatar(
                    name = contact.displayName,
                    callsign = contact.callsign,
                    size = 46.dp,
                    avatarColorIndex = contact.avatarColorIndex,
                    isOnline = contact.connectionState == ConnectionState.CONNECTED
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = contact.displayName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "[${contact.callsign}]",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = EmergencyAmber
                        )
                    }

                    SignalStrengthMeter(
                        rssi = contact.rssi,
                        distanceMeters = contact.distanceEstimateMeters
                    )
                }

                ConnectionStateChip(state = contact.connectionState)
            }

            // Security Fingerprint / Verification Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SecurityVerificationBadge(
                    isVerified = contact.isVerified,
                    onVerifyClick = onVerifyClick
                )

                Text(
                    text = "Node: ${contact.peerId.take(12)}",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary
                )
            }

            // Quick Action Buttons (Chat, Voice Call, Video Call)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onChatClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("chat_button_${contact.peerId}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmergencyAmber,
                        contentColor = androidx.compose.ui.graphics.Color.Black
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubble,
                        contentDescription = "Chat",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Encrypted Chat",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                FilledTonalIconButton(
                    onClick = onVoiceCallClick,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("voice_call_${contact.peerId}"),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = TacticalDarkSurfaceHighlight,
                        contentColor = TacticalGreen
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "P2P Voice Call",
                        modifier = Modifier.size(20.dp)
                    )
                }

                FilledTonalIconButton(
                    onClick = onVideoCallClick,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("video_call_${contact.peerId}"),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = TacticalDarkSurfaceHighlight,
                        contentColor = RadioCyan
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "P2P Video Call (Wi-Fi Direct)",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
