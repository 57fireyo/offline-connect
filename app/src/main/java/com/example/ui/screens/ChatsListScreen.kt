package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConnectionState
import com.example.data.model.ContactEntity
import com.example.data.model.ConversationSummary
import com.example.data.model.MessageStatus
import com.example.ui.components.ConnectionStateChip
import com.example.ui.components.MessageStatusIcon
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatsListScreen(
    conversations: List<ConversationSummary>,
    onSelectContact: (ContactEntity) -> Unit,
    onStartCall: (ContactEntity, Boolean) -> Unit,
    onNavigateToRadar: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredList = remember(conversations, searchQuery) {
        if (searchQuery.isBlank()) {
            conversations
        } else {
            conversations.filter {
                it.contact.displayName.contains(searchQuery, ignoreCase = true) ||
                it.contact.callsign.contains(searchQuery, ignoreCase = true) ||
                (it.lastMessage?.content?.contains(searchQuery, ignoreCase = true) == true)
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Encrypted Status Bar
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = TacticalDarkSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Encrypted",
                            tint = TacticalGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "AIR-GAPPED CHANNELS",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = TacticalGreen
                        )
                    }

                    Text(
                        text = "STORE-AND-FORWARD ON",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = EmergencyAmber,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Search Filter Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("chats_search_field"),
                placeholder = {
                    Text(
                        text = "Search peers, callsigns or transcripts...",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmergencyAmber,
                    unfocusedBorderColor = BorderSubtle,
                    focusedContainerColor = TacticalDarkSurface,
                    unfocusedContainerColor = TacticalDarkSurface,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                singleLine = true
            )
        }

        // Header Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTIVE SECURE THREADS (${filteredList.size})",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "ECDH P-256 / AES-256",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = RadioCyan
                )
            }
        }

        // List of conversations
        if (filteredList.isEmpty()) {
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
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "Empty",
                            tint = EmergencyAmber,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = if (searchQuery.isBlank()) "No Active Encrypted Sessions" else "No matching conversations",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Discover peers in radio range via the Radar tab to open an offline direct encrypted channel.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 16.sp
                        )

                        Button(
                            onClick = onNavigateToRadar,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmergencyAmber,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Radar,
                                contentDescription = "Go to Radar",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Scan Nearby Nodes",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        } else {
            items(filteredList, key = { it.contact.peerId }) { item ->
                ConversationCard(
                    item = item,
                    onClick = { onSelectContact(item.contact) },
                    onVoiceCall = { onStartCall(item.contact, false) },
                    onVideoCall = { onStartCall(item.contact, true) }
                )
            }
        }
    }
}

@Composable
fun ConversationCard(
    item: ConversationSummary,
    onClick: () -> Unit,
    onVoiceCall: () -> Unit,
    onVideoCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contact = item.contact
    val lastMsg = item.lastMessage
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val formattedTime = remember(lastMsg?.timestamp) {
        if (lastMsg != null) timeFormat.format(Date(lastMsg.timestamp)) else ""
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("conversation_card_${contact.peerId}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = TacticalDarkSurface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (item.unreadCount > 0) EmergencyAmber.copy(alpha = 0.5f) else BorderSubtle
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar with Online Badge
            TacticalAvatar(
                name = contact.displayName,
                callsign = contact.callsign,
                size = 50.dp,
                avatarColorIndex = contact.avatarColorIndex,
                isOnline = contact.connectionState == ConnectionState.CONNECTED
            )

            // Contact Info & Last Message Snippet
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
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

                    if (formattedTime.isNotEmpty()) {
                        Text(
                            text = formattedTime,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (item.unreadCount > 0) EmergencyAmber else TextSecondary,
                            fontWeight = if (item.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                // Snippet & Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (lastMsg != null) {
                        MessageStatusIcon(status = lastMsg.status)
                        Text(
                            text = lastMsg.content,
                            fontSize = 13.sp,
                            color = if (item.unreadCount > 0) TextPrimary else TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = if (item.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Text(
                            text = "No messages exchanged yet. Tap to chat.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Unread badge or Queued badge
                    if (item.unreadCount > 0) {
                        Surface(
                            shape = CircleShape,
                            color = EmergencyAmber
                        ) {
                            Text(
                                text = "${item.unreadCount}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else if (item.queuedCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = RadioCyan.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, RadioCyan)
                        ) {
                            Text(
                                text = "${item.queuedCount} QUEUED",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = RadioCyan,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Connection metrics sub-strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (contact.connectionState == ConnectionState.CONNECTED) "LINKED • ${contact.rssi}dBm" else contact.connectionState.name,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (contact.connectionState == ConnectionState.CONNECTED) TacticalGreen else TextSecondary
                    )

                    if (contact.isVerified) {
                        Text(
                            text = "• VERIFIED KEY",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TacticalGreen
                        )
                    }
                }
            }

            // Quick Call Actions
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                FilledTonalIconButton(
                    onClick = onVoiceCall,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("chat_voice_${contact.peerId}"),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = TacticalDarkSurfaceHighlight,
                        contentColor = TacticalGreen
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Voice Call",
                        modifier = Modifier.size(16.dp)
                    )
                }

                FilledTonalIconButton(
                    onClick = onVideoCall,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("chat_video_${contact.peerId}"),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = TacticalDarkSurfaceHighlight,
                        contentColor = RadioCyan
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "Video Call",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
