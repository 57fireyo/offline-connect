package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.components.TacticalAvatar
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
fun SettingsScreen(
    userProfile: UserProfile,
    isDemoMode: Boolean,
    onToggleDemoMode: () -> Unit,
    onSaveProfile: (String, String, Int, Boolean, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var displayName by remember(userProfile.displayName) { mutableStateOf(userProfile.displayName) }
    var callsign by remember(userProfile.callsign) { mutableStateOf(userProfile.callsign) }
    var avatarIndex by remember(userProfile.avatarColorIndex) { mutableStateOf(userProfile.avatarColorIndex) }
    var meshRelay by remember(userProfile.meshRelayEnabled) { mutableStateOf(userProfile.meshRelayEnabled) }
    var batterySaver by remember(userProfile.batterySaverEnabled) { mutableStateOf(userProfile.batterySaverEnabled) }
    var isSaved by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "OPERATOR PROFILE & SETTINGS",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
        }

        // Operator Identity Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = TacticalDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TacticalAvatar(
                            name = displayName,
                            callsign = callsign,
                            size = 54.dp,
                            avatarColorIndex = avatarIndex,
                            isOnline = true
                        )

                        Column {
                            Text(
                                text = "Local Node Identity",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = userProfile.peerId,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TacticalGreen
                            )
                        }
                    }

                    OutlinedTextField(
                        value = displayName,
                        onValueChange = {
                            displayName = it
                            isSaved = false
                        },
                        label = { Text("Operator Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_name_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = TacticalDarkSurfaceVariant,
                            unfocusedContainerColor = TacticalDarkSurfaceVariant,
                            focusedBorderColor = EmergencyAmber,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = callsign,
                        onValueChange = {
                            callsign = it
                            isSaved = false
                        },
                        label = { Text("Field Tactical Callsign") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_callsign_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = TacticalDarkSurfaceVariant,
                            unfocusedContainerColor = TacticalDarkSurfaceVariant,
                            focusedBorderColor = EmergencyAmber,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Button(
                        onClick = {
                            onSaveProfile(displayName, callsign, avatarIndex, meshRelay, batterySaver)
                            isSaved = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmergencyAmber,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("save_profile_button")
                    ) {
                        Text(
                            text = if (isSaved) "SAVED ✓" else "SAVE PROFILE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Mesh Network Configuration
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = TacticalDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "MESH & RADIO PROTOCOLS",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = RadioCyan
                    )

                    // Multi-Hop Relay
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Hub,
                                contentDescription = "Mesh Relay",
                                tint = RadioCyan,
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    text = "Multi-Hop Mesh Relay",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Forward encrypted packets between out-of-range peers (A ➔ B ➔ C)",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    lineHeight = 14.sp
                                )
                            }
                        }

                        Switch(
                            checked = meshRelay,
                            onCheckedChange = {
                                meshRelay = it
                                onSaveProfile(displayName, callsign, avatarIndex, it, batterySaver)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = RadioCyan,
                                checkedTrackColor = RadioCyan.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.testTag("mesh_relay_switch")
                        )
                    }

                    // Battery Saver
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.BatterySaver,
                                contentDescription = "Battery Saver",
                                tint = EmergencyAmber,
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    text = "Battery-Aware Mode",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Throttle scanning frequency when device battery drops below 20%",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    lineHeight = 14.sp
                                )
                            }
                        }

                        Switch(
                            checked = batterySaver,
                            onCheckedChange = {
                                batterySaver = it
                                onSaveProfile(displayName, callsign, avatarIndex, meshRelay, it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = EmergencyAmber,
                                checkedTrackColor = EmergencyAmber.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.testTag("battery_saver_switch")
                        )
                    }

                    // Single-Device Demo Simulator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                                tint = TacticalGreen,
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    text = "Demo Mode Simulator",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Simulate second peer on this device for judging/testing without 2nd phone",
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
                                checkedThumbColor = TacticalGreen,
                                checkedTrackColor = TacticalGreen.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.testTag("settings_demo_mode_switch")
                        )
                    }
                }
            }
        }

        // Hardware Keystore & Zero-Cloud Security Specs
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = TacticalDarkSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Zero Cloud",
                            tint = TacticalGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "AIR-GAPPED & ZERO-CLOUD ARCHITECTURE",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = TacticalGreen
                        )
                    }

                    Text(
                        text = "• Keys generated inside hardware-backed Android KeyStore\n• E2EE via ECDH P-256 and AES-256-GCM authenticated cipher\n• Audio/Video encrypted over SRTP with WebRTC P2P direct socket\n• Zero cloud dependency — all packets remain strictly peer-to-peer",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
